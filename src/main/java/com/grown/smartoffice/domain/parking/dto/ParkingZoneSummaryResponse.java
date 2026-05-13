package com.grown.smartoffice.domain.parking.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ParkingZoneSummaryResponse {
    private Long zoneId;
    private String zoneName;
    private long totalSpots;
    private long occupiedSpots;
    private long availableSpots;
    private List<ParkingSpotResponse> spots;
}
