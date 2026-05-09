package com.grown.smartoffice.domain.sensor.repository;

import com.grown.smartoffice.domain.device.entity.Device;
import com.grown.smartoffice.domain.sensor.entity.SensorLog;
import com.grown.smartoffice.domain.zone.entity.Zone;
import com.grown.smartoffice.domain.zone.entity.ZoneType;
import com.grown.smartoffice.support.RepositoryTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SensorLogRepositoryTest extends RepositoryTestSupport {

    @Autowired SensorLogRepository sensorLogRepository;
    @Autowired TestEntityManager em;

    private Zone zone1;
    private Device device1;

    @BeforeEach
    void setUp() {
        zone1 = Zone.builder()
                .zoneName("1층 대회의실")
                .zoneType(ZoneType.ROOM)
                .build();
        em.persist(zone1);

        device1 = Device.builder()
                .deviceName("센서 1")
                .deviceType("TEMPERATURE")
                .zone(zone1)
                .build();
        em.persist(device1);
        em.flush();
    }

    @Test
    @DisplayName("findByZone_ZoneIdAndLoggedAtBetweenOrderByLoggedAtDesc: 이력 조회")
    void findHistory() {
        LocalDateTime now = LocalDateTime.now();
        SensorLog log1 = SensorLog.builder()
                .zone(zone1)
                .device(device1)
                .sensorType("TEMPERATURE")
                .sensorValue(new BigDecimal("25.0"))
                .sensorUnit("°C")
                .loggedAt(now.minusHours(1))
                .build();
        SensorLog log2 = SensorLog.builder()
                .zone(zone1)
                .device(device1)
                .sensorType("TEMPERATURE")
                .sensorValue(new BigDecimal("26.0"))
                .sensorUnit("°C")
                .loggedAt(now)
                .build();
        em.persist(log1);
        em.persist(log2);
        em.flush();

        List<SensorLog> logs = sensorLogRepository.findByZone_ZoneIdAndLoggedAtBetweenOrderByLoggedAtDesc(
                zone1.getZoneId(), now.minusHours(2), now.plusHours(1));
        
        assertThat(logs).hasSize(2);
        assertThat(logs.get(0).getSensorValue()).isEqualByComparingTo(new BigDecimal("26.0")); // Descending
    }

    @Test
    @DisplayName("findLatestByZoneId: 구역별 최신 데이터 조회 (Native Query)")
    void findLatest() {
        LocalDateTime now = LocalDateTime.now();
        
        // Temperature logs
        em.persist(SensorLog.builder()
                .zone(zone1).device(device1).sensorType("TEMPERATURE")
                .sensorValue(new BigDecimal("25.0")).sensorUnit("°C").loggedAt(now.minusHours(1)).build());
        em.persist(SensorLog.builder()
                .zone(zone1).device(device1).sensorType("TEMPERATURE")
                .sensorValue(new BigDecimal("26.0")).sensorUnit("°C").loggedAt(now).build()); // latest temp

        // Humidity logs
        em.persist(SensorLog.builder()
                .zone(zone1).device(device1).sensorType("HUMIDITY")
                .sensorValue(new BigDecimal("40")).sensorUnit("%").loggedAt(now.minusHours(1)).build());
        em.persist(SensorLog.builder()
                .zone(zone1).device(device1).sensorType("HUMIDITY")
                .sensorValue(new BigDecimal("45")).sensorUnit("%").loggedAt(now).build()); // latest humi
        
        em.flush();

        List<SensorLog> latestLogs = sensorLogRepository.findLatestByZoneId(zone1.getZoneId());
        
        assertThat(latestLogs).hasSize(2);
        assertThat(latestLogs).anyMatch(l -> l.getSensorType().equals("TEMPERATURE") && l.getSensorValue().compareTo(new BigDecimal("26.0")) == 0);
        assertThat(latestLogs).anyMatch(l -> l.getSensorType().equals("HUMIDITY") && l.getSensorValue().compareTo(new BigDecimal("45")) == 0);
    }
}
