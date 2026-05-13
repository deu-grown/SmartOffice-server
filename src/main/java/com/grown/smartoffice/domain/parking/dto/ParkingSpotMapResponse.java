package com.grown.smartoffice.domain.parking.dto;

import com.grown.smartoffice.domain.parking.entity.ParkingSpot;
import com.grown.smartoffice.domain.parking.entity.SpotType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParkingSpotMapResponse {
    private Long spotId;
    private String spotNumber;
    private SpotType spotType;
    private Integer positionX;
    private Integer positionY;
    private boolean occupied;

    public static ParkingSpotMapResponse from(ParkingSpot s) {
        return ParkingSpotMapResponse.builder()
                .spotId(s.getSpotId())
                .spotNumber(s.getSpotNumber())
                .spotType(s.getSpotType())
                .positionX(s.getPositionX())
                .positionY(s.getPositionY())
                .occupied(s.isOccupied())
                .build();
    }
}
