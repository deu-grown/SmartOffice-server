package com.grown.smartoffice.domain.attendance.service;

import com.grown.smartoffice.domain.attendance.dto.AttendanceDailyResponse;
import com.grown.smartoffice.domain.attendance.dto.AttendanceMonthlyResponse;
import com.grown.smartoffice.domain.attendance.entity.Attendance;
import com.grown.smartoffice.domain.attendance.entity.AttendanceStatus;
import com.grown.smartoffice.domain.attendance.entity.MonthlyAttendance;
import com.grown.smartoffice.domain.attendance.repository.AttendanceRepository;
import com.grown.smartoffice.domain.attendance.repository.MonthlyAttendanceRepository;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserRole;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import com.grown.smartoffice.global.common.PageResponse;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AttendanceQueryServiceTest {

    @Mock AttendanceRepository attendanceRepository;
    @Mock MonthlyAttendanceRepository monthlyAttendanceRepository;
    @InjectMocks AttendanceQueryService attendanceQueryService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .employeeNumber("EMP-Q").employeeName("쿼리").employeeEmail("q@grown.com")
                .password("p").role(UserRole.USER).position("사원")
                .status(UserStatus.ACTIVE).hiredAt(LocalDate.now()).build();
        ReflectionTestUtils.setField(user, "userId", 10L);
    }

    @Test
    @DisplayName("getMyDaily — 본인 + 해당 날짜만 매치")
    void getMyDaily_success() {
        Attendance a = Attendance.builder()
                .user(user).workDate(LocalDate.of(2026, 5, 13))
                .checkIn(LocalDateTime.of(2026, 5, 13, 9, 0))
                .attendanceStatus(AttendanceStatus.NORMAL).build();
        given(attendanceRepository.findAll()).willReturn(List.of(a));

        AttendanceDailyResponse res = attendanceQueryService.getMyDaily("q@grown.com",
                LocalDate.of(2026, 5, 13));
        assertThat(res).isNotNull();
    }

    @Test
    @DisplayName("getMyDaily — 일치 없음 → ATTENDANCE_NOT_FOUND")
    void getMyDaily_notFound() {
        given(attendanceRepository.findAll()).willReturn(List.of());
        assertThatThrownBy(() -> attendanceQueryService.getMyDaily("none@grown.com", LocalDate.now()))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.ATTENDANCE_NOT_FOUND);
    }

    @Test
    @DisplayName("getMyMonthly — 미존재 → ATTENDANCE_NOT_FOUND")
    void getMyMonthly_notFound() {
        given(monthlyAttendanceRepository.findByEmailAndYearMonth("q@grown.com", 2026, 5))
                .willReturn(Optional.empty());
        assertThatThrownBy(() -> attendanceQueryService.getMyMonthly("q@grown.com", YearMonth.of(2026, 5)))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.ATTENDANCE_NOT_FOUND);
    }

    @Test
    @DisplayName("getMyMonthly — 성공 시 DTO 변환")
    void getMyMonthly_success() {
        MonthlyAttendance ma = MonthlyAttendance.builder()
                .user(user).monatYear(2026).monatMonth(5)
                .monatTotalWorkMinutes(2700).monatOvertimeMinutes(0)
                .lateCount(0).earlyLeaveCount(0).absentCount(0).build();
        given(monthlyAttendanceRepository.findByEmailAndYearMonth("q@grown.com", 2026, 5))
                .willReturn(Optional.of(ma));

        AttendanceMonthlyResponse res = attendanceQueryService.getMyMonthly("q@grown.com",
                YearMonth.of(2026, 5));
        assertThat(res).isNotNull();
    }

    @Test
    @DisplayName("getAllDaily — 페이지 결과 정상 매핑")
    void getAllDaily_success() {
        Attendance a = Attendance.builder()
                .user(user).workDate(LocalDate.of(2026, 5, 13))
                .checkIn(LocalDateTime.of(2026, 5, 13, 9, 0))
                .attendanceStatus(AttendanceStatus.NORMAL).build();
        Page<Attendance> page = new PageImpl<>(List.of(a), PageRequest.of(0, 10), 1);
        given(attendanceRepository.findAllByDateWithFilters(
                eq(LocalDate.of(2026, 5, 13)), any(), any(), any())).willReturn(page);

        PageResponse<AttendanceDailyResponse> res = attendanceQueryService.getAllDaily(
                LocalDate.of(2026, 5, 13), null, null, 0, 10);
        assertThat(res.content()).hasSize(1);
        assertThat(res.totalElements()).isEqualTo(1);
    }
}
