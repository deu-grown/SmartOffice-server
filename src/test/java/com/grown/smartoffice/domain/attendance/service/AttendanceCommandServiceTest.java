package com.grown.smartoffice.domain.attendance.service;

import com.grown.smartoffice.domain.attendance.entity.Attendance;
import com.grown.smartoffice.domain.attendance.entity.AttendanceStatus;
import com.grown.smartoffice.domain.attendance.repository.AttendanceRepository;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserRole;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AttendanceCommandServiceTest {

    @Mock AttendanceRepository attendanceRepository;
    @Mock UserRepository userRepository;
    @InjectMocks AttendanceCommandService attendanceCommandService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .employeeNumber("EMP-CMD").employeeName("커맨드").employeeEmail("cmd@grown.com")
                .password("p").role(UserRole.USER).position("사원")
                .status(UserStatus.ACTIVE).hiredAt(LocalDate.now()).build();
        ReflectionTestUtils.setField(user, "userId", 100L);
    }

    @Test
    @DisplayName("recordTag — 같은 날 첫 태그면 신규 출근 attendance 생성")
    void recordTag_firstTagCreatesNew() {
        LocalDateTime tagAt = LocalDateTime.of(2026, 5, 13, 9, 0);
        given(attendanceRepository.findByUser_UserIdAndWorkDate(100L, tagAt.toLocalDate()))
                .willReturn(Optional.empty());
        given(userRepository.findById(100L)).willReturn(Optional.of(user));

        attendanceCommandService.recordTag(100L, tagAt);

        ArgumentCaptor<Attendance> captor = ArgumentCaptor.forClass(Attendance.class);
        verify(attendanceRepository).save(captor.capture());
        Attendance saved = captor.getValue();
        assertThat(saved.getCheckIn()).isEqualTo(tagAt);
        assertThat(saved.getCheckOut()).isNull();
        assertThat(saved.getAttendanceStatus()).isEqualTo(AttendanceStatus.NORMAL);
    }

    @Test
    @DisplayName("recordTag — 첫 태그 시 user 미존재 → USER_NOT_FOUND")
    void recordTag_userNotFound() {
        LocalDateTime tagAt = LocalDateTime.of(2026, 5, 13, 9, 0);
        given(attendanceRepository.findByUser_UserIdAndWorkDate(999L, tagAt.toLocalDate()))
                .willReturn(Optional.empty());
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceCommandService.recordTag(999L, tagAt))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("recordTag — 같은 날 두 번째 태그면 기존 attendance에 checkOut 갱신")
    void recordTag_secondTagUpdatesCheckOut() {
        LocalDateTime in  = LocalDateTime.of(2026, 5, 13,  9, 0);
        LocalDateTime out = LocalDateTime.of(2026, 5, 13, 18, 0);

        Attendance existing = Attendance.builder()
                .user(user).workDate(in.toLocalDate()).checkIn(in)
                .attendanceStatus(AttendanceStatus.NORMAL).build();
        ReflectionTestUtils.setField(existing, "attendanceId", 1L);

        given(attendanceRepository.findByUser_UserIdAndWorkDate(100L, in.toLocalDate()))
                .willReturn(Optional.of(existing));

        attendanceCommandService.recordTag(100L, out);

        assertThat(existing.getCheckOut()).isEqualTo(out);
        verify(attendanceRepository, never()).save(existing);   // dirty checking에 의존
        verify(userRepository, never()).findById(any(Long.class));
    }

    @Test
    @DisplayName("correctAttendance — attendanceId 미존재 → ATTENDANCE_NOT_FOUND")
    void correctAttendance_notFound() {
        given(attendanceRepository.findById(999L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> attendanceCommandService.correctAttendance(
                999L, LocalDateTime.now(), LocalDateTime.now(), "보정"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.ATTENDANCE_NOT_FOUND);
    }

    @Test
    @DisplayName("correctAttendance — checkIn/checkOut/note 부분 갱신")
    void correctAttendance_partialUpdate() {
        Attendance a = Attendance.builder()
                .user(user).workDate(LocalDate.of(2026, 5, 13))
                .checkIn(LocalDateTime.of(2026, 5, 13, 9, 0))
                .attendanceStatus(AttendanceStatus.NORMAL).build();
        ReflectionTestUtils.setField(a, "attendanceId", 5L);

        given(attendanceRepository.findById(5L)).willReturn(Optional.of(a));

        attendanceCommandService.correctAttendance(5L, null,
                LocalDateTime.of(2026, 5, 13, 19, 0), "야근");

        assertThat(a.getCheckIn()).isEqualTo(LocalDateTime.of(2026, 5, 13, 9, 0));   // 변경 X
        assertThat(a.getCheckOut()).isEqualTo(LocalDateTime.of(2026, 5, 13, 19, 0));
        assertThat(a.getAttendanceNote()).isEqualTo("야근");
    }

}
