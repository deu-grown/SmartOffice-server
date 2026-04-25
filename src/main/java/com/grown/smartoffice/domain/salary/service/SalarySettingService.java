package com.grown.smartoffice.domain.salary.service;

import com.grown.smartoffice.domain.salary.dto.SalarySettingCreateRequest;
import com.grown.smartoffice.domain.salary.dto.SalarySettingResponse;
import com.grown.smartoffice.domain.salary.dto.SalarySettingUpdateRequest;
import com.grown.smartoffice.domain.salary.entity.SalarySetting;
import com.grown.smartoffice.domain.salary.repository.SalaryRecordRepository;
import com.grown.smartoffice.domain.salary.repository.SalarySettingRepository;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalarySettingService {

    private final SalarySettingRepository salarySettingRepository;
    private final SalaryRecordRepository salaryRecordRepository;

    @Transactional
    public SalarySettingResponse create(SalarySettingCreateRequest request) {
        salarySettingRepository.findActiveByPosition(request.getPosition())
                .ifPresent(prev -> prev.closeAt(request.getEffectiveFrom().minusDays(1)));

        SalarySetting setting = SalarySetting.builder()
                .salsetPosition(request.getPosition())
                .baseSalary(request.getBaseSalary())
                .overtimeRate(request.getOvertimeRate() != null ? request.getOvertimeRate() : new BigDecimal("1.5"))
                .nightRate(request.getNightRate() != null ? request.getNightRate() : new BigDecimal("2.0"))
                .effectiveFrom(request.getEffectiveFrom())
                .build();

        return SalarySettingResponse.from(salarySettingRepository.save(setting));
    }

    @Transactional(readOnly = true)
    public List<SalarySettingResponse> list(String position) {
        List<SalarySetting> settings = position != null
                ? salarySettingRepository.findAllBySalsetPosition(position)
                : salarySettingRepository.findAll();
        return settings.stream().map(SalarySettingResponse::from).toList();
    }

    @Transactional
    public SalarySettingResponse update(Long id, SalarySettingUpdateRequest request) {
        SalarySetting setting = salarySettingRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SALARY_SETTING_NOT_FOUND));

        if (salaryRecordRepository.existsBySalarySetting_SalsetId(id)) {
            throw new CustomException(ErrorCode.SALARY_SETTING_USED);
        }

        int newBaseSalary = request.getBaseSalary() != null ? request.getBaseSalary() : setting.getBaseSalary();
        setting.update(newBaseSalary, request.getOvertimeRate(), request.getNightRate());
        return SalarySettingResponse.from(setting);
    }
}
