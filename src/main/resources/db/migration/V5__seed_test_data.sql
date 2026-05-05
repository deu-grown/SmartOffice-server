-- ============================================================
-- SmartOffice — V5 통합 테스트용 더미 데이터 시드
-- 생성일: 2026-05-05
-- 목적: 모든 구현된 엔드포인트의 전 케이스 테스트 지원
-- 전략: INSERT IGNORE + 고정 ID (V3 패턴 준수, fresh DB 기준)
-- ============================================================
-- 비밀번호: 각 사번과 동일 (BCrypt cost=10)
--   EMP002: $2a$10$kSERe3/B.GT4XwWVhR1aJeg1LN7RJpmb8aWO.x5rG752fQyNS9ZH6
--   EMP003: $2a$10$XnjooudZGHV.VtpW/DUmXuGuytB.Zoj/e0F9jTPKjkWDYkH5/SrRq
--   EMP004: $2a$10$rcIFJR/sgRB51MjzuGSUb.S5nfG9/6PHMD1AZHSyuBa5kqiFaKMwG
--   EMP005: $2a$10$dEA0AQBRrYEEgUbz7SccXOBok/sxmvm.wuqU6uK8Ew1ytd8iv5FPW
--   EMP006: $2a$10$vMnJtgVb4YGA/53kS2eYWu5xoP7ch1aERNvgsdE0UhMckDlf/RmF6
-- ============================================================

-- ① 직원 추가 (user_id 2~6)
-- EMP002~EMP004: ACTIVE, EMP005: INACTIVE(퇴사), EMP006: ACTIVE
INSERT IGNORE INTO users (user_id, dept_id, employee_number, employee_name, employee_email,
                          password, role, position, phone, status, hired_at, created_at, updated_at)
VALUES
    (2,
     (SELECT dept_id FROM departments WHERE dept_name = '개발팀'),
     'EMP002', '이순신', 'lee.sun@grown.com',
     '$2a$10$kSERe3/B.GT4XwWVhR1aJeg1LN7RJpmb8aWO.x5rG752fQyNS9ZH6',
     'USER', '개발자', '010-1111-1111', 'ACTIVE', '2025-06-01', NOW(), NOW()),

    (3,
     (SELECT dept_id FROM departments WHERE dept_name = '개발팀'),
     'EMP003', '장보고', 'jang.bo@grown.com',
     '$2a$10$XnjooudZGHV.VtpW/DUmXuGuytB.Zoj/e0F9jTPKjkWDYkH5/SrRq',
     'USER', '개발자', '010-2222-2222', 'ACTIVE', '2025-07-15', NOW(), NOW()),

    (4,
     (SELECT dept_id FROM departments WHERE dept_name = 'IoT팀'),
     'EMP004', '세종대왕', 'sejong.da@grown.com',
     '$2a$10$rcIFJR/sgRB51MjzuGSUb.S5nfG9/6PHMD1AZHSyuBa5kqiFaKMwG',
     'USER', '펌웨어 엔지니어', '010-3333-3333', 'ACTIVE', '2025-05-01', NOW(), NOW()),

    (5,
     (SELECT dept_id FROM departments WHERE dept_name = '기획팀'),
     'EMP005', '문화왕', 'moon.hwa@grown.com',
     '$2a$10$dEA0AQBRrYEEgUbz7SccXOBok/sxmvm.wuqU6uK8Ew1ytd8iv5FPW',
     'USER', '기획자', '010-4444-4444', 'INACTIVE', '2024-01-01', NOW(), NOW()),

    (6,
     (SELECT dept_id FROM departments WHERE dept_name = '경영지원'),
     'EMP006', '홍길동', 'hong.gildong@grown.com',
     '$2a$10$vMnJtgVb4YGA/53kS2eYWu5xoP7ch1aERNvgsdE0UhMckDlf/RmF6',
     'USER', '회계담당', '010-5555-5555', 'ACTIVE', '2025-04-01', NOW(), NOW());


-- ② NFC 카드 (card_id 2~6)
INSERT IGNORE INTO nfc_cards (card_id, user_id, card_uid, card_type, issued_at, expired_at, created_at, updated_at)
VALUES
    (2, 2, 'EMP002-CARD-UID-001', 'EMPLOYEE', NOW(), NULL, NOW(), NOW()),
    (3, 3, 'EMP003-CARD-UID-001', 'EMPLOYEE', NOW(), NULL, NOW(), NOW()),
    (4, 4, 'EMP004-CARD-UID-001', 'EMPLOYEE', NOW(), NULL, NOW(), NOW()),
    (5, 5, 'EMP005-CARD-UID-001', 'EMPLOYEE', NOW(), NULL, NOW(), NOW()),
    (6, 6, 'EMP006-CARD-UID-001', 'EMPLOYEE', NOW(), NULL, NOW(), NOW());


