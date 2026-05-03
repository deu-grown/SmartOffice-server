package com.grown.smartoffice.domain.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class SensorCurrentResponse {
    private Long zoneId;
    private String zoneName;
    private BigDecimal temp;
    private BigDecimal humi;
    private BigDecimal co2;
    private LocalDateTime updatedAt;
}
