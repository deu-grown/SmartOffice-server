package com.grown.smartoffice.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET = "TestSecretKeyMustBeAtLeast32BytesLongForHS256!!";
    private static final long ACCESS_EXP = 60_000L;
    private static final long REFRESH_EXP = 600_000L;

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(SECRET, ACCESS_EXP, REFRESH_EXP);
    }

    @Test
    @DisplayName("Access Token 생성 → email과 role 클레임 파싱 성공")
    void accessToken_containsEmailAndRole() {
        String token = provider.generateAccessToken("admin@grown.com", "ADMIN");

        assertThat(provider.getEmail(token)).isEqualTo("admin@grown.com");
        assertThat(provider.getRole(token)).isEqualTo("ADMIN");
        assertThat(provider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Refresh Token은 subject만 포함하고 role 클레임은 없다")
    void refreshToken_hasNoRoleClaim() {
        String token = provider.generateRefreshToken("user@grown.com");

        assertThat(provider.getEmail(token)).isEqualTo("user@grown.com");
        assertThat(provider.getRole(token)).isNull();
        assertThat(provider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("만료된 토큰 → validateToken false")
    void expiredToken_isInvalid() throws InterruptedException {
        JwtTokenProvider shortLived = new JwtTokenProvider(SECRET, 1L, 1L);
        String token = shortLived.generateAccessToken("a@b.com", "USER");

        Thread.sleep(10);

        assertThat(shortLived.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("다른 시크릿 키로 서명된 토큰 → validateToken false")
    void tamperedSignature_isInvalid() {
        String otherSecret = "DifferentSecretKeyButAlso32BytesLongForHS256!!!";
        JwtTokenProvider forger = new JwtTokenProvider(otherSecret, ACCESS_EXP, REFRESH_EXP);
        String forged = forger.generateAccessToken("evil@grown.com", "ADMIN");

        assertThat(provider.validateToken(forged)).isFalse();
    }

    @Test
    @DisplayName("빈 문자열 또는 형식이 잘못된 값 → validateToken false")
    void malformedToken_isInvalid() {
        assertThat(provider.validateToken("")).isFalse();
        assertThat(provider.validateToken("not.a.jwt")).isFalse();
        assertThat(provider.validateToken("abc.def.ghi")).isFalse();
    }

    @Test
    @DisplayName("getAccessTokenExpiration은 생성자 파라미터 값을 그대로 노출한다")
    void accessTokenExpirationIsExposed() {
        assertThat(provider.getAccessTokenExpiration()).isEqualTo(ACCESS_EXP);
    }
}
