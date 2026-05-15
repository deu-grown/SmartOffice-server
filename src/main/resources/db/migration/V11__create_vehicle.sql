-- ============================================================
-- SmartOffice — V11 vehicle 테이블 신설 (#14 옵션 A, 4-a)
-- 생성일: 2026-05-15
-- 목적:
--   주차 차량 대장 모델. ParkingSpot 만으로는 "누가 점유했는지" 추적 불가.
--   임직원(STAFF) / 방문객(VISITOR) 차량 식별 + 소유자 + 방문 목적 보존.
--   owner_user_id 는 임직원 차량일 때만 users FK 연결 (방문객은 null).
-- ============================================================

CREATE TABLE `vehicle` (
    `vehicle_id`    BIGINT       NOT NULL AUTO_INCREMENT,
    `plate_number`  VARCHAR(20)  NOT NULL,
    `owner_name`    VARCHAR(50)  NOT NULL,
    `owner_user_id` BIGINT       NULL,
    `vehicle_type`  VARCHAR(10)  NOT NULL DEFAULT 'VISITOR',
    `purpose`       VARCHAR(200) NULL,
    `created_at`    DATETIME     NOT NULL DEFAULT NOW(),
    `updated_at`    DATETIME     NOT NULL DEFAULT NOW(),
    CONSTRAINT PK_VEHICLE PRIMARY KEY (`vehicle_id`),
    CONSTRAINT UQ_VEHICLE_PLATE UNIQUE (`plate_number`),
    CONSTRAINT FK_USERS_TO_VEHICLE FOREIGN KEY (`owner_user_id`) REFERENCES `users` (`user_id`)
);

CREATE INDEX idx_vehicle_type ON `vehicle` (`vehicle_type`);
