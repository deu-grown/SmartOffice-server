package com.grown.smartoffice.domain.user.dto;

import com.grown.smartoffice.domain.user.entity.UserPreferences;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "사용자 환경설정 응답")
public class UserPreferencesResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "알림 수신 여부", example = "true")
    private boolean notificationsEnabled;

    @Schema(description = "언어 (ko | en)", example = "ko")
    private String language;

    @Schema(description = "테마 (light | dark)", example = "light")
    private String theme;

    @Schema(description = "푸시 알림 토큰", example = "fcm-token-abc123")
    private String pushToken;

    @Schema(description = "수정 시각")
    private LocalDateTime updatedAt;

    public static UserPreferencesResponse from(UserPreferences p) {
        return UserPreferencesResponse.builder()
                .userId(p.getUserId())
                .notificationsEnabled(p.isNotificationsEnabled())
                .language(p.getLanguage())
                .theme(p.getTheme())
                .pushToken(p.getPushToken())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
