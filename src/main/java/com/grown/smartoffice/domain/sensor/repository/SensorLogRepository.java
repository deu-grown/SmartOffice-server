package com.grown.smartoffice.domain.sensor.repository;

import com.grown.smartoffice.domain.sensor.entity.SensorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SensorLogRepository extends JpaRepository<SensorLog, Long> {

    List<SensorLog> findByZone_ZoneIdAndLoggedAtBetweenOrderByLoggedAtDesc(Long zoneId, LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT sl.* FROM sensor_logs sl " +
            "INNER JOIN (SELECT sensor_type, MAX(logged_at) as max_logged_at " +
            "            FROM sensor_logs WHERE zone_id = :zoneId GROUP BY sensor_type) latest " +
            "ON sl.sensor_type = latest.sensor_type AND sl.logged_at = latest.max_logged_at " +
            "WHERE sl.zone_id = :zoneId", nativeQuery = true)
    List<SensorLog> findLatestByZoneId(@Param("zoneId") Long zoneId);
}
