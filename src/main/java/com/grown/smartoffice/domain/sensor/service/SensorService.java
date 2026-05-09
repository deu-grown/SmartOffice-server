package com.grown.smartoffice.domain.sensor.service;

import com.grown.smartoffice.domain.device.entity.Device;
import com.grown.smartoffice.domain.device.repository.DeviceRepository;
import com.grown.smartoffice.domain.sensor.dto.*;
import com.grown.smartoffice.domain.sensor.entity.SensorLog;
import com.grown.smartoffice.domain.sensor.repository.SensorLogRepository;
import com.grown.smartoffice.domain.zone.entity.Zone;
import com.grown.smartoffice.domain.zone.repository.ZoneRepository;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SensorService {

    private final SensorLogRepository sensorLogRepository;
    private final ZoneRepository zoneRepository;
    private final DeviceRepository deviceRepository;

    @Transactional
    public SensorLogResponse recordLog(SensorLogRequest request) {
        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new CustomException(ErrorCode.ZONE_NOT_FOUND));

        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new CustomException(ErrorCode.DEVICE_NOT_FOUND));
        
        // Optional: Verify if the device belongs to the zone
        if (!device.getZone().getZoneId().equals(request.getZoneId())) {
            log.warn("Device {} does not belong to zone {}", request.getDeviceId(), request.getZoneId());
            // Based on business logic, we might want to throw an error or just proceed.
            // For now, let's just use the zone from the device to be safe, or throw error.
            throw new CustomException(ErrorCode.INVALID_INPUT); // Or a more specific error
        }

        SensorLog sensorLog = SensorLog.builder()
                .zone(zone)
                .device(device)
                .sensorType(request.getSensorType())
                .value(request.getValue())
                .unit(request.getUnit())
                .loggedAt(request.getTimestamp())
                .build();

        SensorLog savedLog = sensorLogRepository.save(sensorLog);
        return new SensorLogResponse(savedLog.getId());
    }

    public SensorLatestResponse getLatestData(Long zoneId) {
        if (!zoneRepository.existsById(zoneId)) {
            throw new CustomException(ErrorCode.ZONE_NOT_FOUND);
        }

        List<SensorDataDto> latestData = sensorLogRepository.findLatestByZoneId(zoneId).stream()
                .map(SensorDataDto::new)
                .collect(Collectors.toList());

        return SensorLatestResponse.builder()
                .zoneId(zoneId)
                .sensorDataList(latestData)
                .build();
    }

    public SensorHistoryResponse getHistory(Long zoneId, LocalDate startDate, LocalDate endDate) {
        if (!zoneRepository.existsById(zoneId)) {
            throw new CustomException(ErrorCode.ZONE_NOT_FOUND);
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<SensorDataDto> history = sensorLogRepository.findByZone_ZoneIdAndLoggedAtBetweenOrderByLoggedAtDesc(zoneId, start, end).stream()
                .map(SensorDataDto::new)
                .collect(Collectors.toList());

        return SensorHistoryResponse.builder()
                .zoneId(zoneId)
                .startDate(startDate)
                .endDate(endDate)
                .sensorDataList(history)
                .build();
    }
}
