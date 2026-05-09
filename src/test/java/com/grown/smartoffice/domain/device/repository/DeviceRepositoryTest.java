package com.grown.smartoffice.domain.device.repository;

import com.grown.smartoffice.domain.device.entity.Device;
import com.grown.smartoffice.domain.device.entity.DeviceStatus;
import com.grown.smartoffice.domain.zone.entity.Zone;
import com.grown.smartoffice.domain.zone.entity.ZoneType;
import com.grown.smartoffice.support.RepositoryTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceRepositoryTest extends RepositoryTestSupport {

    @Autowired DeviceRepository deviceRepository;
    @Autowired TestEntityManager em;

    private Zone zone1;

    @BeforeEach
    void setUp() {
        zone1 = Zone.builder()
                .zoneName("1층 대회의실")
                .zoneType(ZoneType.ROOM)
                .build();
        em.persist(zone1);
    }

    @Test
    @DisplayName("장치 저장 및 조회")
    void saveAndFind() {
        Device device = Device.builder()
                .deviceName("NFC 리더기 1")
                .deviceType("NFC_READER")
                .zone(zone1)
                .deviceStatus(DeviceStatus.ACTIVE)
                .build();
        
        Device saved = deviceRepository.save(device);
        em.flush();
        em.clear();

        Optional<Device> found = deviceRepository.findById(saved.getDevicesId());
        assertThat(found).isPresent();
        assertThat(found.get().getDeviceName()).isEqualTo("NFC 리더기 1");
    }

    @Test
    @DisplayName("findAllWithZone: 페치 조인으로 구역 정보와 함께 조회")
    void findAllWithZone() {
        Device device = Device.builder()
                .deviceName("센서 1")
                .deviceType("TEMPERATURE")
                .zone(zone1)
                .build();
        em.persist(device);
        em.flush();
        em.clear();

        List<Device> devices = deviceRepository.findAllWithZone();
        assertThat(devices).isNotEmpty();
        
        Device found = devices.stream()
                .filter(d -> d.getDeviceName().equals("센서 1"))
                .findFirst()
                .orElseThrow();
        assertThat(found.getZone().getZoneName()).isEqualTo("1층 대회의실");
    }

    @Test
    @DisplayName("existsByDeviceName: 이름 중복 확인")
    void existsByDeviceName() {
        Device device = Device.builder()
                .deviceName("중복 이름")
                .deviceType("NFC_READER")
                .zone(zone1)
                .build();
        em.persist(device);

        assertThat(deviceRepository.existsByDeviceName("중복 이름")).isTrue();
        assertThat(deviceRepository.existsByDeviceName("다른 이름")).isFalse();
    }
}
