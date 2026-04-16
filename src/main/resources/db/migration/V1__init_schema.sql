-- ============================================================
-- SmartOffice ERD — V1 초기 스키마
-- 팀: 그로운 | 생성일: 2026-04-09
-- ============================================================

CREATE TABLE `departments` (
    `dept_id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `dept_name`        VARCHAR(100) NOT NULL,
    `dept_description` VARCHAR(255) NULL,
    `created_at`       DATETIME     NOT NULL DEFAULT NOW(),
    `updated_at`       DATETIME     NOT NULL DEFAULT NOW(),
    CONSTRAINT PK_DEPARTMENTS PRIMARY KEY (`dept_id`),
    CONSTRAINT UQ_DEPARTMENTS_NAME UNIQUE (`dept_name`)
);

CREATE TABLE `users` (
    `user_id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `dept_id`         BIGINT       NOT NULL,
    `employee_number` VARCHAR(20)  NOT NULL,
    `employee_name`   VARCHAR(50)  NOT NULL,
    `employee_email`  VARCHAR(100) NOT NULL,
    `password`        VARCHAR(255) NOT NULL,
    `role`            VARCHAR(10)  NOT NULL DEFAULT 'USER',
    `position`        VARCHAR(50)  NOT NULL,
    `phone`           VARCHAR(20)  NULL,
    `status`          VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE',
    `hired_at`        DATE         NOT NULL,
    `created_at`      DATETIME     NOT NULL DEFAULT NOW(),
    `updated_at`      DATETIME     NOT NULL DEFAULT NOW(),
    CONSTRAINT PK_USERS PRIMARY KEY (`user_id`),
    CONSTRAINT UQ_USERS_EMPLOYEE_NUMBER UNIQUE (`employee_number`),
    CONSTRAINT UQ_USERS_EMAIL UNIQUE (`employee_email`),
    CONSTRAINT FK_DEPARTMENTS_TO_USERS FOREIGN KEY (`dept_id`) REFERENCES `departments` (`dept_id`)
);

CREATE TABLE `refresh_tokens` (
    `token_id`   BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT       NOT NULL,
    `token`      VARCHAR(500) NOT NULL,
    `expires_at` DATETIME     NOT NULL,
    `created_at` DATETIME     NOT NULL DEFAULT NOW(),
    CONSTRAINT PK_REFRESH_TOKENS PRIMARY KEY (`token_id`),
    CONSTRAINT UQ_REFRESH_TOKENS_TOKEN UNIQUE (`token`),
    CONSTRAINT FK_USERS_TO_REFRESH_TOKENS FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
);

