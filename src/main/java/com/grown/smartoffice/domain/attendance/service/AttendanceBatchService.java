package com.grown.smartoffice.domain.attendance.service;

import com.grown.smartoffice.domain.attendance.AttendancePolicy;
import com.grown.smartoffice.domain.attendance.entity.Attendance;
import com.grown.smartoffice.domain.attendance.entity.AttendanceStatus;
import com.grown.smartoffice.domain.attendance.entity.MonthlyAttendance;
import com.grown.smartoffice.domain.attendance.repository.AttendanceRepository;
import com.grown.smartoffice.domain.attendance.repository.MonthlyAttendanceRepository;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceBatchService {

    private final AttendanceRepository attendanceRepository;
    private final MonthlyAttendanceRepository monthlyAttendanceRepository;
    private final UserRepository userRepository;

    @Transactional
    public int runDailyBatch(LocalDate targetDate, String executedBy) {
        log.info("[AttendanceBatch] 일별 집계 시작 — date={}, by={}", targetDate, executedBy);

        List<Attendance> records = attendanceRepository.findAllByWorkDate(targetDate);

        for (Attendance att : records) {
            if (att.getCheckIn() != null && att.getCheckOut() != null) {
                int rawMinutes = (int) Duration.between(att.getCheckIn(), att.getCheckOut()).toMinutes();
                int workMinutes = Math.max(rawMinutes - AttendancePolicy.LUNCH_BREAK_MINUTES, 0);
                int overtimeMinutes = Math.max(workMinutes - AttendancePolicy.STANDARD_WORK_MINUTES, 0);

                boolean late = att.getCheckIn().toLocalTime().isAfter(AttendancePolicy.STANDARD_START);
                boolean earlyLeave = att.getCheckOut().toLocalTime().isBefore(AttendancePolicy.STANDARD_END);

                AttendanceStatus status;
                if (late && earlyLeave) {
                    status = AttendanceStatus.LATE;
                } else if (late) {
                    status = AttendanceStatus.LATE;
                } else if (earlyLeave) {
                    status = AttendanceStatus.EARLY_LEAVE;
                } else {
                    status = AttendanceStatus.NORMAL;
                }

                att.applyBatchResult(workMinutes, overtimeMinutes, status, null);
            }
        }

        Set<Long> workedUserIds = records.stream()
                .map(a -> a.getUser().getUserId())
                .collect(Collectors.toSet());

        List<User> activeUsers = userRepository.findAll().stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .toList();

        for (User user : activeUsers) {
            if (!workedUserIds.contains(user.getUserId())) {
                Attendance absent = Attendance.builder()
                        .user(user)
                        .workDate(targetDate)
                        .checkIn(null)
                        .attendanceStatus(AttendanceStatus.ABSENT)
                        .build();
                absent.applyBatchResult(0, 0, AttendanceStatus.ABSENT, "무단결근");
                attendanceRepository.save(absent);
            }
        }

        log.info("[AttendanceBatch] 일별 집계 완료 — date={}, 처리={}", targetDate, records.size());
        return records.size();
    }

    @Transactional
    public void runMonthlyAggregation(int year, int month) {
        log.info("[AttendanceBatch] 월별 집계 시작 — {}-{}", year, month);

        List<Attendance> monthlyRecords = attendanceRepository.findAll().stream()
                .filter(a -> {
                    LocalDate d = a.getWorkDate();
                    return d.getYear() == year && d.getMonthValue() == month;
                })
                .toList();

        Map<Long, List<Attendance>> byUser = monthlyRecords.stream()
                .collect(Collectors.groupingBy(a -> a.getUser().getUserId()));

        for (Map.Entry<Long, List<Attendance>> entry : byUser.entrySet()) {
            Long userId = entry.getKey();
            List<Attendance> userRecords = entry.getValue();

            int totalWork = userRecords.stream().mapToInt(a -> a.getWorkMinutes() != null ? a.getWorkMinutes() : 0).sum();
            int totalOvertime = userRecords.stream().mapToInt(a -> a.getOvertimeMinutes() != null ? a.getOvertimeMinutes() : 0).sum();
            int lateCount = (int) userRecords.stream().filter(a -> a.getAttendanceStatus() == AttendanceStatus.LATE).count();
            int earlyLeaveCount = (int) userRecords.stream().filter(a -> a.getAttendanceStatus() == AttendanceStatus.EARLY_LEAVE).count();
            int absentCount = (int) userRecords.stream().filter(a -> a.getAttendanceStatus() == AttendanceStatus.ABSENT).count();

            monthlyAttendanceRepository.findByUser_UserIdAndMonatYearAndMonatMonth(userId, year, month)
                    .ifPresentOrElse(
                            existing -> existing.update(totalWork, totalOvertime, lateCount, earlyLeaveCount, absentCount),
                            () -> {
                                User user = userRecords.get(0).getUser();
                                monthlyAttendanceRepository.save(MonthlyAttendance.builder()
                                        .user(user)
                                        .monatYear(year)
                                        .monatMonth(month)
                                        .monatTotalWorkMinutes(totalWork)
                                        .monatOvertimeMinutes(totalOvertime)
                                        .lateCount(lateCount)
                                        .earlyLeaveCount(earlyLeaveCount)
                                        .absentCount(absentCount)
                                        .build());
                            }
                    );
        }

        log.info("[AttendanceBatch] 월별 집계 완료 — {}-{}, 사용자={}", year, month, byUser.size());
    }
}
