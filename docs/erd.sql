-- ============================================================
-- SmartOffice ERD — 최종본
-- 팀: 그로운 | 생성일: 2026-04-09
-- ============================================================
-- [ERDCloud export 기준 수정 사항]
-- 1. zones.zone_parent_id: NOT NULL → NULL (루트 존 INSERT 불가 버그)
-- 2. zones.zone_type: ENUM 따옴표 오타 수정
-- 3. control_commands: cotrol_commanded_by → command_type (오타+의미)
-- 4. control_commands.control_payload: DEFAULT 'PENDING' 제거 (JSON 타입 오류)
-- 5. devices.device_type: ENUM 값 확정
-- 6. 테이블명 user → users (복수형 통일)
-- 7. AUTO_INCREMENT 전체 반영 (ERDCloud 미지원 → 직접 추가)
-- ============================================================

CREATE TABLE `departments` (
                               `dept_id`          BIGINT          NOT NULL AUTO_INCREMENT,
                               `dept_name`        VARCHAR(100)    NOT NULL COMMENT 'UNIQUE',
                               `dept_description` VARCHAR(255)    NULL,
                               `created_at`       DATETIME        NOT NULL DEFAULT NOW(),
                               `updated_at`       DATETIME        NOT NULL DEFAULT NOW(),
                               CONSTRAINT PK_DEPARTMENTS PRIMARY KEY (`dept_id`),
                               CONSTRAINT UQ_DEPARTMENTS_NAME UNIQUE (`dept_name`)
);

CREATE TABLE `users` (
                         `user_id`         BIGINT          NOT NULL AUTO_INCREMENT,
                         `dept_id`         BIGINT          NOT NULL,
                         `employee_number` VARCHAR(20)     NOT NULL COMMENT 'UNIQUE',
                         `employee_name`   VARCHAR(50)     NOT NULL,
                         `employee_email`  VARCHAR(100)    NOT NULL COMMENT 'UNIQUE, 로그인 ID',
                         `password`        VARCHAR(255)    NOT NULL COMMENT 'BCrypt 암호화',
                         `role`            ENUM('ADMIN','USER') NOT NULL DEFAULT 'USER',
                         `position`        VARCHAR(50)     NOT NULL COMMENT 'salary_settings.salset_position과 매핑',
                         `phone`           VARCHAR(20)     NULL,
                         `status`          ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
                         `hired_at`        DATE            NOT NULL,
                         `created_at`      DATETIME        NOT NULL DEFAULT NOW(),
                         `updated_at`      DATETIME        NOT NULL DEFAULT NOW(),
                         CONSTRAINT PK_USERS PRIMARY KEY (`user_id`),
                         CONSTRAINT UQ_USERS_EMPLOYEE_NUMBER UNIQUE (`employee_number`),
                         CONSTRAINT UQ_USERS_EMAIL UNIQUE (`employee_email`),
                         CONSTRAINT FK_DEPARTMENTS_TO_USERS FOREIGN KEY (`dept_id`) REFERENCES `departments` (`dept_id`)
);

CREATE TABLE `refresh_tokens` (
                                  `token_id`   BIGINT          NOT NULL AUTO_INCREMENT,
                                  `user_id`    BIGINT          NOT NULL,
                                  `token`      VARCHAR(500)    NOT NULL COMMENT 'UNIQUE',
                                  `expires_at` DATETIME        NOT NULL,
                                  `created_at` DATETIME        NOT NULL DEFAULT NOW(),
                                  CONSTRAINT PK_REFRESH_TOKENS PRIMARY KEY (`token_id`),
                                  CONSTRAINT UQ_REFRESH_TOKENS_TOKEN UNIQUE (`token`),
                                  CONSTRAINT FK_USERS_TO_REFRESH_TOKENS FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
);

CREATE TABLE `zones` (
                         `zone_id`          BIGINT          NOT NULL AUTO_INCREMENT,
                         `zone_parent_id`   BIGINT          NULL COMMENT '루트 존은 NULL',
                         `zone_name`        VARCHAR(100)    NOT NULL,
                         `zone_type`        ENUM('FLOOR','AREA') NOT NULL,
                         `zone_description` VARCHAR(255)    NULL,
                         `created_at`       DATETIME        NOT NULL DEFAULT NOW(),
                         `updated_at`       DATETIME        NOT NULL DEFAULT NOW(),
                         CONSTRAINT PK_ZONES PRIMARY KEY (`zone_id`),
                         CONSTRAINT FK_ZONES_TO_ZONES FOREIGN KEY (`zone_parent_id`) REFERENCES `zones` (`zone_id`)
);

