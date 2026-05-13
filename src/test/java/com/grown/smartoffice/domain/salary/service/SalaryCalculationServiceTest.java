package com.grown.smartoffice.domain.salary.service;

import com.grown.smartoffice.domain.attendance.entity.MonthlyAttendance;
import com.grown.smartoffice.domain.attendance.repository.MonthlyAttendanceRepository;
import com.grown.smartoffice.domain.salary.dto.SalaryCalculateRequest;
import com.grown.smartoffice.domain.salary.dto.SalaryCalculateResponse;
import com.grown.smartoffice.domain.salary.entity.SalaryRecord;
import com.grown.smartoffice.domain.salary.entity.SalaryStatus;
import com.grown.smartoffice.domain.salary.entity.SalarySetting;
import com.grown.smartoffice.domain.salary.repository.SalaryRecordRepository;
import com.grown.smartoffice.domain.salary.repository.SalarySettingRepository;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserRole;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SalaryCalculationServiceTest {

    @Mock UserRepository userRepository;
    @Mock MonthlyAttendanceRepository monthlyAttendanceRepository;
    @Mock SalarySettingRepository salarySettingRepository;
    @Mock SalaryRecordRepository salaryRecordRepository;
    @InjectMocks SalaryCalculationService salaryCalculationService;

    private User user1;
    private User user2;
    private SalarySetting setting;

    @BeforeEach
    void setUp() {
        user1 = newUser(1L, "EMP-1", "개발자");
        user2 = newUser(2L, "EMP-2", "개발자");

        setting = SalarySetting.builder().salsetPosition("개발자").baseSalary(4000000)
                .overtimeRate(new BigDecimal("1.5")).nightRate(new BigDecimal("2.0"))
                .effectiveFrom(LocalDate.of(2026, 1, 1)).build();
        ReflectionTestUtils.setField(setting, "salsetId", 2L);
    }

    private User newUser(long id, String num, String position) {
        User u = User.builder()
                .employeeNumber(num).employeeName(num)
                .employeeEmail(num + "@grown.com").password("p")
                .role(UserRole.USER).position(position).status(UserStatus.ACTIVE)
                .hiredAt(LocalDate.now()).build();
        ReflectionTestUtils.setField(u, "userId", id);
        return u;
    }

    private MonthlyAttendance monat(User u, int overtimeMinutes) {
        MonthlyAttendance m = MonthlyAttendance.builder().user(u)
                .monatYear(2026).monatMonth(5)
                .monatTotalWorkMinutes(2700).monatOvertimeMinutes(overtimeMinutes)
                .lateCount(0).earlyLeaveCount(0).absentCount(0).build();
        ReflectionTestUtils.setField(m, "monatId", 10L);
        return m;
    }

    private SalaryCalculateRequest req(List<Long> ids) {
        SalaryCalculateRequest r = new SalaryCalculateRequest();
        ReflectionTestUtils.setField(r, "year", 2026);
        ReflectionTestUtils.setField(r, "month", 5);
        if (ids != null) ReflectionTestUtils.setField(r, "userIds", ids);
        return r;
    }

    @Test
    @DisplayName("calculate — 신규 record 저장, 야근수당 산식 검증")
    void calculate_newRecord_overtimePay() {
        given(userRepository.findAllById(List.of(1L))).willReturn(List.of(user1));
        given(monthlyAttendanceRepository.findByUser_UserIdAndMonatYearAndMonatMonth(1L, 2026, 5))
                .willReturn(Optional.of(monat(user1, 120)));   // 2시간 야근
        given(salarySettingRepository.findApplicableByPositionAndDate(eq("개발자"), any()))
                .willReturn(Optional.of(setting));
        given(salaryRecordRepository.findByUser_UserIdAndSalrecYearAndSalrecMonth(1L, 2026, 5))
                .willReturn(Optional.empty());

        SalaryCalculateResponse res = salaryCalculationService.calculate(req(List.of(1L)));

        assertThat(res.getTotalCount()).isEqualTo(1);
        assertThat(res.getSuccessCount()).isEqualTo(1);
        assertThat(res.getSkipCount()).isZero();
        // baseSalary=4000000, hourlyRate=25000, overtime=2h × 25000 × 1.5 = 75000
        assertThat(res.getRecords().get(0).getOvertimePay()).isEqualTo(75000);
        assertThat(res.getRecords().get(0).getTotalPay()).isEqualTo(4075000);
        verify(salaryRecordRepository).save(any(SalaryRecord.class));
    }

    @Test
    @DisplayName("calculate — 월 근태 없음 → skip 카운트")
    void calculate_skipNoMonat() {
        given(userRepository.findAllById(List.of(1L))).willReturn(List.of(user1));
        given(monthlyAttendanceRepository.findByUser_UserIdAndMonatYearAndMonatMonth(1L, 2026, 5))
                .willReturn(Optional.empty());

        SalaryCalculateResponse res = salaryCalculationService.calculate(req(List.of(1L)));
        assertThat(res.getSkipCount()).isEqualTo(1);
        assertThat(res.getSuccessCount()).isZero();
        verify(salaryRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("calculate — 급여 기준 없음 → skip")
    void calculate_skipNoSetting() {
        given(userRepository.findAllById(List.of(1L))).willReturn(List.of(user1));
        given(monthlyAttendanceRepository.findByUser_UserIdAndMonatYearAndMonatMonth(1L, 2026, 5))
                .willReturn(Optional.of(monat(user1, 0)));
        given(salarySettingRepository.findApplicableByPositionAndDate(eq("개발자"), any()))
                .willReturn(Optional.empty());

        SalaryCalculateResponse res = salaryCalculationService.calculate(req(List.of(1L)));
        assertThat(res.getSkipCount()).isEqualTo(1);
        verify(salaryRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("calculate — 이미 CONFIRMED record 존재 → skip, 덮어쓰기 X")
    void calculate_skipAlreadyConfirmed() {
        SalaryRecord existing = SalaryRecord.builder()
                .user(user1).monthlyAttendance(monat(user1, 0)).salarySetting(setting)
                .salrecYear(2026).salrecMonth(5)
                .salrecBaseSalary(4000000).overtimePay(0).totalPay(4000000).build();
        ReflectionTestUtils.setField(existing, "salrecStatus", SalaryStatus.CONFIRMED);

        given(userRepository.findAllById(List.of(1L))).willReturn(List.of(user1));
        given(monthlyAttendanceRepository.findByUser_UserIdAndMonatYearAndMonatMonth(1L, 2026, 5))
                .willReturn(Optional.of(monat(user1, 0)));
        given(salarySettingRepository.findApplicableByPositionAndDate(eq("개발자"), any()))
                .willReturn(Optional.of(setting));
        given(salaryRecordRepository.findByUser_UserIdAndSalrecYearAndSalrecMonth(1L, 2026, 5))
                .willReturn(Optional.of(existing));

        SalaryCalculateResponse res = salaryCalculationService.calculate(req(List.of(1L)));
        assertThat(res.getSkipCount()).isEqualTo(1);
        verify(salaryRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("calculate — userIds 미지정 시 ACTIVE 사용자 전체 대상")
    void calculate_allActiveUsers() {
        ReflectionTestUtils.setField(user2, "status", UserStatus.INACTIVE);
        given(userRepository.findAll()).willReturn(List.of(user1, user2));
        given(monthlyAttendanceRepository.findByUser_UserIdAndMonatYearAndMonatMonth(eq(1L), eq(2026), eq(5)))
                .willReturn(Optional.empty());

        SalaryCalculateResponse res = salaryCalculationService.calculate(req(null));
        assertThat(res.getTotalCount()).isEqualTo(1);   // ACTIVE 1명만
        // 1명 skip(monat 없음)
        assertThat(res.getSkipCount()).isEqualTo(1);
    }
}
