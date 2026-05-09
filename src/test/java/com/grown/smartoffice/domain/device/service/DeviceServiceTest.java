package com.grown.smartoffice.domain.device.service;

import com.grown.smartoffice.domain.device.dto.*;
import com.grown.smartoffice.domain.device.entity.Device;
import com.grown.smartoffice.domain.device.entity.DeviceStatus;
import com.grown.smartoffice.domain.device.repository.DeviceRepository;
import com.grown.smartoffice.domain.zone.entity.Zone;
import com.grown.smartoffice.domain.zone.entity.ZoneType;
import com.grown.smartoffice.domain.zone.repository.ZoneRepository;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock DeviceRepository deviceRepository;
    @Mock ZoneRepository zoneRepository;
    @InjectMocks DeviceService deviceService;

    private Zone buildZone(Long id, String name) {
        Zone zone = Zone.builder().zoneName(name).zoneType(ZoneType.AREA).build();
        ReflectionTestUtils.setField(zone, "zoneId", id);
        return zone;
    }

    private Device buildDevice(Long id, String name, Zone zone) {
        Device device = Device.builder()
                .deviceName(name)
                .deviceType("NFC_READER")
                .zone(zone)
                .build();
        ReflectionTestUtils.setField(device, "devicesId", id);
        return device;
    }

    @Test
    @DisplayName("장치 등록 성공")
    void registerDevice_success() {
        // given
        DeviceCreateRequest req = DeviceCreateRequest.builder()
                .name("test-device")
                .deviceType("NFC_READER")
                .zoneId(1L)
                .build();
        Zone zone = buildZone(1L, "1층");
        given(zoneRepository.findById(1L)).willReturn(Optional.of(zone));
        given(deviceRepository.existsByDeviceName("test-device")).willReturn(false);
        
        Device savedDevice = buildDevice(10L, "test-device", zone);
        given(deviceRepository.save(any(Device.class))).willReturn(savedDevice);

        // when
        DeviceCreateResponse res = deviceService.registerDevice(req);

        // then
        assertThat(res.getName()).isEqualTo("test-device");
        verify(deviceRepository).save(any(Device.class));
    }

    @Test
    @DisplayName("장치 등록 실패 - 중복된 이름")
    void registerDevice_duplicateName() {
        // given
        DeviceCreateRequest req = DeviceCreateRequest.builder()
                .name("dup-device")
                .build();
        given(deviceRepository.existsByDeviceName("dup-device")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> deviceService.registerDevice(req))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_DEVICE_NAME);
    }

    @Test
    @DisplayName("장치 상세 조회 성공")
    void getDeviceDetail_success() {
        // given
        Zone zone = buildZone(1L, "1층");
        Device device = buildDevice(10L, "test-device", zone);
        given(deviceRepository.findByIdWithZone(10L)).willReturn(Optional.of(device));

        // when
        DeviceDetailResponse res = deviceService.getDeviceDetail(10L);

        // then
        assertThat(res.getId()).isEqualTo(10L);
        assertThat(res.getName()).isEqualTo("test-device");
    }

    @Test
    @DisplayName("장치 정보 수정 성공")
    void updateDevice_success() {
        // given
        Zone zone = buildZone(1L, "1층");
        Device device = buildDevice(10L, "old-name", zone);
        given(deviceRepository.findById(10L)).willReturn(Optional.of(device));
        
        DeviceUpdateRequest req = DeviceUpdateRequest.builder()
                .name("new-name")
                .status(DeviceStatus.INACTIVE)
                .build();
        given(deviceRepository.existsByDeviceName("new-name")).willReturn(false);

        // when
        DeviceUpdateResponse res = deviceService.updateDevice(10L, req);

        // then
        assertThat(device.getDeviceName()).isEqualTo("new-name");
        assertThat(device.getDeviceStatus()).isEqualTo(DeviceStatus.INACTIVE);
    }

    @Test
    @DisplayName("장치 삭제 성공")
    void deleteDevice_success() {
        // given
        Device device = buildDevice(10L, "del-device", null);
        given(deviceRepository.findById(10L)).willReturn(Optional.of(device));

        // when
        deviceService.deleteDevice(10L);

        // then
        verify(deviceRepository).delete(device);
    }
}
