package com.grown.smartoffice.domain.power.dto;

import java.math.BigDecimal;

public interface HourlyPowerProjection {
    Long getId();
    Long getDeviceId();
    String getDeviceName();
    String getHourAt();
    BigDecimal getKwh();
    BigDecimal getAvgWatt();
    BigDecimal getPeakWatt();
}
