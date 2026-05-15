package com.grown.smartoffice.domain.power.service;

import com.grown.smartoffice.domain.power.dto.PowerHourlyResponse;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import com.grown.smartoffice.support.AbstractContainerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PowerService 통합 테스트 (Testcontainers MySQL + V7/V8 시드).
 * V7/V8 시드의 POWER 미터 보유 zone: 2(회의실 A) · 4(회의실 B) · 5(개발팀 좌석) · 7(서버실).
 *
 * 본 테스트는 #11 (GET /power/zones/{id}/hourly HTTP 500 — only_full_group_by SQL 결함) 회귀 방지.
 * findHourlyPowerProjection nativeQuery 의 SELECT/GROUP BY DATE_FORMAT 패턴 정합 검증 +
 * interface projection snake_case alias → camelCase getter 매핑 검증.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PowerServiceIntegrationTest extends AbstractContainerTest {

    @Autowired PowerService powerService;

    @Test
    @DisplayName("getHourlyHistory — V7/V8 POWER zone 5(개발팀 좌석) 시간별 조회 정상 (#11 회귀 방지)")
    void getHourlyHistory_returnsResponse_whenPowerLogsExist() {
        PowerHourlyResponse res = powerService.getHourlyHistory(5L, null, null, null);

        assertThat(res.getZoneId()).isEqualTo(5L);
        assertThat(res.getZoneName()).isEqualTo("개발팀 좌석");
        assertThat(res.getLogs()).isNotNull();
        // V7/V8 시드 device 9(개발팀 전력미터) 의 hourly 집계가 1건 이상 존재
        // hourAt 패턴 'yyyy-MM-dd'T'HH:00:00' 정상 LocalDateTime 매핑 확인
        assertThat(res.getLogs()).allSatisfy(log -> {
            assertThat(log.getHourAt()).isNotNull();
            assertThat(log.getDeviceId()).isNotNull();
            assertThat(log.getDeviceName()).isNotNull();
            assertThat(log.getKwh()).isNotNull();
            assertThat(log.getAvgWatt()).isNotNull();
            assertThat(log.getPeakWatt()).isNotNull();
        });
    }

    @Test
    @DisplayName("getHourlyHistory — POWER zone 2(회의실 A) 정상 응답")
    void getHourlyHistory_zone2() {
        PowerHourlyResponse res = powerService.getHourlyHistory(2L, null, null, null);

        assertThat(res.getZoneId()).isEqualTo(2L);
        assertThat(res.getLogs()).isNotNull();
    }

    @Test
    @DisplayName("getHourlyHistory — 존재하지 않는 zone → ZONE_NOT_FOUND")
    void getHourlyHistory_zoneNotFound() {
        assertThatThrownBy(() -> powerService.getHourlyHistory(99999L, null, null, null))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.ZONE_NOT_FOUND);
    }
}
