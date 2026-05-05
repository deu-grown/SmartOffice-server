package com.grown.smartoffice.domain.auth.controller;

import com.grown.smartoffice.domain.auth.dto.TestLoginRequest;
import com.grown.smartoffice.domain.auth.dto.TestLoginResponse;
import com.grown.smartoffice.domain.auth.service.TestAuthService;
import com.grown.smartoffice.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 (로그인·로그아웃·토큰 재발급)")
@RestController
@RequestMapping("/api/v1/auth")
@Profile("!prod")
@RequiredArgsConstructor
public class TestAuthController {

    private final TestAuthService testAuthService;

    @Operation(
        summary = "[개발 전용] 테스트 로그인",
        description = """
            비밀번호 없이 role·email만으로 JWT 토큰을 즉시 발급합니다. **prod 환경에서는 비활성화됩니다.**

            - `email` 생략 시: 지정 role의 첫 번째 ACTIVE 계정을 자동 선택
            - `email` 지정 + 계정 없음: 더미 계정을 자동 생성하고 토큰 발급 (autoCreated=true)
            - `email` 지정 + INACTIVE 계정: 403 반환
            - `role` 생략 시 기본값: ADMIN
            """
    )
    @PostMapping("/test-login")
    public ResponseEntity<ApiResponse<TestLoginResponse>> testLogin(
            @RequestBody(required = false) TestLoginRequest request) {
        String email = (request != null) ? request.getEmail() : null;
        String role  = (request != null) ? request.getRole()  : null;

        TestLoginResponse response = testAuthService.testLogin(email, role);

        String message = response.isAutoCreated()
                ? "테스트 로그인 성공 (계정 자동 생성)"
                : "테스트 로그인 성공";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }
}
