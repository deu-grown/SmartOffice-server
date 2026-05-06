package com.grown.smartoffice.domain.sensor.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
public class SensorHistoryResponse {
    private final Map<String, Object> searchQuery;
    private final Integer totalCount;
    private final List<SensorDataDto> sensorDataList;

    @Builder
    public SensorHistoryResponse(Long zoneId, LocalDate startDate, LocalDate endDate, List<SensorDataDto> sensorDataList) {
        this.searchQuery = Map.of(
            "zoneId", zoneId,
            "period", startDate + " ~ " + endDate
        );
        this.totalCount = sensorDataList.size();
        this.sensorDataList = sensorDataList;
    }
}
