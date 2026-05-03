package com.grown.smartoffice.domain.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardSummaryResponse {
    private int totalUsers;
    private int todayReservations;
    private int activeDevices;
    private int pendingApprovals;
}
