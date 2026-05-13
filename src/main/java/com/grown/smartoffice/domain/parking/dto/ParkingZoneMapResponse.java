package com.grown.smartoffice.domain.parking.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ParkingZoneMapResponse {
    private Long zoneId;
    private String zoneName;
    private List<ParkingSpotMapResponse> spots;
}
