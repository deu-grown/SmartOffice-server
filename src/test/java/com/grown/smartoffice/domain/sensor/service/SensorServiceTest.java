package com.grown.smartoffice.domain.sensor.service;

import com.grown.smartoffice.domain.device.entity.Device;
import com.grown.smartoffice.domain.device.repository.DeviceRepository;
import com.grown.smartoffice.domain.sensor.dto.*;
import com.grown.smartoffice.domain.sensor.entity.SensorLog;
import com.grown.smartoffice.domain.sensor.repository.SensorLogRepository;
import com.grown.smartoffice.domain.zone.entity.Zone;
import com.grown.smartoffice.domain.zone.repository.ZoneRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SensorServiceTest {

    @Mock SensorLogRepository sensorLogRepository;
    @Mock ZoneRepository zoneRepository;
    @Mock DeviceRepository deviceRepository;
    @InjectMocks SensorService sensorService;

    @Test
    @DisplayName("센서 로그 기록 성공")
    void recordLog_success() {
        // given
        SensorLogRequest req = SensorLogRequest.builder()
                .zoneId(1L)
                .deviceId(10L)
                .sensorType("TEMPERATURE")
                .value(new BigDecimal("24.5"))
                .unit("°C")
                .timestamp(LocalDateTime.now())
                .build();

        Zone zone = Zone.builder().zoneName("1층").build();
        ReflectionTestUtils.setField(zone, "zoneId", 1L);
        
        Device device = Device.builder().deviceName("센서1").deviceType("TEMPERATURE").zone(zone).build();
        ReflectionTestUtils.setField(device, "devicesId", 10L);
        
        given(zoneRepository.findById(1L)).willReturn(Optional.of(zone));
        given(deviceRepository.findById(10L)).willReturn(Optional.of(device));
        
        SensorLog savedLog = SensorLog.builder().build();
        ReflectionTestUtils.setField(savedLog, "id", 100L);
        given(sensorLogRepository.save(any(SensorLog.class))).willReturn(savedLog);

        // when
        SensorLogResponse res = sensorService.recordLog(req);

        // then
        assertThat(res.getLogId()).isEqualTo(100L);
        verify(sensorLogRepository).save(any(SensorLog.class));
    }

    @Test
    @DisplayName("구역별 최신 데이터 조회 성공")
    void getLatestData_success() {
        // given
        given(zoneRepository.existsById(1L)).willReturn(true);
        SensorLog log1 = SensorLog.builder().sensorType("TEMPERATURE").value(new BigDecimal("24.5")).unit("°C").loggedAt(LocalDateTime.now()).build();
        SensorLog log2 = SensorLog.builder().sensorType("HUMIDITY").value(new BigDecimal("45")).unit("%").loggedAt(LocalDateTime.now()).build();
        given(sensorLogRepository.findLatestByZoneId(1L)).willReturn(List.of(log1, log2));

        // when
        SensorLatestResponse res = sensorService.getLatestData(1L);

        // then
        assertThat(res.getTotalCount()).isEqualTo(2);
        assertThat(res.getSensorDataList().get(0).getSensorType()).isEqualTo("TEMPERATURE");
        assertThat(res.getSensorDataList().get(1).getSensorType()).isEqualTo("HUMIDITY");
    }

    @Test
    @DisplayName("구역별 이력 조회 성공")
    void getHistory_success() {
        // given
        given(zoneRepository.existsById(1L)).willReturn(true);
        LocalDate start = LocalDate.of(2026, 4, 1);
        LocalDate end = LocalDate.of(2026, 4, 2);
        
        SensorLog log1 = SensorLog.builder().sensorType("TEMPERATURE").value(new BigDecimal("24.5")).loggedAt(LocalDateTime.now()).build();
        given(sensorLogRepository.findByZone_ZoneIdAndLoggedAtBetweenOrderByLoggedAtDesc(eq(1L), any(), any()))
                .willReturn(List.of(log1));

        // when
        SensorHistoryResponse res = sensorService.getHistory(1L, start, end);

        // then
        assertThat(res.getTotalCount()).isEqualTo(1);
        verify(sensorLogRepository).findByZone_ZoneIdAndLoggedAtBetweenOrderByLoggedAtDesc(eq(1L), any(), any());
    }
}
