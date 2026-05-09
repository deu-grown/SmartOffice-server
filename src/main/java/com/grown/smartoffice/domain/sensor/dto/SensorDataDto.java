package com.grown.smartoffice.domain.sensor.dto;

import com.grown.smartoffice.domain.sensor.entity.SensorLog;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@lombok.Builder
@lombok.AllArgsConstructor
public class SensorDataDto {
    private final Long id;
    private final String sensorType;
    private final BigDecimal value;
    private final String unit;
    private final LocalDateTime timestamp;

    public SensorDataDto(SensorLog log) {
        this.id = log.getId();
        this.sensorType = log.getSensorType();
        this.value = log.getValue();
        this.unit = log.getUnit();
        this.timestamp = log.getLoggedAt();
    }
}
