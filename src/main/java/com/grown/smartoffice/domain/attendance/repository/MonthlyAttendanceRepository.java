package com.grown.smartoffice.domain.attendance.repository;

import com.grown.smartoffice.domain.attendance.entity.MonthlyAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MonthlyAttendanceRepository extends JpaRepository<MonthlyAttendance, Long> {

    Optional<MonthlyAttendance> findByUser_UserIdAndMonatYearAndMonatMonth(Long userId, int year, int month);

    @Query("SELECT m FROM MonthlyAttendance m JOIN FETCH m.user WHERE m.monatYear = :year AND m.monatMonth = :month")
    List<MonthlyAttendance> findAllByYearAndMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT m FROM MonthlyAttendance m WHERE m.user.employeeEmail = :email AND m.monatYear = :year AND m.monatMonth = :month")
    Optional<MonthlyAttendance> findByEmailAndYearMonth(@Param("email") String email,
                                                         @Param("year") int year,
                                                         @Param("month") int month);
}
