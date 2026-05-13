package com.grown.smartoffice.domain.parking.service;

import com.grown.smartoffice.domain.parking.dto.*;
import com.grown.smartoffice.domain.parking.entity.SpotStatus;
import com.grown.smartoffice.domain.parking.entity.SpotType;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import com.grown.smartoffice.support.AbstractContainerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ParkingService 통합 테스트 (Testcontainers MySQL + V5/V8 시드).
 * - V5: spot 1~5 (zone 8 지하1층)
 * - V8: spot 6~25 (zone 8 + 9 지하2층 / device 13~20 매핑)
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ParkingServiceIntegrationTest extends AbstractContainerTest {

    @Autowired ParkingService parkingService;

    @Test
    @DisplayName("지하1층(zone 8) 요약 — V5 5건 + V8 10건 = 총 15건")
    void zoneSummary_floorB1() {
        ParkingZoneSummaryResponse res = parkingService.getZoneSummary(8L);

        assertThat(res.getZoneName()).isEqualTo("지하1층");
        assertThat(res.getTotalSpots()).isEqualTo(15);
        assertThat(res.getTotalSpots())
                .isEqualTo(res.getOccupiedSpots() + res.getAvailableSpots());
        assertThat(res.getSpots()).hasSize(15);
    }

    @Test
    @DisplayName("지하2층(zone 9, V8 신규) 지도 — 10건, 좌표 + 점유 필드 정상")
    void zoneMap_floorB2() {
        ParkingZoneMapResponse res = parkingService.getZoneMap(9L);

        assertThat(res.getZoneName()).isEqualTo("지하2층");
        assertThat(res.getSpots()).hasSize(10);
        assertThat(res.getSpots())
                .allSatisfy(s -> {
                    assertThat(s.getPositionX()).isNotNull();
                    assertThat(s.getPositionY()).isNotNull();
                });
    }

    @Test
    @DisplayName("EV 타입 필터 — V5(0) + V8(4: B1-008·B1-009·B2-003·B2-004 + B2-010) = 5건")
    void filterByEvType() {
        List<ParkingSpotResponse> evs = parkingService.getSpots(null, "EV", null);
        assertThat(evs).hasSizeGreaterThanOrEqualTo(5);
        assertThat(evs).allSatisfy(s -> assertThat(s.getSpotType()).isEqualTo(SpotType.EV));
    }

    @Test
    @DisplayName("INACTIVE 상태 필터 — V8의 B1-013·B2-010 2건")
    void filterByInactiveStatus() {
        List<ParkingSpotResponse> inactives = parkingService.getSpots(null, null, "INACTIVE");
        assertThat(inactives).hasSizeGreaterThanOrEqualTo(2);
        assertThat(inactives).allSatisfy(s -> assertThat(s.getSpotStatus()).isEqualTo(SpotStatus.INACTIVE));
    }

    @Test
    @DisplayName("IoT 점유 업데이트 — V8 spot 6(B1-006, device 13)에 deviceId 일치 → 비점유로 토글")
    void iotUpdate_success_unoccupy() {
        ParkingStatusUpdateRequest req = new ParkingStatusUpdateRequest();
        ReflectionTestUtils.setField(req, "deviceId", 13L);
        ReflectionTestUtils.setField(req, "occupied", false);

        ParkingStatusUpdateResponse res = parkingService.updateOccupancyFromIot(6L, req);

        assertThat(res.getSpotId()).isEqualTo(6L);
        assertThat(res.isOccupied()).isFalse();

        // 재조회로 영속 확인
        ParkingZoneSummaryResponse summary = parkingService.getZoneSummary(8L);
        assertThat(summary.getSpots())
                .filteredOn(s -> s.getSpotId().equals(6L))
                .allSatisfy(s -> assertThat(s.isOccupied()).isFalse());
    }

    @Test
    @DisplayName("IoT 점유 업데이트 — deviceId 불일치 시 DEVICE_SPOT_MISMATCH")
    void iotUpdate_deviceMismatch() {
        ParkingStatusUpdateRequest req = new ParkingStatusUpdateRequest();
        ReflectionTestUtils.setField(req, "deviceId", 99L); // 매핑 안 된 device
        ReflectionTestUtils.setField(req, "occupied", true);

        assertThatThrownBy(() -> parkingService.updateOccupancyFromIot(6L, req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.DEVICE_SPOT_MISMATCH);
    }

    @Test
    @DisplayName("존재하지 않는 spot — PARKING_SPOT_NOT_FOUND")
    void notFound() {
        assertThatThrownBy(() -> parkingService.getZoneSummary(99999L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.ZONE_NOT_FOUND);

        ParkingStatusUpdateRequest req = new ParkingStatusUpdateRequest();
        ReflectionTestUtils.setField(req, "deviceId", 13L);
        ReflectionTestUtils.setField(req, "occupied", true);
        assertThatThrownBy(() -> parkingService.updateOccupancyFromIot(99999L, req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.PARKING_SPOT_NOT_FOUND);
    }
}
