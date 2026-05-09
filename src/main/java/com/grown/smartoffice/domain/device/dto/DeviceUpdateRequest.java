package com.grown.smartoffice.domain.device.dto;

import com.grown.smartoffice.domain.device.entity.DeviceStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceUpdateRequest {

    private String name;
    private String deviceType;
    private String serialNumber;
    private DeviceStatus status;
    private Long zoneId;

    @lombok.Builder
    public DeviceUpdateRequest(String name, String deviceType, String serialNumber, DeviceStatus status, Long zoneId) {
        this.name = name;
        this.deviceType = deviceType;
        this.serialNumber = serialNumber;
        this.status = status;
        this.zoneId = zoneId;
    }
}
