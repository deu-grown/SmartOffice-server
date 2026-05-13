package com.grown.smartoffice.domain.parking.controller;

import com.grown.smartoffice.domain.parking.dto.*;
import com.grown.smartoffice.domain.parking.service.ParkingService;
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

@Tag(name = "Parking", description = "주차면 관리 및 IoT 점유 상태 수신")
@RestController
@RequestMapping("/api/v1/parking")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;

    @Operation(summary = "주차면 등록 [ADMIN]")
    @PostMapping("/spots")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ParkingSpotResponse>> createSpot(
            @RequestBody @Valid ParkingSpotCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("주차면이 등록되었습니다.", parkingService.createSpot(request)));
    }

    @Operation(summary = "주차면 수정 [ADMIN]")
    @PutMapping("/spots/{spotId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ParkingSpotResponse>> updateSpot(
            @PathVariable Long spotId,
            @RequestBody @Valid ParkingSpotUpdateRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("주차면 정보가 수정되었습니다.", parkingService.updateSpot(spotId, request)));
    }

    @Operation(summary = "주차면 삭제 [ADMIN]")
    @DeleteMapping("/spots/{spotId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSpot(@PathVariable Long spotId) {
        parkingService.deleteSpot(spotId);
        return ResponseEntity.ok(ApiResponse.success("주차면이 삭제되었습니다."));
    }

    @Operation(summary = "주차면 목록 조회 [ADMIN]",
               description = "필터: zoneId, spotType(REGULAR|DISABLED|EV), status(ACTIVE|INACTIVE)")
    @GetMapping("/spots")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ParkingSpotResponse>>> getSpots(
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) String spotType,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(
                ApiResponse.success("정상 조회되었습니다.", parkingService.getSpots(zoneId, spotType, status)));
    }

    @Operation(summary = "주차장 전체 현황 조회", description = "구역별 총 주차면·점유·여유 수와 주차면 상세 목록.")
    @GetMapping("/zones/{zoneId}/spots")
    public ResponseEntity<ApiResponse<ParkingZoneSummaryResponse>> getZoneSummary(
            @PathVariable Long zoneId) {
        return ResponseEntity.ok(
                ApiResponse.success("정상 조회되었습니다.", parkingService.getZoneSummary(zoneId)));
    }

    @Operation(summary = "주차장 지도 조회", description = "구역 내 주차면 좌표 + 점유 여부.")
    @GetMapping("/zones/{zoneId}/map")
    public ResponseEntity<ApiResponse<ParkingZoneMapResponse>> getZoneMap(
            @PathVariable Long zoneId) {
        return ResponseEntity.ok(
                ApiResponse.success("정상 조회되었습니다.", parkingService.getZoneMap(zoneId)));
    }

    @Operation(summary = "주차 상태 업데이트 [IoT]",
               description = "IoT 초음파 센서가 점유 상태 변화를 보고. 인증 없음. deviceId 일치 검증.")
    @PostMapping("/spots/{spotId}/status")
    public ResponseEntity<ApiResponse<ParkingStatusUpdateResponse>> updateStatus(
            @PathVariable Long spotId,
            @RequestBody @Valid ParkingStatusUpdateRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("주차 상태가 업데이트되었습니다.",
                        parkingService.updateOccupancyFromIot(spotId, request)));
    }
}
