package com.grown.smartoffice.domain.parking.entity;

/**
 * 주차 예약 상태. parking_reservation.reservation_status 컬럼과 매핑.
 * RESERVED — 사전 예약 (입차 전).
 * PARKED — 입차 완료 (entry_at 기록).
 * EXITED — 출차 완료 (exit_at 기록).
 */
public enum ParkingReservationStatus {
    RESERVED,
    PARKED,
    EXITED
}
