package com.grown.smartoffice.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토큰 재발급 요청.
 * Refresh Token 은 httpOnly 쿠키로 전달하는 것이 표준 경로이며,
 * 본 body 필드는 쿠키 미사용 클라이언트를 위한 폴백이다 (선택).
 */
@Getter
@NoArgsConstructor
public class TokenRefreshRequest {

    @Schema(description = "Refresh Token (쿠키 미사용 클라이언트용 폴백, 선택)")
    private String refreshToken;
}
