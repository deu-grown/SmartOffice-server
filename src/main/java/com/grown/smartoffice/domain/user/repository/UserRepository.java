package com.grown.smartoffice.domain.user.repository;

import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u JOIN FETCH u.department WHERE u.employeeEmail = :email")
    Optional<User> findByEmployeeEmailWithDept(@Param("email") String email);

    Optional<User> findByEmployeeEmail(String employeeEmail);

    @Query("SELECT u FROM User u JOIN FETCH u.department WHERE u.userId = :id")
    Optional<User> findByIdWithDept(@Param("id") Long id);

    boolean existsByEmployeeEmail(String employeeEmail);

    boolean existsByEmployeeNumber(String employeeNumber);

    @Query("SELECT u FROM User u JOIN FETCH u.department d " +
           "WHERE (:deptId IS NULL OR d.deptId = :deptId) " +
           "AND (:status IS NULL OR u.status = :status) " +
           "AND (:keyword IS NULL OR u.employeeName LIKE %:keyword% OR u.employeeNumber LIKE %:keyword%) " +
           "ORDER BY u.createdAt ASC")
    List<User> findAllWithFilters(
            @Param("deptId") Long deptId,
            @Param("status") UserStatus status,
            @Param("keyword") String keyword
    );
}
