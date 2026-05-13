package com.grown.smartoffice.domain.parking.repository;

import com.grown.smartoffice.domain.device.entity.Device;
import com.grown.smartoffice.domain.device.entity.DeviceStatus;
import com.grown.smartoffice.domain.parking.entity.ParkingSpot;
import com.grown.smartoffice.domain.parking.entity.SpotStatus;
import com.grown.smartoffice.domain.parking.entity.SpotType;
import com.grown.smartoffice.domain.zone.entity.Zone;
import com.grown.smartoffice.domain.zone.entity.ZoneType;
import com.grown.smartoffice.support.RepositoryTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ParkingSpotRepositoryTest extends RepositoryTestSupport {

    @Autowired ParkingSpotRepository parkingSpotRepository;
    @Autowired TestEntityManager em;

    private Zone zone;
    private Device device1;
    private Device device2;

    @BeforeEach
    void setUp() {
        zone = Zone.builder().zoneName("지하2층-test").zoneType(ZoneType.FLOOR).build();
        em.persist(zone);

        device1 = Device.builder().zone(zone)
                .deviceName("Repo초음파-Test-A").deviceType("ULTRASONIC")
                .deviceStatus(DeviceStatus.ACTIVE).build();
        em.persist(device1);

        device2 = Device.builder().zone(zone)
                .deviceName("Repo초음파-Test-B").deviceType("ULTRASONIC")
                .deviceStatus(DeviceStatus.ACTIVE).build();
        em.persist(device2);
        em.flush();
    }

    private ParkingSpot spot(String num, SpotType type, Device device, boolean occupied, SpotStatus status) {
        return ParkingSpot.builder()
                .zone(zone).spotNumber(num).spotType(type)
                .device(device).occupied(occupied).spotStatus(status)
                .build();
    }

    @Test
    @DisplayName("동일 zone 내 spot_number 중복 검출")
    void existsByZoneAndNumber() {
        ParkingSpot s = spot("RT-001", SpotType.REGULAR, null, false, SpotStatus.ACTIVE);
        em.persist(s);
        em.flush();

        assertThat(parkingSpotRepository.existsByZone_ZoneIdAndSpotNumber(zone.getZoneId(), "RT-001")).isTrue();
        assertThat(parkingSpotRepository.existsByZone_ZoneIdAndSpotNumber(zone.getZoneId(), "RT-999")).isFalse();
    }

    @Test
    @DisplayName("device 중복 매핑 검출 — 동일 device를 다른 spot에서 사용 불가")
    void existsByDevice() {
        ParkingSpot s1 = spot("RT-002", SpotType.REGULAR, device1, false, SpotStatus.ACTIVE);
        em.persist(s1);
        em.flush();

        assertThat(parkingSpotRepository.existsByDevice_DevicesId(device1.getDevicesId())).isTrue();
        assertThat(parkingSpotRepository.existsByDevice_DevicesId(device2.getDevicesId())).isFalse();
    }

    @Test
    @DisplayName("findAllWithFilters — zoneId/spotType/status 조합 필터링")
    void findAllWithFilters() {
        ParkingSpot s1 = spot("RT-101", SpotType.REGULAR,  null, false, SpotStatus.ACTIVE);
        ParkingSpot s2 = spot("RT-102", SpotType.EV,       null, false, SpotStatus.ACTIVE);
        ParkingSpot s3 = spot("RT-103", SpotType.DISABLED, null, false, SpotStatus.INACTIVE);
        em.persist(s1); em.persist(s2); em.persist(s3);
        em.flush();

        List<ParkingSpot> all = parkingSpotRepository.findAllWithFilters(zone.getZoneId(), null, null);
        assertThat(all).extracting(ParkingSpot::getSpotNumber)
                .containsExactlyInAnyOrder("RT-101", "RT-102", "RT-103");

        List<ParkingSpot> evOnly = parkingSpotRepository.findAllWithFilters(zone.getZoneId(), SpotType.EV, null);
        assertThat(evOnly).extracting(ParkingSpot::getSpotNumber).containsExactly("RT-102");

        List<ParkingSpot> inactiveOnly =
                parkingSpotRepository.findAllWithFilters(zone.getZoneId(), null, SpotStatus.INACTIVE);
        assertThat(inactiveOnly).extracting(ParkingSpot::getSpotNumber).containsExactly("RT-103");
    }

    @Test
    @DisplayName("countByZone 및 countByZoneAndOccupied — 점유 집계")
    void countOccupied() {
        em.persist(spot("RT-201", SpotType.REGULAR, null, true,  SpotStatus.ACTIVE));
        em.persist(spot("RT-202", SpotType.REGULAR, null, true,  SpotStatus.ACTIVE));
        em.persist(spot("RT-203", SpotType.REGULAR, null, false, SpotStatus.ACTIVE));
        em.flush();

        assertThat(parkingSpotRepository.countByZone_ZoneId(zone.getZoneId())).isEqualTo(3);
        assertThat(parkingSpotRepository.countByZone_ZoneIdAndOccupied(zone.getZoneId(), true)).isEqualTo(2);
        assertThat(parkingSpotRepository.countByZone_ZoneIdAndOccupied(zone.getZoneId(), false)).isEqualTo(1);
    }

    @Test
    @DisplayName("findByZoneWithDevice — device fetch 정상")
    void findByZoneWithDevice() {
        em.persist(spot("RT-301", SpotType.REGULAR, device1, false, SpotStatus.ACTIVE));
        em.persist(spot("RT-302", SpotType.EV,      device2, true,  SpotStatus.ACTIVE));
        em.flush();
        em.clear();

        List<ParkingSpot> spots = parkingSpotRepository.findByZoneWithDevice(zone.getZoneId());
        assertThat(spots).hasSize(2);
        assertThat(spots).extracting(s -> s.getDevice() != null ? s.getDevice().getDeviceName() : null)
                .containsExactlyInAnyOrder("Repo초음파-Test-A", "Repo초음파-Test-B");
    }
}
