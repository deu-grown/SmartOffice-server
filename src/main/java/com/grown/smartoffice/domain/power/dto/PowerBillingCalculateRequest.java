package com.grown.smartoffice.domain.power.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PowerBillingCalculateRequest {

    @NotNull
    @Min(2020)
    private Integer year;

    @NotNull
    @Min(1)
    @Max(12)
    private Integer month;

    @NotNull
    @Min(1)
    private Integer unitPrice;

    @NotNull
    @Min(0)
    private Integer baseFee;

    private List<Long> zoneIds;
}
