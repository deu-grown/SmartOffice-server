package com.grown.smartoffice.domain.department.service;

import com.grown.smartoffice.domain.department.dto.*;

import java.util.List;

public interface DepartmentService {

    List<DepartmentListResponse> getDepartments();

    DepartmentCreateResponse createDepartment(DepartmentCreateRequest request);

    DepartmentUpdateResponse updateDepartment(Long deptId, DepartmentUpdateRequest request);

    void deleteDepartment(Long deptId);
}
