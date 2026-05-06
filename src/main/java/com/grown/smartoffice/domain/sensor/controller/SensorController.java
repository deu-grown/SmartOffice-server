package com.grown.smartoffice.domain.sensor.controller;

import com.grown.smartoffice.domain.sensor.dto.*;
import com.grown.smartoffice.domain.sensor.service.SensorService;
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

@Tag(name = "Sensors", description = "환경/센서 데이터 관리")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SensorController {

    private final SensorService sensorService;

    @Operation(summary = "센서 데이터 수신 (IoT → 서버) [SYSTEM]")
    @PostMapping("/sensors/logs")
    public ResponseEntity<ApiResponse<SensorLogResponse>> recordLog(
            @RequestBody @Valid SensorLogRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("데이터가 정상적으로 기록되었습니다.", sensorService.recordLog(request)));
    }

    @Operation(summary = "구역별 최신 센서 데이터 조회 [ADMIN]")
    @GetMapping("/sensors/latest")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SensorLatestResponse>> getLatestData(
            @RequestParam Long zoneId) {
        return ResponseEntity.ok(
                ApiResponse.success("최신 센서 데이터 조회가 완료되었습니다.", sensorService.getLatestData(zoneId)));
    }

    @Operation(summary = "구역별 센서 로그 이력 조회 [ADMIN]")
    @GetMapping("/zones/{id}/sensors/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SensorHistoryResponse>> getHistory(
            @PathVariable("id") Long zoneId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(
                ApiResponse.success("센서 로그 이력 조회가 완료되었습니다.", 
                        sensorService.getHistory(zoneId, startDate, endDate)));
    }
}
