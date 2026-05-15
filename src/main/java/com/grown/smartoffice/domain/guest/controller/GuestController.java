package com.grown.smartoffice.domain.guest.controller;

import com.grown.smartoffice.domain.guest.dto.GuestCreateRequest;
import com.grown.smartoffice.domain.guest.dto.GuestResponse;
import com.grown.smartoffice.domain.guest.dto.GuestUpdateRequest;
import com.grown.smartoffice.domain.guest.service.GuestService;
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

@Tag(name = "Guests", description = "방문객 관리 [ADMIN 전용]")
@RestController
@RequestMapping("/api/v1/guests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class GuestController {

    private final GuestService guestService;

    @Operation(summary = "방문객 등록 [ADMIN]", description = "방문 예약을 SCHEDULED 상태로 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<GuestResponse>> createGuest(
            @RequestBody @Valid GuestCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("방문객이 등록되었습니다.", guestService.createGuest(request)));
    }

    @Operation(summary = "방문객 목록 조회 [ADMIN]",
               description = "필터: status(SCHEDULED|VISITING|COMPLETED|CANCELLED), hostUserId, keyword(이름·회사)")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GuestResponse>>> getGuests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long hostUserId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("정상 조회되었습니다.",
                guestService.getGuests(status, hostUserId, keyword, page, size)));
    }

    @Operation(summary = "방문객 상세 조회 [ADMIN]")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GuestResponse>> getGuest(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("정상 조회되었습니다.", guestService.getGuest(id)));
    }

    @Operation(summary = "방문객 수정 [ADMIN]")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GuestResponse>> updateGuest(
            @PathVariable Long id,
            @RequestBody @Valid GuestUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("방문객 정보가 수정되었습니다.",
                guestService.updateGuest(id, request)));
    }

    @Operation(summary = "방문객 삭제 [ADMIN]")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGuest(@PathVariable Long id) {
        guestService.deleteGuest(id);
        return ResponseEntity.ok(ApiResponse.success("방문객이 삭제되었습니다."));
    }

    @Operation(summary = "방문 시작 (체크인) [ADMIN]",
               description = "SCHEDULED 상태의 방문객을 VISITING 으로 전환하고 입실 시각을 기록합니다.")
    @PostMapping("/{id}/check-in")
    public ResponseEntity<ApiResponse<GuestResponse>> checkIn(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("방문객 체크인이 완료되었습니다.",
                guestService.checkIn(id)));
    }

    @Operation(summary = "방문 종료 (체크아웃) [ADMIN]",
               description = "VISITING 상태의 방문객을 COMPLETED 로 전환하고 퇴실 시각을 기록합니다.")
    @PostMapping("/{id}/check-out")
    public ResponseEntity<ApiResponse<GuestResponse>> checkOut(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("방문객 체크아웃이 완료되었습니다.",
                guestService.checkOut(id)));
    }
}
