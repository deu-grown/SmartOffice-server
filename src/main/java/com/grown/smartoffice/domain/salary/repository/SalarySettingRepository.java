package com.grown.smartoffice.domain.salary.repository;

import com.grown.smartoffice.domain.salary.entity.SalarySetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalarySettingRepository extends JpaRepository<SalarySetting, Long> {

    @Query("""
           SELECT s FROM SalarySetting s
           WHERE s.salsetPosition = :position
             AND s.effectiveTo IS NULL
           ORDER BY s.effectiveFrom DESC
           """)
    Optional<SalarySetting> findActiveByPosition(@Param("position") String position);

    @Query("""
           SELECT s FROM SalarySetting s
           WHERE s.salsetPosition = :position
             AND s.effectiveFrom <= :date
             AND (s.effectiveTo IS NULL OR s.effectiveTo >= :date)
           ORDER BY s.effectiveFrom DESC
           """)
    Optional<SalarySetting> findApplicableByPositionAndDate(@Param("position") String position,
                                                             @Param("date") LocalDate date);

    List<SalarySetting> findAllBySalsetPosition(String position);
}
