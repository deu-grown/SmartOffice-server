package com.grown.smartoffice.domain.accesslog.service;

import com.grown.smartoffice.domain.accesslog.dto.AllAccessLogListResponse;
import com.grown.smartoffice.domain.accesslog.dto.TagEventRequest;
import com.grown.smartoffice.domain.accesslog.dto.TagEventResponse;
import com.grown.smartoffice.domain.accesslog.entity.AccessLog;
import com.grown.smartoffice.domain.accesslog.repository.AccessLogRepository;
import com.grown.smartoffice.support.AbstractContainerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AccessLogService 통합 테스트 (Testcontainers MySQL 사용).
 * V5__seed_test_data.sql 에 의해 삽입된 데이터를 활용하여 실제 DB 연동을 검증한다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AccessLogServiceIntegrationTest extends AbstractContainerTest {

    @Autowired AccessLogService accessLogService;
    @Autowired AccessLogRepository accessLogRepository;

    @Test
    @DisplayName("NFC 태그 성공 시 AccessLog가 DB에 저장된다")
    void processTag_success_savesLog() {
        // Given (V5 데이터의 EMP002 카드 사용)
        TagEventRequest request = TagEventRequest.builder()
                .deviceId(1L) // 1층 입구
                .uid("EMP002-CARD-UID-001")
                .direction("IN")
                .taggedAt(LocalDateTime.of(2026, 5, 8, 10, 0))
                .build();

        // When
        TagEventResponse response = accessLogService.processTag(request);

        // Then
        assertThat(response.getAuthResult()).isEqualTo("APPROVED");
        
        AccessLog savedLog = accessLogRepository.findAll().stream()
                .filter(l -> "EMP002-CARD-UID-001".equals(l.getReadUid()))
                .filter(l -> "APPROVED".equals(l.getAuthResult()))
                .findFirst().orElseThrow();
        
        assertThat(savedLog.getUser().getEmployeeNumber()).isEqualTo("EMP002");
        assertThat(savedLog.getDirection()).isEqualTo("IN");
    }

    @Test
    @DisplayName("등록되지 않은 카드 태그 시 DENIED 결과가 반환되고 로그가 저장되지 않는다")
    void processTag_unregisteredCard_denied() {
        // Given
        TagEventRequest request = TagEventRequest.builder()
                .deviceId(1L)
                .uid("UNKNOWN-CARD-UID")
                .direction("IN")
                .build();

        // When
        TagEventResponse response = accessLogService.processTag(request);

        // Then
        assertThat(response.getAuthResult()).isEqualTo("DENIED");
        assertThat(response.getDenyReason()).isEqualTo("등록되지 않은 카드");
        
        // 등록되지 않은 카드는 현재 로직상 DB에 저장하지 않음 (AccessLog 테이블에 user_id, card_id가 NOT NULL이기 때문)
        boolean exists = accessLogRepository.findAll().stream()
                .anyMatch(l -> "UNKNOWN-CARD-UID".equals(l.getReadUid()));
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("전체 출입 로그 조회 - 날짜 및 결과 필터링")
    void getAllAccessLogs_withFilters() {
        // Given (새로운 로그 하나 추가)
        accessLogService.processTag(TagEventRequest.builder()
                .deviceId(1L).uid("EMP002-CARD-UID-001").direction("IN").build());

        // When
        AllAccessLogListResponse result = accessLogService.getAllAccessLogs(
                null, null, "APPROVED", null, "2026-05-01", "2026-05-31", 0, 10);

        // Then
        assertThat(result.getLogs()).isNotEmpty();
        assertThat(result.getLogs().get(0).getAuthResult()).isEqualTo("APPROVED");
        assertThat(result.getLogs().get(0).getUserName()).isEqualTo("이순신");
    }
}
