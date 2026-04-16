package com.grown.smartoffice.domain.user.service;

import com.grown.smartoffice.domain.department.entity.Department;
import com.grown.smartoffice.domain.department.repository.DepartmentRepository;
import com.grown.smartoffice.domain.user.dto.*;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserRole;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import com.grown.smartoffice.global.common.PageResponse;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    // ── 전체 직원 목록 조회 ───────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserListItemResponse> getUsers(Long departmentId, String status,
                                                       String keyword, int page, int size) {
        UserStatus userStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                userStatus = UserStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new CustomException(ErrorCode.INVALID_INPUT);
            }
        }

        var pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());

        return PageResponse.from(
                userRepository.findAllWithFilters(departmentId, userStatus, keyword, pageable)
                              .map(UserListItemResponse::from)
        );
    }

    // ── 직원 등록 ──────────────────────────────────────────

    @Override
    @Transactional
    public UserCreateResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByEmployeeEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByEmployeeNumber(request.getEmployeeNumber())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMPLOYEE_NUMBER);
        }

        // @NotNull 검증 통과 후 진입 — departmentId는 null 불가
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new CustomException(ErrorCode.DEPARTMENT_NOT_FOUND));

        UserRole role;
        try {
            role = UserRole.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        // 초기 비밀번호 = 사번 (직원은 첫 로그인 후 반드시 비밀번호를 변경해야 합니다)
        String encodedPassword = passwordEncoder.encode(request.getEmployeeNumber());

        User user = User.builder()
                .department(department)
                .employeeNumber(request.getEmployeeNumber())
                .employeeName(request.getName())
                .employeeEmail(request.getEmail())
                .password(encodedPassword)
                .role(role)
                .position(request.getPosition())
                .phone(request.getPhone())
                .status(UserStatus.ACTIVE)
                .hiredAt(request.getHiredAt())
                .build();

        return UserCreateResponse.from(userRepository.save(user));
    }

    // ── 직원 상세 조회 ─────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetail(Long userId) {
        User user = userRepository.findByIdWithDept(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return UserDetailResponse.from(user);
    }

    // ── 직원 정보 수정 ─────────────────────────────────────

    @Override
    @Transactional
    public UserUpdateResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findByIdWithDept(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.DEPARTMENT_NOT_FOUND));
        }

        UserRole role = null;
        if (request.getRole() != null && !request.getRole().isBlank()) {
            try {
                role = UserRole.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new CustomException(ErrorCode.INVALID_INPUT);
            }
        }

        user.updateByAdmin(request.getName(), role, request.getPosition(),
                department, request.getPhone(), request.getHiredAt());

        return UserUpdateResponse.from(user);
    }

    // ── 직원 퇴사 처리 ─────────────────────────────────────

    @Override
    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new CustomException(ErrorCode.USER_ALREADY_INACTIVE);
        }

        user.deactivate();
    }

    // ── 내 정보 조회 ───────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserMeInfoResponse getMyInfo(String email) {
        User user = userRepository.findByEmployeeEmailWithDept(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return UserMeInfoResponse.from(user);
    }

    // ── 내 정보 수정 ───────────────────────────────────────

    @Override
    @Transactional
    public UserMeUpdateResponse updateMyInfo(String email, UserMeUpdateRequest request) {
        User user = userRepository.findByEmployeeEmailWithDept(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String encodedPassword = null;
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD);
            }
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new CustomException(ErrorCode.WRONG_PASSWORD);
            }
            encodedPassword = passwordEncoder.encode(request.getPassword());
        }

        user.updateByMe(request.getPhone(), encodedPassword);

        return UserMeUpdateResponse.from(user);
    }
}
