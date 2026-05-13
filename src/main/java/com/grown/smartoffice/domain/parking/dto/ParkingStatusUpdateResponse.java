package com.grown.smartoffice.domain.parking.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ParkingStatusUpdateResponse {
    private Long spotId;
    private boolean occupied;
    private LocalDateTime updatedAt;
}