CREATE TABLE `devices` (
                           `devices_id`    BIGINT          NOT NULL AUTO_INCREMENT,
                           `zone_id`       BIGINT          NOT NULL,
                           `device_name`   VARCHAR(100)    NOT NULL,
                           `device_type`   ENUM(
                        'NFC_READER',
                        'DOOR_LOCK',
                        'DHT22',
                        'MH_Z19C',
                        'BH1750',
                        'POWER_SENSOR',
                        'IR_CONTROLLER',
                        'RELAY',
                        'LED_STRIP'
                    )               NOT NULL,
                           `serial_number` VARCHAR(100)    NULL COMMENT 'UNIQUE',
                           `mqtt_topic`    VARCHAR(255)    NULL,
                           `device_status` ENUM('ACTIVE','INACTIVE','ERROR') NOT NULL DEFAULT 'ACTIVE',
                           `created_at`    DATETIME        NOT NULL DEFAULT NOW(),
                           `updated_at`    DATETIME        NOT NULL DEFAULT NOW(),
                           CONSTRAINT PK_DEVICES PRIMARY KEY (`devices_id`),
                           CONSTRAINT UQ_DEVICES_SERIAL UNIQUE (`serial_number`),
                           CONSTRAINT FK_ZONES_TO_DEVICES FOREIGN KEY (`zone_id`) REFERENCES `zones` (`zone_id`)
);

CREATE TABLE `nfc_cards` (
                             `card_id`    BIGINT          NOT NULL AUTO_INCREMENT,
                             `user_id`    BIGINT          NOT NULL,
                             `card_uid`   VARCHAR(100)    NOT NULL COMMENT 'UNIQUE',
                             `card_type`  ENUM('EMPLOYEE','VISITOR') NOT NULL DEFAULT 'EMPLOYEE',
                             `issued_at`  DATETIME        NOT NULL,
                             `expired_at` DATETIME        NULL,
                             `created_at` DATETIME        NOT NULL DEFAULT NOW(),
                             `updated_at` DATETIME        NOT NULL DEFAULT NOW(),
                             CONSTRAINT PK_NFC_CARDS PRIMARY KEY (`card_id`),
                             CONSTRAINT UQ_NFC_CARDS_UID UNIQUE (`card_uid`),
                             CONSTRAINT FK_USERS_TO_NFC_CARDS FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
);

CREATE TABLE `access_logs` (
                               `access_log_id` BIGINT          NOT NULL AUTO_INCREMENT,
                               `user_id`       BIGINT          NOT NULL,
                               `card_id`       BIGINT          NOT NULL,
                               `devices_id`    BIGINT          NOT NULL,
                               `zone_id`       BIGINT          NOT NULL,
                               `read_uid`      VARCHAR(100)    NOT NULL,
                               `direction`     ENUM('IN','OUT') NOT NULL,
                               `auth_result`   ENUM('APPROVED','DENIED') NOT NULL,
                               `deny_reason`   VARCHAR(255)    NULL,
                               `tagged_at`     DATETIME        NOT NULL,
                               `created_at`    DATETIME        NOT NULL DEFAULT NOW(),
                               CONSTRAINT PK_ACCESS_LOGS PRIMARY KEY (`access_log_id`),
                               CONSTRAINT FK_USERS_TO_ACCESS_LOGS FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
                               CONSTRAINT FK_NFC_CARDS_TO_ACCESS_LOGS FOREIGN KEY (`card_id`) REFERENCES `nfc_cards` (`card_id`),
                               CONSTRAINT FK_DEVICES_TO_ACCESS_LOGS FOREIGN KEY (`devices_id`) REFERENCES `devices` (`devices_id`),
                               CONSTRAINT FK_ZONES_TO_ACCESS_LOGS FOREIGN KEY (`zone_id`) REFERENCES `zones` (`zone_id`)
);

CREATE TABLE `attendance` (
                              `attendance_id`     BIGINT          NOT NULL AUTO_INCREMENT,
                              `user_id`           BIGINT          NOT NULL,
                              `work_date`         DATE            NOT NULL,
                              `check_in`          DATETIME        NULL,
                              `check_out`         DATETIME        NULL,
                              `work_minutes`      INT             NULL,
                              `overtime_minutes`  INT             NULL DEFAULT 0,
                              `attendance_status` ENUM('NORMAL','LATE','EARLY_LEAVE','ABSENT') NOT NULL DEFAULT 'NORMAL',
                              `attendance_note`   VARCHAR(255)    NULL,
                              `created_at`        DATETIME        NOT NULL DEFAULT NOW(),
                              `updated_at`        DATETIME        NOT NULL DEFAULT NOW(),
                              CONSTRAINT PK_ATTENDANCE PRIMARY KEY (`attendance_id`),
                              CONSTRAINT UQ_ATTENDANCE_USER_DATE UNIQUE (`user_id`, `work_date`),
                              CONSTRAINT FK_USERS_TO_ATTENDANCE FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
);

