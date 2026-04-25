package com.grown.smartoffice.domain.attendance.controller;

import com.grown.smartoffice.domain.attendance.dto.*;
import com.grown.smartoffice.domain.attendance.service.AttendanceBatchService;
import com.grown.smartoffice.domain.attendance.service.AttendanceCommandService;
import com.grown.smartoffice.domain.attendance.service.AttendanceQueryService;
import com.grown.smartoffice.global.common.ApiResponse;
import com.grown.smartoffice.global.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;

@Tag(name = "Attendance", description = "근태 관리 (조회·수동보정·배치 트리거)")
@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceQueryService queryService;
    private final AttendanceCommandService commandService;
    private final AttendanceBatchService batchService;

    @Operation(summary = "내 일별 근태 조회")
    @GetMapping("/me/daily")
    public ResponseEntity<ApiResponse<AttendanceDailyResponse>> getMyDaily(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success("정상 조회되었습니다.",
                queryService.getMyDaily(userDetails.getUsername(), date)));
    }

    @Operation(summary = "내 월별 근태 집계 조회")
    @GetMapping("/me/monthly")
    public ResponseEntity<ApiResponse<AttendanceMonthlyResponse>> getMyMonthly(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        return ResponseEntity.ok(ApiResponse.success("정상 조회되었습니다.",
                queryService.getMyMonthly(userDetails.getUsername(), yearMonth)));
    }

    @Operation(summary = "전체 직원 일별 근태 조회 [ADMIN]")
    @GetMapping("/daily")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<AttendanceDailyResponse>>> getAllDaily(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long deptId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("정상 조회되었습니다.",
                queryService.getAllDaily(date, name, deptId, page, size)));
    }

    @Operation(summary = "근태 수동 보정 [ADMIN]")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> correctAttendance(
            @PathVariable Long id,
            @RequestBody @Valid AttendanceCorrectRequest request) {
        commandService.correctAttendance(id, request.getCheckIn(), request.getCheckOut(), request.getNote());
        return ResponseEntity.ok(ApiResponse.success("근태 정보가 수정되었습니다."));
    }

    @Operation(summary = "일별 배치 수동 트리거 [ADMIN]", description = "지정 날짜의 근태를 즉시 집계합니다.")
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Integer>> triggerBatch(
            @RequestBody @Valid BatchTriggerRequest request) {
        int count = batchService.runDailyBatch(request.getTargetDate(), "ADMIN_MANUAL");
        return ResponseEntity.ok(ApiResponse.success("배치가 완료되었습니다.", count));
    }
}
