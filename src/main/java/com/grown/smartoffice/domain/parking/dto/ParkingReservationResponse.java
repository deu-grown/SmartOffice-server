package com.grown.smartoffice.domain.parking.dto;

import com.grown.smartoffice.domain.parking.entity.ParkingReservation;
import com.grown.smartoffice.domain.parking.entity.ParkingReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ParkingReservationResponse {

    private Long reservationId;
    private Long vehicleId;
    private String vehiclePlateNumber;
    private Long zoneId;
    private String zoneName;
    private Long spotId;
    private String spotNumber;
    private LocalDateTime reservedAt;
    private LocalDateTime entryAt;
    private LocalDateTime exitAt;
    private ParkingReservationStatus reservationStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ParkingReservationResponse from(ParkingReservation r) {
        return ParkingReservationResponse.builder()
                .reservationId(r.getReservationId())
                .vehicleId(r.getVehicle().getVehicleId())
                .vehiclePlateNumber(r.getVehicle().getPlateNumber())
                .zoneId(r.getZone().getZoneId())
                .zoneName(r.getZone().getZoneName())
                .spotId(r.getSpot() != null ? r.getSpot().getSpotId() : null)
                .spotNumber(r.getSpot() != null ? r.getSpot().getSpotNumber() : null)
                .reservedAt(r.getReservedAt())
                .entryAt(r.getEntryAt())
                .exitAt(r.getExitAt())
                .reservationStatus(r.getReservationStatus())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
