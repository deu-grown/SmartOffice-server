package com.grown.smartoffice.domain.zone.repository;

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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ZoneRepositoryTest extends RepositoryTestSupport {

    @Autowired ZoneRepository zoneRepository;
    @Autowired TestEntityManager em;

    private Zone root;
    private Zone childA;
    private Zone childB;

    @BeforeEach
    void setUp() {
        root = Zone.builder().zoneName("ZRT루트-" + System.nanoTime()).zoneType(ZoneType.FLOOR).build();
        em.persist(root);
        childA = Zone.builder().parent(root).zoneName("ZRT자식A").zoneType(ZoneType.AREA).build();
        em.persist(childA);
        childB = Zone.builder().parent(root).zoneName("ZRT자식B").zoneType(ZoneType.ROOM).build();
        em.persist(childB);
        em.flush();
    }

    @Test
    @DisplayName("findAllByParent_ZoneId — 직속 자식만 조회")
    void findAllByParentZoneId() {
        List<Zone> children = zoneRepository.findAllByParent_ZoneId(root.getZoneId());
        assertThat(children).extracting(Zone::getZoneName)
                .containsExactlyInAnyOrder("ZRT자식A", "ZRT자식B");
    }

    @Test
    @DisplayName("existsByParent_ZoneIdAndZoneName — 동일 상위 내 중복 검출")
    void existsByParentAndName() {
        assertThat(zoneRepository.existsByParent_ZoneIdAndZoneName(root.getZoneId(), "ZRT자식A")).isTrue();
        assertThat(zoneRepository.existsByParent_ZoneIdAndZoneName(root.getZoneId(), "없음")).isFalse();
    }

    @Test
    @DisplayName("existsByParentIsNullAndZoneName — 루트 레벨 중복 검출")
    void existsByRoot() {
        String rootName = root.getZoneName();
        assertThat(zoneRepository.existsByParentIsNullAndZoneName(rootName)).isTrue();
        assertThat(zoneRepository.existsByParentIsNullAndZoneName("ZRT-없는루트-XYZ")).isFalse();
    }

    @Test
    @DisplayName("hasChildren — 자식 존재 시 true, 없는 leaf는 false")
    void hasChildren() {
        assertThat(zoneRepository.hasChildren(root.getZoneId())).isTrue();
        assertThat(zoneRepository.hasChildren(childA.getZoneId())).isFalse();
    }

    @Test
    @DisplayName("hasDevices — 구역에 device 매핑 시 true")
    void hasDevices() {
        Device dev = Device.builder().zone(childA)
                .deviceName("ZRT-DEV").deviceType("TEMPERATURE")
                .deviceStatus(DeviceStatus.ACTIVE).build();
        em.persist(dev);
        em.flush();
        em.clear();

        assertThat(zoneRepository.hasDevices(childA.getZoneId())).isTrue();
        assertThat(zoneRepository.hasDevices(childB.getZoneId())).isFalse();
    }

    @Test
    @DisplayName("findByIdWithParent — 상위 zone JOIN FETCH로 즉시 로딩")
    void findByIdWithParent() {
        em.flush();
        em.clear();

        Optional<Zone> found = zoneRepository.findByIdWithParent(childA.getZoneId());
        assertThat(found).isPresent();
        assertThat(found.get().getParent().getZoneName()).isEqualTo(root.getZoneName());
    }

    @Test
    @DisplayName("findAllByParent_ZoneIdAndZoneType — 상위+타입 필터")
    void findAllByParentAndType() {
        List<Zone> roomsUnderRoot =
                zoneRepository.findAllByParent_ZoneIdAndZoneType(root.getZoneId(), ZoneType.ROOM);
        assertThat(roomsUnderRoot).extracting(Zone::getZoneName).containsExactly("ZRT자식B");
    }

    @Test
    @DisplayName("findAllRoots — parent IS NULL 인 zone들만 반환")
    void findAllRoots() {
        List<Zone> roots = zoneRepository.findAllRoots();
        assertThat(roots).extracting(Zone::getZoneName).contains(root.getZoneName());
        assertThat(roots).extracting(Zone::getZoneName).doesNotContain("ZRT자식A", "ZRT자식B");
    }
}
