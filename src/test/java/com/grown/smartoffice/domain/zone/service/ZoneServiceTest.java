package com.grown.smartoffice.domain.zone.service;

import com.grown.smartoffice.domain.zone.dto.ZoneCreateRequest;
import com.grown.smartoffice.domain.zone.dto.ZoneUpdateRequest;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ZoneServiceTest {

    @Mock ZoneRepository zoneRepository;
    @InjectMocks ZoneServiceImpl zoneService;

    Zone buildZone(Long id, String name, ZoneType type, Zone parent) {
        Zone z = Zone.builder().zoneName(name).zoneType(type).parent(parent).build();
        ReflectionTestUtils.setField(z, "zoneId", id);
        return z;
    }

    ZoneCreateRequest createReq(String name, ZoneType type, Long parentId) {
        ZoneCreateRequest req = new ZoneCreateRequest();
        ReflectionTestUtils.setField(req, "name", name);
        ReflectionTestUtils.setField(req, "zoneType", type);
        ReflectionTestUtils.setField(req, "parentId", parentId);
        return req;
    }

    // ── 등록 ──────────────────────────────────────────────

    @Test
    @DisplayName("createZone — 최상위 구역 등록 성공")
    void createZone_root_success() {
        ZoneCreateRequest req = createReq("1층", ZoneType.FLOOR, null);
        given(zoneRepository.existsByParentIsNullAndZoneName("1층")).willReturn(false);
        Zone saved = buildZone(1L, "1층", ZoneType.FLOOR, null);
        given(zoneRepository.save(any())).willReturn(saved);

        zoneService.createZone(req);

        verify(zoneRepository).save(any());
    }

    @Test
    @DisplayName("createZone — 동일 부모 내 이름 중복 → DUPLICATE_ZONE_NAME")
    void createZone_duplicateName_conflict() {
        ZoneCreateRequest req = createReq("회의실A", ZoneType.AREA, 1L);
        Zone parent = buildZone(1L, "1층", ZoneType.FLOOR, null);
        given(zoneRepository.findById(1L)).willReturn(Optional.of(parent));
        given(zoneRepository.existsByParent_ZoneIdAndZoneName(1L, "회의실A")).willReturn(true);

        assertThatThrownBy(() -> zoneService.createZone(req))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_ZONE_NAME);
    }

    @Test
    @DisplayName("createZone — 존재하지 않는 parentId → PARENT_ZONE_NOT_FOUND")
    void createZone_parentNotFound() {
        ZoneCreateRequest req = createReq("회의실B", ZoneType.AREA, 999L);
        given(zoneRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> zoneService.createZone(req))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PARENT_ZONE_NOT_FOUND);
    }

    // ── 삭제 ──────────────────────────────────────────────

    @Test
    @DisplayName("deleteZone — 하위 구역 있으면 ZONE_HAS_CHILDREN")
    void deleteZone_hasChildren_conflict() {
        Zone zone = buildZone(1L, "1층", ZoneType.FLOOR, null);
        given(zoneRepository.findById(1L)).willReturn(Optional.of(zone));
        given(zoneRepository.existsByParent_ZoneId(1L)).willReturn(true);

        assertThatThrownBy(() -> zoneService.deleteZone(1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ZONE_HAS_CHILDREN);
    }

    @Test
    @DisplayName("deleteZone — 설치 장치 있으면 ZONE_HAS_DEVICES")
    void deleteZone_hasDevices_conflict() {
        Zone zone = buildZone(2L, "회의실A", ZoneType.AREA, null);
        given(zoneRepository.findById(2L)).willReturn(Optional.of(zone));
        given(zoneRepository.existsByParent_ZoneId(2L)).willReturn(false);
        given(zoneRepository.hasDevices(2L)).willReturn(true);

        assertThatThrownBy(() -> zoneService.deleteZone(2L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ZONE_HAS_DEVICES);
    }

    @Test
    @DisplayName("deleteZone — 정상 삭제")
    void deleteZone_success() {
        Zone zone = buildZone(3L, "창고", ZoneType.AREA, null);
        given(zoneRepository.findById(3L)).willReturn(Optional.of(zone));
        given(zoneRepository.existsByParent_ZoneId(3L)).willReturn(false);
        given(zoneRepository.hasDevices(3L)).willReturn(false);

        zoneService.deleteZone(3L);

        verify(zoneRepository).delete(zone);
    }

    // ── 수정 — 순환참조 ─────────────────────────────────

    @Test
    @DisplayName("updateZone — 자기 자신을 상위로 지정 → INVALID_ZONE_HIERARCHY")
    void updateZone_selfParent_invalid() {
        Zone zone = buildZone(1L, "1층", ZoneType.FLOOR, null);
        given(zoneRepository.findById(1L)).willReturn(Optional.of(zone));
        given(zoneRepository.findById(1L)).willReturn(Optional.of(zone)); // parent lookup same

        ZoneUpdateRequest req = new ZoneUpdateRequest();
        ReflectionTestUtils.setField(req, "parentId", 1L);

        // candidateParent = zone itself → id matches → circular
        given(zoneRepository.findById(1L)).willReturn(Optional.of(zone));

        assertThatThrownBy(() -> zoneService.updateZone(1L, req))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ZONE_HIERARCHY);
    }

    @Test
    @DisplayName("updateZone — 하위 구역을 상위로 지정 → INVALID_ZONE_HIERARCHY")
    void updateZone_childAsParent_invalid() {
        Zone parent = buildZone(1L, "1층", ZoneType.FLOOR, null);
        Zone child = buildZone(2L, "회의실A", ZoneType.AREA, parent);
        ReflectionTestUtils.setField(parent, "children", List.of(child));

        given(zoneRepository.findById(1L)).willReturn(Optional.of(parent));
        given(zoneRepository.findById(2L)).willReturn(Optional.of(child));

        ZoneUpdateRequest req = new ZoneUpdateRequest();
        ReflectionTestUtils.setField(req, "parentId", 2L);

        assertThatThrownBy(() -> zoneService.updateZone(1L, req))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ZONE_HIERARCHY);
    }
}
