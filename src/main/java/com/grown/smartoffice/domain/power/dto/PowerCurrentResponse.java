package com.grown.smartoffice.domain.power.dto;

import com.grown.smartoffice.domain.sensor.entity.SensorLog;
import com.grown.smartoffice.domain.zone.entity.Zone;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PowerCurrentResponse {

    private Long zoneId;
    private String zoneName;
    private List<DevicePower> devices;

    @Getter
    @Builder
    public static class DevicePower {
        private Long deviceId;
        private String deviceName;
        private Double avgWatt;
        private LocalDateTime measuredAt;
    }

    public static PowerCurrentResponse from(Zone zone, List<SensorLog> logs) {
        List<DevicePower> devices = logs.stream()
                .map(log -> DevicePower.builder()
                        .deviceId(log.getDevice().getDevicesId())
                        .deviceName(log.getDevice().getDeviceName())
                        .avgWatt(log.getSensorValue().doubleValue())
                        .measuredAt(log.getLoggedAt())
                        .build())
                .toList();
        return PowerCurrentResponse.builder()
                .zoneId(zone.getZoneId())
                .zoneName(zone.getZoneName())
                .devices(devices)
                .build();
    }
}
