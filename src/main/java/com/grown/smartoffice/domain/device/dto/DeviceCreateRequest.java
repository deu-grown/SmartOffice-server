package com.grown.smartoffice.domain.device.dto;

import com.grown.smartoffice.domain.device.entity.DeviceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceCreateRequest {

    @NotNull(message = "설치 구역 ID는 필수입니다.")
    private Long zoneId;

    @NotBlank(message = "장치명은 필수입니다.")
    private String name;

    @NotBlank(message = "장치 유형은 필수입니다.")
    private String deviceType;

    private String serialNumber;

    private DeviceStatus status;

    @lombok.Builder
    public DeviceCreateRequest(Long zoneId, String name, String deviceType, String serialNumber, DeviceStatus status) {
        this.zoneId = zoneId;
        this.name = name;
        this.deviceType = deviceType;
        this.serialNumber = serialNumber;
        this.status = status;
    }
}
