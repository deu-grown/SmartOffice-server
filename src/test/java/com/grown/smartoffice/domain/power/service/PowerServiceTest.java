package com.grown.smartoffice.domain.power.service;

import com.grown.smartoffice.domain.power.dto.*;
import com.grown.smartoffice.domain.power.entity.PowerBilling;
import com.grown.smartoffice.domain.power.repository.PowerBillingRepository;
import com.grown.smartoffice.domain.sensor.repository.SensorLogRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PowerServiceTest {

    @Mock PowerBillingRepository powerBillingRepository;
    @Mock SensorLogRepository sensorLogRepository;
    @Mock ZoneRepository zoneRepository;
    @InjectMocks PowerService powerService;

    @Test
    @DisplayName("실시간 전력 조회 — 구역 없으면 ZONE_NOT_FOUND")
    void getCurrentPower_zoneNotFound() {
        given(zoneRepository.findById(99L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> powerService.getCurrentPower(99L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ZONE_NOT_FOUND));
    }

    @Test
    @DisplayName("실시간 전력 조회 성공")
    void getCurrentPower_success() {
        Zone zone = Zone.builder().zoneName("1층").zoneType(ZoneType.FLOOR).build();
        ReflectionTestUtils.setField(zone, "zoneId", 1L);

        given(zoneRepository.findById(1L)).willReturn(Optional.of(zone));
        given(sensorLogRepository.findLatestPowerByZoneId(1L)).willReturn(List.of());

        PowerCurrentResponse res = powerService.getCurrentPower(1L);

        assertThat(res.getZoneId()).isEqualTo(1L);
        assertThat(res.getZoneName()).isEqualTo("1층");
        assertThat(res.getDevices()).isEmpty();
    }

    @Test
    @DisplayName("전체 구역 월 요금 현황 조회 성공")
    void getAllZonesBilling_success() {
        Zone zone = Zone.builder().zoneName("1층").zoneType(ZoneType.FLOOR).build();
        ReflectionTestUtils.setField(zone, "zoneId", 1L);

        PowerBilling billing = PowerBilling.builder()
                .zone(zone)
                .billingYear(2026)
                .billingMonth(4)
                .totalKwh(new BigDecimal("183.24"))
                .unitPrice(120)
                .baseFee(6160)
                .powerFee(21988)
                .totalFee(28148)
                .calculatedAt(LocalDateTime.now())
                .build();

        given(powerBillingRepository.findAllByYearAndMonthWithZone(2026, 4))
                .willReturn(List.of(billing));

        PowerBillingAllResponse res = powerService.getAllZonesBilling(2026, 4);

        assertThat(res.getYear()).isEqualTo(2026);
        assertThat(res.getMonth()).isEqualTo(4);
        assertThat(res.getZones()).hasSize(1);
        assertThat(res.getTotalFee()).isEqualTo(28148);
    }

    @Test
    @DisplayName("요금 산출 — 데이터 없으면 POWER_DATA_NOT_FOUND")
    void calculateBilling_noData() {
        PowerBillingCalculateRequest req = new PowerBillingCalculateRequest();
        ReflectionTestUtils.setField(req, "year", 2026);
        ReflectionTestUtils.setField(req, "month", 4);
        ReflectionTestUtils.setField(req, "unitPrice", 120);
        ReflectionTestUtils.setField(req, "baseFee", 6160);
        ReflectionTestUtils.setField(req, "zoneIds", null);

        given(sensorLogRepository.findMonthlyKwhAllZones(2026, 4)).willReturn(List.of());

        assertThatThrownBy(() -> powerService.calculateBilling(req))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.POWER_DATA_NOT_FOUND));
    }

    @Test
    @DisplayName("요금 산출 성공")
    void calculateBilling_success() {
        Zone zone = Zone.builder().zoneName("1층").zoneType(ZoneType.FLOOR).build();
        ReflectionTestUtils.setField(zone, "zoneId", 1L);

        MonthlyPowerProjection proj = new MonthlyPowerProjection() {
            public Long getZoneId() { return 1L; }
            public BigDecimal getTotalKwh() { return new BigDecimal("183.24"); }
        };

        PowerBillingCalculateRequest req = new PowerBillingCalculateRequest();
        ReflectionTestUtils.setField(req, "year", 2026);
        ReflectionTestUtils.setField(req, "month", 4);
        ReflectionTestUtils.setField(req, "unitPrice", 120);
        ReflectionTestUtils.setField(req, "baseFee", 6160);
        ReflectionTestUtils.setField(req, "zoneIds", null);

        given(sensorLogRepository.findMonthlyKwhAllZones(2026, 4)).willReturn(List.of(proj));
        given(zoneRepository.findAll()).willReturn(List.of(zone));
        given(powerBillingRepository.findByZone_ZoneIdAndBillingYearAndBillingMonth(1L, 2026, 4))
                .willReturn(Optional.empty());

        PowerBillingCalculateResponse res = powerService.calculateBilling(req);

        assertThat(res.getYear()).isEqualTo(2026);
        assertThat(res.getSuccessCount()).isEqualTo(1);
        assertThat(res.getSkipCount()).isEqualTo(0);
        verify(powerBillingRepository).saveAll(any());
    }
}
