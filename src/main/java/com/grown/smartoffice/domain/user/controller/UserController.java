package com.grown.smartoffice.domain.user.controller;

import com.grown.smartoffice.domain.accesslog.dto.UserAccessLogListResponse;
import com.grown.smartoffice.domain.accesslog.service.AccessLogService;
import com.grown.smartoffice.domain.user.dto.*;
import com.grown.smartoffice.domain.user.service.UserPreferencesService;
import com.grown.smartoffice.domain.user.service.UserService;
import com.grown.smartoffice.global.common.ApiResponse;
import com.grown.smartoffice.global.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users", description = "직원 관리 (ADMIN: 전체 CRUD / EMPLOYEE: 본인 조회·수정)")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AccessLogService accessLogService;
    private final UserPreferencesService userPreferencesService;

    @Operation(summary = "직원 목록 조회 [ADMIN]", description = "부서·상태·키워드로 필터링된 직원 목록을 페이지네이션으로 반환합니다.")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserListItemResponse>>> getUsers(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                ApiResponse.success("정상 조회되었습니다.",
                        userService.getUsers(departmentId, status, keyword, page, size)));
    }

    @Operation(summary = "직원 등록 [ADMIN]", description = "신규 직원 계정을 생성합니다. 초기 비밀번호는 사번입니다.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserCreateResponse>> createUser(
            @RequestBody @Valid UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("직원이 등록되었습니다.", userService.createUser(request)));
    }

    @Operation(summary = "내 정보 조회", description = "로그인한 직원 본인의 상세 정보를 반환합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserMeInfoResponse>> getMyInfo(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("정상 조회되었습니다.",
                        userService.getMyInfo(userDetails.getUsername())));
    }

    @Operation(summary = "내 정보 수정", description = "연락처·비밀번호를 변경합니다. 비밀번호 변경 시 currentPassword 필수.")
    @PostMapping("/me")
    public ResponseEntity<ApiResponse<UserMeUpdateResponse>> updateMyInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid UserMeUpdateRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("정보가 수정되었습니다.",
                        userService.updateMyInfo(userDetails.getUsername(), request)));
    }

    @Operation(summary = "내 환경설정 조회",
               description = "알림·언어·테마·푸시 토큰 설정을 반환합니다. 설정이 없으면 기본값으로 생성합니다.")
    @GetMapping("/me/preferences")
    public ResponseEntity<ApiResponse<UserPreferencesResponse>> getMyPreferences(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("정상 조회되었습니다.",
                        userPreferencesService.getMyPreferences(userDetails.getUsername())));
    }

    @Operation(summary = "내 환경설정 수정",
               description = "알림·언어·테마·푸시 토큰을 부분 수정합니다. null 필드는 기존 값을 유지합니다.")
    @PutMapping("/me/preferences")
    public ResponseEntity<ApiResponse<UserPreferencesResponse>> updateMyPreferences(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid UserPreferencesUpdateRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("환경설정이 수정되었습니다.",
                        userPreferencesService.updateMyPreferences(userDetails.getUsername(), request)));
    }

    @Operation(summary = "직원 상세 조회 [ADMIN]")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUserDetail(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("정상 조회되었습니다.", userService.getUserDetail(id)));
    }

    @Operation(summary = "직원 정보 수정 [ADMIN]", description = "부서·직급·역할 등 관리자가 직원 정보를 수정합니다.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserUpdateResponse>> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UserUpdateRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("직원 정보가 수정되었습니다.", userService.updateUser(id, request)));
    }

    @Operation(summary = "직원 퇴사 처리 [ADMIN]", description = "직원 상태를 INACTIVE로 변경합니다. 복구 불가.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("퇴사 처리되었습니다."));
    }

    @Operation(summary = "특정 직원 출입 이력 조회 [ADMIN]", description = "특정 직원의 출입 이력을 조회합니다.")
    @GetMapping("/{id}/access-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserAccessLogListResponse>> getUserAccessLogs(
            @PathVariable Long id,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("정상 조회되었습니다.",
                accessLogService.getUserAccessLogs(id, startDate, endDate, zoneId, direction, page, size)));
    }
}
