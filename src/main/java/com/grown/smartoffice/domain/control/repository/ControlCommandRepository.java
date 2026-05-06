package com.grown.smartoffice.domain.control.repository;

import com.grown.smartoffice.domain.control.entity.ControlCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ControlCommandRepository extends JpaRepository<ControlCommand, Long> {

    @Query("SELECT c FROM ControlCommand c " +
            "JOIN FETCH c.zone " +
            "JOIN FETCH c.device " +
            "WHERE (:zoneId IS NULL OR c.zone.zoneId = :zoneId) " +
            "AND c.triggeredAt >= :start " +
            "ORDER BY c.triggeredAt DESC")
    List<ControlCommand> findHistory(@Param("zoneId") Long zoneId, @Param("start") LocalDateTime start);
}
