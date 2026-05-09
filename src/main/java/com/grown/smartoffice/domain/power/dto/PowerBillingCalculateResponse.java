package com.grown.smartoffice.domain.power.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PowerBillingCalculateResponse {

    private Integer year;
    private Integer month;
    private Integer totalCount;
    private Integer successCount;
    private Integer skipCount;
    private Integer totalFee;
}
