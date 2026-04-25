package com.grown.smartoffice.domain.salary.dto;

import com.grown.smartoffice.domain.salary.entity.SalarySetting;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class SalarySettingResponse {

    private Long id;
    private String position;
    private int baseSalary;
    private BigDecimal overtimeRate;
    private BigDecimal nightRate;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    public static SalarySettingResponse from(SalarySetting s) {
        return SalarySettingResponse.builder()
                .id(s.getSalsetId())
                .position(s.getSalsetPosition())
                .baseSalary(s.getBaseSalary())
                .overtimeRate(s.getOvertimeRate())
                .nightRate(s.getNightRate())
                .effectiveFrom(s.getEffectiveFrom())
                .effectiveTo(s.getEffectiveTo())
                .build();
    }
}
