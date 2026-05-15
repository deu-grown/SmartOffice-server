package com.grown.smartoffice.domain.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "구역별 현재 환경 센서 값")
public class SensorCurrentResponse {

    @Schema(description = "구역 ID", example = "5")
    private Long zoneId;

    @Schema(description = "구역 이름", example = "개발팀 사무실")
    private String zoneName;

    @Schema(description = "온도 (°C)", example = "23.5")
    private BigDecimal temp;

    @Schema(description = "습도 (%)", example = "45.0")
    private BigDecimal humi;

    @Schema(description = "CO2 농도 (ppm)", example = "650")
    private BigDecimal co2;

    @Schema(description = "측정 시각", example = "2026-05-15T14:30:00")
    private LocalDateTime updatedAt;
}
