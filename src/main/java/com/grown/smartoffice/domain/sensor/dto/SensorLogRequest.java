package com.grown.smartoffice.domain.sensor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SensorLogRequest {

    @NotNull(message = "구역 ID는 필수입니다.")
    private Long zoneId;

    @NotNull(message = "장치 ID는 필수입니다.")
    private Long deviceId;

    @NotBlank(message = "센서 타입은 필수입니다.")
    private String sensorType;

    @NotNull(message = "센서 값은 필수입니다.")
    private BigDecimal value;

    @NotBlank(message = "단위는 필수입니다.")
    private String unit;

    @NotNull(message = "측정 시간은 필수입니다.")
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @lombok.Builder
    public SensorLogRequest(Long zoneId, Long deviceId, String sensorType, BigDecimal value, String unit, LocalDateTime timestamp) {
        this.zoneId = zoneId;
        this.deviceId = deviceId;
        this.sensorType = sensorType;
        this.value = value;
        this.unit = unit;
        this.timestamp = timestamp;
    }
}
