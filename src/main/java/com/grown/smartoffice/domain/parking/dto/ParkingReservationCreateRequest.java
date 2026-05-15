package com.grown.smartoffice.domain.parking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ParkingReservationCreateRequest {

    @NotNull(message = "차량 ID는 필수입니다.")
    @Schema(description = "차량 ID", example = "1")
    private Long vehicleId;

    @NotNull(message = "구역 ID는 필수입니다.")
    @Schema(description = "주차 구역 ID", example = "8")
    private Long zoneId;

    @Schema(description = "주차면 ID (예약 시점 미배정 가능, null)", example = "6")
    private Long spotId;

    @NotNull(message = "예약 일시는 필수입니다.")
    @Schema(description = "예약 일시", example = "2026-05-16T09:00:00")
    private LocalDateTime reservedAt;
}
