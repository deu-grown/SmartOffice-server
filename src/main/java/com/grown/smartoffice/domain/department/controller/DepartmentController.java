package com.grown.smartoffice.domain.department.controller;

import com.grown.smartoffice.domain.department.dto.*;
import com.grown.smartoffice.domain.department.service.DepartmentService;
import com.grown.smartoffice.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Departments", description = "부서 관리 [ADMIN 전용]")
@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(summary = "부서 목록 조회 [ADMIN]", description = "전체 부서와 각 부서의 소속 직원 수를 반환합니다.")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DepartmentListResponse>>> getDepartments() {
        return ResponseEntity.ok(
                ApiResponse.success("정상 조회되었습니다.", departmentService.getDepartments()));
    }

    @Operation(summary = "부서 등록 [ADMIN]")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentCreateResponse>> createDepartment(
            @RequestBody @Valid DepartmentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("부서가 등록되었습니다.", departmentService.createDepartment(request)));
    }

    @Operation(summary = "부서 수정 [ADMIN]")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentUpdateResponse>> updateDepartment(
            @PathVariable Long id,
            @RequestBody @Valid DepartmentUpdateRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("부서 정보가 수정되었습니다.", departmentService.updateDepartment(id, request)));
    }

    @Operation(summary = "부서 삭제 [ADMIN]", description = "소속 직원이 없는 부서만 삭제 가능합니다.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success("부서가 삭제되었습니다."));
    }
}
