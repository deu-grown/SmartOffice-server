-- ============================================================
-- SmartOffice — V8 시연용 풍부한 더미데이터 시드
-- 생성일: 2026-05-13
-- 목적:
--   1) 머지된 device 출입 로그 조회 API(전체/사용자별/내) 페이지네이션·필터 검증
--   2) 신규 parking 도메인 7 API 시연 데이터 보강
--   3) 기존 도메인 중 enum 값 일부만 커버 / 0건 상태 보강
--      (control_commands, ZoneType.ROOM, DeviceStatus.INACTIVE,
--       NfcCardStatus.LOST/INACTIVE, access_logs DENIED/BLOCKED 등)
--
-- ID 네임스페이스 (V2/V3/V5/V7 충돌 회피):
--   departments    : 5~  / users           : 7~  / zones        : 9~
--   devices        : 11~ / nfc_cards       : 7~  / assets       : 11~
--   parking_spots  : 6~  / salary_settings : 6~  / reservations : 13~
--
-- 비밀번호: 각 사번과 동일 (BCrypt cost=10)
--   EMP007: $2a$10$kSERe3/B.GT4XwWVhR1aJeg1LN7RJpmb8aWO.x5rG752fQyNS9ZH6  (= EMP002 해시 재사용 → 사번 EMP002와 동일 비번)
--   ※ 실 시연용 — 보안성보다 일관된 사번=비번 정책. V5의 5명 해시를 그대로 재사용.
-- ============================================================

-- ═══════════════════════════════════════════════════════════════
-- ⓪ V5 시드 정정 — parking_spots P-003 spot_type을 'RESERVED'(엔티티 미정의)
--    → 'REGULAR'로 보정. ERD/SpotType enum과 정합 맞춤.
-- ═══════════════════════════════════════════════════════════════
UPDATE `parking_spots` SET `spot_type` = 'REGULAR'
WHERE `spot_id` = 3 AND `spot_type` = 'RESERVED';


-- ═══════════════════════════════════════════════════════════════
-- ① 부서 보강 (dept_id 5~6)
-- ═══════════════════════════════════════════════════════════════
INSERT IGNORE INTO departments (dept_id, dept_name, dept_description, created_at, updated_at)
VALUES
    (5, '디자인팀', 'UX/UI 디자인 및 브랜드',     NOW(), NOW()),
    (6, '인사팀',   '채용·인사·복지 전반',         NOW(), NOW());


-- ═══════════════════════════════════════════════════════════════
-- ② 직원 보강 (user_id 7~12)
--   비밀번호 해시: V5의 EMP002~EMP006 해시 재사용 (사번=비번 정책)
--     EMP007=EMP002해시 / EMP008=EMP003해시 / EMP009=EMP004해시
--     EMP010=EMP005해시 / EMP011=EMP006해시 / EMP012=EMP002해시
-- ═══════════════════════════════════════════════════════════════
INSERT IGNORE INTO users (user_id, dept_id, employee_number, employee_name, employee_email,
                          password, role, position, phone, status, hired_at, created_at, updated_at)
VALUES
    (7,  5, 'EMP007', '신사임당',  'shin.sa@grown.com',
     '$2a$10$kSERe3/B.GT4XwWVhR1aJeg1LN7RJpmb8aWO.x5rG752fQyNS9ZH6',
     'USER', '디자이너',  '010-6666-6666', 'ACTIVE',   '2025-08-01', NOW(), NOW()),

    (8,  5, 'EMP008', '윤동주',    'yoon.dj@grown.com',
     '$2a$10$XnjooudZGHV.VtpW/DUmXuGuytB.Zoj/e0F9jTPKjkWDYkH5/SrRq',
     'USER', '디자이너',  '010-7777-7777', 'ACTIVE',   '2025-09-15', NOW(), NOW()),

    (9,  6, 'EMP009', '김유신',    'kim.ys@grown.com',
     '$2a$10$rcIFJR/sgRB51MjzuGSUb.S5nfG9/6PHMD1AZHSyuBa5kqiFaKMwG',
     'USER', '인사담당',  '010-8888-8888', 'ACTIVE',   '2025-03-01', NOW(), NOW()),

    (10, 6, 'EMP010', '강감찬',    'kang.gc@grown.com',
     '$2a$10$dEA0AQBRrYEEgUbz7SccXOBok/sxmvm.wuqU6uK8Ew1ytd8iv5FPW',
     'USER', '채용담당',  '010-9999-9999', 'ACTIVE',   '2024-11-01', NOW(), NOW()),

    (11, 2, 'EMP011', '안중근',    'ahn.jg@grown.com',
     '$2a$10$vMnJtgVb4YGA/53kS2eYWu5xoP7ch1aERNvgsdE0UhMckDlf/RmF6',
     'USER', '시니어 개발자','010-1010-1010', 'ACTIVE',   '2024-08-01', NOW(), NOW()),

    -- INACTIVE 1명 추가 (퇴사 시뮬레이션)
    (12, 5, 'EMP012', '류관순',    'ryu.gs@grown.com',
     '$2a$10$kSERe3/B.GT4XwWVhR1aJeg1LN7RJpmb8aWO.x5rG752fQyNS9ZH6',
     'USER', '디자이너',  '010-1112-1112', 'INACTIVE', '2024-02-01', NOW(), NOW());


-- ═══════════════════════════════════════════════════════════════
-- ③ 구역 보강 (zone_id 9~14) — ZoneType.ROOM 추가
-- ═══════════════════════════════════════════════════════════════
INSERT IGNORE INTO zones (zone_id, zone_parent_id, zone_name, zone_type, zone_description, created_at, updated_at)
VALUES
    (9,  8,    '지하2층',     'FLOOR', '추가 지하 주차장',          NOW(), NOW()),
    (10, 3,    '회의실 C',    'ROOM',  '2층 소회의실',              NOW(), NOW()),
    (11, 3,    '회의실 D',    'ROOM',  '2층 화상회의실',            NOW(), NOW()),
    (12, 6,    '회의실 E',    'ROOM',  '3층 임원 회의실',           NOW(), NOW()),
    (13, 3,    '휴게실',      'ROOM',  '2층 직원 휴게 공간',        NOW(), NOW()),
    (14, 6,    '카페 라운지', 'ROOM',  '3층 카페 라운지',           NOW(), NOW());


-- ═══════════════════════════════════════════════════════════════
-- ④ 장치 보강 (devices_id 11~20)
--   - 11~12 : INACTIVE 케이스 (DeviceStatus.INACTIVE 커버)
--   - 13~20 : 주차 초음파 센서 (zone 8/9 매핑) — parking_spots 연계
-- ═══════════════════════════════════════════════════════════════
INSERT IGNORE INTO devices (devices_id, zone_id, device_name, device_type, serial_number,
                            mqtt_topic, device_status, created_at, updated_at)
