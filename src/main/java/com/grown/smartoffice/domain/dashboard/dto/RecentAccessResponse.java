package com.grown.smartoffice.domain.dashboard.dto;

import com.grown.smartoffice.domain.accesslog.entity.AccessLog;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RecentAccessResponse {
    private Long id;
    private String userName;
    private String zoneName;
    private LocalDateTime accessTime;
    private String type;

    public static RecentAccessResponse from(AccessLog log) {
        return RecentAccessResponse.builder()
                .id(log.getAccessLogId())
                .userName(log.getUser().getEmployeeName())
                .zoneName(log.getZone().getZoneName())
                .accessTime(log.getTaggedAt())
                .type(log.getDirection())
                .build();
    }
}
