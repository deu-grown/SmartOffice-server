package com.grown.smartoffice.domain.accesslog.controller;

import com.grown.smartoffice.domain.accesslog.dto.AllAccessLogListResponse;
import com.grown.smartoffice.domain.accesslog.dto.TagEventRequest;
import com.grown.smartoffice.domain.accesslog.dto.TagEventResponse;
import com.grown.smartoffice.domain.accesslog.service.AccessLogService;
import com.grown.smartoffice.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Access Logs", description = "NFC 출입 이벤트 처리 및 로그 조회")
@RestController
@RequestMapping("/api/v1/access-logs")
@RequiredArgsConstructor
public class AccessLogController {

    private final AccessLogService accessLogService;

    @Operation(summary = "NFC 태그 이벤트 처리",
               description = "IoT 장치(RPi)가 NFC 태그를 감지하면 호출. 출입 판정 후 결과 반환. 인증 불필요.")
    @PostMapping("/tag")
    public ResponseEntity<ApiResponse<TagEventResponse>> processTag(
            @RequestBody @Valid TagEventRequest request) {
        TagEventResponse response = accessLogService.processTag(request);
        return ResponseEntity.ok(ApiResponse.success("출입 판정이 완료되었습니다.", response));
    }

    @Operation(summary = "전체 출입 로그 조회 [ADMIN]", description = "구역, 직원, 인증 결과 등으로 필터링된 전체 출입 로그를 조회합니다.")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AllAccessLogListResponse>> getAllAccessLogs(
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String authResult,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("정상 조회되었습니다.",
                accessLogService.getAllAccessLogs(zoneId, userId, authResult, direction, startDate, endDate, page, size)));
    }

    @Operation(summary = "내 출입 이력 조회", description = "로그인한 직원 본인의 출입 이력을 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AllAccessLogListResponse>> getMyAccessLogs(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("정상 조회되었습니다.",
                accessLogService.getMyAccessLogs(userDetails.getUsername(), startDate, endDate, direction, page, size)));
    }
}
