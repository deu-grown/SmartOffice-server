package com.grown.smartoffice.domain.accesslog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class TagEventRequest {

    @NotNull
    @Schema(description = "장치 ID", example = "1")
    private Long deviceId;

    @NotBlank
    @Schema(description = "NFC 카드 UID", example = "A1B2C3D4")
    private String uid;

    @NotBlank
    @Schema(description = "출입 방향 (IN | OUT)", example = "IN")
    private String direction;

    @Schema(description = "태그 시각 (null이면 서버 시각 사용)", example = "2026-04-25T09:00:00")
    private LocalDateTime taggedAt;
}
