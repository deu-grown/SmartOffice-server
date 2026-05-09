-- ============================================================
-- SmartOffice — V7 전력·예약 도메인 테스트 시드 데이터
-- 생성일: 2026-05-09
-- 목적: 전력 관리(5 API) + 예약 관리(8 API) 전 케이스 테스트 지원
--   ① POWER 장치       : devices_id 7~10 (구역 2·4·5·7)
--   ② 실시간 전력 로그  : 최근 10분~6시간 (GET current 테스트)
--   ③ 시간별 이력 로그  : 어제·그저께 시간대별 (GET hourly 테스트)
--   ④ 요금 산출 로그    : 2026년 4월 고정 데이터 (POST billing/calculate)
--   ⑤ 전력 청구 레코드  : zone_id 4·5·7 × 3개월 (GET billing 테스트)
--   ⑥ 예약 12건         : CONFIRMED(6) · CHECKED_IN(2) · CANCELLED(2) · NO_SHOW(2)
-- ============================================================

-- ═══════════════════════════════════════════════════════════════
-- ① POWER 타입 장치 (devices_id 7~10)
--    V5 기준 devices_id 1~6 사용 중
-- ═══════════════════════════════════════════════════════════════
INSERT IGNORE INTO devices (devices_id, zone_id, device_name, device_type, serial_number,
                            mqtt_topic, device_status, created_at, updated_at)
VALUES
    (7,  2, '회의실A 전력미터', 'POWER_METER', 'SN-PWR-001', 'smartoffice/2/power', 'ACTIVE', NOW(), NOW()),
    (8,  4, '회의실B 전력미터', 'POWER_METER', 'SN-PWR-002', 'smartoffice/4/power', 'ACTIVE', NOW(), NOW()),
    (9,  5, '개발팀 전력미터',  'POWER_METER', 'SN-PWR-003', 'smartoffice/5/power', 'ACTIVE', NOW(), NOW()),
    (10, 7, '서버실 전력미터',  'POWER_METER', 'SN-PWR-004', 'smartoffice/7/power', 'ACTIVE', NOW(), NOW());


-- ═══════════════════════════════════════════════════════════════
-- ② 센서 로그 — 실시간 전력 조회용 (최근 10분~6시간)
--    GET /api/v1/power/zones/{zoneId}/current
--    findLatestPowerByZoneId: 장치별 MAX(logged_at) 1건 반환
-- ═══════════════════════════════════════════════════════════════
INSERT IGNORE INTO sensor_logs (zone_id, devices_id, sensor_type, sensor_value, sensor_unit,
                                logged_at, created_at)
VALUES
    -- 회의실A (zone_id=2, devices_id=7): 3시점 → 최신 1건이 current
    (2, 7, 'POWER', 1850.00, 'W', NOW() - INTERVAL 3  HOUR,   NOW()),
    (2, 7, 'POWER', 1920.00, 'W', NOW() - INTERVAL 1  HOUR,   NOW()),
    (2, 7, 'POWER', 2100.00, 'W', NOW() - INTERVAL 20 MINUTE, NOW()),

    -- 회의실B (zone_id=4, devices_id=8): 3시점
    (4, 8, 'POWER', 1500.00, 'W', NOW() - INTERVAL 4  HOUR,   NOW()),
    (4, 8, 'POWER', 1650.00, 'W', NOW() - INTERVAL 2  HOUR,   NOW()),
    (4, 8, 'POWER', 1700.00, 'W', NOW() - INTERVAL 15 MINUTE, NOW()),

    -- 개발팀 좌석 (zone_id=5, devices_id=9): 3시점
    (5, 9, 'POWER', 3200.00, 'W', NOW() - INTERVAL 5  HOUR,   NOW()),
    (5, 9, 'POWER', 3350.00, 'W', NOW() - INTERVAL 2  HOUR,   NOW()),
    (5, 9, 'POWER', 3100.00, 'W', NOW() - INTERVAL 30 MINUTE, NOW()),

    -- 서버실 (zone_id=7, devices_id=10): 3시점 (항상 고전력)
    (7, 10, 'POWER', 5500.00, 'W', NOW() - INTERVAL 6  HOUR,   NOW()),
    (7, 10, 'POWER', 5600.00, 'W', NOW() - INTERVAL 3  HOUR,   NOW()),
    (7, 10, 'POWER', 5650.00, 'W', NOW() - INTERVAL 10 MINUTE, NOW());


