package com.grown.smartoffice.domain.parking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ParkingStatusUpdateRequest {

    @NotNull
    @Schema(description = "초음파 센서 장치 ID (해당 주차면에 매핑된 장치 ID와 일치해야 함)", example = "11")
    private Long deviceId;

    @NotNull
    @Schema(description = "점유 여부 (true: 점유, false: 비점유)", example = "true")
    private Boolean occupied;

    @Schema(description = "측정 거리(cm). 선택 — sensor_logs 기록용", example = "25.4")
    private Double distanceCm;
}
