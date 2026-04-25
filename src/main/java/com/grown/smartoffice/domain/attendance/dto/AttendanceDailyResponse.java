package com.grown.smartoffice.domain.attendance.dto;

import com.grown.smartoffice.domain.attendance.entity.Attendance;
import com.grown.smartoffice.domain.attendance.entity.AttendanceStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class AttendanceDailyResponse {

    private Long attendanceId;
    private Long userId;
    private String userName;
    private LocalDate workDate;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private Integer workMinutes;
    private Integer overtimeMinutes;
    private AttendanceStatus attendanceStatus;
    private String attendanceNote;

    public static AttendanceDailyResponse from(Attendance a) {
        return AttendanceDailyResponse.builder()
                .attendanceId(a.getAttendanceId())
                .userId(a.getUser().getUserId())
                .userName(a.getUser().getEmployeeName())
                .workDate(a.getWorkDate())
                .checkIn(a.getCheckIn())
                .checkOut(a.getCheckOut())
                .workMinutes(a.getWorkMinutes())
                .overtimeMinutes(a.getOvertimeMinutes())
                .attendanceStatus(a.getAttendanceStatus())
                .attendanceNote(a.getAttendanceNote())
                .build();
    }
}