-- ③ 구역 추가 (zone_id 3~8)
-- 3: 2층(FLOOR), 4: 회의실B(AREA/3), 5: 개발팀 좌석(AREA/3)
-- 6: 3층(FLOOR), 7: 서버실(AREA/6), 8: 지하1층(FLOOR)
INSERT IGNORE INTO zones (zone_id, zone_parent_id, zone_name, zone_type, zone_description, created_at, updated_at)
VALUES
    (3, NULL, '2층',        'FLOOR', '본관 2층 전체',     NOW(), NOW()),
    (4,    3, '회의실B',    'AREA',  '2층 대회의실',      NOW(), NOW()),
    (5,    3, '개발팀 좌석','AREA',  '2층 개발팀 공간',   NOW(), NOW()),
    (6, NULL, '3층',        'FLOOR', '본관 3층 전체',     NOW(), NOW()),
    (7,    6, '서버실',     'AREA',  '3층 서버실',        NOW(), NOW()),
    (8, NULL, '지하1층',    'FLOOR', '지하 주차장',       NOW(), NOW());


-- ④ 장치 추가 (devices_id 2~6)
INSERT IGNORE INTO devices (devices_id, zone_id, device_name, device_type, serial_number,
                            mqtt_topic, device_status, created_at, updated_at)
VALUES
    (2, 4, '회의실B 출입리더기',     'NFC_READER',  'SN-NFC-002',   'smartoffice/4/access',      'ACTIVE', NOW(), NOW()),
    (3, 5, '개발팀 좌석 온도센서',   'TEMPERATURE', 'SN-TEMP-001',  'smartoffice/5/temperature', 'ACTIVE', NOW(), NOW()),
    (4, 5, '개발팀 좌석 습도센서',   'HUMIDITY',    'SN-HUMID-001', 'smartoffice/5/humidity',    'ACTIVE', NOW(), NOW()),
    (5, 7, '서버실 출입리더기',      'NFC_READER',  'SN-NFC-003',   'smartoffice/7/access',      'ACTIVE', NOW(), NOW()),
    (6, 7, '서버실 온도센서',        'TEMPERATURE', 'SN-TEMP-002',  'smartoffice/7/temperature', 'ACTIVE', NOW(), NOW());


-- ⑤ 근태 기록 (2026-05-01 ~ 05-05, 5일치)
-- user_id 1(관리자): LATE 1건 / user_id 3(장보고): EARLY_LEAVE 1건 / user_id 4(세종대왕): ABSENT 1건
INSERT IGNORE INTO attendance (user_id, work_date, check_in, check_out,
                               work_minutes, overtime_minutes, attendance_status, attendance_note,
                               created_at, updated_at)