CREATE TABLE `monthly_attendance` (
                                      `monat_id`                  BIGINT  NOT NULL AUTO_INCREMENT,
                                      `user_id`                   BIGINT  NOT NULL,
                                      `monat_year`                INT     NOT NULL,
                                      `monat_month`               INT     NOT NULL,
                                      `monat_total_work_minutes`  INT     NULL DEFAULT 0,
                                      `monat_overtime_minutes`    INT     NULL DEFAULT 0,
                                      `late_count`                INT     NULL DEFAULT 0,
                                      `early_leave_count`         INT     NULL DEFAULT 0,
                                      `absent_count`              INT     NULL DEFAULT 0,
                                      `created_at`                DATETIME NOT NULL DEFAULT NOW(),
                                      `updated_at`                DATETIME NOT NULL DEFAULT NOW(),
                                      CONSTRAINT PK_MONTHLY_ATTENDANCE PRIMARY KEY (`monat_id`),
                                      CONSTRAINT UQ_MONTHLY_ATTENDANCE_USER_YM UNIQUE (`user_id`, `monat_year`, `monat_month`),
                                      CONSTRAINT FK_USERS_TO_MONTHLY_ATTENDANCE FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
);

CREATE TABLE `salary_settings` (
                                   `salset_id`       BIGINT          NOT NULL AUTO_INCREMENT,
                                   `salset_position` VARCHAR(50)     NOT NULL COMMENT 'users.position과 매핑',
                                   `base_salary`     INT             NOT NULL,
                                   `overtime_rate`   DECIMAL(5,2)    NOT NULL DEFAULT 1.5,
                                   `night_rate`      DECIMAL(5,2)    NOT NULL DEFAULT 2.0,
                                   `effective_from`  DATE            NOT NULL,
                                   `effective_to`    DATE            NULL COMMENT 'NULL = 현재 적용 중',
                                   `created_at`      DATETIME        NOT NULL DEFAULT NOW(),
                                   `updated_at`      DATETIME        NOT NULL DEFAULT NOW(),
                                   CONSTRAINT PK_SALARY_SETTINGS PRIMARY KEY (`salset_id`)
);

CREATE TABLE `salary_records` (
                                  `salrec_id`          BIGINT  NOT NULL AUTO_INCREMENT,
                                  `user_id`            BIGINT  NOT NULL,
                                  `monat_id`           BIGINT  NOT NULL,
                                  `salset_id`          BIGINT  NOT NULL,
                                  `salrec_year`        INT     NOT NULL,
                                  `salrec_month`       INT     NOT NULL,
                                  `salrec_base_salary` INT     NOT NULL,
                                  `overtime_pay`       INT     NULL DEFAULT 0,
                                  `total_pay`          INT     NOT NULL,
                                  `salrec_status`      ENUM('DRAFT','CONFIRMED') NOT NULL DEFAULT 'DRAFT',
                                  `created_at`         DATETIME NOT NULL DEFAULT NOW(),
                                  `updated_at`         DATETIME NOT NULL DEFAULT NOW(),
                                  CONSTRAINT PK_SALARY_RECORDS PRIMARY KEY (`salrec_id`),
                                  CONSTRAINT UQ_SALARY_RECORDS_USER_YM UNIQUE (`user_id`, `salrec_year`, `salrec_month`),
                                  CONSTRAINT FK_USERS_TO_SALARY_RECORDS FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
                                  CONSTRAINT FK_MONTHLY_ATTENDANCE_TO_SALARY_RECORDS FOREIGN KEY (`monat_id`) REFERENCES `monthly_attendance` (`monat_id`),
                                  CONSTRAINT FK_SALARY_SETTINGS_TO_SALARY_RECORDS FOREIGN KEY (`salset_id`) REFERENCES `salary_settings` (`salset_id`)
);

CREATE TABLE `reservations` (
                                `reservations_id`         BIGINT          NOT NULL AUTO_INCREMENT,
                                `user_id`                 BIGINT          NOT NULL,
                                `zone_id`                 BIGINT          NOT NULL,
                                `reservations_title`      VARCHAR(200)    NOT NULL,
                                `reservations_start_at`   DATETIME        NOT NULL,
                                `reservations_end_at`     DATETIME        NOT NULL,
                                `reservations_status`     ENUM('CONFIRMED','CANCELLED','NO_SHOW','CHECKED_IN') NOT NULL DEFAULT 'CONFIRMED',
                                `reservations_checked_in_at` DATETIME     NULL,
                                `created_at`              DATETIME        NOT NULL DEFAULT NOW(),
                                `updated_at`              DATETIME        NOT NULL DEFAULT NOW(),
                                CONSTRAINT PK_RESERVATIONS PRIMARY KEY (`reservations_id`),
                                CONSTRAINT FK_USERS_TO_RESERVATIONS FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
                                CONSTRAINT FK_ZONES_TO_RESERVATIONS FOREIGN KEY (`zone_id`) REFERENCES `zones` (`zone_id`)
);

