package com.grown.smartoffice.domain.department.controller;

import com.grown.smartoffice.domain.department.dto.*;
import com.grown.smartoffice.domain.department.service.DepartmentService;
import com.grown.smartoffice.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /** 부서 목록 조회 */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DepartmentListResponse>>> getDepartments() {
        return ResponseEntity.ok(
                ApiResponse.success("정상 조회되었습니다.", departmentService.getDepartments()));
    }

    /** 부서 등록 */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentCreateResponse>> createDepartment(
            @RequestBody @Valid DepartmentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("부서가 등록되었습니다.", departmentService.createDepartment(request)));
    }

    /** 부서 수정 */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentUpdateResponse>> updateDepartment(
            @PathVariable Long id,
            @RequestBody @Valid DepartmentUpdateRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("부서 정보가 수정되었습니다.", departmentService.updateDepartment(id, request)));
    }

    /** 부서 삭제 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success("부서가 삭제되었습니다."));
    }
}
