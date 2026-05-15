package com.grown.smartoffice.domain.parking.dto;

import com.grown.smartoffice.domain.zone.entity.ZoneType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParkingZoneListResponse {

    private Long zoneId;
    private String zoneName;
    private ZoneType zoneType;
    private Long totalSpots;
    private Long occupiedSpots;
}
