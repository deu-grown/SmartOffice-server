package com.grown.smartoffice.domain.user.controller;

import com.grown.smartoffice.domain.user.dto.*;
import com.grown.smartoffice.domain.user.service.UserService;
import com.grown.smartoffice.global.common.ApiResponse;
import com.grown.smartoffice.global.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 전체 직원 목록 조회 (페이지네이션) */
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

    /** 직원 등록 */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserCreateResponse>> createUser(
            @RequestBody @Valid UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("직원이 등록되었습니다.", userService.createUser(request)));
    }

    /** 내 정보 조회 (직원) */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserMeInfoResponse>> getMyInfo(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("정상 조회되었습니다.",
                        userService.getMyInfo(userDetails.getUsername())));
    }

    /** 내 정보 수정 */
    @PostMapping("/me")
    public ResponseEntity<ApiResponse<UserMeUpdateResponse>> updateMyInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid UserMeUpdateRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("정보가 수정되었습니다.",
                        userService.updateMyInfo(userDetails.getUsername(), request)));
    }

    /** 직원 상세 조회 */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUserDetail(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("정상 조회되었습니다.", userService.getUserDetail(id)));
    }

    /** 직원 정보 수정 */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserUpdateResponse>> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UserUpdateRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("직원 정보가 수정되었습니다.", userService.updateUser(id, request)));
    }

    /** 직원 퇴사 처리 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("퇴사 처리되었습니다."));
    }
}
