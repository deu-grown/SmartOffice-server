package com.grown.smartoffice.domain.department.service;

import com.grown.smartoffice.domain.department.dto.*;
import com.grown.smartoffice.domain.department.entity.Department;
import com.grown.smartoffice.domain.department.repository.DepartmentRepository;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    // ── 부서 목록 조회 ─────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentListResponse> getDepartments() {
        // 단일 집계 쿼리로 N+1 방지
        Map<Long, Long> countMap = userRepository.getActiveCountByDeptId();

        return departmentRepository.findAllByOrderByCreatedAtAsc().stream()
                .map(dept -> DepartmentListResponse.of(
                        dept,
                        countMap.getOrDefault(dept.getDeptId(), 0L)))
                .toList();
    }

    // ── 부서 등록 ──────────────────────────────────────────

    @Override
    @Transactional
    public DepartmentCreateResponse createDepartment(DepartmentCreateRequest request) {
        if (departmentRepository.existsByDeptName(request.getName())) {
            throw new CustomException(ErrorCode.DUPLICATE_DEPARTMENT_NAME);
        }

        Department department = Department.builder()
                .deptName(request.getName())
                .deptDescription(request.getDescription())
                .build();

        return DepartmentCreateResponse.from(departmentRepository.save(department));
    }

    // ── 부서 수정 ──────────────────────────────────────────

    @Override
    @Transactional
    public DepartmentUpdateResponse updateDepartment(Long deptId, DepartmentUpdateRequest request) {
        Department department = departmentRepository.findById(deptId)
                .orElseThrow(() -> new CustomException(ErrorCode.DEPARTMENT_NOT_FOUND));

        if (departmentRepository.existsByDeptNameAndDeptIdNot(request.getName(), deptId)) {
            throw new CustomException(ErrorCode.DUPLICATE_DEPARTMENT_NAME);
        }

        department.update(request.getName(), request.getDescription());

        return DepartmentUpdateResponse.from(department);
    }

    // ── 부서 삭제 ──────────────────────────────────────────

    @Override
    @Transactional
    public void deleteDepartment(Long deptId) {
        Department department = departmentRepository.findById(deptId)
                .orElseThrow(() -> new CustomException(ErrorCode.DEPARTMENT_NOT_FOUND));

        long userCount = userRepository.countByDeptIdAndStatus(deptId, UserStatus.ACTIVE);
        if (userCount > 0) {
            throw new CustomException(ErrorCode.DEPARTMENT_HAS_USERS);
        }

        departmentRepository.delete(department);
    }
}