CREATE TABLE `zones` (
    `zone_id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `zone_parent_id`   BIGINT       NULL,
    `zone_name`        VARCHAR(100) NOT NULL,
    `zone_type`        VARCHAR(10)  NOT NULL,
    `zone_description` VARCHAR(255) NULL,
    `created_at`       DATETIME     NOT NULL DEFAULT NOW(),
    `updated_at`       DATETIME     NOT NULL DEFAULT NOW(),
    CONSTRAINT PK_ZONES PRIMARY KEY (`zone_id`),
    CONSTRAINT FK_ZONES_TO_ZONES FOREIGN KEY (`zone_parent_id`) REFERENCES `zones` (`zone_id`)
);

CREATE TABLE `devices` (
    `devices_id`    BIGINT       NOT NULL AUTO_INCREMENT,
    `zone_id`       BIGINT       NOT NULL,
    `device_name`   VARCHAR(100) NOT NULL,
    `device_type`   VARCHAR(20)  NOT NULL,
    `serial_number` VARCHAR(100) NULL,
    `mqtt_topic`    VARCHAR(255) NULL,
    `device_status` VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE',
    `created_at`    DATETIME     NOT NULL DEFAULT NOW(),
    `updated_at`    DATETIME     NOT NULL DEFAULT NOW(),
    CONSTRAINT PK_DEVICES PRIMARY KEY (`devices_id`),
    CONSTRAINT UQ_DEVICES_SERIAL UNIQUE (`serial_number`),
    CONSTRAINT FK_ZONES_TO_DEVICES FOREIGN KEY (`zone_id`) REFERENCES `zones` (`zone_id`)
);

CREATE TABLE `nfc_cards` (
    `card_id`    BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT       NOT NULL,
    `card_uid`   VARCHAR(100) NOT NULL,
    `card_type`  VARCHAR(10)  NOT NULL DEFAULT 'EMPLOYEE',
    `issued_at`  DATETIME     NOT NULL,
    `expired_at` DATETIME     NULL,
    `created_at` DATETIME     NOT NULL DEFAULT NOW(),
    `updated_at` DATETIME     NOT NULL DEFAULT NOW(),
    CONSTRAINT PK_NFC_CARDS PRIMARY KEY (`card_id`),
    CONSTRAINT UQ_NFC_CARDS_UID UNIQUE (`card_uid`),
    CONSTRAINT FK_USERS_TO_NFC_CARDS FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
);

CREATE TABLE `access_logs` (
    `access_log_id` BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT       NOT NULL,
    `card_id`       BIGINT       NOT NULL,
    `devices_id`    BIGINT       NOT NULL,
    `zone_id`       BIGINT       NOT NULL,
    `read_uid`      VARCHAR(100) NOT NULL,
    `direction`     VARCHAR(5)   NOT NULL,
    `auth_result`   VARCHAR(10)  NOT NULL,
    `deny_reason`   VARCHAR(255) NULL,
    `tagged_at`     DATETIME     NOT NULL,
    `created_at`    DATETIME     NOT NULL DEFAULT NOW(),
    CONSTRAINT PK_ACCESS_LOGS PRIMARY KEY (`access_log_id`),
    CONSTRAINT FK_USERS_TO_ACCESS_LOGS FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
    CONSTRAINT FK_NFC_CARDS_TO_ACCESS_LOGS FOREIGN KEY (`card_id`) REFERENCES `nfc_cards` (`card_id`),
    CONSTRAINT FK_DEVICES_TO_ACCESS_LOGS FOREIGN KEY (`devices_id`) REFERENCES `devices` (`devices_id`),
    CONSTRAINT FK_ZONES_TO_ACCESS_LOGS FOREIGN KEY (`zone_id`) REFERENCES `zones` (`zone_id`)
);

-- users 테이블 인덱스 (status 필터, 부서+status 복합 조회 최적화)
CREATE INDEX idx_users_status ON `users` (`status`);
CREATE INDEX idx_users_dept_status ON `users` (`dept_id`, `status`);

CREATE TABLE `attendance` (
    `attendance_id`     BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`           BIGINT       NOT NULL,
    `work_date`         DATE         NOT NULL,
    `check_in`          DATETIME     NULL,
    `check_out`         DATETIME     NULL,
    `work_minutes`      INT          NULL,
    `overtime_minutes`  INT          NULL DEFAULT 0,
    `attendance_status` VARCHAR(15)  NOT NULL DEFAULT 'NORMAL',
    `attendance_note`   VARCHAR(255) NULL,
    `created_at`        DATETIME     NOT NULL DEFAULT NOW(),
    `updated_at`        DATETIME     NOT NULL DEFAULT NOW(),
    CONSTRAINT PK_ATTENDANCE PRIMARY KEY (`attendance_id`),
    CONSTRAINT UQ_ATTENDANCE_USER_DATE UNIQUE (`user_id`, `work_date`),
    CONSTRAINT FK_USERS_TO_ATTENDANCE FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
);

CREATE TABLE `monthly_attendance` (
    `monat_id`                 BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`                  BIGINT   NOT NULL,
    `monat_year`               INT      NOT NULL,
    `monat_month`              INT      NOT NULL,
    `monat_total_work_minutes` INT      NULL DEFAULT 0,
    `monat_overtime_minutes`   INT      NULL DEFAULT 0,
    `late_count`               INT      NULL DEFAULT 0,
    `early_leave_count`        INT      NULL DEFAULT 0,
    `absent_count`             INT      NULL DEFAULT 0,
    `created_at`               DATETIME NOT NULL DEFAULT NOW(),
    `updated_at`               DATETIME NOT NULL DEFAULT NOW(),
    CONSTRAINT PK_MONTHLY_ATTENDANCE PRIMARY KEY (`monat_id`),
    CONSTRAINT UQ_MONTHLY_ATTENDANCE_USER_YM UNIQUE (`user_id`, `monat_year`, `monat_month`),
    CONSTRAINT FK_USERS_TO_MONTHLY_ATTENDANCE FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
);

CREATE TABLE `salary_settings` (
    `salset_id`       BIGINT         NOT NULL AUTO_INCREMENT,
    `salset_position` VARCHAR(50)    NOT NULL,
    `base_salary`     INT            NOT NULL,
    `overtime_rate`   DECIMAL(5, 2)  NOT NULL DEFAULT 1.5,
    `night_rate`      DECIMAL(5, 2)  NOT NULL DEFAULT 2.0,
    `effective_from`  DATE           NOT NULL,
    `effective_to`    DATE           NULL,
    `created_at`      DATETIME       NOT NULL DEFAULT NOW(),
    `updated_at`      DATETIME       NOT NULL DEFAULT NOW(),
    CONSTRAINT PK_SALARY_SETTINGS PRIMARY KEY (`salset_id`)
);

CREATE TABLE `salary_records` (
    `salrec_id`          BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`            BIGINT   NOT NULL,
    `monat_id`           BIGINT   NOT NULL,
    `salset_id`          BIGINT   NOT NULL,
    `salrec_year`        INT      NOT NULL,
    `salrec_month`       INT      NOT NULL,
    `salrec_base_salary` INT      NOT NULL,
    `overtime_pay`       INT      NULL DEFAULT 0,
    `total_pay`          INT      NOT NULL,
    `salrec_status`      VARCHAR(10) NOT NULL DEFAULT 'DRAFT',
    `created_at`         DATETIME NOT NULL DEFAULT NOW(),
    `updated_at`         DATETIME NOT NULL DEFAULT NOW(),
    CONSTRAINT PK_SALARY_RECORDS PRIMARY KEY (`salrec_id`),
    CONSTRAINT UQ_SALARY_RECORDS_USER_YM UNIQUE (`user_id`, `salrec_year`, `salrec_month`),
    CONSTRAINT FK_USERS_TO_SALARY_RECORDS FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
    CONSTRAINT FK_MONTHLY_ATTENDANCE_TO_SALARY_RECORDS FOREIGN KEY (`monat_id`) REFERENCES `monthly_attendance` (`monat_id`),
    CONSTRAINT FK_SALARY_SETTINGS_TO_SALARY_RECORDS FOREIGN KEY (`salset_id`) REFERENCES `salary_settings` (`salset_id`)
);

