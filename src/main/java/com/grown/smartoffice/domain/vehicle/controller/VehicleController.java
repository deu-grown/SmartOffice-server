package com.grown.smartoffice.domain.vehicle.controller;

import com.grown.smartoffice.domain.vehicle.dto.VehicleCreateRequest;
import com.grown.smartoffice.domain.vehicle.dto.VehicleResponse;
import com.grown.smartoffice.domain.vehicle.dto.VehicleUpdateRequest;
import com.grown.smartoffice.domain.vehicle.service.VehicleService;
import com.grown.smartoffice.global.common.ApiResponse;
import com.grown.smartoffice.global.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Vehicles", description = "주차 차량 대장 [ADMIN 전용]")
@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class VehicleController {

    private final VehicleService vehicleService;

    @Operation(summary = "차량 등록 [ADMIN]")
    @PostMapping
    public ResponseEntity<ApiResponse<VehicleResponse>> createVehicle(
            @RequestBody @Valid VehicleCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("차량이 등록되었습니다.", vehicleService.createVehicle(request)));
    }

    @Operation(summary = "차량 목록 조회 [ADMIN]",
               description = "필터: vehicleType(STAFF|VISITOR), keyword(번호판·소유자명)")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<VehicleResponse>>> getVehicles(
            @RequestParam(required = false) String vehicleType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("정상 조회되었습니다.",
                vehicleService.getVehicles(vehicleType, keyword, page, size)));
    }

    @Operation(summary = "차량 상세 조회 [ADMIN]")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicle(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("정상 조회되었습니다.", vehicleService.getVehicle(id)));
    }

    @Operation(summary = "차량 수정 [ADMIN]")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @PathVariable Long id,
            @RequestBody @Valid VehicleUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("차량 정보가 수정되었습니다.",
                vehicleService.updateVehicle(id, request)));
    }

    @Operation(summary = "차량 삭제 [ADMIN]")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok(ApiResponse.success("차량이 삭제되었습니다."));
    }
}