VALUES
    (11, 10, '회의실 C 출입리더기(고장)', 'NFC_READER',  'SN-NFC-101', 'smartoffice/10/access', 'INACTIVE', NOW(), NOW()),
    (12, 13, '휴게실 온도센서(점검중)',   'TEMPERATURE', 'SN-TEMP-101','smartoffice/13/temperature','INACTIVE', NOW(), NOW()),

    -- 지하1층 (zone 8) 주차 센서
    (13, 8, '지하1 초음파-001', 'ULTRASONIC', 'SN-ULT-001', 'smartoffice/8/parking/1', 'ACTIVE', NOW(), NOW()),
    (14, 8, '지하1 초음파-002', 'ULTRASONIC', 'SN-ULT-002', 'smartoffice/8/parking/2', 'ACTIVE', NOW(), NOW()),
    (15, 8, '지하1 초음파-003', 'ULTRASONIC', 'SN-ULT-003', 'smartoffice/8/parking/3', 'ACTIVE', NOW(), NOW()),
    (16, 8, '지하1 초음파-004', 'ULTRASONIC', 'SN-ULT-004', 'smartoffice/8/parking/4', 'ACTIVE', NOW(), NOW()),

    -- 지하2층 (zone 9) 주차 센서
    (17, 9, '지하2 초음파-001', 'ULTRASONIC', 'SN-ULT-005', 'smartoffice/9/parking/1', 'ACTIVE', NOW(), NOW()),
    (18, 9, '지하2 초음파-002', 'ULTRASONIC', 'SN-ULT-006', 'smartoffice/9/parking/2', 'ACTIVE', NOW(), NOW()),
    (19, 9, '지하2 초음파-003', 'ULTRASONIC', 'SN-ULT-007', 'smartoffice/9/parking/3', 'ACTIVE', NOW(), NOW()),
    (20, 9, '지하2 초음파-004', 'ULTRASONIC', 'SN-ULT-008', 'smartoffice/9/parking/4', 'ACTIVE', NOW(), NOW());


-- ═══════════════════════════════════════════════════════════════
-- ⑤ NFC 카드 보강 (card_id 7~12) — LOST/INACTIVE 상태 커버
-- ═══════════════════════════════════════════════════════════════
INSERT IGNORE INTO nfc_cards (card_id, user_id, card_uid, card_type, card_status,
                              issued_at, expired_at, created_at, updated_at)
VALUES
    (7,  7, 'EMP007-CARD-UID-001', 'EMPLOYEE', 'ACTIVE',   NOW(), NULL, NOW(), NOW()),
    (8,  8, 'EMP008-CARD-UID-001', 'EMPLOYEE', 'ACTIVE',   NOW(), NULL, NOW(), NOW()),
    (9,  9, 'EMP009-CARD-UID-001', 'EMPLOYEE', 'LOST',     NOW() - INTERVAL 30 DAY, NULL, NOW(), NOW()),
    (10, 10,'EMP010-CARD-UID-001', 'EMPLOYEE', 'LOST',     NOW() - INTERVAL 60 DAY, NULL, NOW(), NOW()),
    (11, 11,'EMP011-CARD-UID-001', 'EMPLOYEE', 'INACTIVE', NOW() - INTERVAL 90 DAY, NOW() - INTERVAL 1 DAY, NOW(), NOW()),
    (12, 7, 'EMP007-CARD-UID-002', 'EMPLOYEE', 'ACTIVE',   NOW(), NULL, NOW(), NOW());


-- ═══════════════════════════════════════════════════════════════
-- ⑥ 주차면 보강 (spot_id 6~25) — REGULAR/DISABLED/EV 혼합, 점유 다양화
--   - 지하1층(zone 8): 6~15 (10건) — 4건은 device 매핑(13~16)
--   - 지하2층(zone 9): 16~25 (10건) — 4건은 device 매핑(17~20)
-- ═══════════════════════════════════════════════════════════════
INSERT IGNORE INTO parking_spots (spot_id, zone_id, spot_number, spot_type,
                                  device_id, position_x, position_y,
                                  is_occupied, spot_status, created_at, updated_at)
VALUES
    -- 지하1층 (총 10건)
    (6,  8, 'B1-006', 'REGULAR',  13,   6, 1, 1, 'ACTIVE',   NOW(), NOW()),
    (7,  8, 'B1-007', 'REGULAR',  14,   7, 1, 0, 'ACTIVE',   NOW(), NOW()),
    (8,  8, 'B1-008', 'EV',       15,   8, 1, 1, 'ACTIVE',   NOW(), NOW()),
    (9,  8, 'B1-009', 'EV',       16,   9, 1, 0, 'ACTIVE',   NOW(), NOW()),
    (10, 8, 'B1-010', 'DISABLED', NULL,10, 1, 0, 'ACTIVE',   NOW(), NOW()),
    (11, 8, 'B1-011', 'REGULAR',  NULL, 1, 2, 1, 'ACTIVE',   NOW(), NOW()),
    (12, 8, 'B1-012', 'REGULAR',  NULL, 2, 2, 0, 'ACTIVE',   NOW(), NOW()),
    (13, 8, 'B1-013', 'REGULAR',  NULL, 3, 2, 0, 'INACTIVE', NOW(), NOW()),
    (14, 8, 'B1-014', 'REGULAR',  NULL, 4, 2, 1, 'ACTIVE',   NOW(), NOW()),
    (15, 8, 'B1-015', 'DISABLED', NULL, 5, 2, 0, 'ACTIVE',   NOW(), NOW()),

    -- 지하2층 (총 10건)
    (16, 9, 'B2-001', 'REGULAR',  17,   1, 1, 0, 'ACTIVE',   NOW(), NOW()),
    (17, 9, 'B2-002', 'REGULAR',  18,   2, 1, 1, 'ACTIVE',   NOW(), NOW()),
    (18, 9, 'B2-003', 'EV',       19,   3, 1, 0, 'ACTIVE',   NOW(), NOW()),
    (19, 9, 'B2-004', 'EV',       20,   4, 1, 1, 'ACTIVE',   NOW(), NOW()),
    (20, 9, 'B2-005', 'DISABLED', NULL, 5, 1, 0, 'ACTIVE',   NOW(), NOW()),
    (21, 9, 'B2-006', 'REGULAR',  NULL, 1, 2, 1, 'ACTIVE',   NOW(), NOW()),
    (22, 9, 'B2-007', 'REGULAR',  NULL, 2, 2, 0, 'ACTIVE',   NOW(), NOW()),
    (23, 9, 'B2-008', 'REGULAR',  NULL, 3, 2, 1, 'ACTIVE',   NOW(), NOW()),
    (24, 9, 'B2-009', 'REGULAR',  NULL, 4, 2, 0, 'ACTIVE',   NOW(), NOW()),
    (25, 9, 'B2-010', 'EV',       NULL, 5, 2, 0, 'INACTIVE', NOW(), NOW());


