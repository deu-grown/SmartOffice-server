package com.grown.smartoffice.domain.sensor.dto;

import lombok.Getter;

@Getter
public class SensorLogResponse {
    private final Long logId;

    public SensorLogResponse(Long logId) {
        this.logId = logId;
    }
}
