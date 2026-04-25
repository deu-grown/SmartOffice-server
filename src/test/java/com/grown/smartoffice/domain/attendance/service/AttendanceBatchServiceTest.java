package com.grown.smartoffice.domain.attendance.service;

import com.grown.smartoffice.domain.attendance.entity.Attendance;
import com.grown.smartoffice.domain.attendance.entity.AttendanceStatus;
import com.grown.smartoffice.domain.attendance.entity.MonthlyAttendance;
import com.grown.smartoffice.domain.attendance.repository.AttendanceRepository;
import com.grown.smartoffice.domain.attendance.repository.MonthlyAttendanceRepository;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserRole;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AttendanceBatchServiceTest {

    @Mock AttendanceRepository attendanceRepository;
    @Mock MonthlyAttendanceRepository monthlyAttendanceRepository;
    @Mock UserRepository userRepository;

    @InjectMocks AttendanceBatchService batchService;

    User buildUser(Long id) {
        User u = User.builder()
                .employeeNumber("EMP" + id)
                .employeeName("직원" + id)
                .employeeEmail("user" + id + "@grown.com")
                .password("pw")
                .role(UserRole.USER)
                .position("사원")
                .hiredAt(LocalDate.of(2026, 1, 1))
                .build();
        ReflectionTestUtils.setField(u, "userId", id);
        ReflectionTestUtils.setField(u, "status", UserStatus.ACTIVE);
        return u;
    }

    Attendance buildAttendance(User user, LocalDateTime checkIn, LocalDateTime checkOut) {
        Attendance a = Attendance.builder()
                .user(user)
                .workDate(checkIn.toLocalDate())
                .checkIn(checkIn)
                .attendanceStatus(AttendanceStatus.NORMAL)
                .build();
        a.recordCheckOut(checkOut);
        return a;
    }

    // ── 일별 배치 ─────────────────────────────────────────

    @Test
    @DisplayName("runDailyBatch — 정시 출퇴근: NORMAL, work_minutes=480")
    void dailyBatch_normal() {
        User user = buildUser(1L);
        LocalDate date = LocalDate.of(2026, 4, 25);
        Attendance att = buildAttendance(user,
                LocalDateTime.of(2026, 4, 25, 9, 0),
                LocalDateTime.of(2026, 4, 25, 18, 0));

        given(attendanceRepository.findAllByWorkDate(date)).willReturn(List.of(att));
        given(userRepository.findAll()).willReturn(List.of(user));

        batchService.runDailyBatch(date, "SYSTEM");

        assertThat(att.getAttendanceStatus()).isEqualTo(AttendanceStatus.NORMAL);
        assertThat(att.getWorkMinutes()).isEqualTo(480);
        assertThat(att.getOvertimeMinutes()).isEqualTo(0);
    }

    @Test
    @DisplayName("runDailyBatch — 지각: 09:01 출근 → LATE")
    void dailyBatch_late() {
        User user = buildUser(2L);
        LocalDate date = LocalDate.of(2026, 4, 25);
        Attendance att = buildAttendance(user,
                LocalDateTime.of(2026, 4, 25, 9, 1),
                LocalDateTime.of(2026, 4, 25, 18, 0));

        given(attendanceRepository.findAllByWorkDate(date)).willReturn(List.of(att));
        given(userRepository.findAll()).willReturn(List.of(user));

        batchService.runDailyBatch(date, "SYSTEM");

        assertThat(att.getAttendanceStatus()).isEqualTo(AttendanceStatus.LATE);
    }

    @Test
    @DisplayName("runDailyBatch — 초과근무: 9시간 근무 → overtime=60")
    void dailyBatch_overtime() {
        User user = buildUser(3L);
        LocalDate date = LocalDate.of(2026, 4, 25);
        Attendance att = buildAttendance(user,
                LocalDateTime.of(2026, 4, 25, 9, 0),
                LocalDateTime.of(2026, 4, 25, 19, 0));

        given(attendanceRepository.findAllByWorkDate(date)).willReturn(List.of(att));
        given(userRepository.findAll()).willReturn(List.of(user));

        batchService.runDailyBatch(date, "SYSTEM");

        assertThat(att.getOvertimeMinutes()).isEqualTo(60);
    }

    @Test
    @DisplayName("runDailyBatch — 근태 없는 ACTIVE 직원 → ABSENT 삽입")
    void dailyBatch_absent() {
        User user = buildUser(4L);
        LocalDate date = LocalDate.of(2026, 4, 25);

        given(attendanceRepository.findAllByWorkDate(date)).willReturn(List.of());
        given(userRepository.findAll()).willReturn(List.of(user));

        batchService.runDailyBatch(date, "SYSTEM");

        verify(attendanceRepository).save(any(Attendance.class));
    }

    // ── 월별 집계 ─────────────────────────────────────────

    @Test
    @DisplayName("runMonthlyAggregation — 신규 생성")
    void monthlyAgg_create() {
        User user = buildUser(1L);
        LocalDate date = LocalDate.of(2026, 4, 1);
        Attendance att = buildAttendance(user,
                LocalDateTime.of(2026, 4, 1, 9, 0),
                LocalDateTime.of(2026, 4, 1, 18, 0));
        att.applyBatchResult(480, 0, AttendanceStatus.NORMAL, null);

        given(attendanceRepository.findAll()).willReturn(List.of(att));
        given(monthlyAttendanceRepository.findByUser_UserIdAndMonatYearAndMonatMonth(1L, 2026, 4))
                .willReturn(Optional.empty());

        batchService.runMonthlyAggregation(2026, 4);

        verify(monthlyAttendanceRepository).save(any(MonthlyAttendance.class));
    }
}
