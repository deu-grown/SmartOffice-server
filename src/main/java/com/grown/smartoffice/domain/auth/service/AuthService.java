package com.grown.smartoffice.domain.auth.service;

import com.grown.smartoffice.domain.auth.dto.*;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import com.grown.smartoffice.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String REFRESH_PREFIX = "auth:refresh:";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // ── 로그인 ────────────────────────────────────────────

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmployeeEmailWithDept(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        // 퇴사 계정 확인을 먼저 → 비밀번호 검증 전에 차단해 타이밍 어택 방지
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new CustomException(ErrorCode.ACCOUNT_INACTIVE);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken  = jwtTokenProvider.generateAccessToken(user.getEmployeeEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmployeeEmail());

        // Refresh Token → Redis (기존 토큰 덮어쓰기 = 단일 세션 정책)
        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + user.getEmployeeEmail(),
                refreshToken,
                refreshTokenExpiration,
                TimeUnit.MILLISECONDS
        );

        String deptName = (user.getDepartment() != null) ? user.getDepartment().getDeptName() : null;

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn((int) (jwtTokenProvider.getAccessTokenExpiration() / 1000))
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getUserId())
                        .name(user.getEmployeeName())
                        .email(user.getEmployeeEmail())
                        .role(user.getRole().name())
                        .position(user.getPosition())
                        .department(deptName)
                        .build())
                .build();
    }

    // ── 로그아웃 ──────────────────────────────────────────

    // @Transactional 불필요 — Redis 연산만 수행하며 DB 트랜잭션이 필요없음
    public void logout(String email) {
        redisTemplate.delete(REFRESH_PREFIX + email);
        log.debug("[Auth] 로그아웃 처리 완료: {}", email);
    }

    // ── 토큰 재발급 ───────────────────────────────────────

    @Transactional(readOnly = true)
    public TokenRefreshResponse refresh(TokenRefreshRequest request) {
        String token = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(token)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        String email = jwtTokenProvider.getEmail(token);

        String stored = redisTemplate.opsForValue().get(REFRESH_PREFIX + email);
        if (stored == null) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }
        if (!stored.equals(token)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        User user = userRepository.findByEmployeeEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtTokenProvider.generateAccessToken(email, user.getRole().name());

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn((int) (jwtTokenProvider.getAccessTokenExpiration() / 1000))
                .build();
    }

    // ── 내 정보 조회 ──────────────────────────────────────

    @Transactional(readOnly = true)
    public MeResponse getMe(String email) {
        User user = userRepository.findByEmployeeEmailWithDept(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new CustomException(ErrorCode.ACCOUNT_INACTIVE);
        }

        String deptName = (user.getDepartment() != null) ? user.getDepartment().getDeptName() : null;

        return MeResponse.builder()
                .id(user.getUserId())
                .employeeNumber(user.getEmployeeNumber())
                .name(user.getEmployeeName())
                .email(user.getEmployeeEmail())
                .role(user.getRole().name())
                .position(user.getPosition())
                .department(deptName)
                .phone(user.getPhone())
                .hiredAt(user.getHiredAt() != null ? user.getHiredAt().toString() : null)
                .status(user.getStatus().name())
                .build();
    }
}
