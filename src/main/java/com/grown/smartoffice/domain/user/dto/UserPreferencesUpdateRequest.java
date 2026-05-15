package com.grown.smartoffice.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 환경설정 수정 요청. 모든 필드 선택 — null 인 필드는 기존 값 유지 (부분 수정).
 */
@Getter
@NoArgsConstructor
public class UserPreferencesUpdateRequest {

    @Schema(description = "알림 수신 여부", example = "false")
    private Boolean notificationsEnabled;

    @Size(max = 10)
    @Schema(description = "언어 (ko | en)", example = "en")
    private String language;

    @Size(max = 10)
    @Schema(description = "테마 (light | dark)", example = "dark")
    private String theme;

    @Size(max = 255)
    @Schema(description = "푸시 알림 토큰", example = "fcm-token-abc123")
    private String pushToken;
}
