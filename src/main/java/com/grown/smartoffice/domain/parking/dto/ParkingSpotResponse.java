package com.grown.smartoffice.domain.parking.dto;

import com.grown.smartoffice.domain.parking.entity.ParkingSpot;
import com.grown.smartoffice.domain.parking.entity.SpotStatus;
import com.grown.smartoffice.domain.parking.entity.SpotType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ParkingSpotResponse {
    private Long spotId;
    private Long zoneId;
    private String zoneName;
    private String spotNumber;
    private SpotType spotType;
    private Long deviceId;
    private String deviceName;
    private Integer positionX;
    private Integer positionY;
    private boolean occupied;
    private SpotStatus spotStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ParkingSpotResponse from(ParkingSpot s) {
        return ParkingSpotResponse.builder()
                .spotId(s.getSpotId())
                .zoneId(s.getZone().getZoneId())
                .zoneName(s.getZone().getZoneName())
                .spotNumber(s.getSpotNumber())
                .spotType(s.getSpotType())
                .deviceId(s.getDevice() != null ? s.getDevice().getDevicesId() : null)
                .deviceName(s.getDevice() != null ? s.getDevice().getDeviceName() : null)
                .positionX(s.getPositionX())
                .positionY(s.getPositionY())
                .occupied(s.isOccupied())
                .spotStatus(s.getSpotStatus())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
