-- ============================================================
-- SmartOffice — V4 NFC 카드 상태 컬럼 추가
-- ============================================================

ALTER TABLE `nfc_cards` ADD COLUMN `card_status` VARCHAR(10) NOT NULL DEFAULT 'ACTIVE' AFTER `card_type`;
CREATE INDEX idx_nfc_cards_status ON `nfc_cards` (`card_status`);
