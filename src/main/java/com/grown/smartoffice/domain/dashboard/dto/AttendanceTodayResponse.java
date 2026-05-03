package com.grown.smartoffice.domain.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AttendanceTodayResponse {
    private int presentCount;
    private int absentCount;
    private int lateCount;
    private int totalExpected;
}
