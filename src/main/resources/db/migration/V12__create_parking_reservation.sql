-- ============================================================
-- SmartOffice — V12 parking_reservation 테이블 신설 (#14 옵션 A, 4-b)
-- 생성일: 2026-05-15
-- 목적:
--   차량 주차 예약/입출차 추적 모델. Vehicle(V11) + Zone + ParkingSpot 연결.
--   사전 예약(RESERVED) → 입차(PARKED) → 출차(EXITED) 상태 흐름.
--   spot_id 는 nullable — 예약 시점에 미배정, 입차 시 배정 가능.
-- ============================================================

CREATE TABLE `parking_reservation` (
    `reservation_id`     BIGINT      NOT NULL AUTO_INCREMENT,
    `vehicle_id`         BIGINT      NOT NULL,
    `zone_id`            BIGINT      NOT NULL,
    `spot_id`            BIGINT      NULL,
    `reserved_at`        DATETIME    NOT NULL,
    `entry_at`           DATETIME    NULL,
    `exit_at`            DATETIME    NULL,
    `reservation_status` VARCHAR(10) NOT NULL DEFAULT 'RESERVED',
    `created_at`         DATETIME    NOT NULL DEFAULT NOW(),
    `updated_at`         DATETIME    NOT NULL DEFAULT NOW(),
    CONSTRAINT PK_PARKING_RESERVATION PRIMARY KEY (`reservation_id`),
    CONSTRAINT FK_VEHICLE_TO_PARKING_RESERVATION FOREIGN KEY (`vehicle_id`) REFERENCES `vehicle` (`vehicle_id`),
    CONSTRAINT FK_ZONES_TO_PARKING_RESERVATION FOREIGN KEY (`zone_id`) REFERENCES `zones` (`zone_id`),
    CONSTRAINT FK_PARKING_SPOTS_TO_PARKING_RESERVATION FOREIGN KEY (`spot_id`) REFERENCES `parking_spots` (`spot_id`)
);

CREATE INDEX idx_parking_reservation_status ON `parking_reservation` (`reservation_status`);
CREATE INDEX idx_parking_reservation_vehicle ON `parking_reservation` (`vehicle_id`);
