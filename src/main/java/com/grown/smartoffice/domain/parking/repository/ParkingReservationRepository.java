package com.grown.smartoffice.domain.parking.repository;

import com.grown.smartoffice.domain.parking.entity.ParkingReservation;
import com.grown.smartoffice.domain.parking.entity.ParkingReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ParkingReservationRepository extends JpaRepository<ParkingReservation, Long> {

    @Query("""
            SELECT r FROM ParkingReservation r
            JOIN FETCH r.vehicle
            JOIN FETCH r.zone
            LEFT JOIN FETCH r.spot
            WHERE r.reservationId = :id
            """)
    Optional<ParkingReservation> findByIdWithRelations(@Param("id") Long id);

    @Query(value = """
            SELECT r FROM ParkingReservation r
            WHERE (:zoneId IS NULL OR r.zone.zoneId = :zoneId)
              AND (:status IS NULL OR r.reservationStatus = :status)
            ORDER BY r.reservedAt DESC
            """,
           countQuery = """
            SELECT COUNT(r) FROM ParkingReservation r
            WHERE (:zoneId IS NULL OR r.zone.zoneId = :zoneId)
              AND (:status IS NULL OR r.reservationStatus = :status)
            """)
    Page<ParkingReservation> findAllWithFilters(
            @Param("zoneId") Long zoneId,
            @Param("status") ParkingReservationStatus status,
            Pageable pageable);
}
