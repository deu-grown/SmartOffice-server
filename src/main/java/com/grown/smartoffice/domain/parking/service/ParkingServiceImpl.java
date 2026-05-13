package com.grown.smartoffice.domain.parking.service;

import com.grown.smartoffice.domain.device.entity.Device;
import com.grown.smartoffice.domain.device.repository.DeviceRepository;
import com.grown.smartoffice.domain.parking.dto.*;
import com.grown.smartoffice.domain.parking.entity.ParkingSpot;
import com.grown.smartoffice.domain.parking.entity.SpotStatus;
import com.grown.smartoffice.domain.parking.entity.SpotType;
import com.grown.smartoffice.domain.parking.repository.ParkingSpotRepository;
import com.grown.smartoffice.domain.zone.entity.Zone;
import com.grown.smartoffice.domain.zone.repository.ZoneRepository;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingServiceImpl implements ParkingService {

    private final ParkingSpotRepository parkingSpotRepository;
    private final ZoneRepository zoneRepository;
    private final DeviceRepository deviceRepository;

    @Override
    @Transactional
    public ParkingSpotResponse createSpot(ParkingSpotCreateRequest request) {
        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new CustomException(ErrorCode.ZONE_NOT_FOUND));

        if (parkingSpotRepository.existsByZone_ZoneIdAndSpotNumber(zone.getZoneId(), request.getSpotNumber())) {
            throw new CustomException(ErrorCode.DUPLICATE_SPOT_NUMBER);
        }

        Device device = resolveDevice(request.getDeviceId(), null);

        ParkingSpot spot = ParkingSpot.builder()
                .zone(zone)
                .spotNumber(request.getSpotNumber())
                .spotType(parseSpotType(request.getSpotType()))
                .device(device)
                .positionX(request.getPositionX())
                .positionY(request.getPositionY())
                .occupied(false)
                .spotStatus(parseSpotStatus(request.getSpotStatus()))
                .build();
        return ParkingSpotResponse.from(parkingSpotRepository.save(spot));
    }

    @Override
    @Transactional
    public ParkingSpotResponse updateSpot(Long spotId, ParkingSpotUpdateRequest request) {
        ParkingSpot spot = parkingSpotRepository.findByIdWithRelations(spotId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARKING_SPOT_NOT_FOUND));

        if (request.getSpotNumber() != null
                && parkingSpotRepository.existsByZone_ZoneIdAndSpotNumberAndSpotIdNot(
                        spot.getZone().getZoneId(), request.getSpotNumber(), spotId)) {
            throw new CustomException(ErrorCode.DUPLICATE_SPOT_NUMBER);
        }

        Device device = resolveDevice(request.getDeviceId(), spotId);

        spot.update(
                request.getSpotNumber(),
                parseSpotType(request.getSpotType()),
                device,
                request.getPositionX(),
                request.getPositionY(),
                parseSpotStatus(request.getSpotStatus())
        );
        return ParkingSpotResponse.from(spot);
    }

    @Override
    @Transactional
    public void deleteSpot(Long spotId) {
        ParkingSpot spot = parkingSpotRepository.findById(spotId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARKING_SPOT_NOT_FOUND));
        parkingSpotRepository.delete(spot);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParkingSpotResponse> getSpots(Long zoneId, String spotType, String status) {
        SpotType type = (spotType != null) ? parseSpotType(spotType) : null;
        SpotStatus st  = (status   != null) ? parseSpotStatus(status) : null;
        return parkingSpotRepository.findAllWithFilters(zoneId, type, st).stream()
                .map(ParkingSpotResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ParkingZoneSummaryResponse getZoneSummary(Long zoneId) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new CustomException(ErrorCode.ZONE_NOT_FOUND));

        List<ParkingSpot> spots = parkingSpotRepository.findByZoneWithDevice(zoneId);
        long total = spots.size();
        long occupied = spots.stream().filter(ParkingSpot::isOccupied).count();
        return ParkingZoneSummaryResponse.builder()
                .zoneId(zone.getZoneId())
                .zoneName(zone.getZoneName())
                .totalSpots(total)
                .occupiedSpots(occupied)
                .availableSpots(total - occupied)
                .spots(spots.stream().map(ParkingSpotResponse::from).toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ParkingZoneMapResponse getZoneMap(Long zoneId) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new CustomException(ErrorCode.ZONE_NOT_FOUND));

        List<ParkingSpot> spots = parkingSpotRepository.findByZoneWithDevice(zoneId);
        return ParkingZoneMapResponse.builder()
                .zoneId(zone.getZoneId())
                .zoneName(zone.getZoneName())
                .spots(spots.stream().map(ParkingSpotMapResponse::from).toList())
                .build();
    }

    @Override
    @Transactional
    public ParkingStatusUpdateResponse updateOccupancyFromIot(Long spotId, ParkingStatusUpdateRequest request) {
        ParkingSpot spot = parkingSpotRepository.findByIdWithRelations(spotId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARKING_SPOT_NOT_FOUND));

        if (spot.getDevice() == null
                || !spot.getDevice().getDevicesId().equals(request.getDeviceId())) {
            throw new CustomException(ErrorCode.DEVICE_SPOT_MISMATCH);
        }

        spot.updateOccupancy(Boolean.TRUE.equals(request.getOccupied()));
        return ParkingStatusUpdateResponse.builder()
                .spotId(spot.getSpotId())
                .occupied(spot.isOccupied())
                .updatedAt(spot.getUpdatedAt())
                .build();
    }

    private Device resolveDevice(Long deviceId, Long excludeSpotId) {
        if (deviceId == null) return null;
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new CustomException(ErrorCode.DEVICE_NOT_FOUND));

        boolean conflict = (excludeSpotId == null)
                ? parkingSpotRepository.existsByDevice_DevicesId(deviceId)
                : parkingSpotRepository.existsByDevice_DevicesIdAndSpotIdNot(deviceId, excludeSpotId);
        if (conflict) {
            throw new CustomException(ErrorCode.DEVICE_ALREADY_MAPPED);
        }
        return device;
    }

    private SpotType parseSpotType(String value) {
        if (value == null) return null;
        try {
            return SpotType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
    }

    private SpotStatus parseSpotStatus(String value) {
        if (value == null) return null;
        try {
            return SpotStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
    }
}
