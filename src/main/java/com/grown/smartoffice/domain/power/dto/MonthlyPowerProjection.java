package com.grown.smartoffice.domain.power.dto;

import java.math.BigDecimal;

public interface MonthlyPowerProjection {
    Long getZoneId();
    BigDecimal getTotalKwh();
}
