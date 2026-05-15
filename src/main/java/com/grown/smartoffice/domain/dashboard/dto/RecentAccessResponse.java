package com.grown.smartoffice.domain.dashboard.dto;

import com.grown.smartoffice.domain.accesslog.entity.AccessLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "최근 출입 기록")
public class RecentAccessResponse {

    @Schema(description = "출입 로그 ID", example = "1024")
    private Long id;

    @Schema(description = "사용자 이름", example = "박성종")
    private String userName;

    @Schema(description = "구역 이름", example = "개발팀 사무실")
    private String zoneName;

    @Schema(description = "출입 시각", example = "2026-05-15T09:02:00")
    private LocalDateTime accessTime;

    @Schema(description = "출입 방향 (IN | OUT)", example = "IN")
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
