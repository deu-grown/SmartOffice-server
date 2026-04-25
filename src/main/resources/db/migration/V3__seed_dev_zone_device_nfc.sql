-- ============================================================
-- SmartOffice — V3 로컬 개발용 Zone/Device/NFC 시드 데이터
-- 주의: prod 영향 없도록 INSERT IGNORE + 고정 ID 사용
-- ============================================================

-- 기본 Zone (1층 FLOOR → 회의실A AREA)
INSERT IGNORE INTO zones (zone_id, zone_parent_id, zone_name, zone_type, zone_description, created_at, updated_at)
VALUES
    (1, NULL, '1층', 'FLOOR', '본관 1층 전체', NOW(), NOW()),
    (2,    1, '회의실A', 'AREA', '1층 소회의실', NOW(), NOW());

-- 기본 Device (회의실A 출입 리더기)
INSERT IGNORE INTO devices (devices_id, zone_id, device_name, device_type, serial_number, mqtt_topic, device_status, created_at, updated_at)
VALUES
    (1, 2, '회의실A 출입리더기', 'NFC_READER', 'SN-NFC-001', 'smartoffice/2/access', 'ACTIVE', NOW(), NOW());

-- 관리자(user_id=1) NFC 카드 (admin 테스트용)
INSERT IGNORE INTO nfc_cards (card_id, user_id, card_uid, card_type, issued_at, expired_at, created_at, updated_at)
VALUES
    (1, 1, 'ADMIN-CARD-UID-001', 'EMPLOYEE', NOW(), NULL, NOW(), NOW());
