package com.grown.smartoffice.domain.salary.service;

import com.grown.smartoffice.domain.salary.dto.SalaryRecordResponse;
import com.grown.smartoffice.domain.salary.entity.SalaryRecord;
import com.grown.smartoffice.domain.salary.entity.SalaryStatus;
import com.grown.smartoffice.domain.salary.entity.SalarySetting;
import com.grown.smartoffice.domain.salary.repository.SalaryRecordRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SalaryRecordServiceTest {

    @Mock SalaryRecordRepository salaryRecordRepository;
    @InjectMocks SalaryRecordService salaryRecordService;

    private User user;
    private SalarySetting setting;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .employeeNumber("EMP-S").employeeName("샐러리").employeeEmail("s@grown.com")
                .password("p").role(UserRole.USER).position("개발자")
                .status(UserStatus.ACTIVE).hiredAt(LocalDate.now()).build();
        ReflectionTestUtils.setField(user, "userId", 1L);

        setting = SalarySetting.builder()
                .salsetPosition("개발자").baseSalary(4000000)
                .overtimeRate(new BigDecimal("1.5")).nightRate(new BigDecimal("2.0"))
                .effectiveFrom(LocalDate.of(2026, 1, 1)).build();
        ReflectionTestUtils.setField(setting, "salsetId", 2L);
    }

    private SalaryRecord record(SalaryStatus status, int totalPay) {
        SalaryRecord r = SalaryRecord.builder()
                .user(user).monthlyAttendance(null).salarySetting(setting)
                .salrecYear(2026).salrecMonth(5)
                .salrecBaseSalary(4000000).overtimePay(0).totalPay(totalPay).build();
        ReflectionTestUtils.setField(r, "salrecId", 100L);
        ReflectionTestUtils.setField(r, "salrecStatus", status);
        return r;
    }

    @Test
    @DisplayName("confirm — DRAFT → CONFIRMED 전이")
    void confirm_success() {
        SalaryRecord r = record(SalaryStatus.DRAFT, 4000000);
        given(salaryRecordRepository.findById(100L)).willReturn(Optional.of(r));

        SalaryRecordResponse res = salaryRecordService.confirm(100L);
        assertThat(r.getSalrecStatus()).isEqualTo(SalaryStatus.CONFIRMED);
        assertThat(res.getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("confirm — 이미 CONFIRMED → SALARY_RECORD_ALREADY_CONFIRMED")
    void confirm_alreadyConfirmed() {
        SalaryRecord r = record(SalaryStatus.CONFIRMED, 4000000);
        given(salaryRecordRepository.findById(100L)).willReturn(Optional.of(r));

        assertThatThrownBy(() -> salaryRecordService.confirm(100L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.SALARY_RECORD_ALREADY_CONFIRMED);
    }

    @Test
    @DisplayName("confirm — 미존재 → SALARY_RECORD_NOT_FOUND")
    void confirm_notFound() {
        given(salaryRecordRepository.findById(999L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> salaryRecordService.confirm(999L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.SALARY_RECORD_NOT_FOUND);
    }

    @Test
    @DisplayName("getMy — CONFIRMED 본인 급여 조회")
    void getMy_success() {
        SalaryRecord r = record(SalaryStatus.CONFIRMED, 4000000);
        given(salaryRecordRepository.findMyConfirmed("s@grown.com", 2026, 5)).willReturn(Optional.of(r));

        SalaryRecordResponse res = salaryRecordService.getMy("s@grown.com", 2026, 5);
        assertThat(res.getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("getMy — 미존재 → SALARY_RECORD_NOT_FOUND")
    void getMy_notFound() {
        given(salaryRecordRepository.findMyConfirmed("s@grown.com", 2026, 6)).willReturn(Optional.empty());
        assertThatThrownBy(() -> salaryRecordService.getMy("s@grown.com", 2026, 6))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.SALARY_RECORD_NOT_FOUND);
    }

    @Test
    @DisplayName("getAll — status 필터 변환 + 페이지 매핑")
    void getAll_withStatusFilter() {
        SalaryRecord r = record(SalaryStatus.DRAFT, 4000000);
        Page<SalaryRecord> page = new PageImpl<>(List.of(r), PageRequest.of(0, 10), 1);
        given(salaryRecordRepository.findAllByYearMonthFiltered(
                eq(2026), eq(5), eq(null), eq(SalaryStatus.DRAFT), any())).willReturn(page);

        PageResponse<SalaryRecordResponse> res = salaryRecordService.getAll(2026, 5, null, "DRAFT", 0, 10);
        assertThat(res.content()).hasSize(1);
        assertThat(res.totalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("getAll — status null 허용")
    void getAll_nullStatus() {
        Page<SalaryRecord> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        given(salaryRecordRepository.findAllByYearMonthFiltered(
                eq(2026), eq(5), any(), eq(null), any())).willReturn(page);

        PageResponse<SalaryRecordResponse> res = salaryRecordService.getAll(2026, 5, 1L, null, 0, 10);
        assertThat(res.content()).isEmpty();
    }
}
