package com.grown.smartoffice.domain.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AttendanceCorrectRequest {

    @Schema(description = "수정할 출근 시각")
    private LocalDateTime checkIn;

    @Schema(description = "수정할 퇴근 시각")
    private LocalDateTime checkOut;

    @Schema(description = "수정 사유")
    private String note;
}
