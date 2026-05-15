package com.grown.smartoffice.domain.vehicle.dto;

import com.grown.smartoffice.domain.vehicle.entity.Vehicle;
import com.grown.smartoffice.domain.vehicle.entity.VehicleType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class VehicleResponse {

    private Long vehicleId;
    private String plateNumber;
    private String ownerName;
    private Long ownerUserId;
    private String ownerUserName;
    private VehicleType vehicleType;
    private String purpose;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VehicleResponse from(Vehicle v) {
        return VehicleResponse.builder()
                .vehicleId(v.getVehicleId())
                .plateNumber(v.getPlateNumber())
                .ownerName(v.getOwnerName())
                .ownerUserId(v.getOwnerUser() != null ? v.getOwnerUser().getUserId() : null)
                .ownerUserName(v.getOwnerUser() != null ? v.getOwnerUser().getEmployeeName() : null)
                .vehicleType(v.getVehicleType())
                .purpose(v.getPurpose())
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }
}