-- ═══════════════════════════════════════════════════════════════
-- ③ 센서 로그 — 시간별 이력 조회용 (어제·그저께)
--    GET /api/v1/power/zones/{zoneId}/hourly
--    한 시간 버킷 = AVG(watt)/1000 → kWh
-- ═══════════════════════════════════════════════════════════════
INSERT IGNORE INTO sensor_logs (zone_id, devices_id, sensor_type, sensor_value, sensor_unit,
                                logged_at, created_at)
VALUES
    -- 회의실A 어제 시간대별 (devices_id=7)
    (2, 7, 'POWER', 1200.00, 'W', DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL  9 HOUR), NOW()),
    (2, 7, 'POWER', 1850.00, 'W', DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL 10 HOUR), NOW()),
    (2, 7, 'POWER', 2100.00, 'W', DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL 11 HOUR), NOW()),
    (2, 7, 'POWER', 1950.00, 'W', DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL 13 HOUR), NOW()),
    (2, 7, 'POWER', 2250.00, 'W', DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL 14 HOUR), NOW()),
    (2, 7, 'POWER', 1800.00, 'W', DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL 15 HOUR), NOW()),
    (2, 7, 'POWER', 1600.00, 'W', DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL 17 HOUR), NOW()),

    -- 회의실A 그저께 (devices_id=7)
    (2, 7, 'POWER', 1100.00, 'W', DATE_ADD(CURDATE() - INTERVAL 2 DAY, INTERVAL  9 HOUR), NOW()),
    (2, 7, 'POWER', 2000.00, 'W', DATE_ADD(CURDATE() - INTERVAL 2 DAY, INTERVAL 10 HOUR), NOW()),
    (2, 7, 'POWER', 1950.00, 'W', DATE_ADD(CURDATE() - INTERVAL 2 DAY, INTERVAL 14 HOUR), NOW()),
    (2, 7, 'POWER', 1700.00, 'W', DATE_ADD(CURDATE() - INTERVAL 2 DAY, INTERVAL 15 HOUR), NOW()),

    -- 회의실B 어제 (devices_id=8)
    (4, 8, 'POWER', 1300.00, 'W', DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL  9 HOUR), NOW()),
    (4, 8, 'POWER', 1500.00, 'W', DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL 10 HOUR), NOW()),
    (4, 8, 'POWER', 1800.00, 'W', DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL 13 HOUR), NOW()),
    (4, 8, 'POWER', 1650.00, 'W', DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL 14 HOUR), NOW()),
    (4, 8, 'POWER', 1400.00, 'W', DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL 16 HOUR), NOW()),

    -- 회의실B 그저께 (devices_id=8)
    (4, 8, 'POWER', 1200.00, 'W', DATE_ADD(CURDATE() - INTERVAL 2 DAY, INTERVAL 10 HOUR), NOW()),
    (4, 8, 'POWER', 1600.00, 'W', DATE_ADD(CURDATE() - INTERVAL 2 DAY, INTERVAL 14 HOUR), NOW()),
    (4, 8, 'POWER', 1450.00, 'W', DATE_ADD(CURDATE() - INTERVAL 2 DAY, INTERVAL 15 HOUR), NOW()),

    -- 개발팀 어제 (devices_id=9)
    (5, 9, 'POWER', 3000.00, 'W', DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL  9 HOUR), NOW()),
    (5, 9, 'POWER', 3200.00, 'W', DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL 10 HOUR), NOW()),
    (5, 9, 'POWER', 3500.00, 'W', DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL 14 HOUR), NOW()),
    (5, 9, 'POWER', 3100.00, 'W', DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL 16 HOUR), NOW()),

    -- 개발팀 그저께 (devices_id=9)
    (5, 9, 'POWER', 3400.00, 'W', DATE_ADD(CURDATE() - INTERVAL 2 DAY, INTERVAL 10 HOUR), NOW()),
    (5, 9, 'POWER', 3300.00, 'W', DATE_ADD(CURDATE() - INTERVAL 2 DAY, INTERVAL 15 HOUR), NOW()),

    -- 서버실 어제 (devices_id=10) — 24시간 고전력 운영
    (7, 10, 'POWER', 5400.00, 'W', DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL  9 HOUR), NOW()),
    (7, 10, 'POWER', 5500.00, 'W', DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL 12 HOUR), NOW()),
    (7, 10, 'POWER', 5600.00, 'W', DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL 15 HOUR), NOW()),
    (7, 10, 'POWER', 5450.00, 'W', DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL 18 HOUR), NOW());


