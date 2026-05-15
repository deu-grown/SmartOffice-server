package com.grown.smartoffice.domain.guest.entity;

/**
 * 방문객 상태. guests.guest_status 컬럼과 매핑.
 * SCHEDULED — 방문 예약 등록 (입실 전).
 * VISITING — 방문 중 (체크인 완료, actual_entry_at 기록).
 * COMPLETED — 방문 종료 (체크아웃 완료, actual_exit_at 기록).
 * CANCELLED — 방문 취소.
 */
public enum GuestStatus {
    SCHEDULED,
    VISITING,
    COMPLETED,
    CANCELLED
}
