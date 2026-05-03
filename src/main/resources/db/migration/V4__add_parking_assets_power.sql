-- ============================================================
-- SmartOffice V4 — 주차 / 자산 / 전력 테이블 추가
-- 생성일: 2026-05-03
-- ============================================================

CREATE TABLE `parking_spots` (
    `spot_id`     BIGINT      NOT NULL AUTO_INCREMENT,
    `zone_id`     BIGINT      NOT NULL,
    `spot_number` VARCHAR(20) NOT NULL,
    `spot_type`   VARCHAR(10) NOT NULL DEFAULT 'REGULAR',
    `device_id`   BIGINT      NULL,
    `position_x`  INT         NULL,
    `position_y`  INT         NULL,
    `is_occupied` TINYINT(1)  NOT NULL DEFAULT 0,
    `spot_status` VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
    `created_at`  DATETIME    NOT NULL DEFAULT NOW(),
    `updated_at`  DATETIME    NOT NULL DEFAULT NOW(),
    CONSTRAINT PK_PARKING_SPOTS PRIMARY KEY (`spot_id`),
    CONSTRAINT UQ_PARKING_SPOTS_NUMBER UNIQUE (`zone_id`, `spot_number`),
    CONSTRAINT UQ_PARKING_SPOTS_DEVICE UNIQUE (`device_id`),
    CONSTRAINT FK_ZONES_TO_PARKING_SPOTS FOREIGN KEY (`zone_id`) REFERENCES `zones` (`zone_id`),
    CONSTRAINT FK_DEVICES_TO_PARKING_SPOTS FOREIGN KEY (`device_id`) REFERENCES `devices` (`devices_id`)
);

CREATE TABLE `assets` (
    `asset_id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `asset_number`     VARCHAR(50)  NOT NULL,
    `asset_name`       VARCHAR(100) NOT NULL,
    `category`         VARCHAR(50)  NOT NULL,
    `assigned_user_id` BIGINT       NULL,
    `description`      VARCHAR(500) NULL,
    `asset_status`     VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE',
    `purchased_at`     DATE         NULL,
    `created_at`       DATETIME     NOT NULL DEFAULT NOW(),
    `updated_at`       DATETIME     NOT NULL DEFAULT NOW(),
    CONSTRAINT PK_ASSETS PRIMARY KEY (`asset_id`),
    CONSTRAINT UQ_ASSETS_NUMBER UNIQUE (`asset_number`),
    CONSTRAINT FK_USERS_TO_ASSETS FOREIGN KEY (`assigned_user_id`) REFERENCES `users` (`user_id`)
);

CREATE TABLE `power_billing` (
    `billing_id`    BIGINT        NOT NULL AUTO_INCREMENT,
    `zone_id`       BIGINT        NOT NULL,
    `billing_year`  INT           NOT NULL,
    `billing_month` INT           NOT NULL,
    `total_kwh`     DECIMAL(10,2) NOT NULL,
    `unit_price`    INT           NOT NULL,
    `base_fee`      INT           NOT NULL DEFAULT 0,
    `power_fee`     INT           NOT NULL,
    `total_fee`     INT           NOT NULL,
    `calculated_at` DATETIME      NOT NULL DEFAULT NOW(),
    CONSTRAINT PK_POWER_BILLING PRIMARY KEY (`billing_id`),
    CONSTRAINT UQ_POWER_BILLING_ZONE_YM UNIQUE (`zone_id`, `billing_year`, `billing_month`),
    CONSTRAINT FK_ZONES_TO_POWER_BILLING FOREIGN KEY (`zone_id`) REFERENCES `zones` (`zone_id`)
);

CREATE INDEX idx_parking_spots_zone_occupied ON `parking_spots` (`zone_id`, `is_occupied`);
CREATE INDEX idx_assets_status ON `assets` (`asset_status`);
