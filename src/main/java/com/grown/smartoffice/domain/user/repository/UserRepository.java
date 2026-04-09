package com.grown.smartoffice.domain.user.repository;

import com.grown.smartoffice.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u JOIN FETCH u.department WHERE u.employeeEmail = :email")
    Optional<User> findByEmployeeEmailWithDept(@Param("email") String email);

    Optional<User> findByEmployeeEmail(String employeeEmail);

    boolean existsByEmployeeEmail(String employeeEmail);

    boolean existsByEmployeeNumber(String employeeNumber);
}
