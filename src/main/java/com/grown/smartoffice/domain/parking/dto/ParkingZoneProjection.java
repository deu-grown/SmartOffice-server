package com.grown.smartoffice.domain.parking.dto;

import com.grown.smartoffice.domain.zone.entity.ZoneType;

public interface ParkingZoneProjection {
    Long getZoneId();
    String getZoneName();
    ZoneType getZoneType();
    Long getTotalSpots();
    Long getOccupiedSpots();
}
