package com.grown.smartoffice.domain.attendance.repository;

import com.grown.smartoffice.domain.attendance.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByUser_UserIdAndWorkDate(Long userId, LocalDate workDate);

    List<Attendance> findAllByWorkDate(LocalDate workDate);

    @Query("""
           SELECT a FROM Attendance a JOIN FETCH a.user u
           WHERE a.workDate = :date
             AND (:name IS NULL OR u.employeeName LIKE %:name%)
             AND (:deptId IS NULL OR u.department.deptId = :deptId)
           """)
    Page<Attendance> findAllByDateWithFilters(@Param("date") LocalDate date,
                                              @Param("name") String name,
                                              @Param("deptId") Long deptId,
                                              Pageable pageable);

    @Query("SELECT a FROM Attendance a WHERE a.workDate = :date")
    List<Attendance> findAllByWorkDateEager(@Param("date") LocalDate date);
}
