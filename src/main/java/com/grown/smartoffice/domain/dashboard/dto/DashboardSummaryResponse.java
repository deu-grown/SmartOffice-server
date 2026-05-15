package com.grown.smartoffice.domain.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "대시보드 요약 — KPI 카드 4종")
public class DashboardSummaryResponse {

    @Schema(description = "전체 사용자 수", example = "10")
    private int totalUsers;

    @Schema(description = "오늘 예약 건수", example = "5")
    private int todayReservations;

    @Schema(description = "활성 장치 수", example = "18")
    private int activeDevices;

    @Schema(description = "대기 중인 승인 건수", example = "0")
    private int pendingApprovals;
}
