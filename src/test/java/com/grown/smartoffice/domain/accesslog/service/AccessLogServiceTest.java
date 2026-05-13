package com.grown.smartoffice.domain.accesslog.service;

import com.grown.smartoffice.domain.accesslog.dto.AllAccessLogListResponse;
import com.grown.smartoffice.domain.accesslog.dto.TagEventRequest;
import com.grown.smartoffice.domain.accesslog.dto.TagEventResponse;
import com.grown.smartoffice.domain.accesslog.dto.UserAccessLogListResponse;
import com.grown.smartoffice.domain.accesslog.entity.AccessLog;
import com.grown.smartoffice.domain.accesslog.repository.AccessLogRepository;
import com.grown.smartoffice.domain.attendance.service.AttendanceCommandService;
import com.grown.smartoffice.domain.device.entity.Device;
import com.grown.smartoffice.domain.device.entity.DeviceStatus;
import com.grown.smartoffice.domain.device.repository.DeviceRepository;
import com.grown.smartoffice.domain.nfccard.entity.NfcCard;
import com.grown.smartoffice.domain.nfccard.entity.NfcCardStatus;
import com.grown.smartoffice.domain.nfccard.repository.NfcCardRepository;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserRole;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import com.grown.smartoffice.domain.zone.entity.Zone;
import com.grown.smartoffice.domain.zone.entity.ZoneType;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccessLogServiceTest {

    @Mock DeviceRepository deviceRepository;
    @Mock NfcCardRepository nfcCardRepository;
    @Mock AccessLogRepository accessLogRepository;
    @Mock UserRepository userRepository;
    @Mock AttendanceCommandService attendanceCommandService;
    @InjectMocks AccessLogService accessLogService;

    private Zone zone;
    private Device device;
    private User activeUser;
    private NfcCard activeCard;

    @BeforeEach
    void setUp() {
        zone = Zone.builder().zoneName("회의실A").zoneType(ZoneType.AREA).build();
        ReflectionTestUtils.setField(zone, "zoneId", 2L);

        device = Device.builder().zone(zone).deviceName("리더기").deviceType("NFC_READER")
                .deviceStatus(DeviceStatus.ACTIVE).build();
        ReflectionTestUtils.setField(device, "devicesId", 1L);

        activeUser = User.builder()
                .employeeNumber("EMP001").employeeName("관리자").employeeEmail("admin@grown.com")
                .password("p").role(UserRole.ADMIN).position("팀장")
                .status(UserStatus.ACTIVE).hiredAt(LocalDate.now()).build();
        ReflectionTestUtils.setField(activeUser, "userId", 1L);

        activeCard = NfcCard.builder().user(activeUser).cardUid("CARD-001")
                .cardType("EMPLOYEE").cardStatus(NfcCardStatus.ACTIVE)
                .issuedAt(LocalDateTime.now()).build();
        ReflectionTestUtils.setField(activeCard, "cardId", 1L);
    }

    private TagEventRequest tagRequest(String uid) {
        return TagEventRequest.builder()
                .deviceId(1L).uid(uid).direction("IN")
                .taggedAt(LocalDateTime.of(2026, 5, 13, 9, 0))
                .build();
    }

    @Test
    @DisplayName("processTag — 등록 안 된 카드 → DENIED, DB 저장 X, 근태 기록 X")
    void processTag_unregisteredCard() {
        given(deviceRepository.findByIdWithZone(1L)).willReturn(Optional.of(device));
        given(nfcCardRepository.findByCardUidWithUser("UNKNOWN")).willReturn(Optional.empty());

        TagEventResponse res = accessLogService.processTag(tagRequest("UNKNOWN"));

        assertThat(res.getAuthResult()).isEqualTo("DENIED");
        assertThat(res.getDenyReason()).isEqualTo("등록되지 않은 카드");
        verify(accessLogRepository, never()).save(any());
        verify(attendanceCommandService, never()).recordTag(any(), any());
    }

    @Test
    @DisplayName("processTag — INACTIVE 사용자 → DENIED, 로그 저장됨, 근태 기록 X")
    void processTag_inactiveUser() {
        ReflectionTestUtils.setField(activeUser, "status", UserStatus.INACTIVE);
        given(deviceRepository.findByIdWithZone(1L)).willReturn(Optional.of(device));
        given(nfcCardRepository.findByCardUidWithUser("CARD-001")).willReturn(Optional.of(activeCard));

        TagEventResponse res = accessLogService.processTag(tagRequest("CARD-001"));

        assertThat(res.getAuthResult()).isEqualTo("DENIED");
        assertThat(res.getDenyReason()).isEqualTo("퇴사 처리된 계정");
        verify(accessLogRepository).save(any(AccessLog.class));
        verify(attendanceCommandService, never()).recordTag(any(), any());
    }

    @Test
    @DisplayName("processTag — 만료된 카드 → DENIED")
    void processTag_expiredCard() {
        ReflectionTestUtils.setField(activeCard, "expiredAt", LocalDateTime.now().minusDays(1));
        given(deviceRepository.findByIdWithZone(1L)).willReturn(Optional.of(device));
        given(nfcCardRepository.findByCardUidWithUser("CARD-001")).willReturn(Optional.of(activeCard));

        TagEventResponse res = accessLogService.processTag(tagRequest("CARD-001"));

        assertThat(res.getAuthResult()).isEqualTo("DENIED");
        assertThat(res.getDenyReason()).isEqualTo("만료된 카드");
        verify(attendanceCommandService, never()).recordTag(any(), any());
    }

    @Test
    @DisplayName("processTag — APPROVED 시 attendance.recordTag 호출")
    void processTag_approvedCallsAttendance() {
        given(deviceRepository.findByIdWithZone(1L)).willReturn(Optional.of(device));
        given(nfcCardRepository.findByCardUidWithUser("CARD-001")).willReturn(Optional.of(activeCard));

        TagEventResponse res = accessLogService.processTag(tagRequest("CARD-001"));

        assertThat(res.getAuthResult()).isEqualTo("APPROVED");
        verify(accessLogRepository).save(any(AccessLog.class));
        verify(attendanceCommandService).recordTag(eq(1L), any());
    }

    @Test
    @DisplayName("processTag — 장치 미존재 시 DEVICE_NOT_FOUND")
    void processTag_deviceNotFound() {
        given(deviceRepository.findByIdWithZone(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> accessLogService.processTag(tagRequest("X")))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.DEVICE_NOT_FOUND);
    }

    @Test
    @DisplayName("getAllAccessLogs — 잘못된 날짜 형식 → INVALID_INPUT")
    void getAllAccessLogs_invalidDate() {
        assertThatThrownBy(() ->
                accessLogService.getAllAccessLogs(null, null, null, null, "INVALID", null, 0, 10))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("getAllAccessLogs — 날짜 null/빈문자 허용, 필터 페이지 정상 위임")
    void getAllAccessLogs_emptyDateOk() {
        Page<AccessLog> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        given(accessLogRepository.findAllWithFilters(eq(2L), eq(1L), eq("APPROVED"), eq("IN"),
                eq(null), eq(null), any())).willReturn(page);

        AllAccessLogListResponse res = accessLogService.getAllAccessLogs(2L, 1L, "APPROVED", "IN", "", "", 0, 10);
        assertThat(res.getLogs()).isEmpty();
        assertThat(res.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("getUserAccessLogs — 없는 user → USER_NOT_FOUND")
    void getUserAccessLogs_userNotFound() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());
        assertThatThrownBy(() ->
                accessLogService.getUserAccessLogs(999L, null, null, null, null, 0, 10))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("getUserAccessLogs — 본인의 로그만 조회됨 (userId 필터 전달)")
    void getUserAccessLogs_filtersByUser() {
        given(userRepository.findById(1L)).willReturn(Optional.of(activeUser));
        Page<AccessLog> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        given(accessLogRepository.findAllWithFilters(any(), eq(1L), eq(null), any(), any(), any(), any()))
                .willReturn(page);

        UserAccessLogListResponse res =
                accessLogService.getUserAccessLogs(1L, null, null, null, null, 0, 10);
        assertThat(res.getUserId()).isEqualTo(1L);
        assertThat(res.getUserName()).isEqualTo("관리자");
    }

    @Test
    @DisplayName("getMyAccessLogs — 이메일로 user 조회 후 본인+APPROVED 필터")
    void getMyAccessLogs_byEmail() {
        given(userRepository.findByEmployeeEmail("admin@grown.com")).willReturn(Optional.of(activeUser));
        Page<AccessLog> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        given(accessLogRepository.findAllWithFilters(eq(null), eq(1L), eq("APPROVED"), any(), any(), any(), any()))
                .willReturn(page);

        AllAccessLogListResponse res =
                accessLogService.getMyAccessLogs("admin@grown.com", null, null, null, 0, 10);
        assertThat(res.getLogs()).isEmpty();
    }
}
