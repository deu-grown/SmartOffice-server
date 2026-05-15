package com.grown.smartoffice.domain.guest.dto;

import com.grown.smartoffice.domain.guest.entity.Guest;
import com.grown.smartoffice.domain.guest.entity.GuestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "방문객 응답")
public class GuestResponse {

    @Schema(description = "방문객 ID", example = "1")
    private Long guestId;

    @Schema(description = "방문객 이름", example = "김방문")
    private String guestName;

    @Schema(description = "소속 회사", example = "그로운파트너스")
    private String company;

    @Schema(description = "방문 대상 임직원 ID", example = "1")
    private Long hostUserId;

    @Schema(description = "방문 대상 임직원 이름", example = "박성종")
    private String hostUserName;

    @Schema(description = "방문 목적", example = "협력사 미팅")
    private String purpose;

    @Schema(description = "연락처", example = "010-1234-5678")
    private String contactPhone;

    @Schema(description = "방문객 상태", example = "SCHEDULED")
    private GuestStatus guestStatus;

    @Schema(description = "방문 예정 시각", example = "2026-05-16T14:00:00")
    private LocalDateTime scheduledEntryAt;

    @Schema(description = "실제 입실 시각 (체크인 시 기록)", example = "2026-05-16T14:05:00")
    private LocalDateTime actualEntryAt;

    @Schema(description = "실제 퇴실 시각 (체크아웃 시 기록)", example = "2026-05-16T15:40:00")
    private LocalDateTime actualExitAt;

    @Schema(description = "등록 시각")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시각")
    private LocalDateTime updatedAt;

    public static GuestResponse from(Guest g) {
        return GuestResponse.builder()
                .guestId(g.getGuestId())
                .guestName(g.getGuestName())
                .company(g.getCompany())
                .hostUserId(g.getHostUser() != null ? g.getHostUser().getUserId() : null)
                .hostUserName(g.getHostUser() != null ? g.getHostUser().getEmployeeName() : null)
                .purpose(g.getPurpose())
                .contactPhone(g.getContactPhone())
                .guestStatus(g.getGuestStatus())
                .scheduledEntryAt(g.getScheduledEntryAt())
                .actualEntryAt(g.getActualEntryAt())
                .actualExitAt(g.getActualExitAt())
                .createdAt(g.getCreatedAt())
                .updatedAt(g.getUpdatedAt())
                .build();
    }
}
