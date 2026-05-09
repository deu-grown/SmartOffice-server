package com.grown.smartoffice.domain.device.dto;

import com.grown.smartoffice.domain.device.entity.Device;
import com.grown.smartoffice.domain.device.entity.DeviceStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@lombok.Builder
@lombok.AllArgsConstructor
public class DeviceUpdateResponse {

    private final Long id;
    private final String name;
    private final String deviceType;
    private final String serialNumber;
    private final DeviceStatus status;
    private final Long zoneId;
    private final String zoneName;
    private final LocalDateTime updatedAt;

    public DeviceUpdateResponse(Device device) {
        this.id = device.getDevicesId();
        this.name = device.getDeviceName();
        this.deviceType = device.getDeviceType();
        this.serialNumber = device.getSerialNumber();
        this.status = device.getDeviceStatus();
        this.zoneId = device.getZone().getZoneId();
        this.zoneName = device.getZone().getZoneName();
        this.updatedAt = device.getUpdatedAt();
    }
}
