package com.grown.smartoffice.domain.parking.service;

import com.grown.smartoffice.domain.parking.dto.*;

import java.util.List;

public interface ParkingService {

    ParkingSpotResponse createSpot(ParkingSpotCreateRequest request);

    ParkingSpotResponse updateSpot(Long spotId, ParkingSpotUpdateRequest request);

    void deleteSpot(Long spotId);

    List<ParkingSpotResponse> getSpots(Long zoneId, String spotType, String status);

    List<ParkingZoneListResponse> getParkingZones();

    ParkingZoneSummaryResponse getZoneSummary(Long zoneId);

    ParkingZoneMapResponse getZoneMap(Long zoneId);

    ParkingStatusUpdateResponse updateOccupancyFromIot(Long spotId, ParkingStatusUpdateRequest request);
}