-- ═══════════════════════════════════════════════════════════════
-- ④ 센서 로그 — 요금 산출용 (2026년 4월, zone_id 2·4·5·7)
--    POST /api/v1/power/billing/calculate?year=2026&month=4
--    월별 kWh = Σ(시간 버킷별 AVG_W / 1000)
-- ═══════════════════════════════════════════════════════════════
INSERT IGNORE INTO sensor_logs (zone_id, devices_id, sensor_type, sensor_value, sensor_unit,
                                logged_at, created_at)
VALUES
    -- 회의실A 4월 (devices_id=7) ── 5일 × 4~5시간 = 22건
    (2, 7, 'POWER', 1800.00, 'W', '2026-04-01 09:00:00', NOW()),
    (2, 7, 'POWER', 2000.00, 'W', '2026-04-01 10:00:00', NOW()),
    (2, 7, 'POWER', 2100.00, 'W', '2026-04-01 11:00:00', NOW()),
    (2, 7, 'POWER', 1900.00, 'W', '2026-04-01 14:00:00', NOW()),
    (2, 7, 'POWER', 1700.00, 'W', '2026-04-01 15:00:00', NOW()),

    (2, 7, 'POWER', 1850.00, 'W', '2026-04-07 09:00:00', NOW()),
    (2, 7, 'POWER', 2050.00, 'W', '2026-04-07 10:00:00', NOW()),
    (2, 7, 'POWER', 2200.00, 'W', '2026-04-07 14:00:00', NOW()),
    (2, 7, 'POWER', 1950.00, 'W', '2026-04-07 15:00:00', NOW()),

    (2, 7, 'POWER', 1700.00, 'W', '2026-04-14 09:00:00', NOW()),
    (2, 7, 'POWER', 1900.00, 'W', '2026-04-14 10:00:00', NOW()),
    (2, 7, 'POWER', 2100.00, 'W', '2026-04-14 14:00:00', NOW()),

    (2, 7, 'POWER', 1950.00, 'W', '2026-04-21 09:00:00', NOW()),
    (2, 7, 'POWER', 2200.00, 'W', '2026-04-21 10:00:00', NOW()),
    (2, 7, 'POWER', 2000.00, 'W', '2026-04-21 15:00:00', NOW()),

    (2, 7, 'POWER', 1800.00, 'W', '2026-04-28 09:00:00', NOW()),
    (2, 7, 'POWER', 1750.00, 'W', '2026-04-28 10:00:00', NOW()),
    (2, 7, 'POWER', 1950.00, 'W', '2026-04-28 14:00:00', NOW()),

    -- 회의실B 4월 (devices_id=8) ── 4일 × 3시간 = 12건
    (4, 8, 'POWER', 1400.00, 'W', '2026-04-02 09:00:00', NOW()),
    (4, 8, 'POWER', 1600.00, 'W', '2026-04-02 10:00:00', NOW()),
    (4, 8, 'POWER', 1500.00, 'W', '2026-04-02 14:00:00', NOW()),

    (4, 8, 'POWER', 1700.00, 'W', '2026-04-10 09:00:00', NOW()),
    (4, 8, 'POWER', 1550.00, 'W', '2026-04-10 10:00:00', NOW()),
    (4, 8, 'POWER', 1450.00, 'W', '2026-04-10 14:00:00', NOW()),

    (4, 8, 'POWER', 1600.00, 'W', '2026-04-20 09:00:00', NOW()),
    (4, 8, 'POWER', 1700.00, 'W', '2026-04-20 10:00:00', NOW()),
    (4, 8, 'POWER', 1500.00, 'W', '2026-04-20 14:00:00', NOW()),

    (4, 8, 'POWER', 1550.00, 'W', '2026-04-25 09:00:00', NOW()),
    (4, 8, 'POWER', 1600.00, 'W', '2026-04-25 14:00:00', NOW()),
    (4, 8, 'POWER', 1650.00, 'W', '2026-04-25 15:00:00', NOW()),

    -- 개발팀 좌석 4월 (devices_id=9) ── 3일 × 3시간 = 9건
    (5, 9, 'POWER', 3100.00, 'W', '2026-04-05 09:00:00', NOW()),
    (5, 9, 'POWER', 3300.00, 'W', '2026-04-05 10:00:00', NOW()),
    (5, 9, 'POWER', 3200.00, 'W', '2026-04-05 14:00:00', NOW()),

    (5, 9, 'POWER', 3400.00, 'W', '2026-04-15 09:00:00', NOW()),
    (5, 9, 'POWER', 3500.00, 'W', '2026-04-15 10:00:00', NOW()),
    (5, 9, 'POWER', 3250.00, 'W', '2026-04-15 14:00:00', NOW()),

    (5, 9, 'POWER', 3150.00, 'W', '2026-04-25 09:00:00', NOW()),
    (5, 9, 'POWER', 3350.00, 'W', '2026-04-25 10:00:00', NOW()),
    (5, 9, 'POWER', 3200.00, 'W', '2026-04-25 14:00:00', NOW()),

    -- 서버실 4월 (devices_id=10) ── 3일 × 3시간 = 9건 (고전력)
    (7, 10, 'POWER', 5500.00, 'W', '2026-04-03 09:00:00', NOW()),
    (7, 10, 'POWER', 5600.00, 'W', '2026-04-03 12:00:00', NOW()),
    (7, 10, 'POWER', 5550.00, 'W', '2026-04-03 18:00:00', NOW()),

    (7, 10, 'POWER', 5600.00, 'W', '2026-04-13 09:00:00', NOW()),
    (7, 10, 'POWER', 5700.00, 'W', '2026-04-13 12:00:00', NOW()),
    (7, 10, 'POWER', 5650.00, 'W', '2026-04-13 18:00:00', NOW()),

    (7, 10, 'POWER', 5500.00, 'W', '2026-04-23 09:00:00', NOW()),
    (7, 10, 'POWER', 5600.00, 'W', '2026-04-23 12:00:00', NOW()),
    (7, 10, 'POWER', 5450.00, 'W', '2026-04-23 15:00:00', NOW());


