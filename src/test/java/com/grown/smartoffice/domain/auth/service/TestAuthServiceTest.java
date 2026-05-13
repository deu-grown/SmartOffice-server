package com.grown.smartoffice.domain.auth.service;

import com.grown.smartoffice.domain.auth.dto.TestLoginResponse;
import com.grown.smartoffice.domain.department.entity.Department;
import com.grown.smartoffice.domain.department.repository.DepartmentRepository;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserRole;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import com.grown.smartoffice.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestAuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock DepartmentRepository departmentRepository;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> valueOps;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks TestAuthService testAuthService;

    private Department department;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(testAuthService, "refreshTokenExpiration", 604_800_000L);

        department = Department.builder().deptName("개발팀").build();
        ReflectionTestUtils.setField(department, "deptId", 1L);

        given(redisTemplate.opsForValue()).willReturn(valueOps);
        given(jwtTokenProvider.generateAccessToken(anyString(), anyString())).willReturn("ACCESS");
        given(jwtTokenProvider.generateRefreshToken(anyString())).willReturn("REFRESH");
        given(jwtTokenProvider.getAccessTokenExpiration()).willReturn(1_800_000L);
    }

    private User newUser(long id, String email, UserRole role, UserStatus status) {
        User u = User.builder()
                .department(department)
                .employeeNumber("E-" + id).employeeName("이름-" + id)
                .employeeEmail(email).password("pw")
                .role(role).position("사원").status(status)
                .hiredAt(LocalDate.now()).build();
        ReflectionTestUtils.setField(u, "userId", id);
        return u;
    }

    @Test
    @DisplayName("email null — role의 첫 ACTIVE 계정 자동 선택 (autoCreated=false)")
    void emailNull_pickFirstActive() {
        User admin = newUser(1L, "admin@grown.com", UserRole.ADMIN, UserStatus.ACTIVE);
        given(userRepository.findFirstByRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE))
                .willReturn(Optional.of(admin));

        TestLoginResponse res = testAuthService.testLogin(null, null);

        assertThat(res.isAutoCreated()).isFalse();
        assertThat(res.getUser().getEmail()).isEqualTo("admin@grown.com");
        verify(valueOps).set(eq("auth:refresh:admin@grown.com"), eq("REFRESH"),
                eq(604_800_000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("email null + 매치 계정 없음 — 더미 계정 생성 (autoCreated=true)")
    void emailNull_noMatch_createsDummy() {
        given(userRepository.findFirstByRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE))
                .willReturn(Optional.empty());
        given(departmentRepository.findAllByOrderByCreatedAtAsc()).willReturn(List.of(department));
        given(passwordEncoder.encode(anyString())).willReturn("HASH");
        given(userRepository.save(any(User.class))).willAnswer(inv -> {
            User u = inv.getArgument(0);
            ReflectionTestUtils.setField(u, "userId", 100L);
            return u;
        });

        TestLoginResponse res = testAuthService.testLogin(null, "ADMIN");
        assertThat(res.isAutoCreated()).isTrue();
        assertThat(res.getUser().getEmail()).isEqualTo("test-admin@smartoffice.local");
    }

    @Test
    @DisplayName("email 지정 + 미존재 — 더미 생성")
    void emailGiven_userNotFound_createsDummy() {
        given(userRepository.findByEmployeeEmail("new@grown.com")).willReturn(Optional.empty());
        given(departmentRepository.findAllByOrderByCreatedAtAsc()).willReturn(List.of(department));
        given(passwordEncoder.encode(anyString())).willReturn("HASH");
        given(userRepository.save(any(User.class))).willAnswer(inv -> {
            User u = inv.getArgument(0);
            ReflectionTestUtils.setField(u, "userId", 200L);
            return u;
        });

        TestLoginResponse res = testAuthService.testLogin("new@grown.com", "USER");
        assertThat(res.isAutoCreated()).isTrue();
    }

    @Test
    @DisplayName("email 지정 + INACTIVE 계정 — ACCOUNT_INACTIVE 차단")
    void emailGiven_inactiveAccount() {
        User u = newUser(5L, "x@grown.com", UserRole.USER, UserStatus.INACTIVE);
        given(userRepository.findByEmployeeEmail("x@grown.com")).willReturn(Optional.of(u));

        assertThatThrownBy(() -> testAuthService.testLogin("x@grown.com", "USER"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.ACCOUNT_INACTIVE);
    }

    @Test
    @DisplayName("role 잘못된 값 — INVALID_INPUT")
    void invalidRole() {
        assertThatThrownBy(() -> testAuthService.testLogin(null, "MANAGER"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("더미 생성 — 부서 없음 시 INTERNAL_SERVER_ERROR")
    void noDepartments_internalError() {
        given(userRepository.findFirstByRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE))
                .willReturn(Optional.empty());
        given(departmentRepository.findAllByOrderByCreatedAtAsc()).willReturn(List.of());

        assertThatThrownBy(() -> testAuthService.testLogin(null, "ADMIN"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
