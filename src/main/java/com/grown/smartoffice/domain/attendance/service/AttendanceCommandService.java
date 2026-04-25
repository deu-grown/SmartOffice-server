package com.grown.smartoffice.domain.attendance.service;

import com.grown.smartoffice.domain.attendance.entity.Attendance;
import com.grown.smartoffice.domain.attendance.entity.AttendanceStatus;
import com.grown.smartoffice.domain.attendance.repository.AttendanceRepository;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceCommandService {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    @Transactional
    public void recordTag(Long userId, LocalDateTime taggedAt) {
        LocalDate workDate = taggedAt.toLocalDate();

        Optional<Attendance> existing = attendanceRepository.findByUser_UserIdAndWorkDate(userId, workDate);

        if (existing.isEmpty()) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            Attendance attendance = Attendance.builder()
                    .user(user)
                    .workDate(workDate)
                    .checkIn(taggedAt)
                    .attendanceStatus(AttendanceStatus.NORMAL)
                    .build();
            attendanceRepository.save(attendance);
        } else {
            existing.get().recordCheckOut(taggedAt);
        }
    }

    @Transactional
    public void correctAttendance(Long attendanceId, LocalDateTime checkIn, LocalDateTime checkOut, String note) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new CustomException(ErrorCode.ATTENDANCE_NOT_FOUND));
        attendance.correct(checkIn, checkOut, note);
    }
}