-- ═══════════════════════════════════════════════════════════════
-- ⑤ 전력 청구 레코드 — 추가 구역 (zone_id 4·5·7 × 3개월)
--    V5에서 zone_id 1·2 × 3개월 등록 완료
--    GET /api/v1/power/billing + GET /api/v1/power/zones/{zoneId}/billing
-- ═══════════════════════════════════════════════════════════════
INSERT IGNORE INTO power_billing (zone_id, billing_year, billing_month,
                                  total_kwh, unit_price, base_fee, power_fee, total_fee,
                                  calculated_at)
VALUES
    -- 회의실B (zone_id=4)
    (4, 2026, 3,  870.50, 150, 6000, 130575, 136575, NOW()),
    (4, 2026, 4,  940.25, 150, 6000, 141037, 147037, NOW()),
    (4, 2026, 5,  810.75, 150, 6000, 121612, 127612, NOW()),

    -- 개발팀 좌석 (zone_id=5)
    (5, 2026, 3, 2100.00, 150, 6000, 315000, 321000, NOW()),
    (5, 2026, 4, 2350.50, 150, 6000, 352575, 358575, NOW()),
    (5, 2026, 5, 2000.25, 150, 6000, 300037, 306037, NOW()),

    -- 서버실 (zone_id=7) — 24시간 가동으로 높은 전력
    (7, 2026, 3, 4200.00, 150, 6000, 630000, 636000, NOW()),
    (7, 2026, 4, 4350.75, 150, 6000, 652612, 658612, NOW()),
    (7, 2026, 5, 4100.50, 150, 6000, 615075, 621075, NOW());


-- ═══════════════════════════════════════════════════════════════
-- ⑥ 예약 (12건 — 모든 상태 포함)
--    구역: zone_id=2(회의실A), zone_id=4(회의실B)
--    같은 구역·같은 날 CONFIRMED 예약은 시간 겹침 없음
-- ═══════════════════════════════════════════════════════════════
INSERT IGNORE INTO reservations (reservations_id, user_id, zone_id,
                                 reservations_title,
                                 reservations_start_at, reservations_end_at,
                                 reservations_status, reservations_checked_in_at,
                                 created_at, updated_at)
