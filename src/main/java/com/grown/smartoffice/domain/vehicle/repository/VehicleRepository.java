package com.grown.smartoffice.domain.vehicle.repository;

import com.grown.smartoffice.domain.vehicle.entity.Vehicle;
import com.grown.smartoffice.domain.vehicle.entity.VehicleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    boolean existsByPlateNumber(String plateNumber);

    boolean existsByPlateNumberAndVehicleIdNot(String plateNumber, Long vehicleId);

    @Query("SELECT v FROM Vehicle v LEFT JOIN FETCH v.ownerUser WHERE v.vehicleId = :id")
    Optional<Vehicle> findByIdWithOwner(@Param("id") Long id);

    @Query(value = """
            SELECT v FROM Vehicle v
            WHERE (:vehicleType IS NULL OR v.vehicleType = :vehicleType)
              AND (:keyword IS NULL
                   OR v.plateNumber LIKE %:keyword%
                   OR v.ownerName LIKE %:keyword%)
            """,
           countQuery = """
            SELECT COUNT(v) FROM Vehicle v
            WHERE (:vehicleType IS NULL OR v.vehicleType = :vehicleType)
              AND (:keyword IS NULL
                   OR v.plateNumber LIKE %:keyword%
                   OR v.ownerName LIKE %:keyword%)
            """)
    Page<Vehicle> findAllWithFilters(
            @Param("vehicleType") VehicleType vehicleType,
            @Param("keyword") String keyword,
            Pageable pageable);
}
