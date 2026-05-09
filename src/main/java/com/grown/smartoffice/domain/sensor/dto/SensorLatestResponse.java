package com.grown.smartoffice.domain.sensor.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class SensorLatestResponse {
    private final Map<String, Object> searchQuery;
    private final Integer totalCount;
    private final List<SensorDataDto> sensorDataList;

    @Builder
    public SensorLatestResponse(Long zoneId, List<SensorDataDto> sensorDataList) {
        this.searchQuery = Map.of("zoneId", zoneId);
        this.totalCount = sensorDataList.size();
        this.sensorDataList = sensorDataList;
    }
}
