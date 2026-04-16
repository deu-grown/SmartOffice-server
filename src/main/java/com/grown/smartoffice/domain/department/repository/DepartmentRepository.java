package com.grown.smartoffice.domain.department.repository;

import com.grown.smartoffice.domain.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByDeptName(String deptName);

    boolean existsByDeptNameAndDeptIdNot(String deptName, Long deptId);

    List<Department> findAllByOrderByCreatedAtAsc();
}