VALUES
    -- ── CONFIRMED: 오늘 (2건 회의실A + 1건 회의실B, 시간 비겹침) ──────
    -- 회의실A 10:00~11:00 (관리자/EMP001)
    (1,  1, 2, '주간 팀 스탠드업',
         DATE_ADD(CURDATE(), INTERVAL 10 HOUR),
         DATE_ADD(CURDATE(), INTERVAL 11 HOUR),
         'CONFIRMED', NULL, NOW(), NOW()),

    -- 회의실A 14:00~16:00 (이순신/EMP002) — 위와 시간 겹침 없음
    (2,  2, 2, '고객사 데모 미팅',
         DATE_ADD(CURDATE(), INTERVAL 14 HOUR),
         DATE_ADD(CURDATE(), INTERVAL 16 HOUR),
         'CONFIRMED', NULL, NOW(), NOW()),

    -- 회의실B 13:00~14:30 (장보고/EMP003)
    (3,  3, 4, '코드 리뷰 세션',
         DATE_ADD(CURDATE(), INTERVAL 13 HOUR),
         DATE_ADD(CURDATE(), INTERVAL '14:30' HOUR_MINUTE),
         'CONFIRMED', NULL, NOW(), NOW()),

    -- ── CONFIRMED: 내일 ────────────────────────────────────────────────
    -- 회의실A 10:00~12:00 (세종대왕/EMP004)
    (4,  4, 2, '신규 채용 면접',
         DATE_ADD(CURDATE() + INTERVAL 1 DAY, INTERVAL 10 HOUR),
         DATE_ADD(CURDATE() + INTERVAL 1 DAY, INTERVAL 12 HOUR),
         'CONFIRMED', NULL, NOW(), NOW()),

    -- 회의실B 15:00~17:00 (홍길동/EMP006)
    (5,  6, 4, '프로젝트 킥오프 미팅',
         DATE_ADD(CURDATE() + INTERVAL 1 DAY, INTERVAL 15 HOUR),
         DATE_ADD(CURDATE() + INTERVAL 1 DAY, INTERVAL 17 HOUR),
         'CONFIRMED', NULL, NOW(), NOW()),

    -- ── CONFIRMED: 다음 주 ─────────────────────────────────────────────
    -- 회의실A 09:00~10:00 (이순신/EMP002)
    (6,  2, 2, '분기 보고 준비 회의',
         DATE_ADD(CURDATE() + INTERVAL 7 DAY, INTERVAL  9 HOUR),
         DATE_ADD(CURDATE() + INTERVAL 7 DAY, INTERVAL 10 HOUR),
         'CONFIRMED', NULL, NOW(), NOW()),

    -- ── CHECKED_IN: 어제 (체크인 완료) ────────────────────────────────
    -- 회의실A 09:00~10:00, 체크인 08:58 (관리자/EMP001)
    (7,  1, 2, '스프린트 플래닝',
         DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL  9 HOUR),
         DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL 10 HOUR),
         'CHECKED_IN',
         DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL '8:58' HOUR_MINUTE),
         NOW(), NOW()),

    -- 회의실B 14:00~16:00, 체크인 13:55 (세종대왕/EMP004)
    (8,  4, 4, '기술 세미나',
         DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL 14 HOUR),
         DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL 16 HOUR),
         'CHECKED_IN',
         DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL '13:55' HOUR_MINUTE),
         NOW(), NOW()),

    -- ── CANCELLED: 취소된 예약 ─────────────────────────────────────────
    -- 회의실A 3일 전 10:00~11:00 (장보고/EMP003)
    (9,  3, 2, '외부 협력사 방문 (취소됨)',
         DATE_ADD(CURDATE() - INTERVAL 3 DAY, INTERVAL 10 HOUR),
         DATE_ADD(CURDATE() - INTERVAL 3 DAY, INTERVAL 11 HOUR),
         'CANCELLED', NULL, NOW(), NOW()),

    -- 회의실B 2일 전 14:00~15:00 (홍길동/EMP006)
    (10, 6, 4, '제품 데모 (취소됨)',
         DATE_ADD(CURDATE() - INTERVAL 2 DAY, INTERVAL 14 HOUR),
         DATE_ADD(CURDATE() - INTERVAL 2 DAY, INTERVAL 15 HOUR),
         'CANCELLED', NULL, NOW(), NOW()),

    -- ── NO_SHOW: 체크인 없이 종료 ─────────────────────────────────────
    -- 회의실A 5일 전 09:00~10:00 (이순신/EMP002)
    (11, 2, 2, '채용 인터뷰 (노쇼)',
         DATE_ADD(CURDATE() - INTERVAL 5 DAY, INTERVAL 9 HOUR),
         DATE_ADD(CURDATE() - INTERVAL 5 DAY, INTERVAL 10 HOUR),
         'NO_SHOW', NULL, NOW(), NOW()),

    -- 회의실B 4일 전 13:00~14:00 (장보고/EMP003)
    (12, 3, 4, '협력사 미팅 (노쇼)',
         DATE_ADD(CURDATE() - INTERVAL 4 DAY, INTERVAL 13 HOUR),
         DATE_ADD(CURDATE() - INTERVAL 4 DAY, INTERVAL 14 HOUR),
         'NO_SHOW', NULL, NOW(), NOW());
