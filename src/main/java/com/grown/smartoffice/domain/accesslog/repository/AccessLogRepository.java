package com.grown.smartoffice.domain.accesslog.repository;

import com.grown.smartoffice.domain.accesslog.entity.AccessLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
