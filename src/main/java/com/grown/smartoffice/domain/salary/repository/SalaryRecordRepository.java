package com.grown.smartoffice.domain.salary.repository;

import com.grown.smartoffice.domain.salary.entity.SalaryRecord;
import com.grown.smartoffice.domain.salary.entity.SalaryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SalaryRecordRepository extends JpaRepository<SalaryRecord, Long> {

    Optional<SalaryRecord> findByUser_UserIdAndSalrecYearAndSalrecMonth(Long userId, int year, int month);

    boolean existsBySalarySetting_SalsetId(Long salsetId);

    @Query("""
           SELECT r FROM SalaryRecord r JOIN FETCH r.user
           WHERE r.user.employeeEmail = :email
             AND r.salrecYear = :year
             AND r.salrecMonth = :month
             AND r.salrecStatus = 'CONFIRMED'
           """)
    Optional<SalaryRecord> findMyConfirmed(@Param("email") String email,
                                           @Param("year") int year,
                                           @Param("month") int month);

    @Query("""
           SELECT r FROM SalaryRecord r JOIN FETCH r.user
           WHERE r.salrecYear = :year
             AND r.salrecMonth = :month
             AND (:userId IS NULL OR r.user.userId = :userId)
             AND (:status IS NULL OR r.salrecStatus = :status)
           """)
    Page<SalaryRecord> findAllByYearMonthFiltered(@Param("year") int year,
                                                   @Param("month") int month,
                                                   @Param("userId") Long userId,
                                                   @Param("status") SalaryStatus status,
                                                   Pageable pageable);

    List<SalaryRecord> findAllBySalarySetting_SalsetId(Long salsetId);
}
