package com.grown.smartoffice.domain.salary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SalaryCalculateRequest {

    @NotNull @Min(2020) @Max(2099)
    @Schema(description = "산출 연도", example = "2026")
    private Integer year;

    @NotNull @Min(1) @Max(12)
    @Schema(description = "산출 월", example = "4")
    private Integer month;

    @Schema(description = "산출 대상 userId 목록 (null이면 전체)")
    private List<Long> userIds;
}
