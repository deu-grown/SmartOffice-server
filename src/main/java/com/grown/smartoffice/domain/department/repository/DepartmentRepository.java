package com.grown.smartoffice.domain.department.repository;

import com.grown.smartoffice.domain.department.entity.Department;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByDeptName(String deptName);

    boolean existsByDeptNameAndDeptIdNot(String deptName, Long deptId);

    List<Department> findAllByOrderByCreatedAtAsc();

    @Query("SELECT COUNT(u) FROM User u WHERE u.department.deptId = :deptId AND u.status = :status")
    long countByDeptIdAndStatus(@Param("deptId") Long deptId, @Param("status") UserStatus status);
}
