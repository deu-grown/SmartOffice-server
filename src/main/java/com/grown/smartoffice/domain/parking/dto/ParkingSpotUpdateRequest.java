package com.grown.smartoffice.domain.parking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ParkingSpotUpdateRequest {

    @Size(max = 20)
    @Schema(description = "주차면 번호", example = "B1-002")
    private String spotNumber;

    @Schema(description = "주차면 유형 (REGULAR | DISABLED | EV)", example = "EV")
    private String spotType;

    @Schema(description = "초음파 센서 장치 ID (해제 시 null)", example = "12")
    private Long deviceId;

    @Schema(description = "지도 X 좌표", example = "150")
    private Integer positionX;

    @Schema(description = "지도 Y 좌표", example = "250")
    private Integer positionY;

    @Schema(description = "주차면 상태 (ACTIVE | INACTIVE)", example = "INACTIVE")
    private String spotStatus;
}
