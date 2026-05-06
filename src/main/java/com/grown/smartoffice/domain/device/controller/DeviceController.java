package com.grown.smartoffice.domain.device.controller;

import com.grown.smartoffice.domain.device.dto.*;
import com.grown.smartoffice.domain.device.service.DeviceService;
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

@Tag(name = "Devices", description = "장치 관리 [ADMIN 전용]")
@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DeviceController {

    private final DeviceService deviceService;

    @Operation(summary = "전체 장치 목록 조회 [ADMIN]")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DeviceListItemResponse>>> getAllDevices() {
        return ResponseEntity.ok(
                ApiResponse.success("정상 조회되었습니다.", deviceService.getAllDevices()));
    }

    @Operation(summary = "장치 등록 [ADMIN]")
    @PostMapping
    public ResponseEntity<ApiResponse<DeviceCreateResponse>> registerDevice(
            @RequestBody @Valid DeviceCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("장치가 등록되었습니다.", deviceService.registerDevice(request)));
    }

    @Operation(summary = "장치 상세 조회 [ADMIN]")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeviceDetailResponse>> getDeviceDetail(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("정상 조회되었습니다.", deviceService.getDeviceDetail(id)));
    }

    @Operation(summary = "장치 정보 수정 [ADMIN]")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DeviceUpdateResponse>> updateDevice(
            @PathVariable Long id,
            @RequestBody @Valid DeviceUpdateRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("장치 정보가 수정되었습니다.", deviceService.updateDevice(id, request)));
    }

    @Operation(summary = "장치 삭제 [ADMIN]")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.ok(ApiResponse.success("장치가 삭제되었습니다."));
    }
}
