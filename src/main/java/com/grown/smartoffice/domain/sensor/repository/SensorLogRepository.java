package com.grown.smartoffice.domain.sensor.repository;

import com.grown.smartoffice.domain.power.dto.HourlyPowerProjection;
import com.grown.smartoffice.domain.power.dto.MonthlyPowerProjection;
import com.grown.smartoffice.domain.sensor.entity.SensorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface SensorLogRepository extends JpaRepository<SensorLog, Long> {

    List<SensorLog> findByZone_ZoneIdAndLoggedAtBetweenOrderByLoggedAtDesc(Long zoneId, LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT sl.* FROM sensor_logs sl " +
            "INNER JOIN (SELECT sensor_type, MAX(logged_at) as max_logged_at " +
            "            FROM sensor_logs WHERE zone_id = :zoneId GROUP BY sensor_type) latest " +
            "ON sl.sensor_type = latest.sensor_type AND sl.logged_at = latest.max_logged_at " +
            "WHERE sl.zone_id = :zoneId", nativeQuery = true)
    List<SensorLog> findLatestByZoneId(@Param("zoneId") Long zoneId);

    /**
     * 구역별 센서 타입(TEMPERATURE·HUMIDITY·CO2)별 최신 로그를 한 번에 조회.
     * JPQL이 상관 서브쿼리 + GROUP BY 조합을 지원하지 않아 nativeQuery 사용.
     */
    @Query(value = """
            SELECT s.*
            FROM sensor_logs s
            INNER JOIN (
                SELECT zone_id, sensor_type, MAX(logged_at) AS max_logged_at
                FROM sensor_logs
                WHERE sensor_type IN ('TEMPERATURE', 'HUMIDITY', 'CO2')
                GROUP BY zone_id, sensor_type
            ) latest
            ON s.zone_id        = latest.zone_id
               AND s.sensor_type    = latest.sensor_type
               AND s.logged_at      = latest.max_logged_at
            """, nativeQuery = true)
    List<SensorLog> findLatestPerZoneAndType();

    @Query(value = """
            SELECT sl.* FROM sensor_logs sl
            INNER JOIN (
                SELECT devices_id, MAX(logged_at) AS max_logged_at
                FROM sensor_logs WHERE zone_id = :zoneId AND sensor_type = 'POWER'
                GROUP BY devices_id
            ) latest ON sl.devices_id = latest.devices_id AND sl.logged_at = latest.max_logged_at
            WHERE sl.zone_id = :zoneId AND sl.sensor_type = 'POWER'
            """, nativeQuery = true)
    List<SensorLog> findLatestPowerByZoneId(@Param("zoneId") Long zoneId);

    @Query(value = """
            SELECT MIN(sl.sensor_logs_id) AS id,
                   sl.devices_id AS device_id,
                   d.device_name AS device_name,
                   DATE_FORMAT(sl.logged_at, '%Y-%m-%dT%H:00:00') AS hour_at,
                   ROUND(AVG(sl.sensor_value) / 1000.0, 4) AS kwh,
                   ROUND(AVG(sl.sensor_value), 2) AS avg_watt,
                   ROUND(MAX(sl.sensor_value), 2) AS peak_watt
            FROM sensor_logs sl
            JOIN devices d ON d.devices_id = sl.devices_id
            WHERE sl.zone_id = :zoneId
              AND sl.sensor_type = 'POWER'
              AND sl.logged_at BETWEEN :start AND :end
              AND (:deviceId IS NULL OR sl.devices_id = :deviceId)
            GROUP BY DATE_FORMAT(sl.logged_at, '%Y-%m-%dT%H:00:00'), sl.devices_id, d.device_name
            ORDER BY hour_at ASC
            """, nativeQuery = true)
    List<HourlyPowerProjection> findHourlyPowerProjection(
            @Param("zoneId") Long zoneId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("deviceId") Long deviceId);

    @Query(value = """
            SELECT zone_id AS zone_id, ROUND(SUM(h_avg) / 1000.0, 2) AS total_kwh
            FROM (
                SELECT zone_id, AVG(sensor_value) AS h_avg
                FROM sensor_logs
                WHERE sensor_type = 'POWER'
                  AND YEAR(logged_at) = :year AND MONTH(logged_at) = :month
                  AND zone_id IN (:zoneIds)
                GROUP BY zone_id, DATE_FORMAT(logged_at, '%Y-%m-%d %H')
            ) t
            GROUP BY zone_id
            """, nativeQuery = true)
    List<MonthlyPowerProjection> findMonthlyKwhByZones(
            @Param("year") int year,
            @Param("month") int month,
            @Param("zoneIds") Collection<Long> zoneIds);

    @Query(value = """
            SELECT zone_id AS zone_id, ROUND(SUM(h_avg) / 1000.0, 2) AS total_kwh
            FROM (
                SELECT zone_id, AVG(sensor_value) AS h_avg
                FROM sensor_logs
                WHERE sensor_type = 'POWER'
                  AND YEAR(logged_at) = :year AND MONTH(logged_at) = :month
                GROUP BY zone_id, DATE_FORMAT(logged_at, '%Y-%m-%d %H')
            ) t
            GROUP BY zone_id
            """, nativeQuery = true)
    List<MonthlyPowerProjection> findMonthlyKwhAllZones(
            @Param("year") int year,
            @Param("month") int month);
}
