package com.grown.smartoffice.domain.salary.controller;

import com.grown.smartoffice.domain.salary.dto.*;
import com.grown.smartoffice.domain.salary.service.SalarySettingService;
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

@Tag(name = "Salary Settings", description = "급여 기준 관리 [ADMIN 전용]")
@RestController
@RequestMapping("/api/v1/salary/settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SalarySettingController {

    private final SalarySettingService salarySettingService;

    @Operation(summary = "급여 기준 등록 [ADMIN]", description = "동일 직급의 이전 기준이 있으면 effectiveTo를 자동 설정합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<SalarySettingResponse>> create(
            @RequestBody @Valid SalarySettingCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("급여 기준이 등록되었습니다.", salarySettingService.create(request)));
    }

    @Operation(summary = "급여 기준 목록 조회 [ADMIN]")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SalarySettingResponse>>> list(
            @RequestParam(required = false) String position) {
        return ResponseEntity.ok(ApiResponse.success("정상 조회되었습니다.", salarySettingService.list(position)));
    }

    @Operation(summary = "급여 기준 수정 [ADMIN]", description = "이미 급여 산출에 사용된 기준은 수정 불가 (409).")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SalarySettingResponse>> update(
            @PathVariable Long id,
            @RequestBody @Valid SalarySettingUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("급여 기준이 수정되었습니다.", salarySettingService.update(id, request)));
    }
}