-- ═══════════════════════════════════════════════════════════════
-- ⑦ 자산 보강 (asset_id 11~20)
-- ═══════════════════════════════════════════════════════════════
INSERT IGNORE INTO assets (asset_id, asset_number, asset_name, category,
                           assigned_user_id, description, asset_status, purchased_at,
                           created_at, updated_at)
VALUES
    (11, 'AST-2026-011', 'iPad Pro 13"',            'IT기기',   7, 'M4, 1TB',                  'ACTIVE',   '2025-09-01', NOW(), NOW()),
    (12, 'AST-2026-012', 'Wacom Cintiq 22',         'IT기기',   7, '디자이너용 펜 디스플레이', 'ACTIVE',   '2025-09-01', NOW(), NOW()),
    (13, 'AST-2026-013', 'MacBook Air 15"',         'IT기기',   8, 'M3, 16GB',                 'ACTIVE',   '2025-10-15', NOW(), NOW()),
    (14, 'AST-2026-014', '갤럭시 탭 S9 Ultra',       'IT기기',   9, '인사팀 면접용',            'ACTIVE',   '2025-03-01', NOW(), NOW()),
    (15, 'AST-2026-015', '프린터 복합기',            '사무기기', NULL,'사무실 공용',            'ACTIVE',   '2024-04-01', NOW(), NOW()),
    (16, 'AST-2026-016', '회의실 빔프로젝터',         '사무기기', NULL,'회의실 C 전용',          'ACTIVE',   '2024-12-01', NOW(), NOW()),
    (17, 'AST-2026-017', '캡슐 커피머신',            '가전',     NULL,'카페 라운지',            'ACTIVE',   '2025-01-15', NOW(), NOW()),
    (18, 'AST-2026-018', '안마의자',                 '가구',     NULL,'휴게실 공용',            'ACTIVE',   '2025-02-01', NOW(), NOW()),
    (19, 'AST-2026-019', '드론 (시연용)',            'IT기기',   10,'행사 데모용',              'INACTIVE', '2024-06-01', NOW(), NOW()),
    (20, 'AST-2026-020', '아이폰 15 Pro (분실)',     'IT기기',   NULL,'외근 중 분실 처리',      'LOST',     '2025-04-01', NOW(), NOW());


-- ═══════════════════════════════════════════════════════════════
-- ⑧ 급여 기준 보강 (salset_id 6~8) — 신규 직책
-- ═══════════════════════════════════════════════════════════════
INSERT IGNORE INTO salary_settings (salset_id, salset_position, base_salary, overtime_rate, night_rate,
                                    effective_from, effective_to, created_at, updated_at)
VALUES
    (6, '디자이너',       3900000, 1.5, 2.0, '2026-01-01', NULL, NOW(), NOW()),
    (7, '인사담당',       3700000, 1.5, 2.0, '2026-01-01', NULL, NOW(), NOW()),
    (8, '시니어 개발자',  5500000, 1.5, 2.0, '2026-01-01', NULL, NOW(), NOW());


-- ═══════════════════════════════════════════════════════════════
-- ⑨ 출퇴근 (attendance) — 2026-04월 + 2026-05월 일부 보강 (다중 사용자 × 다일자)
--   - 2026-04-27 ~ 2026-04-30 (4일치 × 사용자 6명 = 24건)
--   - 2026-05-06 ~ 2026-05-12 (7일치 × 사용자 4명 = 28건)
--   상태 분포: NORMAL 위주, LATE/EARLY_LEAVE/ABSENT 각 1~2건씩 혼합
-- ═══════════════════════════════════════════════════════════════
INSERT IGNORE INTO attendance (user_id, work_date, check_in, check_out,
                               work_minutes, overtime_minutes, attendance_status, attendance_note,
                               created_at, updated_at)
