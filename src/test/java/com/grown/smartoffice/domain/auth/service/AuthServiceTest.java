package com.grown.smartoffice.domain.auth.service;

import com.grown.smartoffice.domain.auth.dto.LoginRequest;
import com.grown.smartoffice.domain.auth.dto.LoginResponse;
import com.grown.smartoffice.domain.auth.dto.MeResponse;
import com.grown.smartoffice.domain.auth.dto.TokenRefreshRequest;
import com.grown.smartoffice.domain.auth.dto.TokenRefreshResponse;
import com.grown.smartoffice.domain.department.entity.Department;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import com.grown.smartoffice.jwt.JwtTokenProvider;
import com.grown.smartoffice.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> valueOperations;

    @InjectMocks AuthService authService;

    private Department dept;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpiration", 604_800_000L);
        dept = TestFixtures.department(1L, "개발팀");
    }

    // ── 로그인 ────────────────────────────────────────────

    @Test
    @DisplayName("로그인 성공 시 access/refresh 토큰 반환 + Redis에 refresh 저장")
    void login_success_storesRefreshInRedis() {
        User user = TestFixtures.adminUser(1L, "admin@grown.com", dept);
        LoginRequest req = loginRequest("admin@grown.com", "EMP001");

        given(userRepository.findByEmployeeEmailWithDept("admin@grown.com"))
                .willReturn(Optional.of(user));
        given(passwordEncoder.matches("EMP001", user.getPassword())).willReturn(true);
        given(jwtTokenProvider.generateAccessToken("admin@grown.com", "ADMIN"))
                .willReturn("access-token");
        given(jwtTokenProvider.generateRefreshToken("admin@grown.com"))
                .willReturn("refresh-token");
        given(jwtTokenProvider.getAccessTokenExpiration()).willReturn(1_800_000L);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        LoginResponse res = authService.login(req);

        assertThat(res.getAccessToken()).isEqualTo("access-token");
        assertThat(res.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(res.getTokenType()).isEqualTo("Bearer");
        assertThat(res.getExpiresIn()).isEqualTo(1800);
        assertThat(res.getUser().getEmail()).isEqualTo("admin@grown.com");
        assertThat(res.getUser().getRole()).isEqualTo("ADMIN");
        assertThat(res.getUser().getDepartment()).isEqualTo("개발팀");

        verify(valueOperations).set(
                eq("auth:refresh:admin@grown.com"),
                eq("refresh-token"),
                eq(604_800_000L),
                eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("존재하지 않는 이메일 → INVALID_CREDENTIALS")
    void login_unknownEmail_throws() {
        given(userRepository.findByEmployeeEmailWithDept(any())).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest("none@grown.com", "pw")))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("퇴사 처리된 계정은 비밀번호 확인 전에 ACCOUNT_INACTIVE")
    void login_inactive_throwsBeforePasswordCheck() {
        User inactive = TestFixtures.inactiveUser(2L, "bye@grown.com", dept);
        given(userRepository.findByEmployeeEmailWithDept("bye@grown.com"))
                .willReturn(Optional.of(inactive));

        assertThatThrownBy(() -> authService.login(loginRequest("bye@grown.com", "pw")))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.ACCOUNT_INACTIVE);

        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("비밀번호 불일치 → INVALID_CREDENTIALS")
    void login_wrongPassword_throws() {
        User user = TestFixtures.adminUser(1L, "admin@grown.com", dept);
        given(userRepository.findByEmployeeEmailWithDept("admin@grown.com"))
                .willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong", user.getPassword())).willReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest("admin@grown.com", "wrong")))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    // ── 로그아웃 ──────────────────────────────────────────

    @Test
    @DisplayName("로그아웃 시 Redis에서 refresh 토큰 삭제")
    void logout_deletesRedisKey() {
        authService.logout("admin@grown.com");

        verify(redisTemplate).delete("auth:refresh:admin@grown.com");
    }

    // ── 토큰 재발급 ───────────────────────────────────────

    @Test
    @DisplayName("유효한 refresh 토큰으로 새 access 토큰 발급")
    void refresh_success() {
        User user = TestFixtures.adminUser(1L, "admin@grown.com", dept);
        given(jwtTokenProvider.validateToken("refresh-ok")).willReturn(true);
        given(jwtTokenProvider.getEmail("refresh-ok")).willReturn("admin@grown.com");
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("auth:refresh:admin@grown.com")).willReturn("refresh-ok");
        given(userRepository.findByEmployeeEmail("admin@grown.com")).willReturn(Optional.of(user));
        given(jwtTokenProvider.generateAccessToken("admin@grown.com", "ADMIN"))
                .willReturn("new-access");
        given(jwtTokenProvider.getAccessTokenExpiration()).willReturn(1_800_000L);

        TokenRefreshResponse res = authService.refresh(refreshRequest("refresh-ok"));

        assertThat(res.getAccessToken()).isEqualTo("new-access");
        assertThat(res.getExpiresIn()).isEqualTo(1800);
    }

    @Test
    @DisplayName("유효하지 않은 refresh 토큰 → INVALID_TOKEN")
    void refresh_invalidToken_throws() {
        given(jwtTokenProvider.validateToken("bad")).willReturn(false);

        assertThatThrownBy(() -> authService.refresh(refreshRequest("bad")))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    @DisplayName("Redis에 저장된 토큰이 없으면 REFRESH_TOKEN_EXPIRED")
    void refresh_redisMissing_throws() {
        given(jwtTokenProvider.validateToken("ok")).willReturn(true);
        given(jwtTokenProvider.getEmail("ok")).willReturn("admin@grown.com");
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("auth:refresh:admin@grown.com")).willReturn(null);

        assertThatThrownBy(() -> authService.refresh(refreshRequest("ok")))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.REFRESH_TOKEN_EXPIRED);
    }

    @Test
    @DisplayName("Redis에 저장된 토큰과 요청 토큰이 불일치 → INVALID_TOKEN")
    void refresh_tokenMismatch_throws() {
        given(jwtTokenProvider.validateToken("current")).willReturn(true);
        given(jwtTokenProvider.getEmail("current")).willReturn("admin@grown.com");
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("auth:refresh:admin@grown.com")).willReturn("different");

        assertThatThrownBy(() -> authService.refresh(refreshRequest("current")))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    // ── 내 정보 조회 ──────────────────────────────────────

    @Test
    @DisplayName("getMe: 존재하지 않는 사용자 → USER_NOT_FOUND")
    void getMe_notFound_throws() {
        given(userRepository.findByEmployeeEmailWithDept("unknown@grown.com"))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getMe("unknown@grown.com"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("getMe: INACTIVE 사용자 → ACCOUNT_INACTIVE")
    void getMe_inactive_throws() {
        User inactive = TestFixtures.inactiveUser(3L, "gone@grown.com", dept);
        given(userRepository.findByEmployeeEmailWithDept("gone@grown.com"))
                .willReturn(Optional.of(inactive));

        assertThatThrownBy(() -> authService.getMe("gone@grown.com"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.ACCOUNT_INACTIVE);
    }

    @Test
    @DisplayName("getMe: ACTIVE 사용자 → MeResponse 반환")
    void getMe_active_returnsResponse() {
        User user = TestFixtures.adminUser(1L, "admin@grown.com", dept);
        given(userRepository.findByEmployeeEmailWithDept("admin@grown.com"))
                .willReturn(Optional.of(user));

        MeResponse res = authService.getMe("admin@grown.com");

        assertThat(res.getEmail()).isEqualTo("admin@grown.com");
        assertThat(res.getRole()).isEqualTo("ADMIN");
        assertThat(res.getDepartment()).isEqualTo("개발팀");
        assertThat(res.getStatus()).isEqualTo("ACTIVE");
    }

    // ── 헬퍼 ──────────────────────────────────────────────

    private LoginRequest loginRequest(String email, String password) {
        LoginRequest r = new LoginRequest();
        ReflectionTestUtils.setField(r, "email", email);
        ReflectionTestUtils.setField(r, "password", password);
        return r;
    }

    private TokenRefreshRequest refreshRequest(String token) {
        TokenRefreshRequest r = new TokenRefreshRequest();
        ReflectionTestUtils.setField(r, "refreshToken", token);
        return r;
    }
}
