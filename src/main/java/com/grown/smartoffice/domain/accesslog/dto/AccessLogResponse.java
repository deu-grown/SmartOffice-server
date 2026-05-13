package com.grown.smartoffice.domain.accesslog.dto;

import com.grown.smartoffice.domain.accesslog.entity.AccessLog;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AccessLogResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String employeeNumber;
    private String uid;
    private Long deviceId;
    private String deviceName;
    private Long zoneId;
    private String zoneName;
    private String direction;
    private String authResult;
    private String denyReason;
    private LocalDateTime taggedAt;

    public static AccessLogResponse from(AccessLog log) {
        return AccessLogResponse.builder()
                .id(log.getAccessLogId())
                .userId(log.getUser() != null ? log.getUser().getUserId() : null)
                .userName(log.getUser() != null ? log.getUser().getEmployeeName() : null)
                .employeeNumber(log.getUser() != null ? log.getUser().getEmployeeNumber() : null)
                .uid(log.getReadUid())
                .deviceId(log.getDevice().getDevicesId())
                .deviceName(log.getDevice().getDeviceName())
                .zoneId(log.getZone().getZoneId())
                .zoneName(log.getZone().getZoneName())
                .direction(log.getDirection())
                .authResult(log.getAuthResult())
                .denyReason(log.getDenyReason())
                .taggedAt(log.getTaggedAt())
                .build();
    }
}
