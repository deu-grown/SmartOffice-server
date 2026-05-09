package com.grown.smartoffice.domain.power.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PowerHourlyResponse {

    private Long zoneId;
    private String zoneName;
    private List<HourlyLog> logs;

    @Getter
    @Builder
    public static class HourlyLog {
        private Long id;
        private Long deviceId;
        private String deviceName;
        private LocalDateTime hourAt;
        private BigDecimal kwh;
        private BigDecimal avgWatt;
        private BigDecimal peakWatt;
    }
}
