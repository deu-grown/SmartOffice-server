package com.grown.smartoffice.domain.control.repository;

import com.grown.smartoffice.domain.control.entity.ControlCommand;
import com.grown.smartoffice.domain.control.entity.ControlStatus;
import com.grown.smartoffice.domain.device.entity.Device;
import com.grown.smartoffice.domain.device.entity.DeviceStatus;
import com.grown.smartoffice.domain.zone.entity.Zone;
import com.grown.smartoffice.domain.zone.entity.ZoneType;
import com.grown.smartoffice.support.RepositoryTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ControlCommandRepositoryTest extends RepositoryTestSupport {

    @Autowired ControlCommandRepository controlCommandRepository;
    @Autowired TestEntityManager em;

    private Zone zoneA;
    private Zone zoneB;
    private Device deviceA;
    private Device deviceB;

    @BeforeEach
    void setUp() {
        zoneA = Zone.builder().zoneName("CTL-A-" + System.nanoTime()).zoneType(ZoneType.AREA).build();
        zoneB = Zone.builder().zoneName("CTL-B-" + System.nanoTime()).zoneType(ZoneType.AREA).build();
        em.persist(zoneA);
        em.persist(zoneB);

        deviceA = Device.builder().zone(zoneA).deviceName("CTL-DEV-A").deviceType("AC")
                .deviceStatus(DeviceStatus.ACTIVE).build();
        deviceB = Device.builder().zone(zoneB).deviceName("CTL-DEV-B").deviceType("LIGHT")
                .deviceStatus(DeviceStatus.ACTIVE).build();
        em.persist(deviceA);
        em.persist(deviceB);
        em.flush();
    }

    private ControlCommand cmd(Zone z, Device d, ControlStatus st, LocalDateTime trigAt) {
        return ControlCommand.builder().zone(z).device(d)
                .commandType("AC").payload("{}").status(st).triggeredAt(trigAt).build();
    }

    @Test
    @DisplayName("findHistory — zoneId 필터 + 시작 시각 이후만 + 최신순")
    void findHistory_filterByZoneAndTime() {
        LocalDateTime now = LocalDateTime.now();
        em.persist(cmd(zoneA, deviceA, ControlStatus.COMPLETED, now.minusHours(3)));
        em.persist(cmd(zoneA, deviceA, ControlStatus.PENDING,   now.minusHours(1)));
        em.persist(cmd(zoneA, deviceA, ControlStatus.FAILED,    now.minusDays(2)));
        em.persist(cmd(zoneB, deviceB, ControlStatus.COMPLETED, now.minusHours(2)));
        em.flush();
        em.clear();

        List<ControlCommand> recent = controlCommandRepository.findHistory(zoneA.getZoneId(), now.minusHours(6));
        assertThat(recent).hasSize(2);
        assertThat(recent.get(0).getStatus()).isEqualTo(ControlStatus.PENDING);    // 최신
        assertThat(recent.get(1).getStatus()).isEqualTo(ControlStatus.COMPLETED);
    }

    @Test
    @DisplayName("findHistory — zoneId null 이면 전체 zone 대상")
    void findHistory_zoneIdNull_returnsAll() {
        LocalDateTime now = LocalDateTime.now();
        em.persist(cmd(zoneA, deviceA, ControlStatus.COMPLETED, now.minusHours(1)));
        em.persist(cmd(zoneB, deviceB, ControlStatus.COMPLETED, now.minusMinutes(30)));
        em.flush();
        em.clear();

        List<ControlCommand> all = controlCommandRepository.findHistory(null, now.minusHours(6));
        assertThat(all).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("findHistory — start 이후의 데이터만 (경계 케이스 포함)")
    void findHistory_excludesBeforeStart() {
        LocalDateTime now = LocalDateTime.now();
        em.persist(cmd(zoneA, deviceA, ControlStatus.FAILED,    now.minusDays(10)));
        em.persist(cmd(zoneA, deviceA, ControlStatus.COMPLETED, now.minusHours(1)));
        em.flush();
        em.clear();

        List<ControlCommand> recent = controlCommandRepository.findHistory(zoneA.getZoneId(), now.minusHours(6));
        assertThat(recent).extracting(ControlCommand::getStatus).containsOnly(ControlStatus.COMPLETED);
    }

    @Test
    @DisplayName("findHistory — 결과 없음 시 빈 리스트")
    void findHistory_empty() {
        List<ControlCommand> none = controlCommandRepository.findHistory(zoneA.getZoneId(),
                LocalDateTime.now().plusYears(1));
        assertThat(none).isEmpty();
    }
}
