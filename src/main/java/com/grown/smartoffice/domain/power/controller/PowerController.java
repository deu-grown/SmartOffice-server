package com.grown.smartoffice.domain.power.controller;

import com.grown.smartoffice.domain.power.dto.*;
import com.grown.smartoffice.domain.power.service.PowerService;
import com.grown.smartoffice.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Power", description = "전력 관리 [ADMIN 전용]")
@RestController
@RequestMapping("/api/v1/power")
@RequiredArgsConstructor
public class PowerController {

    private final PowerService powerService;

    @Operation(summary = "구역별 실시간 전력 현황 [ADMIN]",
               description = "POWER 타입 센서 로그에서 장치별 최신 전력값을 반환합니다.")
    @GetMapping("/zones/{zoneId}/current")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PowerCurrentResponse>> getCurrentPower(
            @PathVariable Long zoneId) {
        return ResponseEntity.ok(ApiResponse.success("정상 조회되었습니다.",
                powerService.getCurrentPower(zoneId)));
    }

    @Operation(summary = "구역별 시간별 전력 이력 [ADMIN]",
               description = "날짜 범위 내 시간별 집계 전력 데이터를 반환합니다.")
    @GetMapping("/zones/{zoneId}/hourly")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PowerHourlyResponse>> getHourlyHistory(
            @PathVariable Long zoneId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long deviceId) {
        return ResponseEntity.ok(ApiResponse.success("정상 조회되었습니다.",
                powerService.getHourlyHistory(zoneId, startDate, endDate, deviceId)));
    }

    @Operation(summary = "전체 구역 월 요금 현황 [ADMIN]",
               description = "특정 연월의 전체 구역 전력 요금 현황을 반환합니다.")
    @GetMapping("/billing")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PowerBillingAllResponse>> getAllZonesBilling(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(ApiResponse.success("정상 조회되었습니다.",
                powerService.getAllZonesBilling(year, month)));
    }

    @Operation(summary = "구역별 월 요금 내역 [ADMIN]",
               description = "특정 구역의 월별 전력 요금 산출 내역을 반환합니다.")
    @GetMapping("/zones/{zoneId}/billing")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PowerBillingZoneResponse>> getZoneBillingHistory(
            @PathVariable Long zoneId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(ApiResponse.success("정상 조회되었습니다.",
                powerService.getZoneBillingHistory(zoneId, year, month)));
    }

    @Operation(summary = "전력 요금 산출 수동 실행 [ADMIN]",
               description = "특정 연월의 전력 요금을 sensor_logs 기반으로 산출합니다. 기존 내역은 덮어씁니다.")
    @PostMapping("/billing/calculate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PowerBillingCalculateResponse>> calculateBilling(
            @RequestBody @Valid PowerBillingCalculateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("전력 요금 산출이 완료되었습니다.",
                powerService.calculateBilling(request)));
    }
}
