package com.grown.smartoffice.domain.control.controller;

import com.grown.smartoffice.domain.control.dto.*;
import com.grown.smartoffice.domain.control.service.ControlService;
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

@Tag(name = "Control", description = "장치 제어 관리 [ADMIN 전용]")
@RestController
@RequestMapping("/api/v1/controls")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ControlController {

    private final ControlService controlService;

    @Operation(summary = "제어 명령 발송 [ADMIN]")
    @PostMapping
    public ResponseEntity<ApiResponse<ControlResponse>> sendCommand(
            @RequestBody @Valid ControlRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("제어 명령이 발송되었습니다.", controlService.sendCommand(request)));
    }

    @Operation(summary = "제어 명령 상세/상태 조회 [ADMIN]")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ControlDetailResponse>> getControlDetail(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("상세 조회가 완료되었습니다.", controlService.getControlDetail(id)));
    }

    @Operation(summary = "제어 명령 이력 조회 [ADMIN]")
    @GetMapping
    public ResponseEntity<ApiResponse<ControlHistoryResponse>> getHistory(
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        return ResponseEntity.ok(
                ApiResponse.success("조회 완료", controlService.getHistory(zoneId, startDate)));
    }
}
