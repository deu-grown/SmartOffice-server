package com.grown.smartoffice.domain.accesslog.service;

import com.grown.smartoffice.domain.accesslog.dto.*;
import com.grown.smartoffice.domain.accesslog.entity.AccessLog;
import com.grown.smartoffice.domain.accesslog.repository.AccessLogRepository;
import com.grown.smartoffice.domain.attendance.service.AttendanceCommandService;
import com.grown.smartoffice.domain.device.entity.Device;
import com.grown.smartoffice.domain.device.repository.DeviceRepository;
import com.grown.smartoffice.domain.nfccard.entity.NfcCard;
import com.grown.smartoffice.domain.nfccard.repository.NfcCardRepository;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import com.grown.smartoffice.global.common.PageResponse;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class AccessLogService {

    private final DeviceRepository deviceRepository;
    private final NfcCardRepository nfcCardRepository;
    private final AccessLogRepository accessLogRepository;
    private final UserRepository userRepository;
    private final AttendanceCommandService attendanceCommandService;

    @Transactional
    public TagEventResponse processTag(TagEventRequest request) {
        LocalDateTime taggedAt = request.getTaggedAt() != null ? request.getTaggedAt() : LocalDateTime.now();

        Device device = deviceRepository.findByIdWithZone(request.getDeviceId())
                .orElseThrow(() -> new CustomException(ErrorCode.DEVICE_NOT_FOUND));

        NfcCard card = nfcCardRepository.findByCardUidWithUser(request.getUid()).orElse(null);

        if (card == null) {
            return TagEventResponse.builder()
                    .authResult("DENIED")
                    .denyReason("등록되지 않은 카드")
                    .taggedAt(taggedAt)
                    .build();
        }

        String denyReason = null;
        String authResult;

        if (UserStatus.INACTIVE == card.getUser().getStatus()) {
            denyReason = "퇴사 처리된 계정";
            authResult = "DENIED";
        } else if (card.isExpired()) {
            denyReason = "만료된 카드";
            authResult = "DENIED";
        } else {
            authResult = "APPROVED";
        }

        AccessLog log = AccessLog.builder()
                .user(card.getUser())
                .card(card)
                .device(device)
                .zone(device.getZone())
                .readUid(request.getUid())
                .direction(request.getDirection())
                .authResult(authResult)
                .denyReason(denyReason)
                .taggedAt(taggedAt)
                .build();
        accessLogRepository.save(log);

        if ("APPROVED".equals(authResult)) {
            attendanceCommandService.recordTag(card.getUser().getUserId(), taggedAt);
        }

        return TagEventResponse.builder()
                .authResult(authResult)
                .denyReason(denyReason)
                .userId("APPROVED".equals(authResult) ? card.getUser().getUserId() : null)
                .taggedAt(taggedAt)
                .build();
    }

    @Transactional(readOnly = true)
    public AllAccessLogListResponse getAllAccessLogs(Long zoneId, Long userId, String authResult,
                                                     String direction, String startDateStr, String endDateStr,
                                                     int page, int size) {
        LocalDateTime startDate = parseStartDate(startDateStr);
        LocalDateTime endDate = parseEndDate(endDateStr);

        var pageable = PageRequest.of(page, size, Sort.by("taggedAt").descending());
        var pageResult = accessLogRepository.findAllWithFilters(zoneId, userId, authResult, direction, startDate, endDate, pageable);

        return AllAccessLogListResponse.from(PageResponse.from(pageResult.map(AccessLogResponse::from)));
    }

    @Transactional(readOnly = true)
    public UserAccessLogListResponse getUserAccessLogs(Long userId, String startDateStr, String endDateStr,
                                                       Long zoneId, String direction, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        LocalDateTime startDate = parseStartDate(startDateStr);
        LocalDateTime endDate = parseEndDate(endDateStr);

        var pageable = PageRequest.of(page, size, Sort.by("taggedAt").descending());
        var pageResult = accessLogRepository.findAllWithFilters(zoneId, userId, null, direction, startDate, endDate, pageable);

        return UserAccessLogListResponse.of(userId, user.getEmployeeName(), PageResponse.from(pageResult.map(AccessLogResponse::from)));
    }

    @Transactional(readOnly = true)
    public AllAccessLogListResponse getMyAccessLogs(String email, String startDateStr, String endDateStr,
                                                    String direction, int page, int size) {
        User user = userRepository.findByEmployeeEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        LocalDateTime startDate = parseStartDate(startDateStr);
        LocalDateTime endDate = parseEndDate(endDateStr);

        var pageable = PageRequest.of(page, size, Sort.by("taggedAt").descending());
        var pageResult = accessLogRepository.findAllWithFilters(null, user.getUserId(), "APPROVED", direction, startDate, endDate, pageable);

        return AllAccessLogListResponse.from(PageResponse.from(pageResult.map(AccessLogResponse::from)));
    }

    private LocalDateTime parseStartDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr).atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT); // Or a specific date format error
        }
    }

    private LocalDateTime parseEndDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr).atTime(LocalTime.MAX);
        } catch (DateTimeParseException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
    }
}
