package com.grown.smartoffice.domain.device.service;

import com.grown.smartoffice.domain.device.dto.*;
import com.grown.smartoffice.domain.device.entity.Device;
import com.grown.smartoffice.domain.device.repository.DeviceRepository;
import com.grown.smartoffice.domain.zone.entity.Zone;
import com.grown.smartoffice.domain.zone.repository.ZoneRepository;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final ZoneRepository zoneRepository;

    @Transactional
    public DeviceCreateResponse registerDevice(DeviceCreateRequest request) {
        if (deviceRepository.existsByDeviceName(request.getName())) {
            throw new CustomException(ErrorCode.DUPLICATE_DEVICE_NAME);
        }

        if (request.getSerialNumber() != null && !request.getSerialNumber().isBlank()) {
            if (deviceRepository.existsBySerialNumber(request.getSerialNumber())) {
                throw new CustomException(ErrorCode.DUPLICATE_SERIAL_NUMBER);
            }
        }

        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new CustomException(ErrorCode.ZONE_NOT_FOUND));

        Device device = Device.builder()
                .deviceName(request.getName())
                .deviceType(request.getDeviceType())
                .serialNumber(request.getSerialNumber())
                .deviceStatus(request.getStatus())
                .zone(zone)
                .build();

        Device savedDevice = deviceRepository.save(device);
        
        // MQTT topic generation
        String suffix = getTopicSuffix(savedDevice.getDeviceType());
        String mqttTopic = String.format("smartoffice/zones/%d/devices/%d/%s", 
                zone.getZoneId(), savedDevice.getDevicesId(), suffix);
        savedDevice.updateMqttTopic(mqttTopic);

        return new DeviceCreateResponse(savedDevice);
    }

    public List<DeviceListItemResponse> getAllDevices() {
        return deviceRepository.findAllWithZone().stream()
                .map(DeviceListItemResponse::new)
                .collect(Collectors.toList());
    }

    public DeviceDetailResponse getDeviceDetail(Long id) {
        Device device = deviceRepository.findByIdWithZone(id)
                .orElseThrow(() -> new CustomException(ErrorCode.DEVICE_NOT_FOUND));
        return new DeviceDetailResponse(device);
    }

    @Transactional
    public DeviceUpdateResponse updateDevice(Long id, DeviceUpdateRequest request) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.DEVICE_NOT_FOUND));

        if (request.getName() != null && !request.getName().equals(device.getDeviceName())) {
            if (deviceRepository.existsByDeviceName(request.getName())) {
                throw new CustomException(ErrorCode.DUPLICATE_DEVICE_NAME);
            }
        }

        if (request.getSerialNumber() != null && !request.getSerialNumber().equals(device.getSerialNumber())) {
            if (deviceRepository.existsBySerialNumber(request.getSerialNumber())) {
                throw new CustomException(ErrorCode.DUPLICATE_SERIAL_NUMBER);
            }
        }

        Zone zone = null;
        if (request.getZoneId() != null) {
            zone = zoneRepository.findById(request.getZoneId())
                    .orElseThrow(() -> new CustomException(ErrorCode.ZONE_NOT_FOUND));
        }

        device.updateInfo(
                request.getName(),
                request.getDeviceType(),
                request.getSerialNumber(),
                request.getStatus(),
                zone
        );

        return new DeviceUpdateResponse(device);
    }

    @Transactional
    public void deleteDevice(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.DEVICE_NOT_FOUND));
        deviceRepository.delete(device);
    }

    private String getTopicSuffix(String deviceType) {
        if (deviceType == null) return "common";
        String type = deviceType.toUpperCase();
        if (type.contains("NFC")) return "nfc";
        if (type.contains("SENSOR")) return "sensor";
        if (type.contains("LIGHT")) return "light";
        return type.toLowerCase();
    }
}
