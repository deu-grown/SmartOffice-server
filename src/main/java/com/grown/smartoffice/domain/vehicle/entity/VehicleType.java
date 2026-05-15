package com.grown.smartoffice.domain.vehicle.entity;

/**
 * 차량 구분. vehicle.vehicle_type 컬럼과 매핑.
 * STAFF — 임직원 차량 (owner_user_id 로 users FK 연결).
 * VISITOR — 방문객 차량 (owner_user_id 는 null).
 */
public enum VehicleType {
    STAFF,
    VISITOR
}