VALUES
    -- 2026-04-27 (월)
    (1,  '2026-04-27', '2026-04-27 09:00:00', '2026-04-27 18:00:00', 540, 0,  'NORMAL', NULL, NOW(), NOW()),
    (2,  '2026-04-27', '2026-04-27 09:00:00', '2026-04-27 18:00:00', 540, 0,  'NORMAL', NULL, NOW(), NOW()),
    (3,  '2026-04-27', '2026-04-27 09:18:00', '2026-04-27 18:00:00', 522, 0,  'LATE',   NULL, NOW(), NOW()),
    (4,  '2026-04-27', '2026-04-27 09:00:00', '2026-04-27 18:30:00', 570, 30, 'NORMAL', NULL, NOW(), NOW()),
    (6,  '2026-04-27', '2026-04-27 09:00:00', '2026-04-27 18:00:00', 540, 0,  'NORMAL', NULL, NOW(), NOW()),
    (7,  '2026-04-27', '2026-04-27 09:05:00', '2026-04-27 18:00:00', 535, 0,  'NORMAL', NULL, NOW(), NOW()),

    -- 2026-04-28 (화)
    (1,  '2026-04-28', '2026-04-28 09:00:00', '2026-04-28 18:00:00', 540, 0,  'NORMAL',      NULL, NOW(), NOW()),
    (2,  '2026-04-28', '2026-04-28 09:00:00', '2026-04-28 18:00:00', 540, 0,  'NORMAL',      NULL, NOW(), NOW()),
    (3,  '2026-04-28', '2026-04-28 09:00:00', '2026-04-28 18:00:00', 540, 0,  'NORMAL',      NULL, NOW(), NOW()),
    (4,  '2026-04-28', NULL,                  NULL,                    0, 0,  'ABSENT',      '연차', NOW(), NOW()),
    (6,  '2026-04-28', '2026-04-28 09:00:00', '2026-04-28 17:30:00', 510, 0,  'EARLY_LEAVE','병원 진료', NOW(), NOW()),
    (7,  '2026-04-28', '2026-04-28 09:00:00', '2026-04-28 19:00:00', 600, 60, 'NORMAL',      NULL, NOW(), NOW()),

    -- 2026-04-29 (수)
    (1,  '2026-04-29', '2026-04-29 09:00:00', '2026-04-29 18:00:00', 540, 0,  'NORMAL', NULL, NOW(), NOW()),
    (2,  '2026-04-29', '2026-04-29 09:00:00', '2026-04-29 18:00:00', 540, 0,  'NORMAL', NULL, NOW(), NOW()),
    (3,  '2026-04-29', '2026-04-29 09:00:00', '2026-04-29 18:00:00', 540, 0,  'NORMAL', NULL, NOW(), NOW()),
    (4,  '2026-04-29', '2026-04-29 09:00:00', '2026-04-29 18:00:00', 540, 0,  'NORMAL', NULL, NOW(), NOW()),
    (6,  '2026-04-29', '2026-04-29 09:00:00', '2026-04-29 18:00:00', 540, 0,  'NORMAL', NULL, NOW(), NOW()),
    (7,  '2026-04-29', '2026-04-29 09:00:00', '2026-04-29 18:00:00', 540, 0,  'NORMAL', NULL, NOW(), NOW()),

    -- 2026-04-30 (목)
    (1,  '2026-04-30', '2026-04-30 09:00:00', '2026-04-30 18:00:00', 540, 0,  'NORMAL', NULL, NOW(), NOW()),
    (2,  '2026-04-30', '2026-04-30 09:00:00', '2026-04-30 20:00:00', 660,120, 'NORMAL', NULL, NOW(), NOW()),
    (3,  '2026-04-30', '2026-04-30 09:00:00', '2026-04-30 18:00:00', 540, 0,  'NORMAL', NULL, NOW(), NOW()),
    (4,  '2026-04-30', '2026-04-30 09:00:00', '2026-04-30 18:00:00', 540, 0,  'NORMAL', NULL, NOW(), NOW()),
    (6,  '2026-04-30', '2026-04-30 09:00:00', '2026-04-30 18:00:00', 540, 0,  'NORMAL', NULL, NOW(), NOW()),
    (7,  '2026-04-30', '2026-04-30 09:25:00', '2026-04-30 18:00:00', 515, 0,  'LATE',   NULL, NOW(), NOW()),

    -- 2026-05-06 (수, 5월 첫째 주 보강)
    (2,  '2026-05-06', '2026-05-06 09:00:00', '2026-05-06 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),
    (3,  '2026-05-06', '2026-05-06 09:00:00', '2026-05-06 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),
    (4,  '2026-05-06', '2026-05-06 09:00:00', '2026-05-06 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),
    (6,  '2026-05-06', '2026-05-06 09:00:00', '2026-05-06 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),

    -- 2026-05-07 (목)
    (2,  '2026-05-07', '2026-05-07 09:00:00', '2026-05-07 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),
    (3,  '2026-05-07', '2026-05-07 09:00:00', '2026-05-07 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),
    (4,  '2026-05-07', '2026-05-07 09:00:00', '2026-05-07 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),
    (6,  '2026-05-07', '2026-05-07 09:00:00', '2026-05-07 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),

    -- 2026-05-08 (금)
    (2,  '2026-05-08', '2026-05-08 09:00:00', '2026-05-08 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),
    (3,  '2026-05-08', '2026-05-08 09:00:00', '2026-05-08 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),
    (4,  '2026-05-08', '2026-05-08 09:00:00', '2026-05-08 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),
    (6,  '2026-05-08', '2026-05-08 09:00:00', '2026-05-08 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),

    -- 2026-05-11 (월)
    (2,  '2026-05-11', '2026-05-11 09:00:00', '2026-05-11 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),
    (3,  '2026-05-11', '2026-05-11 09:00:00', '2026-05-11 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),
    (4,  '2026-05-11', '2026-05-11 09:12:00', '2026-05-11 18:00:00', 528, 0, 'LATE',   NULL, NOW(), NOW()),
    (6,  '2026-05-11', '2026-05-11 09:00:00', '2026-05-11 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),

    -- 2026-05-12 (화)
    (2,  '2026-05-12', '2026-05-12 09:00:00', '2026-05-12 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),
    (3,  '2026-05-12', '2026-05-12 09:00:00', '2026-05-12 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),
    (4,  '2026-05-12', '2026-05-12 09:00:00', '2026-05-12 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),
    (6,  '2026-05-12', '2026-05-12 09:00:00', '2026-05-12 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW());


-- ═══════════════════════════════════════════════════════════════
-- ⑩ 월간 근태 — 2026년 4월 (다월 시뮬레이션)
--   user_id 1·2·3·4·6 — V5의 5월 데이터와 별개로 4월 집계 추가
-- ═══════════════════════════════════════════════════════════════
INSERT IGNORE INTO monthly_attendance (user_id, monat_year, monat_month,
                                       monat_total_work_minutes, monat_overtime_minutes,
                                       late_count, early_leave_count, absent_count,
                                       created_at, updated_at)
VALUES
    (1, 2026, 4, 2160,   0, 0, 0, 0, NOW(), NOW()),
    (2, 2026, 4, 2160, 120, 0, 0, 0, NOW(), NOW()),
    (3, 2026, 4, 2142,   0, 1, 0, 0, NOW(), NOW()),
    (4, 2026, 4, 2190,  30, 0, 0, 1, NOW(), NOW()),
    (6, 2026, 4, 2130,   0, 0, 1, 0, NOW(), NOW());


-- ═══════════════════════════════════════════════════════════════
-- ⑪ 급여 — 2026년 4월 (DRAFT + CONFIRMED 혼합)
-- ═══════════════════════════════════════════════════════════════
INSERT IGNORE INTO salary_records (user_id, monat_id, salset_id,
                                   salrec_year, salrec_month,
                                   salrec_base_salary, overtime_pay, total_pay, salrec_status,
                                   created_at, updated_at)
VALUES
    (1, (SELECT monat_id FROM monthly_attendance WHERE user_id = 1 AND monat_year = 2026 AND monat_month = 4),
     1, 2026, 4, 5000000,      0, 5000000, 'CONFIRMED', NOW(), NOW()),
    (2, (SELECT monat_id FROM monthly_attendance WHERE user_id = 2 AND monat_year = 2026 AND monat_month = 4),
     2, 2026, 4, 4000000, 750000, 4750000, 'CONFIRMED', NOW(), NOW()),
    (3, (SELECT monat_id FROM monthly_attendance WHERE user_id = 3 AND monat_year = 2026 AND monat_month = 4),
     2, 2026, 4, 4000000,      0, 4000000, 'DRAFT',     NOW(), NOW()),
    (4, (SELECT monat_id FROM monthly_attendance WHERE user_id = 4 AND monat_year = 2026 AND monat_month = 4),
     3, 2026, 4, 4200000, 187500, 4387500, 'DRAFT',     NOW(), NOW()),
    (6, (SELECT monat_id FROM monthly_attendance WHERE user_id = 6 AND monat_year = 2026 AND monat_month = 4),
     5, 2026, 4, 3600000,      0, 3600000, 'CONFIRMED', NOW(), NOW());


-- ═══════════════════════════════════════════════════════════════
-- ⑫ 출입 로그 — 다양한 사용자 × 30일치 × ALLOW/DENIED/BLOCKED 혼합 (총 ~80건)
--   * 코드는 'APPROVED' / 'DENIED' / 'BLOCKED' 를 사용
--     V5는 'ALLOW' 사용 (기존 데이터 보존). V8는 코드 기준.
--   * device 머지 신규 API의 페이지네이션/필터/날짜 범위 검증을 위한 충분 분량.
-- ═══════════════════════════════════════════════════════════════
INSERT IGNORE INTO access_logs (user_id, card_id, devices_id, zone_id,
                                read_uid, direction, auth_result, deny_reason,
                                tagged_at, created_at)
VALUES
    -- ── 2026-04-15 ~ 2026-04-30 (구간 1) ─────────────────────────
    (1, 1, 1, 2, 'ADMIN-CARD-UID-001',  'IN',  'APPROVED', NULL, '2026-04-15 08:55:00', NOW()),
    (1, 1, 1, 2, 'ADMIN-CARD-UID-001',  'OUT', 'APPROVED', NULL, '2026-04-15 18:10:00', NOW()),
    (2, 2, 1, 2, 'EMP002-CARD-UID-001', 'IN',  'APPROVED', NULL, '2026-04-15 09:02:00', NOW()),
    (2, 2, 1, 2, 'EMP002-CARD-UID-001', 'OUT', 'APPROVED', NULL, '2026-04-15 18:05:00', NOW()),
    (3, 3, 1, 2, 'EMP003-CARD-UID-001', 'IN',  'APPROVED', NULL, '2026-04-16 09:00:00', NOW()),
    (3, 3, 1, 2, 'EMP003-CARD-UID-001', 'OUT', 'APPROVED', NULL, '2026-04-16 18:00:00', NOW()),
    (4, 4, 2, 4, 'EMP004-CARD-UID-001', 'IN',  'APPROVED', NULL, '2026-04-17 10:00:00', NOW()),
    (4, 4, 2, 4, 'EMP004-CARD-UID-001', 'OUT', 'APPROVED', NULL, '2026-04-17 11:30:00', NOW()),
    (6, 6, 1, 2, 'EMP006-CARD-UID-001', 'IN',  'APPROVED', NULL, '2026-04-18 09:00:00', NOW()),

    -- 미등록 UID(NULL user_id 회피 위해 분실 카드 user 9의 카드로) — DENIED 케이스
    (9, 9, 5, 7, 'EMP009-CARD-UID-001', 'IN',  'DENIED',   '분실 신고된 카드', '2026-04-19 09:30:00', NOW()),
    (9, 9, 5, 7, 'EMP009-CARD-UID-001', 'IN',  'DENIED',   '분실 신고된 카드', '2026-04-19 14:00:00', NOW()),

    -- 퇴사자(EMP005, user_id=5) 시도 — DENIED
    (5, 5, 5, 7, 'EMP005-CARD-UID-001', 'IN',  'DENIED',   '퇴사 처리된 계정', '2026-04-20 08:00:00', NOW()),

    -- BLOCKED (만료된 카드 — card_id=11 EMP011 INACTIVE)
    (11,11, 5, 7, 'EMP011-CARD-UID-001','IN',  'BLOCKED',  '만료된 카드',       '2026-04-21 09:10:00', NOW()),

    (1, 1, 5, 7, 'ADMIN-CARD-UID-001',  'IN',  'APPROVED', NULL, '2026-04-22 09:00:00', NOW()),
    (1, 1, 5, 7, 'ADMIN-CARD-UID-001',  'OUT', 'APPROVED', NULL, '2026-04-22 18:00:00', NOW()),
    (7, 7, 1, 2, 'EMP007-CARD-UID-001', 'IN',  'APPROVED', NULL, '2026-04-23 09:00:00', NOW()),
    (7, 7, 1, 2, 'EMP007-CARD-UID-001', 'OUT', 'APPROVED', NULL, '2026-04-23 18:00:00', NOW()),
    (8, 8, 1, 2, 'EMP008-CARD-UID-001', 'IN',  'APPROVED', NULL, '2026-04-24 09:00:00', NOW()),
    (8, 8, 1, 2, 'EMP008-CARD-UID-001', 'OUT', 'APPROVED', NULL, '2026-04-24 18:00:00', NOW()),

    -- ── 2026-04-25 ~ 2026-04-30 (구간 2) ─────────────────────────
    (2, 2, 2, 4, 'EMP002-CARD-UID-001', 'IN',  'APPROVED', NULL, '2026-04-25 13:30:00', NOW()),
    (2, 2, 2, 4, 'EMP002-CARD-UID-001', 'OUT', 'APPROVED', NULL, '2026-04-25 15:00:00', NOW()),
    (3, 3, 1, 2, 'EMP003-CARD-UID-001', 'IN',  'APPROVED', NULL, '2026-04-27 09:00:00', NOW()),
    (3, 3, 1, 2, 'EMP003-CARD-UID-001', 'OUT', 'APPROVED', NULL, '2026-04-27 18:00:00', NOW()),
    (4, 4, 5, 7, 'EMP004-CARD-UID-001', 'IN',  'APPROVED', NULL, '2026-04-28 10:30:00', NOW()),
    (4, 4, 5, 7, 'EMP004-CARD-UID-001', 'OUT', 'APPROVED', NULL, '2026-04-28 16:00:00', NOW()),
    (6, 6, 1, 2, 'EMP006-CARD-UID-001', 'IN',  'APPROVED', NULL, '2026-04-29 09:00:00', NOW()),
    (6, 6, 1, 2, 'EMP006-CARD-UID-001', 'OUT', 'APPROVED', NULL, '2026-04-29 18:00:00', NOW()),
    (10,10,1, 2, 'EMP010-CARD-UID-001', 'IN',  'DENIED',   '분실 신고된 카드', '2026-04-29 10:00:00', NOW()),

    -- ── 2026-05-01 ~ 2026-05-13 (구간 3) ─────────────────────────
    (1, 1, 1, 2, 'ADMIN-CARD-UID-001',  'IN',  'APPROVED', NULL, '2026-05-01 09:00:00', NOW()),
    (1, 1, 1, 2, 'ADMIN-CARD-UID-001',  'OUT', 'APPROVED', NULL, '2026-05-01 18:00:00', NOW()),
    (2, 2, 1, 2, 'EMP002-CARD-UID-001', 'IN',  'APPROVED', NULL, '2026-05-02 09:00:00', NOW()),
    (2, 2, 1, 2, 'EMP002-CARD-UID-001', 'OUT', 'APPROVED', NULL, '2026-05-02 18:00:00', NOW()),
    (3, 3, 1, 2, 'EMP003-CARD-UID-001', 'IN',  'APPROVED', NULL, '2026-05-03 09:00:00', NOW()),
    (3, 3, 1, 2, 'EMP003-CARD-UID-001', 'OUT', 'APPROVED', NULL, '2026-05-03 18:00:00', NOW()),
    (4, 4, 2, 4, 'EMP004-CARD-UID-001', 'IN',  'APPROVED', NULL, '2026-05-04 13:30:00', NOW()),
    (4, 4, 2, 4, 'EMP004-CARD-UID-001', 'OUT', 'APPROVED', NULL, '2026-05-04 15:30:00', NOW()),
    (6, 6, 1, 2, 'EMP006-CARD-UID-001', 'IN',  'APPROVED', NULL, '2026-05-05 09:00:00', NOW()),

    (7, 7, 1, 2, 'EMP007-CARD-UID-001', 'IN',  'APPROVED', NULL, '2026-05-06 09:00:00', NOW()),
    (7, 7, 1, 2, 'EMP007-CARD-UID-001', 'OUT', 'APPROVED', NULL, '2026-05-06 18:00:00', NOW()),
    (8, 8, 2, 4, 'EMP008-CARD-UID-001', 'IN',  'APPROVED', NULL, '2026-05-07 14:00:00', NOW()),
    (8, 8, 2, 4, 'EMP008-CARD-UID-001', 'OUT', 'APPROVED', NULL, '2026-05-07 17:00:00', NOW()),
    (9, 9, 5, 7, 'EMP009-CARD-UID-001', 'IN',  'DENIED',   '분실 신고된 카드', '2026-05-08 10:00:00', NOW()),

    -- 미등록 UID 시도 (random user_id로 매핑 못 함 — user_id NOT NULL 제약 회피 위해
    --   user_id는 카드 등록자로 두되, deny_reason에 명시. 코드 service.processTag는 카드 없으면 DB 저장 자체를 안 함.
    --   따라서 V8에서 '등록되지 않은 카드' 케이스는 직접 시연용으로만 1건 — admin이 시도한 형태로 기록)
    (1, 1, 1, 2, 'UNKNOWN-UID-XXX',     'IN',  'DENIED',   '등록되지 않은 카드', '2026-05-08 12:00:00', NOW()),

    (2, 2, 1, 2, 'EMP002-CARD-UID-001', 'IN',  'APPROVED', NULL, '2026-05-09 09:00:00', NOW()),
    (2, 2, 1, 2, 'EMP002-CARD-UID-001', 'OUT', 'APPROVED', NULL, '2026-05-09 18:00:00', NOW()),
    (3, 3, 1, 2, 'EMP003-CARD-UID-001', 'IN',  'APPROVED', NULL, '2026-05-09 09:15:00', NOW()),
    (3, 3, 1, 2, 'EMP003-CARD-UID-001', 'OUT', 'APPROVED', NULL, '2026-05-09 18:00:00', NOW()),
    (4, 4, 1, 2, 'EMP004-CARD-UID-001', 'IN',  'APPROVED', NULL, '2026-05-11 09:00:00', NOW()),
    (4, 4, 1, 2, 'EMP004-CARD-UID-001', 'OUT', 'APPROVED', NULL, '2026-05-11 18:00:00', NOW()),
    (6, 6, 1, 2, 'EMP006-CARD-UID-001', 'IN',  'APPROVED', NULL, '2026-05-11 09:00:00', NOW()),
    (6, 6, 1, 2, 'EMP006-CARD-UID-001', 'OUT', 'APPROVED', NULL, '2026-05-11 18:00:00', NOW()),
    (7, 7, 1, 2, 'EMP007-CARD-UID-001', 'IN',  'APPROVED', NULL, '2026-05-12 09:00:00', NOW()),
    (7, 7, 1, 2, 'EMP007-CARD-UID-001', 'OUT', 'APPROVED', NULL, '2026-05-12 18:00:00', NOW()),
    (8, 8, 1, 2, 'EMP008-CARD-UID-001', 'IN',  'APPROVED', NULL, '2026-05-12 09:00:00', NOW()),
    (8, 8, 1, 2, 'EMP008-CARD-UID-001', 'OUT', 'APPROVED', NULL, '2026-05-12 18:00:00', NOW()),
    (11,11,1, 2, 'EMP011-CARD-UID-001', 'IN',  'BLOCKED',  '만료된 카드',       '2026-05-12 10:00:00', NOW()),

    -- 2026-05-13 (오늘)
    (1, 1, 1, 2, 'ADMIN-CARD-UID-001',  'IN',  'APPROVED', NULL, '2026-05-13 08:55:00', NOW()),
    (2, 2, 1, 2, 'EMP002-CARD-UID-001', 'IN',  'APPROVED', NULL, '2026-05-13 09:00:00', NOW()),
    (3, 3, 1, 2, 'EMP003-CARD-UID-001', 'IN',  'APPROVED', NULL, '2026-05-13 09:10:00', NOW()),
    (6, 6, 1, 2, 'EMP006-CARD-UID-001', 'IN',  'APPROVED', NULL, '2026-05-13 09:05:00', NOW()),
    (7, 7, 1, 2, 'EMP007-CARD-UID-001', 'IN',  'APPROVED', NULL, '2026-05-13 09:20:00', NOW());


-- ═══════════════════════════════════════════════════════════════
-- ⑬ 센서 로그 — 환경 데이터 보강 (TEMPERATURE / HUMIDITY × 최근 며칠)
--   현재 V5는 5/5 한 일자만. V8에서 5/10~5/13 분산 추가.
-- ═══════════════════════════════════════════════════════════════
INSERT IGNORE INTO sensor_logs (zone_id, devices_id, sensor_type, sensor_value, sensor_unit,
                                logged_at, created_at)
VALUES
    -- 개발팀 좌석 온도 (zone 5, devices 3) — 5/10~5/13 4일치
    (5, 3, 'TEMPERATURE', 22.10, '°C', '2026-05-10 09:00:00', NOW()),
    (5, 3, 'TEMPERATURE', 23.50, '°C', '2026-05-10 12:00:00', NOW()),
    (5, 3, 'TEMPERATURE', 24.80, '°C', '2026-05-10 15:00:00', NOW()),
    (5, 3, 'TEMPERATURE', 22.40, '°C', '2026-05-11 09:00:00', NOW()),
    (5, 3, 'TEMPERATURE', 23.70, '°C', '2026-05-11 12:00:00', NOW()),
    (5, 3, 'TEMPERATURE', 25.10, '°C', '2026-05-11 15:00:00', NOW()),
    (5, 3, 'TEMPERATURE', 22.80, '°C', '2026-05-12 09:00:00', NOW()),
    (5, 3, 'TEMPERATURE', 24.00, '°C', '2026-05-12 12:00:00', NOW()),
    (5, 3, 'TEMPERATURE', 25.50, '°C', '2026-05-12 15:00:00', NOW()),
    (5, 3, 'TEMPERATURE', 23.00, '°C', '2026-05-13 09:00:00', NOW()),
    (5, 3, 'TEMPERATURE', 24.50, '°C', '2026-05-13 12:00:00', NOW()),

    -- 개발팀 좌석 습도 (zone 5, devices 4)
    (5, 4, 'HUMIDITY', 46.00, '%', '2026-05-10 09:00:00', NOW()),
    (5, 4, 'HUMIDITY', 49.20, '%', '2026-05-10 12:00:00', NOW()),
    (5, 4, 'HUMIDITY', 51.50, '%', '2026-05-10 15:00:00', NOW()),
    (5, 4, 'HUMIDITY', 47.00, '%', '2026-05-11 09:00:00', NOW()),
    (5, 4, 'HUMIDITY', 50.10, '%', '2026-05-11 12:00:00', NOW()),
    (5, 4, 'HUMIDITY', 52.80, '%', '2026-05-11 15:00:00', NOW()),
    (5, 4, 'HUMIDITY', 48.00, '%', '2026-05-12 09:00:00', NOW()),
    (5, 4, 'HUMIDITY', 51.20, '%', '2026-05-12 12:00:00', NOW()),
    (5, 4, 'HUMIDITY', 53.50, '%', '2026-05-12 15:00:00', NOW()),
    (5, 4, 'HUMIDITY', 47.50, '%', '2026-05-13 09:00:00', NOW()),

    -- 서버실 온도 (zone 7, devices 6) — 항상 저온 유지
    (7, 6, 'TEMPERATURE', 18.20, '°C', '2026-05-10 09:00:00', NOW()),
    (7, 6, 'TEMPERATURE', 18.80, '°C', '2026-05-10 14:00:00', NOW()),
    (7, 6, 'TEMPERATURE', 19.50, '°C', '2026-05-11 09:00:00', NOW()),
    (7, 6, 'TEMPERATURE', 19.90, '°C', '2026-05-11 14:00:00', NOW()),
    (7, 6, 'TEMPERATURE', 18.50, '°C', '2026-05-12 09:00:00', NOW()),
    (7, 6, 'TEMPERATURE', 18.90, '°C', '2026-05-12 14:00:00', NOW()),
    (7, 6, 'TEMPERATURE', 18.30, '°C', '2026-05-13 09:00:00', NOW());


-- ═══════════════════════════════════════════════════════════════
-- ⑭ 제어 명령 (control_commands) — 0건 → 15건 보강
--   ControlStatus: PENDING / COMPLETED / FAILED 전수 커버
--   commandType: AC / LIGHT / FAN
-- ═══════════════════════════════════════════════════════════════
INSERT IGNORE INTO control_commands (zone_id, devices_id, command_type, control_payload,
                                     control_status, triggered_at, created_at)
VALUES
    -- 개발팀 좌석(zone 5, devices 3=온도센서) — 에어컨 명령 (디바이스는 가상)
    (5, 3, 'AC',    '{"action":"ON","targetTemp":24}',  'COMPLETED', '2026-05-10 15:01:00', NOW()),
    (5, 3, 'AC',    '{"action":"SET","targetTemp":23}', 'COMPLETED', '2026-05-11 15:30:00', NOW()),
    (5, 3, 'AC',    '{"action":"ON","targetTemp":24}',  'COMPLETED', '2026-05-12 15:45:00', NOW()),
    (5, 3, 'AC',    '{"action":"OFF"}',                  'PENDING',  '2026-05-13 12:30:00', NOW()),

    -- 서버실(zone 7, devices 6) — 항상 냉방
    (7, 6, 'AC',    '{"action":"SET","targetTemp":18}', 'COMPLETED', '2026-05-10 09:05:00', NOW()),
    (7, 6, 'AC',    '{"action":"SET","targetTemp":18}', 'COMPLETED', '2026-05-12 09:05:00', NOW()),

    -- 회의실A(zone 2) — 조명/팬
    (2, 1, 'LIGHT', '{"action":"ON","brightness":80}',  'COMPLETED', '2026-05-13 08:50:00', NOW()),
    (2, 1, 'FAN',   '{"action":"ON","speed":2}',         'COMPLETED', '2026-05-13 08:55:00', NOW()),
    (2, 1, 'LIGHT', '{"action":"OFF"}',                  'PENDING',  '2026-05-13 12:00:00', NOW()),

    -- 회의실B(zone 4) — 실패 케이스 (MQTT 연결 실패 시뮬)
    (4, 2, 'AC',    '{"action":"ON","targetTemp":25}',  'FAILED',    '2026-05-12 14:00:00', NOW()),
    (4, 2, 'LIGHT', '{"action":"ON"}',                   'FAILED',    '2026-05-12 14:05:00', NOW()),

    -- 휴게실(zone 13) — INACTIVE 장치(devices 12) 호출 → FAILED
    (13,12,'AC',    '{"action":"ON","targetTemp":24}',  'FAILED',    '2026-05-13 10:00:00', NOW()),

    -- 카페 라운지(zone 14) — 신규 명령
    (14, 6, 'LIGHT','{"action":"ON","brightness":70}',  'PENDING',   '2026-05-13 11:00:00', NOW()),
    (14, 6, 'FAN',  '{"action":"ON","speed":1}',         'COMPLETED', '2026-05-13 11:02:00', NOW()),
    (14, 6, 'AC',   '{"action":"SET","targetTemp":22}', 'COMPLETED', '2026-05-13 11:05:00', NOW());


-- ═══════════════════════════════════════════════════════════════
-- ⑮ 예약 보강 (reservations_id 13~32) — 과거/현재/미래 + 신규 회의실(zone 10~12)
-- ═══════════════════════════════════════════════════════════════
INSERT IGNORE INTO reservations (reservations_id, user_id, zone_id,
                                 reservations_title,
                                 reservations_start_at, reservations_end_at,
                                 reservations_status, reservations_checked_in_at,
                                 created_at, updated_at)
VALUES
    -- 미래 (3일 후 ~ 14일 후) — CONFIRMED
    (13,  7, 10, '디자인 스프린트 워크숍',
         DATE_ADD(CURDATE() + INTERVAL 3 DAY, INTERVAL  9 HOUR),
         DATE_ADD(CURDATE() + INTERVAL 3 DAY, INTERVAL 12 HOUR),
         'CONFIRMED', NULL, NOW(), NOW()),
    (14,  8, 11, '화상회의 — 해외 협력사',
         DATE_ADD(CURDATE() + INTERVAL 3 DAY, INTERVAL 14 HOUR),
         DATE_ADD(CURDATE() + INTERVAL 3 DAY, INTERVAL 15 HOUR),
         'CONFIRMED', NULL, NOW(), NOW()),
    (15,  9, 12, '임원 보고 — 1분기 결산',
         DATE_ADD(CURDATE() + INTERVAL 5 DAY, INTERVAL 10 HOUR),
         DATE_ADD(CURDATE() + INTERVAL 5 DAY, INTERVAL 12 HOUR),
         'CONFIRMED', NULL, NOW(), NOW()),
    (16, 10, 10, '신규 인력 온보딩 안내',
         DATE_ADD(CURDATE() + INTERVAL 5 DAY, INTERVAL 14 HOUR),
         DATE_ADD(CURDATE() + INTERVAL 5 DAY, INTERVAL 16 HOUR),
         'CONFIRMED', NULL, NOW(), NOW()),
    (17, 11, 11, '아키텍처 리뷰',
         DATE_ADD(CURDATE() + INTERVAL 7 DAY, INTERVAL 10 HOUR),
         DATE_ADD(CURDATE() + INTERVAL 7 DAY, INTERVAL 12 HOUR),
         'CONFIRMED', NULL, NOW(), NOW()),
    (18,  2, 12, '연간 전략 회의',
         DATE_ADD(CURDATE() + INTERVAL 10 DAY, INTERVAL  9 HOUR),
         DATE_ADD(CURDATE() + INTERVAL 10 DAY, INTERVAL 12 HOUR),
         'CONFIRMED', NULL, NOW(), NOW()),
    (19,  3, 14, '점심 회식 (라운지)',
         DATE_ADD(CURDATE() + INTERVAL 14 DAY, INTERVAL 12 HOUR),
         DATE_ADD(CURDATE() + INTERVAL 14 DAY, INTERVAL 13 HOUR),
         'CONFIRMED', NULL, NOW(), NOW()),

    -- 오늘 — CONFIRMED (zone 10/11/12 사용)
    (20,  7, 10, '신규 디자인 리뷰',
         DATE_ADD(CURDATE(), INTERVAL 16 HOUR),
         DATE_ADD(CURDATE(), INTERVAL 17 HOUR),
         'CONFIRMED', NULL, NOW(), NOW()),
    (21,  8, 11, '제품 데모 화상회의',
         DATE_ADD(CURDATE(), INTERVAL '17:30' HOUR_MINUTE),
         DATE_ADD(CURDATE(), INTERVAL '18:30' HOUR_MINUTE),
         'CONFIRMED', NULL, NOW(), NOW()),

    -- 어제 — CHECKED_IN (체크인 완료)
    (22,  9, 11, '인사 면접 (어제)',
         DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL 10 HOUR),
         DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL 12 HOUR),
         'CHECKED_IN',
         DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL '9:55' HOUR_MINUTE),
         NOW(), NOW()),
    (23,  11,12, '시니어 개발자 면담 (어제)',
         DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL 15 HOUR),
         DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL 16 HOUR),
         'CHECKED_IN',
         DATE_ADD(CURDATE() - INTERVAL 1 DAY, INTERVAL '14:55' HOUR_MINUTE),
         NOW(), NOW()),

    -- 과거 — CANCELLED
    (24,  7, 10, '디자인 회의 (취소됨)',
         DATE_ADD(CURDATE() - INTERVAL 2 DAY, INTERVAL 13 HOUR),
         DATE_ADD(CURDATE() - INTERVAL 2 DAY, INTERVAL 14 HOUR),
         'CANCELLED', NULL, NOW(), NOW()),
    (25,  8, 11, '주간 미팅 (취소됨)',
         DATE_ADD(CURDATE() - INTERVAL 4 DAY, INTERVAL 10 HOUR),
         DATE_ADD(CURDATE() - INTERVAL 4 DAY, INTERVAL 11 HOUR),
         'CANCELLED', NULL, NOW(), NOW()),
    (26, 10, 12, '월간 리포트 회의 (취소됨)',
         DATE_ADD(CURDATE() - INTERVAL 6 DAY, INTERVAL 14 HOUR),
         DATE_ADD(CURDATE() - INTERVAL 6 DAY, INTERVAL 16 HOUR),
         'CANCELLED', NULL, NOW(), NOW()),

    -- 과거 — NO_SHOW
    (27,  9, 11, '인터뷰 (노쇼)',
         DATE_ADD(CURDATE() - INTERVAL 3 DAY, INTERVAL  9 HOUR),
         DATE_ADD(CURDATE() - INTERVAL 3 DAY, INTERVAL 10 HOUR),
         'NO_SHOW', NULL, NOW(), NOW()),
    (28,  11,10, '디자인 협업 (노쇼)',
         DATE_ADD(CURDATE() - INTERVAL 5 DAY, INTERVAL 11 HOUR),
         DATE_ADD(CURDATE() - INTERVAL 5 DAY, INTERVAL 12 HOUR),
         'NO_SHOW', NULL, NOW(), NOW()),

    -- 과거 — CHECKED_IN (역사적 완료 회의)
    (29,  2, 10, '주간 디자인 리뷰',
         DATE_ADD(CURDATE() - INTERVAL 7 DAY, INTERVAL 14 HOUR),
         DATE_ADD(CURDATE() - INTERVAL 7 DAY, INTERVAL 15 HOUR),
         'CHECKED_IN',
         DATE_ADD(CURDATE() - INTERVAL 7 DAY, INTERVAL '13:58' HOUR_MINUTE),
         NOW(), NOW()),
    (30,  3, 12, '월간 보고',
         DATE_ADD(CURDATE() - INTERVAL 14 DAY, INTERVAL 10 HOUR),
         DATE_ADD(CURDATE() - INTERVAL 14 DAY, INTERVAL 11 HOUR),
         'CHECKED_IN',
         DATE_ADD(CURDATE() - INTERVAL 14 DAY, INTERVAL '9:55' HOUR_MINUTE),
         NOW(), NOW()),

    -- 미래 — 다양한 사용자 추가
    (31,  4, 14, '점심 회식 (라운지)',
         DATE_ADD(CURDATE() + INTERVAL 2 DAY, INTERVAL 12 HOUR),
         DATE_ADD(CURDATE() + INTERVAL 2 DAY, INTERVAL 13 HOUR),
         'CONFIRMED', NULL, NOW(), NOW()),
    (32,  6, 13, '팀 휴게실 미팅',
         DATE_ADD(CURDATE() + INTERVAL 2 DAY, INTERVAL 16 HOUR),
         DATE_ADD(CURDATE() + INTERVAL 2 DAY, INTERVAL 17 HOUR),
         'CONFIRMED', NULL, NOW(), NOW());


-- ═══════════════════════════════════════════════════════════════
-- ⑯ 전력 청구 보강 — 신규 zone(10/11/12/13/14) × 2026-04월
-- ═══════════════════════════════════════════════════════════════
INSERT IGNORE INTO power_billing (zone_id, billing_year, billing_month,
                                  total_kwh, unit_price, base_fee, power_fee, total_fee,
                                  calculated_at)
VALUES
    (10, 2026, 4,  450.50, 150, 6000,  67575,  73575, NOW()),
    (11, 2026, 4,  380.25, 150, 6000,  57037,  63037, NOW()),
    (12, 2026, 4,  520.75, 150, 6000,  78112,  84112, NOW()),
    (13, 2026, 4,  210.00, 150, 6000,  31500,  37500, NOW()),
    (14, 2026, 4,  640.50, 150, 6000,  96075, 102075, NOW()),

    -- 2026-03월도 일부 보강
    (10, 2026, 3,  420.00, 150, 6000,  63000,  69000, NOW()),
    (11, 2026, 3,  360.50, 150, 6000,  54075,  60075, NOW()),
    (14, 2026, 3,  610.25, 150, 6000,  91537,  97537, NOW());

-- ─── 끝 ───────────────────────────────────────────────────────
