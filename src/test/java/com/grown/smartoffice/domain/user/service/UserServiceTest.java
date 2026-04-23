package com.grown.smartoffice.domain.user.service;

import com.grown.smartoffice.domain.department.entity.Department;
import com.grown.smartoffice.domain.department.repository.DepartmentRepository;
import com.grown.smartoffice.domain.user.dto.UserCreateRequest;
import com.grown.smartoffice.domain.user.dto.UserCreateResponse;
import com.grown.smartoffice.domain.user.dto.UserDetailResponse;
import com.grown.smartoffice.domain.user.dto.UserListItemResponse;
import com.grown.smartoffice.domain.user.dto.UserMeInfoResponse;
import com.grown.smartoffice.domain.user.dto.UserMeUpdateRequest;
import com.grown.smartoffice.domain.user.dto.UserMeUpdateResponse;
import com.grown.smartoffice.domain.user.dto.UserUpdateRequest;
import com.grown.smartoffice.domain.user.dto.UserUpdateResponse;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import com.grown.smartoffice.global.common.PageResponse;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import com.grown.smartoffice.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock DepartmentRepository departmentRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks UserServiceImpl userService;

    private Department dept;

    @BeforeEach
    void setUp() {
        dept = TestFixtures.department(1L, "개발팀");
    }

    // ── 목록 조회 ──────────────────────────────────────────

    @Test
    @DisplayName("getUsers: 유효하지 않은 status 파라미터 → INVALID_INPUT")
    void getUsers_invalidStatus_throws() {
        assertThatThrownBy(() -> userService.getUsers(null, "NOT_A_STATUS", null, 0, 20))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("getUsers: 정상 호출 시 PageResponse로 매핑")
    void getUsers_returnsPageResponse() {
        User user = TestFixtures.employeeUser(10L, "e@grown.com", dept);
        Page<User> page = new PageImpl<>(List.of(user));
        given(userRepository.findAllWithFilters(any(), any(), any(), any(Pageable.class)))
                .willReturn(page);

        PageResponse<UserListItemResponse> res = userService.getUsers(null, null, null, 0, 20);

        assertThat(res.content()).hasSize(1);
        assertThat(res.content().get(0).getEmail()).isEqualTo("e@grown.com");
    }

    // ── 직원 등록 ──────────────────────────────────────────

    @Test
    @DisplayName("createUser: 이메일 중복 시 DUPLICATE_EMAIL")
    void createUser_duplicateEmail() {
        UserCreateRequest req = createRequest("EMP100", "dup@grown.com", "ADMIN", 1L);
        given(userRepository.existsByEmployeeEmail("dup@grown.com")).willReturn(true);

        assertThatThrownBy(() -> userService.createUser(req))
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    @DisplayName("createUser: 사번 중복 시 DUPLICATE_EMPLOYEE_NUMBER")
    void createUser_duplicateEmployeeNumber() {
        UserCreateRequest req = createRequest("EMP100", "ok@grown.com", "USER", 1L);
        given(userRepository.existsByEmployeeEmail(anyString())).willReturn(false);
        given(userRepository.existsByEmployeeNumber("EMP100")).willReturn(true);

        assertThatThrownBy(() -> userService.createUser(req))
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_EMPLOYEE_NUMBER);
    }

    @Test
    @DisplayName("createUser: 부서 없음 → DEPARTMENT_NOT_FOUND")
    void createUser_departmentNotFound() {
        UserCreateRequest req = createRequest("EMP100", "ok@grown.com", "USER", 999L);
        given(userRepository.existsByEmployeeEmail(anyString())).willReturn(false);
        given(userRepository.existsByEmployeeNumber(anyString())).willReturn(false);
        given(departmentRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.createUser(req))
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.DEPARTMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("createUser: role 문자열이 enum 이 아니면 INVALID_INPUT")
    void createUser_invalidRole() {
        UserCreateRequest req = createRequest("EMP100", "ok@grown.com", "SUPER", 1L);
        given(userRepository.existsByEmployeeEmail(anyString())).willReturn(false);
        given(userRepository.existsByEmployeeNumber(anyString())).willReturn(false);
        given(departmentRepository.findById(1L)).willReturn(Optional.of(dept));

        assertThatThrownBy(() -> userService.createUser(req))
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("createUser: 초기 비밀번호는 사번을 BCrypt 로 인코딩하여 저장")
    void createUser_initialPasswordEqualsEmployeeNumber() {
        UserCreateRequest req = createRequest("EMP100", "new@grown.com", "USER", 1L);
        given(userRepository.existsByEmployeeEmail(anyString())).willReturn(false);
        given(userRepository.existsByEmployeeNumber(anyString())).willReturn(false);
        given(departmentRepository.findById(1L)).willReturn(Optional.of(dept));
        given(passwordEncoder.encode("EMP100")).willReturn("enc-EMP100");
        given(userRepository.save(any(User.class)))
                .willAnswer(inv -> {
                    User u = inv.getArgument(0);
                    ReflectionTestUtils.setField(u, "userId", 42L);
                    ReflectionTestUtils.setField(u, "createdAt", java.time.LocalDateTime.now());
                    return u;
                });

        UserCreateResponse res = userService.createUser(req);

        assertThat(res.getId()).isEqualTo(42L);
        assertThat(res.getEmail()).isEqualTo("new@grown.com");
        assertThat(res.getRole()).isEqualTo("USER");
        assertThat(res.getStatus()).isEqualTo("ACTIVE");
        verify(passwordEncoder).encode("EMP100");
    }

    // ── 상세 조회 ──────────────────────────────────────────

    @Test
    @DisplayName("getUserDetail: 없으면 USER_NOT_FOUND")
    void getUserDetail_notFound() {
        given(userRepository.findByIdWithDept(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserDetail(99L))
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("getUserDetail: 있으면 DetailResponse 반환")
    void getUserDetail_ok() {
        User user = TestFixtures.adminUser(1L, "admin@grown.com", dept);
        given(userRepository.findByIdWithDept(1L)).willReturn(Optional.of(user));

        UserDetailResponse res = userService.getUserDetail(1L);

        assertThat(res.getId()).isEqualTo(1L);
        assertThat(res.getDepartment()).isEqualTo("개발팀");
    }

    // ── 수정 ──────────────────────────────────────────────

    @Test
    @DisplayName("updateUser: 부서 ID 지정 시 부서 없으면 DEPARTMENT_NOT_FOUND")
    void updateUser_deptNotFound() {
        User user = TestFixtures.employeeUser(1L, "e@grown.com", dept);
        UserUpdateRequest req = updateRequest("새이름", null, null, 99L, null, null);
        given(userRepository.findByIdWithDept(1L)).willReturn(Optional.of(user));
        given(departmentRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(1L, req))
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.DEPARTMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("updateUser: null 필드는 덮어쓰지 않는다")
    void updateUser_partialUpdate() {
        User user = TestFixtures.employeeUser(1L, "e@grown.com", dept);
        UserUpdateRequest req = updateRequest("새이름", null, null, null, null, null);
        given(userRepository.findByIdWithDept(1L)).willReturn(Optional.of(user));

        UserUpdateResponse res = userService.updateUser(1L, req);

        assertThat(res.getName()).isEqualTo("새이름");
        assertThat(res.getDepartment()).isEqualTo("개발팀"); // 기존 값 유지
    }

    // ── 퇴사 처리 ──────────────────────────────────────────

    @Test
    @DisplayName("deactivateUser: 없으면 USER_NOT_FOUND")
    void deactivate_notFound() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deactivateUser(99L))
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("deactivateUser: 이미 INACTIVE 면 USER_ALREADY_INACTIVE")
    void deactivate_alreadyInactive() {
        User u = TestFixtures.inactiveUser(1L, "bye@grown.com", dept);
        given(userRepository.findById(1L)).willReturn(Optional.of(u));

        assertThatThrownBy(() -> userService.deactivateUser(1L))
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_ALREADY_INACTIVE);
    }

    @Test
    @DisplayName("deactivateUser: 성공 시 status 가 INACTIVE 로 변경")
    void deactivate_success() {
        User u = TestFixtures.employeeUser(1L, "ok@grown.com", dept);
        given(userRepository.findById(1L)).willReturn(Optional.of(u));

        userService.deactivateUser(1L);

        assertThat(u.getStatus()).isEqualTo(UserStatus.INACTIVE);
    }

    // ── 내 정보 조회 ───────────────────────────────────────

    @Test
    @DisplayName("getMyInfo: 이메일로 조회 성공 → UserMeInfoResponse")
    void getMyInfo_ok() {
        User user = TestFixtures.adminUser(1L, "admin@grown.com", dept);
        given(userRepository.findByEmployeeEmailWithDept("admin@grown.com"))
                .willReturn(Optional.of(user));

        UserMeInfoResponse res = userService.getMyInfo("admin@grown.com");

        assertThat(res.getEmail()).isEqualTo("admin@grown.com");
        assertThat(res.getDepartment()).isEqualTo("개발팀");
    }

    // ── 내 정보 수정 ───────────────────────────────────────

    @Test
    @DisplayName("updateMyInfo: 비밀번호 변경 시 현재 비밀번호 누락 → MISSING_REQUIRED_FIELD")
    void updateMyInfo_missingCurrentPassword() {
        User user = TestFixtures.adminUser(1L, "admin@grown.com", dept);
        UserMeUpdateRequest req = meUpdateRequest(null, "newPassword1!", null);
        given(userRepository.findByEmployeeEmailWithDept("admin@grown.com"))
                .willReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.updateMyInfo("admin@grown.com", req))
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.MISSING_REQUIRED_FIELD);
    }

    @Test
    @DisplayName("updateMyInfo: 현재 비밀번호 불일치 → WRONG_PASSWORD")
    void updateMyInfo_wrongCurrentPassword() {
        User user = TestFixtures.adminUser(1L, "admin@grown.com", dept);
        UserMeUpdateRequest req = meUpdateRequest(null, "newPassword1!", "wrong");
        given(userRepository.findByEmployeeEmailWithDept("admin@grown.com"))
                .willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong", user.getPassword())).willReturn(false);

        assertThatThrownBy(() -> userService.updateMyInfo("admin@grown.com", req))
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.WRONG_PASSWORD);
    }

    @Test
    @DisplayName("updateMyInfo: 전화번호만 변경, 비밀번호 검증 없음")
    void updateMyInfo_phoneOnly() {
        User user = TestFixtures.adminUser(1L, "admin@grown.com", dept);
        UserMeUpdateRequest req = meUpdateRequest("010-1234-5678", null, null);
        given(userRepository.findByEmployeeEmailWithDept("admin@grown.com"))
                .willReturn(Optional.of(user));

        UserMeUpdateResponse res = userService.updateMyInfo("admin@grown.com", req);

        assertThat(res.getPhone()).isEqualTo("010-1234-5678");
        verify(passwordEncoder, never()).matches(any(), any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("updateMyInfo: 비밀번호 정상 변경 경로")
    void updateMyInfo_changePassword() {
        User user = TestFixtures.adminUser(1L, "admin@grown.com", dept);
        UserMeUpdateRequest req = meUpdateRequest(null, "newPassword1!", "oldPassword1!");
        given(userRepository.findByEmployeeEmailWithDept("admin@grown.com"))
                .willReturn(Optional.of(user));
        given(passwordEncoder.matches("oldPassword1!", user.getPassword())).willReturn(true);
        given(passwordEncoder.encode("newPassword1!")).willReturn("enc-new");

        userService.updateMyInfo("admin@grown.com", req);

        assertThat(user.getPassword()).isEqualTo("enc-new");
    }

    // ── 헬퍼 ──────────────────────────────────────────────

    private UserCreateRequest createRequest(String empNumber, String email, String role, Long deptId) {
        UserCreateRequest r = new UserCreateRequest();
        ReflectionTestUtils.setField(r, "employeeNumber", empNumber);
        ReflectionTestUtils.setField(r, "name", "홍길동");
        ReflectionTestUtils.setField(r, "email", email);
        ReflectionTestUtils.setField(r, "role", role);
        ReflectionTestUtils.setField(r, "position", "사원");
        ReflectionTestUtils.setField(r, "departmentId", deptId);
        ReflectionTestUtils.setField(r, "phone", "010-0000-0000");
        ReflectionTestUtils.setField(r, "hiredAt", LocalDate.of(2026, 3, 2));
        return r;
    }

    private UserUpdateRequest updateRequest(String name, String role, String position,
                                             Long deptId, String phone, LocalDate hiredAt) {
        UserUpdateRequest r = new UserUpdateRequest();
        ReflectionTestUtils.setField(r, "name", name);
        ReflectionTestUtils.setField(r, "role", role);
        ReflectionTestUtils.setField(r, "position", position);
        ReflectionTestUtils.setField(r, "departmentId", deptId);
        ReflectionTestUtils.setField(r, "phone", phone);
        ReflectionTestUtils.setField(r, "hiredAt", hiredAt);
        return r;
    }

    private UserMeUpdateRequest meUpdateRequest(String phone, String newPassword, String currentPassword) {
        UserMeUpdateRequest r = new UserMeUpdateRequest();
        ReflectionTestUtils.setField(r, "phone", phone);
        ReflectionTestUtils.setField(r, "password", newPassword);
        ReflectionTestUtils.setField(r, "currentPassword", currentPassword);
        return r;
    }
}
