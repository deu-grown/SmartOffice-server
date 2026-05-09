package com.grown.smartoffice.domain.reservation.controller;

import com.grown.smartoffice.domain.reservation.dto.*;
import com.grown.smartoffice.domain.reservation.service.ReservationService;
import com.grown.smartoffice.global.common.ApiResponse;
import com.grown.smartoffice.global.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Reservations", description = "예약 관리")
@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "예약 생성")
    @PostMapping("/api/v1/reservations")
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @RequestBody @Valid ReservationCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("예약이 성공적으로 생성되었습니다.",
                        reservationService.createReservation(request, userDetails.getUsername())));
    }

    @Operation(summary = "예약 상세 조회")
    @GetMapping("/api/v1/reservations/{id}")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservation(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("예약 상세 조회가 완료되었습니다.",
                reservationService.getReservation(id)));
    }

    @Operation(summary = "예약 수정 (본인/ADMIN)")
    @PutMapping("/api/v1/reservations/{id}")
    public ResponseEntity<ApiResponse<ReservationResponse>> updateReservation(
            @PathVariable Long id,
            @RequestBody @Valid ReservationUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("예약 정보가 성공적으로 수정되었습니다.",
                reservationService.updateReservation(id, request, userDetails.getUsername())));
    }

    @Operation(summary = "예약 취소 (본인/ADMIN)")
    @DeleteMapping("/api/v1/reservations/{id}")
    public ResponseEntity<ApiResponse<Long>> cancelReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long cancelledId = reservationService.cancelReservation(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("예약이 정상적으로 취소되었습니다.", cancelledId));
    }

    @Operation(summary = "전체 예약 목록 조회 [ADMIN]")
    @GetMapping("/api/v1/reservations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<ReservationListResponse.ReservationListItem>>> getAllReservations(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("전체 예약 목록 조회가 완료되었습니다.",
                reservationService.getAllReservations(status, page, size)));
    }

    @Operation(summary = "내 예약 목록 조회")
    @GetMapping("/api/v1/reservations/me")
    public ResponseEntity<ApiResponse<ReservationListResponse>> getMyReservations(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("내 예약 목록 조회가 완료되었습니다.",
                reservationService.getMyReservations(userDetails.getUsername())));
    }

    @Operation(summary = "구역별 예약 현황 조회",
               description = "특정 날짜의 구역 예약 현황을 반환합니다. date 미입력 시 오늘 기준.")
    @GetMapping("/api/v1/zones/{zoneId}/reservations")
    public ResponseEntity<ApiResponse<ReservationListResponse>> getZoneReservations(
            @PathVariable Long zoneId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success("구역별 예약 현황 조회가 완료되었습니다.",
                reservationService.getZoneReservations(zoneId, date)));
    }

    @Operation(summary = "예약 NFC 체크인",
               description = "예약 시작 10분 전부터 종료 전까지 체크인 가능합니다.")
    @PostMapping("/api/v1/reservations/{id}/check-in")
    public ResponseEntity<ApiResponse<ReservationCheckInResponse>> checkIn(
            @PathVariable Long id,
            @RequestBody @Valid ReservationCheckInRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("체크인이 성공적으로 완료되었습니다.",
                reservationService.checkIn(id, request, userDetails.getUsername())));
    }
}
