package com.grown.smartoffice.domain.salary.controller;

import com.grown.smartoffice.domain.salary.dto.*;
import com.grown.smartoffice.domain.salary.service.SalaryCalculationService;
import com.grown.smartoffice.domain.salary.service.SalaryRecordService;
import com.grown.smartoffice.global.common.ApiResponse;
import com.grown.smartoffice.global.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Salary Records", description = "급여 산출·확정·조회")
@RestController
@RequestMapping("/api/v1/salary/records")
@RequiredArgsConstructor
public class SalaryRecordController {

    private final SalaryCalculationService calculationService;
    private final SalaryRecordService recordService;

    @Operation(summary = "급여 산출 [ADMIN]", description = "월 근태 집계 기반으로 DRAFT 급여 내역 생성. CONFIRMED 내역은 스킵.")
    @PostMapping("/calculate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SalaryCalculateResponse>> calculate(
            @RequestBody @Valid SalaryCalculateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("급여 산출이 완료되었습니다.", calculationService.calculate(request)));
    }

    @Operation(summary = "급여 확정 [ADMIN]", description = "DRAFT → CONFIRMED. 이후 재계산 대상에서 제외.")
    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SalaryRecordResponse>> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("급여가 확정되었습니다.", recordService.confirm(id)));
    }

    @Operation(summary = "내 급여 조회", description = "CONFIRMED 상태의 내 급여 내역 조회.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<SalaryRecordResponse>> getMy(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(ApiResponse.success("정상 조회되었습니다.",
                recordService.getMy(userDetails.getUsername(), year, month)));
    }

    @Operation(summary = "전체 급여 내역 조회 [ADMIN]")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<SalaryRecordResponse>>> getAll(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("정상 조회되었습니다.",
                recordService.getAll(year, month, userId, status, page, size)));
    }
}
