package com.grown.smartoffice.domain.auth.controller;

import com.grown.smartoffice.domain.auth.dto.*;
import com.grown.smartoffice.domain.auth.service.AuthService;
import com.grown.smartoffice.domain.auth.support.RefreshTokenCookieProvider;
import com.grown.smartoffice.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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
    private final RefreshTokenCookieProvider cookieProvider;

    @Operation(summary = "로그인",
               description = "이메일·비밀번호로 로그인합니다. Access Token 은 응답 body, "
                       + "Refresh Token 은 httpOnly 쿠키로 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @RequestBody @Valid LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieProvider.create(response.getRefreshToken()).toString())
                .body(ApiResponse.success("로그인에 성공했습니다.", response));
    }

    @Operation(summary = "로그아웃", description = "Refresh Token을 무효화하고 쿠키를 소거합니다. (Bearer 토큰 필요)")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserDetails userDetails) {
        authService.logout(userDetails.getUsername());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieProvider.clear().toString())
                .body(ApiResponse.success("로그아웃 되었습니다."));
    }

    @Operation(summary = "토큰 재발급",
               description = "httpOnly 쿠키의 Refresh Token 으로 새 Access Token 을 발급합니다. "
                       + "쿠키가 없으면 요청 body 의 refreshToken 을 폴백으로 사용합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refresh(
            @RequestBody(required = false) TokenRefreshRequest request,
            HttpServletRequest httpRequest) {
        String token = cookieProvider.resolve(httpRequest);
        if (token == null && request != null) {
            token = request.getRefreshToken();
        }
        TokenRefreshResponse response = authService.refresh(token);
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
