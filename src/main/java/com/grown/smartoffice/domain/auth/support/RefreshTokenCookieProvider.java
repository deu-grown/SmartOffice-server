package com.grown.smartoffice.domain.auth.support;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Refresh Token 을 httpOnly 쿠키로 발급·소거·추출하는 컴포넌트.
 * XSS 로 인한 토큰 탈취를 막기 위해 자바스크립트 접근이 불가능한 쿠키에 보관한다.
 * secure / same-site 속성은 프로파일별 설정으로 주입한다 (로컬 http = secure false).
 */
@Component
public class RefreshTokenCookieProvider {

    public static final String COOKIE_NAME = "refreshToken";
    private static final String COOKIE_PATH = "/api/v1/auth";

    private final boolean secure;
    private final String sameSite;
    private final long maxAgeSeconds;

    public RefreshTokenCookieProvider(
            @Value("${app.refresh-cookie.secure:false}") boolean secure,
            @Value("${app.refresh-cookie.same-site:Lax}") String sameSite,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpirationMillis) {
        this.secure = secure;
        this.sameSite = sameSite;
        this.maxAgeSeconds = refreshTokenExpirationMillis / 1000;
    }

    /** 로그인 시 Refresh Token 을 담은 httpOnly 쿠키 생성. */
    public ResponseCookie create(String refreshToken) {
        return base(refreshToken, maxAgeSeconds);
    }

    /** 로그아웃 시 Refresh Token 쿠키 즉시 만료. */
    public ResponseCookie clear() {
        return base("", 0);
    }

    /** 요청 쿠키에서 Refresh Token 추출 (부재 시 null). */
    public String resolve(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private ResponseCookie base(String value, long maxAge) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(COOKIE_PATH)
                .maxAge(maxAge)
                .build();
    }
}