VALUES
    -- EMP001 (관리자)
    (1, '2026-05-01', '2026-05-01 09:00:00', '2026-05-01 18:00:00', 540,  0, 'NORMAL',      NULL,       NOW(), NOW()),
    (1, '2026-05-02', '2026-05-02 09:00:00', '2026-05-02 18:30:00', 570, 30, 'NORMAL',      NULL,       NOW(), NOW()),
    (1, '2026-05-03', '2026-05-03 09:00:00', '2026-05-03 18:00:00', 540,  0, 'NORMAL',      NULL,       NOW(), NOW()),
    (1, '2026-05-04', '2026-05-04 09:15:00', '2026-05-04 18:00:00', 525,  0, 'LATE',        NULL,       NOW(), NOW()),
    (1, '2026-05-05', '2026-05-05 09:00:00', '2026-05-05 18:00:00', 540,  0, 'NORMAL',      NULL,       NOW(), NOW()),

    -- EMP002 (이순신, 개근)
    (2, '2026-05-01', '2026-05-01 09:00:00', '2026-05-01 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),
    (2, '2026-05-02', '2026-05-02 09:00:00', '2026-05-02 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),
    (2, '2026-05-03', '2026-05-03 09:00:00', '2026-05-03 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),
    (2, '2026-05-04', '2026-05-04 09:00:00', '2026-05-04 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),
    (2, '2026-05-05', '2026-05-05 09:00:00', '2026-05-05 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),

    -- EMP003 (장보고, 5/1 조퇴)
    (3, '2026-05-01', '2026-05-01 09:00:00', '2026-05-01 15:30:00', 390, 0, 'EARLY_LEAVE', '병원 방문', NOW(), NOW()),
    (3, '2026-05-02', '2026-05-02 09:00:00', '2026-05-02 18:00:00', 540, 0, 'NORMAL',      NULL,       NOW(), NOW()),
    (3, '2026-05-03', '2026-05-03 09:00:00', '2026-05-03 18:00:00', 540, 0, 'NORMAL',      NULL,       NOW(), NOW()),
    (3, '2026-05-04', '2026-05-04 09:00:00', '2026-05-04 18:00:00', 540, 0, 'NORMAL',      NULL,       NOW(), NOW()),
    (3, '2026-05-05', '2026-05-05 09:00:00', '2026-05-05 18:00:00', 540, 0, 'NORMAL',      NULL,       NOW(), NOW()),

    -- EMP004 (세종대왕, 5/1 결근)
    (4, '2026-05-01', NULL,                  NULL,                  0,   0, 'ABSENT', '병가', NOW(), NOW()),
    (4, '2026-05-02', '2026-05-02 09:00:00', '2026-05-02 18:00:00', 540, 0, 'NORMAL', NULL,  NOW(), NOW()),
    (4, '2026-05-03', '2026-05-03 09:00:00', '2026-05-03 18:00:00', 540, 0, 'NORMAL', NULL,  NOW(), NOW()),
    (4, '2026-05-04', '2026-05-04 09:00:00', '2026-05-04 18:00:00', 540, 0, 'NORMAL', NULL,  NOW(), NOW()),
    (4, '2026-05-05', '2026-05-05 09:00:00', '2026-05-05 18:00:00', 540, 0, 'NORMAL', NULL,  NOW(), NOW()),

    -- EMP006 (홍길동, 개근)
    (6, '2026-05-01', '2026-05-01 09:00:00', '2026-05-01 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),
    (6, '2026-05-02', '2026-05-02 09:00:00', '2026-05-02 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),
    (6, '2026-05-03', '2026-05-03 09:00:00', '2026-05-03 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),
    (6, '2026-05-04', '2026-05-04 09:00:00', '2026-05-04 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW()),
    (6, '2026-05-05', '2026-05-05 09:00:00', '2026-05-05 18:00:00', 540, 0, 'NORMAL', NULL, NOW(), NOW());


-- ⑥ 월간 근태 집계 (2026년 5월)
-- work_minutes 합계: EMP001=2745(30분 야근 포함), EMP002=2700, EMP003=2610, EMP004=2160, EMP006=2700
INSERT IGNORE INTO monthly_attendance (user_id, monat_year, monat_month,
                                       monat_total_work_minutes, monat_overtime_minutes,
                                       late_count, early_leave_count, absent_count,
                                       created_at, updated_at)
VALUES
    (1, 2026, 5, 2745, 30, 1, 0, 0, NOW(), NOW()),
    (2, 2026, 5, 2700,  0, 0, 0, 0, NOW(), NOW()),
    (3, 2026, 5, 2610,  0, 0, 1, 0, NOW(), NOW()),
    (4, 2026, 5, 2160,  0, 0, 0, 1, NOW(), NOW()),
    (6, 2026, 5, 2700,  0, 0, 0, 0, NOW(), NOW());


-- ⑦ 급여 기준 (직급별, effective_from 2026-01-01)
INSERT IGNORE INTO salary_settings (salset_id, salset_position, base_salary, overtime_rate, night_rate,
                                    effective_from, effective_to, created_at, updated_at)
VALUES
    (1, '팀장',           5000000, 1.5, 2.0, '2026-01-01', NULL, NOW(), NOW()),
    (2, '개발자',         4000000, 1.5, 2.0, '2026-01-01', NULL, NOW(), NOW()),
    (3, '펌웨어 엔지니어',4200000, 1.5, 2.0, '2026-01-01', NULL, NOW(), NOW()),
    (4, '기획자',         3800000, 1.5, 2.0, '2026-01-01', NULL, NOW(), NOW()),
    (5, '회계담당',       3600000, 1.5, 2.0, '2026-01-01', NULL, NOW(), NOW());


-- ⑧ 급여 기록 (2026년 5월, DRAFT+CONFIRMED 혼합)
-- monat_id는 monthly_attendance 참조
INSERT IGNORE INTO salary_records (user_id, monat_id, salset_id,
                                   salrec_year, salrec_month,
                                   salrec_base_salary, overtime_pay, total_pay, salrec_status,
                                   created_at, updated_at)
VALUES
    -- EMP001 (팀장, 야근수당 포함, DRAFT)
    (1,
     (SELECT monat_id FROM monthly_attendance WHERE user_id = 1 AND monat_year = 2026 AND monat_month = 5),
     1, 2026, 5, 5000000, 187500, 5187500, 'DRAFT', NOW(), NOW()),

    -- EMP002 (개발자, CONFIRMED)
    (2,
     (SELECT monat_id FROM monthly_attendance WHERE user_id = 2 AND monat_year = 2026 AND monat_month = 5),
     2, 2026, 5, 4000000, 0, 4000000, 'CONFIRMED', NOW(), NOW()),

    -- EMP003 (개발자, DRAFT)
    (3,
     (SELECT monat_id FROM monthly_attendance WHERE user_id = 3 AND monat_year = 2026 AND monat_month = 5),
     2, 2026, 5, 4000000, 0, 4000000, 'DRAFT', NOW(), NOW()),

    -- EMP004 (펌웨어 엔지니어, CONFIRMED)
    (4,
     (SELECT monat_id FROM monthly_attendance WHERE user_id = 4 AND monat_year = 2026 AND monat_month = 5),
     3, 2026, 5, 4200000, 0, 4200000, 'CONFIRMED', NOW(), NOW()),

    -- EMP006 (회계담당, DRAFT)
    (6,
     (SELECT monat_id FROM monthly_attendance WHERE user_id = 6 AND monat_year = 2026 AND monat_month = 5),
     5, 2026, 5, 3600000, 0, 3600000, 'DRAFT', NOW(), NOW());


-- ⑨ 자산 (10건: IT기기·가구·소모품, ACTIVE/INACTIVE/LOST 혼합)
INSERT IGNORE INTO assets (asset_id, asset_number, asset_name, category,
                           assigned_user_id, description, asset_status, purchased_at,
                           created_at, updated_at)
VALUES
    (1,  'AST-2026-001', 'MacBook Pro 16"',             'IT기기',  1, 'M3 Max, 36GB RAM',         'ACTIVE',   '2025-06-01', NOW(), NOW()),
    (2,  'AST-2026-002', 'LG UltraFine 27" 모니터',     'IT기기',  2, '4K IPS 디스플레이',        'ACTIVE',   '2025-06-01', NOW(), NOW()),
    (3,  'AST-2026-003', 'Keychron 기계식 키보드',       'IT기기',  3, 'RGB, 핫스왑',              'ACTIVE',   '2025-07-15', NOW(), NOW()),
    (4,  'AST-2026-004', 'Dell 27" 144Hz 모니터',        'IT기기',  4, 'QHD 144Hz',                'ACTIVE',   '2025-05-01', NOW(), NOW()),
    (5,  'AST-2026-005', 'Logitech MX Master 3S',        'IT기기', NULL,'무선 마우스, 미배정',     'INACTIVE', '2024-01-01', NOW(), NOW()),
    (6,  'AST-2026-006', 'HP Pavilion 노트북',           'IT기기', NULL,'분실 처리',               'LOST',     '2024-06-01', NOW(), NOW()),
    (7,  'AST-2026-007', 'Herman Miller Aeron 의자',     '가구',    1, '블랙, 정사이즈',            'ACTIVE',   '2025-03-01', NOW(), NOW()),
    (8,  'AST-2026-008', '스탠딩 데스크 Pro',            '가구',    2, '높이 조절식 전동 책상',    'ACTIVE',   '2025-03-01', NOW(), NOW()),
    (9,  'AST-2026-009', '회의실 테이블 2m',             '가구',   NULL,'회의실A 전용 테이블',     'ACTIVE',   '2024-12-01', NOW(), NOW()),
    (10, 'AST-2026-010', 'A4 용지 (5묶음)',              '소모품', NULL,'사무용 소모품',            'ACTIVE',   '2026-05-01', NOW(), NOW());


-- ⑩ 주차면 (5건, zone_id=8 지하1층, REGULAR/RESERVED/DISABLED, 점유 혼합)
INSERT IGNORE INTO parking_spots (spot_id, zone_id, spot_number, spot_type,
                                  device_id, position_x, position_y,
                                  is_occupied, spot_status, created_at, updated_at)
VALUES
    (1, 8, 'P-001', 'REGULAR',  NULL, 1, 1, 0, 'ACTIVE', NOW(), NOW()),
    (2, 8, 'P-002', 'REGULAR',  NULL, 2, 1, 1, 'ACTIVE', NOW(), NOW()),
    (3, 8, 'P-003', 'RESERVED', NULL, 3, 1, 0, 'ACTIVE', NOW(), NOW()),
    (4, 8, 'P-004', 'REGULAR',  NULL, 4, 1, 1, 'ACTIVE', NOW(), NOW()),
    (5, 8, 'P-005', 'DISABLED', NULL, 5, 1, 0, 'ACTIVE', NOW(), NOW());


-- ⑪ 전력 청구 (zone_id 1·2, 3개월분: 2026-03, 04, 05)
INSERT IGNORE INTO power_billing (zone_id, billing_year, billing_month,
                                  total_kwh, unit_price, base_fee, power_fee, total_fee,
                                  calculated_at)
VALUES
    (1, 2026, 3, 1200.50, 150, 6000, 180075, 186075, NOW()),
    (1, 2026, 4, 1350.75, 150, 6000, 202612, 208612, NOW()),
    (1, 2026, 5, 1100.25, 150, 6000, 165037, 171037, NOW()),
    (2, 2026, 3, 1050.00, 150, 6000, 157500, 163500, NOW()),
    (2, 2026, 4, 1200.50, 150, 6000, 180075, 186075, NOW()),
    (2, 2026, 5,  950.25, 150, 6000, 142537, 148537, NOW());


-- ⑫ 센서 로그 (devices_id 3·4·6, 3시점씩 총 9건)
INSERT IGNORE INTO sensor_logs (zone_id, devices_id, sensor_type, sensor_value, sensor_unit,
                                logged_at, created_at)
VALUES
    -- 개발팀 좌석 온도 (devices_id=3)
    (5, 3, 'TEMPERATURE', 22.50, '°C', '2026-05-05 09:00:00', NOW()),
    (5, 3, 'TEMPERATURE', 23.10, '°C', '2026-05-05 12:00:00', NOW()),
    (5, 3, 'TEMPERATURE', 24.20, '°C', '2026-05-05 15:00:00', NOW()),

    -- 개발팀 좌석 습도 (devices_id=4)
    (5, 4, 'HUMIDITY', 45.00, '%', '2026-05-05 09:00:00', NOW()),
    (5, 4, 'HUMIDITY', 48.50, '%', '2026-05-05 12:00:00', NOW()),
    (5, 4, 'HUMIDITY', 50.20, '%', '2026-05-05 15:00:00', NOW()),

    -- 서버실 온도 (devices_id=6)
    (7, 6, 'TEMPERATURE', 18.50, '°C', '2026-05-05 09:00:00', NOW()),
    (7, 6, 'TEMPERATURE', 19.20, '°C', '2026-05-05 12:00:00', NOW()),
    (7, 6, 'TEMPERATURE', 20.10, '°C', '2026-05-05 15:00:00', NOW());


-- ⑬ 출입 로그 (8건, 성공 이벤트만 — user_id/card_id NOT NULL 제약)
-- devices_id: 1=회의실A, 2=회의실B, 5=서버실
INSERT IGNORE INTO access_logs (user_id, card_id, devices_id, zone_id,
                                read_uid, direction, auth_result, deny_reason,
                                tagged_at, created_at)
VALUES
    -- 회의실A (zone_id=2, devices_id=1)
    (1, 1, 1, 2, 'ADMIN-CARD-UID-001', 'IN',  'ALLOW', NULL, '2026-05-05 09:05:00', NOW()),
    (2, 2, 1, 2, 'EMP002-CARD-UID-001','IN',  'ALLOW', NULL, '2026-05-05 09:10:00', NOW()),
    (3, 3, 1, 2, 'EMP003-CARD-UID-001','IN',  'ALLOW', NULL, '2026-05-05 09:15:00', NOW()),
    (1, 1, 1, 2, 'ADMIN-CARD-UID-001', 'OUT', 'ALLOW', NULL, '2026-05-05 11:30:00', NOW()),

    -- 회의실B (zone_id=4, devices_id=2)
    (4, 4, 2, 4, 'EMP004-CARD-UID-001','IN',  'ALLOW', NULL, '2026-05-05 14:00:00', NOW()),
    (4, 4, 2, 4, 'EMP004-CARD-UID-001','OUT', 'ALLOW', NULL, '2026-05-05 16:00:00', NOW()),

    -- 서버실 (zone_id=7, devices_id=5)
    (1, 1, 5, 7, 'ADMIN-CARD-UID-001', 'IN',  'ALLOW', NULL, '2026-05-05 17:00:00', NOW()),
    (1, 1, 5, 7, 'ADMIN-CARD-UID-001', 'OUT', 'ALLOW', NULL, '2026-05-05 17:30:00', NOW());
