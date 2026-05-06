package com.grown.smartoffice.domain.device.dto;

import com.grown.smartoffice.domain.device.entity.Device;
import com.grown.smartoffice.domain.device.entity.DeviceStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@lombok.Builder
@lombok.AllArgsConstructor
public class DeviceListItemResponse {

    private final Long id;
    private final String name;
    private final String deviceType;
    private final String serialNumber;
    private final String mqttTopic;
    private final Long zoneId;
    private final String zoneName;
    private final DeviceStatus status;
    private final LocalDateTime createdAt;

    public DeviceListItemResponse(Device device) {
        this.id = device.getDevicesId();
        this.name = device.getDeviceName();
        this.deviceType = device.getDeviceType();
        this.serialNumber = device.getSerialNumber();
        this.mqttTopic = device.getMqttTopic();
        this.zoneId = device.getZone().getZoneId();
        this.zoneName = device.getZone().getZoneName();
        this.status = device.getDeviceStatus();
        this.createdAt = device.getCreatedAt();
    }
}
