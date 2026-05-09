package com.grown.smartoffice.domain.power.repository;

import com.grown.smartoffice.domain.power.entity.PowerBilling;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PowerBillingRepository extends JpaRepository<PowerBilling, Long> {

    Optional<PowerBilling> findByZone_ZoneIdAndBillingYearAndBillingMonth(
            Long zoneId, Integer year, Integer month);

    @Query("SELECT pb FROM PowerBilling pb JOIN FETCH pb.zone " +
           "WHERE pb.billingYear = :year AND pb.billingMonth = :month")
    List<PowerBilling> findAllByYearAndMonthWithZone(
            @Param("year") Integer year,
            @Param("month") Integer month);

    @Query("SELECT pb FROM PowerBilling pb JOIN FETCH pb.zone " +
           "WHERE pb.zone.zoneId = :zoneId " +
           "AND (:year IS NULL OR pb.billingYear = :year) " +
           "AND (:month IS NULL OR pb.billingMonth = :month) " +
           "ORDER BY pb.billingYear DESC, pb.billingMonth DESC")
    List<PowerBilling> findByZoneIdWithFilters(
            @Param("zoneId") Long zoneId,
            @Param("year") Integer year,
            @Param("month") Integer month);
}
