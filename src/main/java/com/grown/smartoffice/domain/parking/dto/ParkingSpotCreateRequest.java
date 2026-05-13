package com.grown.smartoffice.domain.parking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ParkingSpotCreateRequest {

    @NotNull
    @Schema(description = "구역 ID", example = "8")
    private Long zoneId;

    @NotBlank
    @Size(max = 20)
    @Schema(description = "주차면 번호", example = "B1-001")
    private String spotNumber;

    @NotNull
    @Schema(description = "주차면 유형 (REGULAR | DISABLED | EV)", example = "REGULAR")
    private String spotType;

    @Schema(description = "초음파 센서 장치 ID (1:1 매핑, null 가능)", example = "11")
    private Long deviceId;

    @Schema(description = "지도 X 좌표", example = "100")
    private Integer positionX;

    @Schema(description = "지도 Y 좌표", example = "200")
    private Integer positionY;

    @Schema(description = "주차면 상태 (ACTIVE | INACTIVE)", example = "ACTIVE")
    private String spotStatus;
}
