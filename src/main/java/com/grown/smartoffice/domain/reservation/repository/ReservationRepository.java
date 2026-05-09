package com.grown.smartoffice.domain.reservation.repository;

import com.grown.smartoffice.domain.reservation.entity.Reservation;
import com.grown.smartoffice.domain.reservation.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT COUNT(r) FROM Reservation r " +
           "WHERE r.zone.zoneId = :zoneId AND r.status = :status " +
           "AND r.startAt < :endAt AND r.endAt > :startAt " +
           "AND (:excludeId IS NULL OR r.reservationsId <> :excludeId)")
    long countConflict(@Param("zoneId") Long zoneId,
                       @Param("status") ReservationStatus status,
                       @Param("startAt") LocalDateTime startAt,
                       @Param("endAt") LocalDateTime endAt,
                       @Param("excludeId") Long excludeId);

    @Query("SELECT COUNT(r) FROM Reservation r " +
           "WHERE r.status = :status " +
           "AND r.startAt >= :startOfDay AND r.startAt < :endOfDay")
    long countTodayConfirmed(@Param("status") ReservationStatus status,
                             @Param("startOfDay") LocalDateTime startOfDay,
                             @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.user JOIN FETCH r.zone " +
           "WHERE r.user.userId = :userId ORDER BY r.startAt DESC")
    List<Reservation> findByUserIdWithDetails(@Param("userId") Long userId);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.user JOIN FETCH r.zone " +
           "WHERE r.zone.zoneId = :zoneId " +
           "AND r.startAt >= :start AND r.startAt < :end " +
           "ORDER BY r.startAt ASC")
    List<Reservation> findByZoneAndDateWithDetails(@Param("zoneId") Long zoneId,
                                                   @Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.user JOIN FETCH r.zone " +
           "WHERE (:status IS NULL OR r.status = :status) " +
           "ORDER BY r.startAt DESC")
    Page<Reservation> findAllWithDetailsFiltered(@Param("status") ReservationStatus status,
                                                 Pageable pageable);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.user JOIN FETCH r.zone " +
           "WHERE r.reservationsId = :id")
    java.util.Optional<Reservation> findByIdWithDetails(@Param("id") Long id);
}
