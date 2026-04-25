package com.grown.smartoffice.domain.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class BatchTriggerRequest {

    @NotNull
    @Schema(description = "배치 대상 날짜", example = "2026-04-25")
    private LocalDate targetDate;
}
