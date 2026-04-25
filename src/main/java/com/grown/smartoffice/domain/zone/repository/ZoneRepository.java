package com.grown.smartoffice.domain.zone.repository;

import com.grown.smartoffice.domain.zone.entity.Zone;
import com.grown.smartoffice.domain.zone.entity.ZoneType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ZoneRepository extends JpaRepository<Zone, Long> {

    @Query("SELECT z FROM Zone z LEFT JOIN FETCH z.children WHERE z.parent IS NULL")
    List<Zone> findRootsWithChildren();

    @Query("SELECT z FROM Zone z WHERE z.parent IS NULL")
    List<Zone> findAllRoots();

    List<Zone> findAllByParent_ZoneId(Long parentId);

    boolean existsByParent_ZoneIdAndZoneName(Long parentId, String zoneName);

    boolean existsByParentIsNullAndZoneName(String zoneName);

    boolean existsByParent_ZoneId(Long parentId);

    @Query("SELECT z FROM Zone z LEFT JOIN FETCH z.parent WHERE z.zoneId = :id")
    Optional<Zone> findByIdWithParent(@Param("id") Long id);

    @Query("SELECT COUNT(d) > 0 FROM Zone z JOIN z.children d WHERE z.zoneId = :zoneId")
    boolean hasChildren(@Param("zoneId") Long zoneId);

    @Query(value = "SELECT COUNT(*) > 0 FROM devices WHERE zone_id = :zoneId", nativeQuery = true)
    boolean hasDevices(@Param("zoneId") Long zoneId);

    List<Zone> findAllByZoneType(ZoneType zoneType);

    List<Zone> findAllByParent_ZoneIdAndZoneType(Long parentId, ZoneType zoneType);
}
