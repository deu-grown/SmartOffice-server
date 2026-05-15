-- ============================================================
-- SmartOffice — V10 parking_spots 좌표 UNIQUE 제약
-- 생성일: 2026-05-15
-- 목적:
--   같은 zone 의 동일 좌표 (position_x, position_y) 에 spot 다중 등록 차단.
--   기존 결함: ParkingZoneMap 평면도에서 동일 좌표 spot 이 z-stack 으로
--             마지막 등록만 노출되어 운영자가 인지 못한 상태로 데이터 무결성 손상.
--   MySQL UNIQUE 의 NULL ≠ NULL 정책으로 좌표 둘 다 null 인 grid fallback spot 은
--   자유 통과 (좌표 없는 spot 은 위치 충돌 개념 자체가 없음).
--   null XOR 가드 (한쪽만 null 차단) 는 ParkingServiceImpl 코드에서 처리.
--   기존 V8 시드(zone 8/9 좌표 분포)는 충돌 없음 — migration 안전.
-- ============================================================

ALTER TABLE parking_spots
  ADD CONSTRAINT UQ_PARKING_SPOTS_POSITION UNIQUE (zone_id, position_x, position_y);
