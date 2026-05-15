package com.grown.smartoffice.domain.guest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class GuestCreateRequest {

    @NotBlank(message = "방문객 이름은 필수입니다.")
    @Size(max = 50)
    @Schema(description = "방문객 이름", example = "김방문")
    private String guestName;

    @Size(max = 100)
    @Schema(description = "소속 회사", example = "그로운파트너스")
    private String company;

    @Schema(description = "방문 대상 임직원 ID (미지정 가능)", example = "1")
    private Long hostUserId;

    @Size(max = 200)
    @Schema(description = "방문 목적", example = "협력사 미팅")
    private String purpose;

    @Size(max = 20)
    @Schema(description = "연락처", example = "010-1234-5678")
    private String contactPhone;

    @NotNull(message = "방문 예정 시각은 필수입니다.")
    @Schema(description = "방문 예정 시각", example = "2026-05-16T14:00:00")
    private LocalDateTime scheduledEntryAt;
}
