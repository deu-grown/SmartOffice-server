-- ============================================================
-- SmartOffice — V14 user_preferences 테이블 신설 (#3 사용자 환경설정)
-- 생성일: 2026-05-15
-- 목적:
--   사용자별 환경설정(알림 on/off · 언어 · 테마 · 푸시 토큰) 저장.
--   user_id 는 users 와 1:1 — PK 이자 FK (shared primary key).
--   설정 행은 최초 조회/수정 시점에 기본값으로 lazy 생성된다.
-- ============================================================

CREATE TABLE `user_preferences` (
    `user_id`               BIGINT       NOT NULL,
    `notifications_enabled` BOOLEAN      NOT NULL DEFAULT TRUE,
    `language`              VARCHAR(10)  NOT NULL DEFAULT 'ko',
    `theme`                 VARCHAR(10)  NOT NULL DEFAULT 'light',
    `push_token`            VARCHAR(255) NULL,
    `created_at`            DATETIME     NOT NULL DEFAULT NOW(),
    `updated_at`            DATETIME     NOT NULL DEFAULT NOW(),
    CONSTRAINT PK_USER_PREFERENCES PRIMARY KEY (`user_id`),
    CONSTRAINT FK_USERS_TO_USER_PREFERENCES FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
);
