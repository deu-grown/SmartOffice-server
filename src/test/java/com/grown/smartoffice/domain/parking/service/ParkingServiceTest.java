package com.grown.smartoffice.domain.parking.service;

import com.grown.smartoffice.domain.device.entity.Device;
import com.grown.smartoffice.domain.device.entity.DeviceStatus;
import com.grown.smartoffice.domain.device.repository.DeviceRepository;
import com.grown.smartoffice.domain.parking.dto.ParkingSpotCreateRequest;
import com.grown.smartoffice.domain.parking.dto.ParkingSpotResponse;
import com.grown.smartoffice.domain.parking.dto.ParkingStatusUpdateRequest;
import com.grown.smartoffice.domain.parking.dto.ParkingStatusUpdateResponse;
import com.grown.smartoffice.domain.parking.entity.ParkingSpot;
import com.grown.smartoffice.domain.parking.entity.SpotStatus;
import com.grown.smartoffice.domain.parking.entity.SpotType;
import com.grown.smartoffice.domain.parking.repository.ParkingSpotRepository;
import com.grown.smartoffice.domain.zone.entity.Zone;
import com.grown.smartoffice.domain.zone.entity.ZoneType;
import com.grown.smartoffice.domain.zone.repository.ZoneRepository;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

    @Mock ParkingSpotRepository parkingSpotRepository;
    @Mock ZoneRepository zoneRepository;
    @Mock DeviceRepository deviceRepository;
    @InjectMocks ParkingServiceImpl parkingService;

    private Zone zone;
    private Device device;

    @BeforeEach
    void setUp() {
        zone = Zone.builder().zoneName("지하1층").zoneType(ZoneType.FLOOR).build();
        ReflectionTestUtils.setField(zone, "zoneId", 8L);

        device = Device.builder()
                .zone(zone).deviceName("초음파-001").deviceType("ULTRASONIC")
                .deviceStatus(DeviceStatus.ACTIVE).build();
        ReflectionTestUtils.setField(device, "devicesId", 11L);
    }

    @Test
    @DisplayName("주차면 등록 성공 — device 미지정")
    void createSpot_success_withoutDevice() {
        ParkingSpotCreateRequest req = new ParkingSpotCreateRequest();
        ReflectionTestUtils.setField(req, "zoneId", 8L);
        ReflectionTestUtils.setField(req, "spotNumber", "B1-001");
        ReflectionTestUtils.setField(req, "spotType", "REGULAR");

        given(zoneRepository.findById(8L)).willReturn(Optional.of(zone));
        given(parkingSpotRepository.existsByZone_ZoneIdAndSpotNumber(8L, "B1-001")).willReturn(false);
        given(parkingSpotRepository.save(any(ParkingSpot.class))).willAnswer(inv -> {
            ParkingSpot saved = inv.getArgument(0);
            ReflectionTestUtils.setField(saved, "spotId", 100L);
            return saved;
        });

        ParkingSpotResponse res = parkingService.createSpot(req);

        assertThat(res.getSpotId()).isEqualTo(100L);
        assertThat(res.getSpotType()).isEqualTo(SpotType.REGULAR);
        assertThat(res.getSpotStatus()).isEqualTo(SpotStatus.ACTIVE);
        assertThat(res.isOccupied()).isFalse();
    }

    @Test
    @DisplayName("주차면 등록 — 동일 zone 내 spot_number 중복 시 DUPLICATE_SPOT_NUMBER")
    void createSpot_duplicateNumber() {
        ParkingSpotCreateRequest req = new ParkingSpotCreateRequest();
        ReflectionTestUtils.setField(req, "zoneId", 8L);
        ReflectionTestUtils.setField(req, "spotNumber", "B1-001");
        ReflectionTestUtils.setField(req, "spotType", "REGULAR");

        given(zoneRepository.findById(8L)).willReturn(Optional.of(zone));
        given(parkingSpotRepository.existsByZone_ZoneIdAndSpotNumber(8L, "B1-001")).willReturn(true);

        assertThatThrownBy(() -> parkingService.createSpot(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_SPOT_NUMBER);
    }

    @Test
    @DisplayName("주차면 등록 — device 중복 매핑 시 DEVICE_ALREADY_MAPPED")
    void createSpot_duplicateDevice() {
        ParkingSpotCreateRequest req = new ParkingSpotCreateRequest();
        ReflectionTestUtils.setField(req, "zoneId", 8L);
        ReflectionTestUtils.setField(req, "spotNumber", "B1-002");
        ReflectionTestUtils.setField(req, "spotType", "REGULAR");
        ReflectionTestUtils.setField(req, "deviceId", 11L);

        given(zoneRepository.findById(8L)).willReturn(Optional.of(zone));
        given(parkingSpotRepository.existsByZone_ZoneIdAndSpotNumber(8L, "B1-002")).willReturn(false);
        given(deviceRepository.findById(11L)).willReturn(Optional.of(device));
        given(parkingSpotRepository.existsByDevice_DevicesId(11L)).willReturn(true);

        assertThatThrownBy(() -> parkingService.createSpot(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.DEVICE_ALREADY_MAPPED);
    }

    @Test
    @DisplayName("주차면 등록 — 존재하지 않는 zone → ZONE_NOT_FOUND")
    void createSpot_zoneNotFound() {
        ParkingSpotCreateRequest req = new ParkingSpotCreateRequest();
        ReflectionTestUtils.setField(req, "zoneId", 999L);
        ReflectionTestUtils.setField(req, "spotNumber", "X-1");
        ReflectionTestUtils.setField(req, "spotType", "REGULAR");

        given(zoneRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> parkingService.createSpot(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.ZONE_NOT_FOUND);
    }

    @Test
    @DisplayName("IoT 점유 업데이트 — deviceId 불일치 시 DEVICE_SPOT_MISMATCH")
    void updateOccupancy_deviceMismatch() {
        ParkingSpot spot = ParkingSpot.builder()
                .zone(zone).spotNumber("B1-001").spotType(SpotType.REGULAR)
                .device(device).occupied(false).spotStatus(SpotStatus.ACTIVE).build();
        ReflectionTestUtils.setField(spot, "spotId", 50L);

        ParkingStatusUpdateRequest req = new ParkingStatusUpdateRequest();
        ReflectionTestUtils.setField(req, "deviceId", 99L); // 다른 device id
        ReflectionTestUtils.setField(req, "occupied", true);

        given(parkingSpotRepository.findByIdWithRelations(50L)).willReturn(Optional.of(spot));

        assertThatThrownBy(() -> parkingService.updateOccupancyFromIot(50L, req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.DEVICE_SPOT_MISMATCH);
    }

    @Test
    @DisplayName("IoT 점유 업데이트 — device 없는 spot은 DEVICE_SPOT_MISMATCH")
    void updateOccupancy_noDeviceMapped() {
        ParkingSpot spot = ParkingSpot.builder()
                .zone(zone).spotNumber("B1-003").spotType(SpotType.REGULAR)
                .device(null).occupied(false).spotStatus(SpotStatus.ACTIVE).build();
        ReflectionTestUtils.setField(spot, "spotId", 51L);

        ParkingStatusUpdateRequest req = new ParkingStatusUpdateRequest();
        ReflectionTestUtils.setField(req, "deviceId", 11L);
        ReflectionTestUtils.setField(req, "occupied", true);

        given(parkingSpotRepository.findByIdWithRelations(51L)).willReturn(Optional.of(spot));

        assertThatThrownBy(() -> parkingService.updateOccupancyFromIot(51L, req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.DEVICE_SPOT_MISMATCH);
    }

    @Test
    @DisplayName("IoT 점유 업데이트 성공")
    void updateOccupancy_success() {
        ParkingSpot spot = ParkingSpot.builder()
                .zone(zone).spotNumber("B1-001").spotType(SpotType.REGULAR)
                .device(device).occupied(false).spotStatus(SpotStatus.ACTIVE).build();
        ReflectionTestUtils.setField(spot, "spotId", 50L);

        ParkingStatusUpdateRequest req = new ParkingStatusUpdateRequest();
        ReflectionTestUtils.setField(req, "deviceId", 11L);
        ReflectionTestUtils.setField(req, "occupied", true);

        given(parkingSpotRepository.findByIdWithRelations(50L)).willReturn(Optional.of(spot));

        ParkingStatusUpdateResponse res = parkingService.updateOccupancyFromIot(50L, req);

        assertThat(res.isOccupied()).isTrue();
        assertThat(res.getSpotId()).isEqualTo(50L);
        assertThat(spot.isOccupied()).isTrue();
    }

    @Test
    @DisplayName("주차면 삭제 — 존재하지 않으면 PARKING_SPOT_NOT_FOUND")
    void deleteSpot_notFound() {
        given(parkingSpotRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> parkingService.deleteSpot(999L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.PARKING_SPOT_NOT_FOUND);
    }
}
