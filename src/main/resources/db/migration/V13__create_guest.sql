-- ============================================================
-- SmartOffice — V13 guests 테이블 신설 (#1 guest 도메인)
-- 생성일: 2026-05-15
-- 목적:
--   방문객 관리 모델. reservations(직원 회의실 예약)는 외부인 식별 필드가
--   부족하여 별도 도메인으로 분리. 회사·연락처·호스트·방문 목적 + 체크인/아웃 추적.
--   host_user_id 는 방문 대상 임직원 (미지정 가능 — nullable).
--   상태 흐름: SCHEDULED(예약) → VISITING(방문 중) → COMPLETED(종료), CANCELLED(취소).
-- ============================================================

CREATE TABLE `guests` (
    `guest_id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `guest_name`         VARCHAR(50)  NOT NULL,
    `company`            VARCHAR(100) NULL,
    `host_user_id`       BIGINT       NULL,
    `purpose`            VARCHAR(200) NULL,
    `contact_phone`      VARCHAR(20)  NULL,
    `guest_status`       VARCHAR(15)  NOT NULL DEFAULT 'SCHEDULED',
    `scheduled_entry_at` DATETIME     NOT NULL,
    `actual_entry_at`    DATETIME     NULL,
    `actual_exit_at`     DATETIME     NULL,
    `created_at`         DATETIME     NOT NULL DEFAULT NOW(),
    `updated_at`         DATETIME     NOT NULL DEFAULT NOW(),
    CONSTRAINT PK_GUEST PRIMARY KEY (`guest_id`),
    CONSTRAINT FK_USERS_TO_GUEST FOREIGN KEY (`host_user_id`) REFERENCES `users` (`user_id`)
);

CREATE INDEX idx_guest_status ON `guests` (`guest_status`);
CREATE INDEX idx_guest_host ON `guests` (`host_user_id`);
