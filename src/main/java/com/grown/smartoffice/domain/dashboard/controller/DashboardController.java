package com.grown.smartoffice.domain.dashboard.controller;

import com.grown.smartoffice.domain.dashboard.dto.AttendanceTodayResponse;
import com.grown.smartoffice.domain.dashboard.dto.DashboardSummaryResponse;
import com.grown.smartoffice.domain.dashboard.dto.RecentAccessResponse;
import com.grown.smartoffice.domain.dashboard.dto.SensorCurrentResponse;
import com.grown.smartoffice.domain.dashboard.service.DashboardService;
import com.grown.smartoffice.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Dashboard", description = "대시보드 요약 정보 [ADMIN 전용]")
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "전체 현황 요약 [ADMIN]",
               description = "총 직원 수, 정상 가동 장치 수 등 대시보드 핵심 지표를 반환합니다.")
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success("정상 조회되었습니다.", dashboardService.getSummary()));
    }

    @Operation(summary = "현재 환경 센서 현황 [ADMIN]",
               description = "구역별 온도·습도·CO2 최신값을 반환합니다. 센서 데이터가 없으면 빈 배열을 반환합니다.")
    @GetMapping("/sensors/current")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SensorCurrentResponse>>> getCurrentSensors() {
        return ResponseEntity.ok(ApiResponse.success("정상 조회되었습니다.", dashboardService.getCurrentSensors()));
    }

    @Operation(summary = "오늘 근태 현황 [ADMIN]",
               description = "오늘 날짜 기준 전체 직원의 출근·결근·지각 현황을 반환합니다.")
    @GetMapping("/attendance/today")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceTodayResponse>> getTodayAttendance() {
        return ResponseEntity.ok(ApiResponse.success("정상 조회되었습니다.", dashboardService.getTodayAttendance()));
    }

    @Operation(summary = "최근 출입 기록 [ADMIN]",
               description = "limit: 최대 건수 (기본 20), type: IN | OUT (생략 시 전체)")
    @GetMapping("/access/recent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RecentAccessResponse>>> getRecentAccess(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(ApiResponse.success("정상 조회되었습니다.",
                dashboardService.getRecentAccess(limit, type)));
    }
}
