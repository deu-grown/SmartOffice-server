package com.grown.smartoffice.domain.control.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grown.smartoffice.domain.control.dto.ControlRequest;
import com.grown.smartoffice.domain.control.dto.ControlResponse;
import com.grown.smartoffice.domain.control.entity.ControlCommand;
import com.grown.smartoffice.domain.control.repository.ControlCommandRepository;
import com.grown.smartoffice.domain.device.entity.Device;
import com.grown.smartoffice.domain.device.repository.DeviceRepository;
import com.grown.smartoffice.domain.zone.entity.Zone;
import com.grown.smartoffice.domain.zone.repository.ZoneRepository;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ControlServiceTest {

    @Mock ControlCommandRepository controlCommandRepository;
    @Mock ZoneRepository zoneRepository;
    @Mock DeviceRepository deviceRepository;
    @Mock MqttClient mqttClient;
    @Spy ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks ControlService controlService;

    @Test
    @DisplayName("제어 명령 발송 성공")
    void sendCommand_success() throws Exception {
        // given
        ControlRequest req = ControlRequest.builder()
                .zoneId(1L)
                .deviceId(10L)
                .command("AC")
                .value("24")
                .build();

        Zone zone = Zone.builder().zoneName("1층").build();
        ReflectionTestUtils.setField(zone, "zoneId", 1L);
        Device device = Device.builder().deviceName("에어컨").build();
        ReflectionTestUtils.setField(device, "devicesId", 10L);

        given(zoneRepository.findById(1L)).willReturn(Optional.of(zone));
        given(deviceRepository.findById(10L)).willReturn(Optional.of(device));
        
        // Injecting MqttClient manually since InjectMocks might struggle with Optional<MqttClient>
        ReflectionTestUtils.setField(controlService, "mqttClient", Optional.of(mqttClient));

        ControlCommand saved = ControlCommand.builder().build();
        ReflectionTestUtils.setField(saved, "id", 1001L);
        given(controlCommandRepository.save(any(ControlCommand.class))).willReturn(saved);

        // when
        ControlResponse res = controlService.sendCommand(req);

        // then
        assertThat(res.getControlId()).isEqualTo(1001L);
        verify(controlCommandRepository).save(any(ControlCommand.class));
        verify(mqttClient).publish(any(), any());
    }

    @Test
    @DisplayName("제어 명령 발송 — enum 외 값 → INVALID_COMMAND_TYPE (#12 회귀 방지)")
    void sendCommand_invalidCommandType() {
        ControlRequest req = ControlRequest.builder()
                .zoneId(1L)
                .deviceId(10L)
                .command("POWER_ON")
                .value("24")
                .build();

        Zone zone = Zone.builder().zoneName("1층").build();
        ReflectionTestUtils.setField(zone, "zoneId", 1L);
        Device device = Device.builder().deviceName("에어컨").build();
        ReflectionTestUtils.setField(device, "devicesId", 10L);

        given(zoneRepository.findById(1L)).willReturn(Optional.of(zone));
        given(deviceRepository.findById(10L)).willReturn(Optional.of(device));

        assertThatThrownBy(() -> controlService.sendCommand(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_COMMAND_TYPE);
    }
}