CREATE TABLE `sensor_logs` (
                               `sensor_logs_id` BIGINT          NOT NULL AUTO_INCREMENT,
                               `zone_id`        BIGINT          NOT NULL,
                               `devices_id`     BIGINT          NOT NULL,
                               `sensor_type`    ENUM('TEMPERATURE','HUMIDITY','CO2','LIGHT','POWER') NOT NULL,
                               `sensor_value`   DECIMAL(10,2)   NOT NULL,
                               `sensor_unit`    VARCHAR(20)     NOT NULL,
                               `logged_at`      DATETIME        NOT NULL,
                               `created_at`     DATETIME        NOT NULL DEFAULT NOW(),
                               CONSTRAINT PK_SENSOR_LOGS PRIMARY KEY (`sensor_logs_id`),
                               CONSTRAINT FK_ZONES_TO_SENSOR_LOGS FOREIGN KEY (`zone_id`) REFERENCES `zones` (`zone_id`),
                               CONSTRAINT FK_DEVICES_TO_SENSOR_LOGS FOREIGN KEY (`devices_id`) REFERENCES `devices` (`devices_id`)
);

CREATE TABLE `control_commands` (
                                    `control_id`      BIGINT      NOT NULL AUTO_INCREMENT,
                                    `zone_id`         BIGINT      NOT NULL,
                                    `devices_id`      BIGINT      NOT NULL,
                                    `command_type`    ENUM('LOCK','UNLOCK','LIGHT_ON','LIGHT_OFF','HVAC_ON','HVAC_OFF','BROADCAST') NOT NULL,
                                    `control_payload` JSON        NOT NULL,
                                    `control_status`  ENUM('PENDING','SENT','SUCCESS','FAILED') NOT NULL DEFAULT 'PENDING',
                                    `triggered_at`    DATETIME    NOT NULL,
                                    `created_at`      DATETIME    NOT NULL DEFAULT NOW(),
                                    CONSTRAINT PK_CONTROL_COMMANDS PRIMARY KEY (`control_id`),
                                    CONSTRAINT FK_ZONES_TO_CONTROL_COMMANDS FOREIGN KEY (`zone_id`) REFERENCES `zones` (`zone_id`),
                                    CONSTRAINT FK_DEVICES_TO_CONTROL_COMMANDS FOREIGN KEY (`devices_id`) REFERENCES `devices` (`devices_id`)
);

-- ============================================================
-- V4 추가 테이블: 주차 / 자산 / 전력
-- 추가일: 2026-05-03
-- ============================================================

CREATE TABLE `parking_spots` (
                                 `spot_id`     BIGINT      NOT NULL AUTO_INCREMENT,
                                 `zone_id`     BIGINT      NOT NULL,
                                 `spot_number` VARCHAR(20) NOT NULL,
                                 `spot_type`   ENUM('REGULAR','DISABLED','EV') NOT NULL DEFAULT 'REGULAR',
                                 `device_id`   BIGINT      NULL COMMENT 'UNIQUE — 초음파 센서 1:1 매핑',
                                 `position_x`  INT         NULL,
                                 `position_y`  INT         NULL,
                                 `is_occupied` TINYINT(1)  NOT NULL DEFAULT 0,
                                 `spot_status` ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
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
                          `asset_number`     VARCHAR(50)  NOT NULL COMMENT 'UNIQUE',
                          `asset_name`       VARCHAR(100) NOT NULL,
                          `category`         VARCHAR(50)  NOT NULL,
                          `assigned_user_id` BIGINT       NULL COMMENT '담당자',
                          `description`      VARCHAR(500) NULL,
                          `asset_status`     ENUM('ACTIVE','INACTIVE','LOST') NOT NULL DEFAULT 'ACTIVE',
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
                                 `unit_price`    INT           NOT NULL COMMENT '원/kWh',
                                 `base_fee`      INT           NOT NULL DEFAULT 0,
                                 `power_fee`     INT           NOT NULL,
                                 `total_fee`     INT           NOT NULL,
                                 `calculated_at` DATETIME      NOT NULL DEFAULT NOW(),
                                 CONSTRAINT PK_POWER_BILLING PRIMARY KEY (`billing_id`),
                                 CONSTRAINT UQ_POWER_BILLING_ZONE_YM UNIQUE (`zone_id`, `billing_year`, `billing_month`),
                                 CONSTRAINT FK_ZONES_TO_POWER_BILLING FOREIGN KEY (`zone_id`) REFERENCES `zones` (`zone_id`)
);