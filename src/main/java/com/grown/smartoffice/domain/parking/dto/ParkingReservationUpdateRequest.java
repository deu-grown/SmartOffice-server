package com.grown.smartoffice.domain.parking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ParkingReservationUpdateRequest {

    @Schema(description = "주차면 ID (입차 시 배정, 해제 시 null)", example = "6")
    private Long spotId;

    @Schema(description = "입차 일시", example = "2026-05-16T09:05:00")
    private LocalDateTime entryAt;

    @Schema(description = "출차 일시", example = "2026-05-16T18:00:00")
    private LocalDateTime exitAt;

    @Schema(description = "예약 상태 (RESERVED | PARKED | EXITED)", example = "PARKED")
    private String reservationStatus;
}
