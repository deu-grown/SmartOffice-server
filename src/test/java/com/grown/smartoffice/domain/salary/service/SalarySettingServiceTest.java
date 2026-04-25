package com.grown.smartoffice.domain.salary.service;

import com.grown.smartoffice.domain.salary.dto.SalarySettingCreateRequest;
import com.grown.smartoffice.domain.salary.dto.SalarySettingUpdateRequest;
import com.grown.smartoffice.domain.salary.entity.SalarySetting;
import com.grown.smartoffice.domain.salary.repository.SalaryRecordRepository;
import com.grown.smartoffice.domain.salary.repository.SalarySettingRepository;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SalarySettingServiceTest {

    @Mock SalarySettingRepository salarySettingRepository;
    @Mock SalaryRecordRepository salaryRecordRepository;
    @InjectMocks SalarySettingService salarySettingService;

    SalarySetting buildSetting(Long id, String position, LocalDate from) {
        SalarySetting s = SalarySetting.builder()
                .salsetPosition(position)
                .baseSalary(2500000)
                .overtimeRate(new BigDecimal("1.5"))
                .nightRate(new BigDecimal("2.0"))
                .effectiveFrom(from)
                .build();
        ReflectionTestUtils.setField(s, "salsetId", id);
        return s;
    }

    @Test
    @DisplayName("create — 이전 기준의 effectiveTo가 신규 effectiveFrom-1일로 자동 설정")
    void create_closePreviousSetting() {
        SalarySetting prev = buildSetting(1L, "사원", LocalDate.of(2026, 1, 1));
        given(salarySettingRepository.findActiveByPosition("사원")).willReturn(Optional.of(prev));

        SalarySettingCreateRequest req = new SalarySettingCreateRequest();
        ReflectionTestUtils.setField(req, "position", "사원");
        ReflectionTestUtils.setField(req, "baseSalary", 2800000);
        ReflectionTestUtils.setField(req, "effectiveFrom", LocalDate.of(2026, 7, 1));

        SalarySetting newSetting = buildSetting(2L, "사원", LocalDate.of(2026, 7, 1));
        given(salarySettingRepository.save(any())).willReturn(newSetting);

        salarySettingService.create(req);

        assertThat(prev.getEffectiveTo()).isEqualTo(LocalDate.of(2026, 6, 30));
    }

    @Test
    @DisplayName("update — 급여 산출에 이미 사용된 기준 수정 → SALARY_SETTING_USED")
    void update_usedSetting_conflict() {
        SalarySetting setting = buildSetting(1L, "사원", LocalDate.of(2026, 1, 1));
        given(salarySettingRepository.findById(1L)).willReturn(Optional.of(setting));
        given(salaryRecordRepository.existsBySalarySetting_SalsetId(1L)).willReturn(true);

        SalarySettingUpdateRequest req = new SalarySettingUpdateRequest();
        ReflectionTestUtils.setField(req, "baseSalary", 3000000);

        assertThatThrownBy(() -> salarySettingService.update(1L, req))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SALARY_SETTING_USED);
    }
}
