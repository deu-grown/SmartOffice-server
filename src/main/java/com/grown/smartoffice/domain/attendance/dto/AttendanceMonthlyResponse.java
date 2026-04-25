package com.grown.smartoffice.domain.attendance.dto;

import com.grown.smartoffice.domain.attendance.entity.MonthlyAttendance;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AttendanceMonthlyResponse {

    private Long monatId;
    private Long userId;
    private int year;
    private int month;
    private int totalWorkMinutes;
    private int overtimeMinutes;
    private int lateCount;
    private int earlyLeaveCount;
    private int absentCount;

    public static AttendanceMonthlyResponse from(MonthlyAttendance m) {
        return AttendanceMonthlyResponse.builder()
                .monatId(m.getMonatId())
                .userId(m.getUser().getUserId())
                .year(m.getMonatYear())
                .month(m.getMonatMonth())
                .totalWorkMinutes(m.getMonatTotalWorkMinutes())
                .overtimeMinutes(m.getMonatOvertimeMinutes())
                .lateCount(m.getLateCount())
                .earlyLeaveCount(m.getEarlyLeaveCount())
                .absentCount(m.getAbsentCount())
                .build();
    }
}
