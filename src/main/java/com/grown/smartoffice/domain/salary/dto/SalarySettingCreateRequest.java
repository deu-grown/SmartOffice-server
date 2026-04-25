package com.grown.smartoffice.domain.salary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class SalarySettingCreateRequest {

    @NotBlank
    @Schema(description = "직급", example = "사원")
    private String position;

    @NotNull @Positive
    @Schema(description = "기본급 (원)", example = "2500000")
    private Integer baseSalary;

    @Schema(description = "초과근무 배율", example = "1.5")
    private BigDecimal overtimeRate;

    @Schema(description = "야간 배율", example = "2.0")
    private BigDecimal nightRate;

    @NotNull
    @Schema(description = "적용 시작일", example = "2026-01-01")
    private LocalDate effectiveFrom;
}
