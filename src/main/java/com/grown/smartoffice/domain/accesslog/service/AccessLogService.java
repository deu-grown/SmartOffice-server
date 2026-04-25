package com.grown.smartoffice.domain.accesslog.service;

import com.grown.smartoffice.domain.accesslog.dto.TagEventRequest;
import com.grown.smartoffice.domain.accesslog.dto.TagEventResponse;
import com.grown.smartoffice.domain.accesslog.entity.AccessLog;
import com.grown.smartoffice.domain.accesslog.repository.AccessLogRepository;
import com.grown.smartoffice.domain.device.entity.Device;
import com.grown.smartoffice.domain.device.repository.DeviceRepository;
import com.grown.smartoffice.domain.nfccard.entity.NfcCard;
import com.grown.smartoffice.domain.nfccard.repository.NfcCardRepository;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccessLogService {

    private final DeviceRepository deviceRepository;
    private final NfcCardRepository nfcCardRepository;
    private final AccessLogRepository accessLogRepository;

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

        return TagEventResponse.builder()
                .authResult(authResult)
                .denyReason(denyReason)
                .userId("APPROVED".equals(authResult) ? card.getUser().getUserId() : null)
                .taggedAt(taggedAt)
                .build();
    }
}
