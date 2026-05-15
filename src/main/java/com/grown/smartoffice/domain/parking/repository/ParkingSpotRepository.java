package com.grown.smartoffice.domain.parking.repository;

import com.grown.smartoffice.domain.parking.entity.ParkingSpot;
import com.grown.smartoffice.domain.parking.entity.SpotStatus;
import com.grown.smartoffice.domain.parking.entity.SpotType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {

    boolean existsByZone_ZoneIdAndSpotNumber(Long zoneId, String spotNumber);

    boolean existsByZone_ZoneIdAndSpotNumberAndSpotIdNot(Long zoneId, String spotNumber, Long spotId);

    boolean existsByDevice_DevicesId(Long deviceId);

    boolean existsByDevice_DevicesIdAndSpotIdNot(Long deviceId, Long spotId);

    boolean existsByZone_ZoneIdAndPositionXAndPositionY(Long zoneId, Integer positionX, Integer positionY);

    boolean existsByZone_ZoneIdAndPositionXAndPositionYAndSpotIdNot(
            Long zoneId, Integer positionX, Integer positionY, Long spotId);

    @Query("""
            SELECT s FROM ParkingSpot s
            LEFT JOIN FETCH s.device
            JOIN FETCH s.zone
            WHERE s.spotId = :id
            """)
    Optional<ParkingSpot> findByIdWithRelations(@Param("id") Long id);

    @Query("""
            SELECT s FROM ParkingSpot s
            LEFT JOIN FETCH s.device
            JOIN FETCH s.zone
            WHERE (:zoneId   IS NULL OR s.zone.zoneId  = :zoneId)
              AND (:spotType IS NULL OR s.spotType    = :spotType)
              AND (:status   IS NULL OR s.spotStatus  = :status)
            ORDER BY s.zone.zoneId, s.spotNumber
            """)
    List<ParkingSpot> findAllWithFilters(
            @Param("zoneId") Long zoneId,
            @Param("spotType") SpotType spotType,
            @Param("status") SpotStatus status);

    @Query("""
            SELECT s FROM ParkingSpot s
            LEFT JOIN FETCH s.device
            WHERE s.zone.zoneId = :zoneId
            ORDER BY s.spotNumber
            """)
    List<ParkingSpot> findByZoneWithDevice(@Param("zoneId") Long zoneId);

    long countByZone_ZoneId(Long zoneId);

    long countByZone_ZoneIdAndOccupied(Long zoneId, boolean occupied);
}