CREATE TABLE `reservations` (
    `reservations_id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`                    BIGINT       NOT NULL,
    `zone_id`                    BIGINT       NOT NULL,
    `reservations_title`         VARCHAR(200) NOT NULL,
    `reservations_start_at`      DATETIME     NOT NULL,
    `reservations_end_at`        DATETIME     NOT NULL,
    `reservations_status`        VARCHAR(15)  NOT NULL DEFAULT 'CONFIRMED',
    `reservations_checked_in_at` DATETIME     NULL,
    `created_at`                 DATETIME     NOT NULL DEFAULT NOW(),
    `updated_at`                 DATETIME     NOT NULL DEFAULT NOW(),
    CONSTRAINT PK_RESERVATIONS PRIMARY KEY (`reservations_id`),
    CONSTRAINT FK_USERS_TO_RESERVATIONS FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
    CONSTRAINT FK_ZONES_TO_RESERVATIONS FOREIGN KEY (`zone_id`) REFERENCES `zones` (`zone_id`)
);

CREATE TABLE `sensor_logs` (
    `sensor_logs_id` BIGINT        NOT NULL AUTO_INCREMENT,
    `zone_id`        BIGINT        NOT NULL,
    `devices_id`     BIGINT        NOT NULL,
    `sensor_type`    VARCHAR(15)   NOT NULL,
    `sensor_value`   DECIMAL(10,2) NOT NULL,
    `sensor_unit`    VARCHAR(20)   NOT NULL,
    `logged_at`      DATETIME      NOT NULL,
    `created_at`     DATETIME      NOT NULL DEFAULT NOW(),
    CONSTRAINT PK_SENSOR_LOGS PRIMARY KEY (`sensor_logs_id`),
    CONSTRAINT FK_ZONES_TO_SENSOR_LOGS FOREIGN KEY (`zone_id`) REFERENCES `zones` (`zone_id`),
    CONSTRAINT FK_DEVICES_TO_SENSOR_LOGS FOREIGN KEY (`devices_id`) REFERENCES `devices` (`devices_id`)
);

-- attendance 조회 최적화 (user_id + work_date 복합 인덱스)
CREATE INDEX idx_attendance_user_date ON `attendance` (`user_id`, `work_date`);

CREATE TABLE `control_commands` (
    `control_id`      BIGINT      NOT NULL AUTO_INCREMENT,
    `zone_id`         BIGINT      NOT NULL,
    `devices_id`      BIGINT      NOT NULL,
    `command_type`    VARCHAR(15) NOT NULL,
    `control_payload` TEXT        NOT NULL,
    `control_status`  VARCHAR(10) NOT NULL DEFAULT 'PENDING',
    `triggered_at`    DATETIME    NOT NULL,
    `created_at`      DATETIME    NOT NULL DEFAULT NOW(),
    CONSTRAINT PK_CONTROL_COMMANDS PRIMARY KEY (`control_id`),
    CONSTRAINT FK_ZONES_TO_CONTROL_COMMANDS FOREIGN KEY (`zone_id`) REFERENCES `zones` (`zone_id`),
    CONSTRAINT FK_DEVICES_TO_CONTROL_COMMANDS FOREIGN KEY (`devices_id`) REFERENCES `devices` (`devices_id`)
);
