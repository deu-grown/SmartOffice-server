package com.grown.smartoffice.domain.auth.controller;

import com.grown.smartoffice.domain.auth.dto.*;
import com.grown.smartoffice.domain.auth.service.AuthService;
import com.grown.smartoffice.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 (로그인·로그아웃·토큰 재발급)")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "로그인", description = "이메일·비밀번호로 로그인하여 JWT 토큰을 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @RequestBody @Valid LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("로그인에 성공했습니다.", response));
    }

    @Operation(summary = "로그아웃", description = "Refresh Token을 무효화합니다. (Bearer 토큰 필요)")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserDetails userDetails) {
        authService.logout(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("로그아웃 되었습니다."));
    }

    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 새 Access Token을 발급합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refresh(
            @RequestBody @Valid TokenRefreshRequest request) {
        TokenRefreshResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success("토큰이 재발급되었습니다.", response));
    }

    @Operation(summary = "내 인증 정보 조회", description = "JWT에서 파싱한 로그인 사용자 정보를 반환합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MeResponse>> me(
            @AuthenticationPrincipal UserDetails userDetails) {
        MeResponse response = authService.getMe(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("정상 조회되었습니다.", response));
    }
}
