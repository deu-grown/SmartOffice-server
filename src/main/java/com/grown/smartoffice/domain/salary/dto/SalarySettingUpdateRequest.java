package com.grown.smartoffice.domain.salary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class SalarySettingUpdateRequest {

    @Positive
    @Schema(description = "변경할 기본급 (원)", example = "2800000")
    private Integer baseSalary;

    @Schema(description = "변경할 초과근무 배율")
    private BigDecimal overtimeRate;

    @Schema(description = "변경할 야간 배율")
    private BigDecimal nightRate;
}
