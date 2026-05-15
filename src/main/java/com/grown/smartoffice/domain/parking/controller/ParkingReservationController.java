package com.grown.smartoffice.domain.parking.controller;

import com.grown.smartoffice.domain.parking.dto.ParkingReservationCreateRequest;
import com.grown.smartoffice.domain.parking.dto.ParkingReservationResponse;
import com.grown.smartoffice.domain.parking.dto.ParkingReservationUpdateRequest;
import com.grown.smartoffice.domain.parking.service.ParkingReservationService;
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

@Tag(name = "Parking Reservations", description = "주차 예약 관리 [ADMIN 전용]")
@RestController
@RequestMapping("/api/v1/parking/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ParkingReservationController {

    private final ParkingReservationService parkingReservationService;

    @Operation(summary = "주차 예약 등록 [ADMIN]")
    @PostMapping
    public ResponseEntity<ApiResponse<ParkingReservationResponse>> createReservation(
            @RequestBody @Valid ParkingReservationCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("주차 예약이 등록되었습니다.",
                        parkingReservationService.createReservation(request)));
    }

    @Operation(summary = "주차 예약 목록 조회 [ADMIN]",
               description = "필터: zoneId, status(RESERVED|PARKED|EXITED)")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ParkingReservationResponse>>> getReservations(
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("정상 조회되었습니다.",
                parkingReservationService.getReservations(zoneId, status, page, size)));
    }

    @Operation(summary = "주차 예약 상세 조회 [ADMIN]")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ParkingReservationResponse>> getReservation(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("정상 조회되었습니다.",
                parkingReservationService.getReservation(id)));
    }

    @Operation(summary = "주차 예약 수정 [ADMIN]", description = "입차/출차 시각 + 주차면 배정 + 상태 변경.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ParkingReservationResponse>> updateReservation(
            @PathVariable Long id,
            @RequestBody @Valid ParkingReservationUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("주차 예약이 수정되었습니다.",
                parkingReservationService.updateReservation(id, request)));
    }

    @Operation(summary = "주차 예약 삭제 [ADMIN]")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReservation(@PathVariable Long id) {
        parkingReservationService.deleteReservation(id);
        return ResponseEntity.ok(ApiResponse.success("주차 예약이 삭제되었습니다."));
    }
}
