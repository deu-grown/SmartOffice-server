package com.grown.smartoffice.domain.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "오늘 근태 현황")
public class AttendanceTodayResponse {

    @Schema(description = "출근 인원", example = "8")
    private int presentCount;

    @Schema(description = "결근 인원", example = "1")
    private int absentCount;

    @Schema(description = "지각 인원", example = "1")
    private int lateCount;

    @Schema(description = "출근 예정 총원", example = "10")
    private int totalExpected;
}
