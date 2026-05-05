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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Profile("!prod")
@RequiredArgsConstructor
public class TestAuthService {

    private static final String REFRESH_PREFIX = "auth:refresh:";

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Transactional
    public TestLoginResponse testLogin(String requestEmail, String requestRole) {
        UserRole role = parseRole(requestRole);

        User user;
        boolean autoCreated;

        if (requestEmail == null) {
            Optional<User> found = userRepository.findFirstByRoleAndStatus(role, UserStatus.ACTIVE);
            if (found.isPresent()) {
                user = found.get();
                autoCreated = false;
            } else {
                String generatedEmail = "test-" + role.name().toLowerCase() + "@smartoffice.local";
                user = createDummyUser(generatedEmail, role);
                autoCreated = true;
            }
        } else {
            user = userRepository.findByEmployeeEmail(requestEmail).orElse(null);
            if (user == null) {
                user = createDummyUser(requestEmail, role);
                autoCreated = true;
            } else if (user.getStatus() == UserStatus.INACTIVE) {
                throw new CustomException(ErrorCode.ACCOUNT_INACTIVE);
            } else {
                autoCreated = false;
            }
        }

        String email = user.getEmployeeEmail();
        String accessToken  = jwtTokenProvider.generateAccessToken(email, user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(email);

        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + email,
                refreshToken,
                refreshTokenExpiration,
                TimeUnit.MILLISECONDS
        );

        String deptName = (user.getDepartment() != null) ? user.getDepartment().getDeptName() : null;

        return TestLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn((int) (jwtTokenProvider.getAccessTokenExpiration() / 1000))
                .autoCreated(autoCreated)
                .user(TestLoginResponse.UserInfo.builder()
                        .id(user.getUserId())
                        .name(user.getEmployeeName())
                        .email(email)
                        .role(user.getRole().name())
                        .position(user.getPosition())
                        .department(deptName)
                        .build())
                .build();
    }

    private User createDummyUser(String email, UserRole role) {
        List<Department> departments = departmentRepository.findAllByOrderByCreatedAtAsc();
        if (departments.isEmpty()) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        Department dept = departments.get(0);

        String employeeNumber = "TEST-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

        User dummy = User.builder()
                .department(dept)
                .employeeNumber(employeeNumber)
                .employeeName("테스트 " + role.name())
                .employeeEmail(email)
                .password(passwordEncoder.encode("TEST_DUMMY_PASSWORD"))
                .role(role)
                .position("테스트직급")
                .status(UserStatus.ACTIVE)
                .hiredAt(LocalDate.now())
                .build();

        return userRepository.save(dummy);
    }

    private UserRole parseRole(String roleStr) {
        if (roleStr == null) return UserRole.ADMIN;
        try {
            return UserRole.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
    }
}
