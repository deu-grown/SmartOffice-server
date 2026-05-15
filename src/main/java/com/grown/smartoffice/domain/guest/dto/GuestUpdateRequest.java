package com.grown.smartoffice.domain.guest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class GuestUpdateRequest {

    @Size(max = 50)
    @Schema(description = "방문객 이름", example = "이방문")
    private String guestName;

    @Size(max = 100)
    @Schema(description = "소속 회사", example = "동의테크")
    private String company;

    @Schema(description = "방문 대상 임직원 ID (해제 시 null)", example = "2")
    private Long hostUserId;

    @Size(max = 200)
    @Schema(description = "방문 목적", example = "장비 점검")
    private String purpose;

    @Size(max = 20)
    @Schema(description = "연락처", example = "010-9876-5432")
    private String contactPhone;

    @Schema(description = "방문객 상태 (SCHEDULED | VISITING | COMPLETED | CANCELLED)", example = "CANCELLED")
    private String guestStatus;

    @Schema(description = "방문 예정 시각", example = "2026-05-16T15:30:00")
    private LocalDateTime scheduledEntryAt;
}
