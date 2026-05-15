package com.grown.smartoffice.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자 환경설정. users 와 1:1 — user_id 가 PK 이자 users FK (DB 제약으로 강제).
 * userId 는 애플리케이션이 할당한다 (users.user_id 와 동일 값).
 */
@Entity
@Table(name = "user_preferences")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class UserPreferences {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "notifications_enabled", nullable = false)
    private boolean notificationsEnabled;

    @Column(name = "language", nullable = false, length = 10)
    private String language;

    @Column(name = "theme", nullable = false, length = 10)
    private String theme;

    @Column(name = "push_token", length = 255)
    private String pushToken;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public UserPreferences(Long userId, boolean notificationsEnabled,
                           String language, String theme, String pushToken) {
        this.userId = userId;
        this.notificationsEnabled = notificationsEnabled;
        this.language = language;
        this.theme = theme;
        this.pushToken = pushToken;
    }

    /** 사용자 기본 환경설정 — 알림 on, 한국어, 라이트 테마. */
    public static UserPreferences defaults(Long userId) {
        return UserPreferences.builder()
                .userId(userId)
                .notificationsEnabled(true)
                .language("ko")
                .theme("light")
                .pushToken(null)
                .build();
    }

    public void update(Boolean notificationsEnabled, String language,
                       String theme, String pushToken) {
        if (notificationsEnabled != null) this.notificationsEnabled = notificationsEnabled;
        if (language != null) this.language = language;
        if (theme != null) this.theme = theme;
        if (pushToken != null) this.pushToken = pushToken;
    }
}
