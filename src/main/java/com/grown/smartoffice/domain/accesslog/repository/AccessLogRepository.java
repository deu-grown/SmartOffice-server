package com.grown.smartoffice.domain.accesslog.repository;

import com.grown.smartoffice.domain.accesslog.entity.AccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {

    @Query("""
            SELECT a FROM AccessLog a
            JOIN FETCH a.user
            JOIN FETCH a.zone
            WHERE (:type IS NULL OR a.direction = :type)
            ORDER BY a.taggedAt DESC
            """)
    List<AccessLog> findRecentWithUserAndZone(@Param("type") String type, Pageable pageable);

    boolean existsByCard_CardId(Long cardId);

    @Query("""
            SELECT a FROM AccessLog a
            JOIN FETCH a.user
            JOIN FETCH a.device
            JOIN FETCH a.zone
            WHERE (:zoneId     IS NULL OR a.zone.zoneId   = :zoneId)
              AND (:userId     IS NULL OR a.user.userId   = :userId)
              AND (:authResult IS NULL OR a.authResult    = :authResult)
              AND (:direction  IS NULL OR a.direction     = :direction)
              AND (:startDate  IS NULL OR a.taggedAt     >= :startDate)
              AND (:endDate    IS NULL OR a.taggedAt     <= :endDate)
            """)
    Page<AccessLog> findAllWithFilters(
            @Param("zoneId") Long zoneId,
            @Param("userId") Long userId,
            @Param("authResult") String authResult,
            @Param("direction") String direction,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
