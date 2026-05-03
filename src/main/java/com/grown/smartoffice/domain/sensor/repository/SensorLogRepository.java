package com.grown.smartoffice.domain.sensor.repository;

import com.grown.smartoffice.domain.sensor.entity.SensorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SensorLogRepository extends JpaRepository<SensorLog, Long> {

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
}
