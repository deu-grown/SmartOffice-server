-- ============================================================
-- SmartOffice — V9 access_logs.auth_result ALLOW → APPROVED 통일
-- 생성일: 2026-05-15
-- 목적:
--   V5 시드 잔존 'ALLOW' 8건을 코드 표준 'APPROVED' 로 통일.
--   AccessLogService NFC 태그 처리 코드는 'APPROVED'/'DENIED'/'BLOCKED' 만 사용.
--   본 마이그레이션 후 web SmartOffice-web/src/features/accesslog/types.ts 의
--   'ALLOW' literal 호환은 단계적 제거 가능 (BACKEND_SUGGESTIONS #8).
-- ============================================================

UPDATE access_logs SET auth_result = 'APPROVED' WHERE auth_result = 'ALLOW';
