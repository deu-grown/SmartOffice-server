package com.grown.smartoffice.domain.department.service;

import com.grown.smartoffice.domain.department.dto.DepartmentCreateRequest;
import com.grown.smartoffice.domain.department.dto.DepartmentCreateResponse;
import com.grown.smartoffice.domain.department.dto.DepartmentListResponse;
import com.grown.smartoffice.domain.department.dto.DepartmentUpdateRequest;
import com.grown.smartoffice.domain.department.dto.DepartmentUpdateResponse;
import com.grown.smartoffice.domain.department.entity.Department;
import com.grown.smartoffice.domain.department.repository.DepartmentRepository;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import com.grown.smartoffice.support.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock DepartmentRepository departmentRepository;
    @Mock UserRepository userRepository;

    @InjectMocks DepartmentServiceImpl departmentService;

    // ── 목록 조회 ──────────────────────────────────────────

    @Test
    @DisplayName("getDepartments: 부서별 ACTIVE 수가 응답에 매핑된다")
    void getDepartments_mapsUserCounts() {
        Department d1 = TestFixtures.department(1L, "개발팀");
        Department d2 = TestFixtures.department(2L, "기획팀");
        given(departmentRepository.findAllByOrderByCreatedAtAsc()).willReturn(List.of(d1, d2));
        given(userRepository.getActiveCountByDeptId()).willReturn(Map.of(1L, 5L));

        List<DepartmentListResponse> res = departmentService.getDepartments();

        assertThat(res).hasSize(2);
        assertThat(res.get(0).getUserCount()).isEqualTo(5L);
        assertThat(res.get(1).getUserCount()).isEqualTo(0L); // 없는 경우 기본값 0
    }

    // ── 등록 ──────────────────────────────────────────────

    @Test
    @DisplayName("createDepartment: 이름 중복 시 DUPLICATE_DEPARTMENT_NAME")
    void create_duplicate() {
        given(departmentRepository.existsByDeptName("개발팀")).willReturn(true);

        assertThatThrownBy(() -> departmentService.createDepartment(createRequest("개발팀", "desc")))
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_DEPARTMENT_NAME);
    }

    @Test
    @DisplayName("createDepartment: 성공 → CreateResponse 반환")
    void create_success() {
        given(departmentRepository.existsByDeptName("신규팀")).willReturn(false);
        given(departmentRepository.save(any(Department.class)))
                .willAnswer(inv -> {
                    Department d = inv.getArgument(0);
                    ReflectionTestUtils.setField(d, "deptId", 99L);
                    ReflectionTestUtils.setField(d, "createdAt", java.time.LocalDateTime.now());
                    return d;
                });

        DepartmentCreateResponse res = departmentService.createDepartment(createRequest("신규팀", "설명"));

        assertThat(res.getId()).isEqualTo(99L);
        assertThat(res.getName()).isEqualTo("신규팀");
    }

    // ── 수정 ──────────────────────────────────────────────

    @Test
    @DisplayName("updateDepartment: 존재하지 않는 ID → DEPARTMENT_NOT_FOUND")
    void update_notFound() {
        given(departmentRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.updateDepartment(99L, updateRequest("새이름", null)))
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.DEPARTMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("updateDepartment: 다른 부서가 같은 이름을 쓰고 있으면 DUPLICATE_DEPARTMENT_NAME")
    void update_duplicateName() {
        Department existing = TestFixtures.department(1L, "개발팀");
        given(departmentRepository.findById(1L)).willReturn(Optional.of(existing));
        given(departmentRepository.existsByDeptNameAndDeptIdNot("기획팀", 1L)).willReturn(true);

        assertThatThrownBy(() -> departmentService.updateDepartment(1L, updateRequest("기획팀", null)))
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_DEPARTMENT_NAME);
    }

    @Test
    @DisplayName("updateDepartment: 성공 → name/description 갱신")
    void update_success() {
        Department existing = TestFixtures.department(1L, "개발팀");
        given(departmentRepository.findById(1L)).willReturn(Optional.of(existing));
        given(departmentRepository.existsByDeptNameAndDeptIdNot("개발팀2", 1L)).willReturn(false);

        DepartmentUpdateResponse res = departmentService.updateDepartment(1L, updateRequest("개발팀2", "새 설명"));

        assertThat(res.getName()).isEqualTo("개발팀2");
        assertThat(res.getDescription()).isEqualTo("새 설명");
    }

    // ── 삭제 ──────────────────────────────────────────────

    @Test
    @DisplayName("deleteDepartment: 존재하지 않는 ID → DEPARTMENT_NOT_FOUND")
    void delete_notFound() {
        given(departmentRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.deleteDepartment(99L))
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.DEPARTMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("deleteDepartment: ACTIVE 소속 직원이 있으면 DEPARTMENT_HAS_USERS")
    void delete_hasUsers() {
        Department existing = TestFixtures.department(1L, "개발팀");
        given(departmentRepository.findById(1L)).willReturn(Optional.of(existing));
        given(userRepository.countByDeptIdAndStatus(1L, UserStatus.ACTIVE)).willReturn(3L);

        assertThatThrownBy(() -> departmentService.deleteDepartment(1L))
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.DEPARTMENT_HAS_USERS);
    }

    @Test
    @DisplayName("deleteDepartment: 직원 없음 → delete 호출")
    void delete_success() {
        Department existing = TestFixtures.department(1L, "개발팀");
        given(departmentRepository.findById(1L)).willReturn(Optional.of(existing));
        given(userRepository.countByDeptIdAndStatus(1L, UserStatus.ACTIVE)).willReturn(0L);

        departmentService.deleteDepartment(1L);

        verify(departmentRepository).delete(existing);
    }

    // ── 헬퍼 ──────────────────────────────────────────────

    private DepartmentCreateRequest createRequest(String name, String desc) {
        DepartmentCreateRequest r = new DepartmentCreateRequest();
        ReflectionTestUtils.setField(r, "name", name);
        ReflectionTestUtils.setField(r, "description", desc);
        return r;
    }

    private DepartmentUpdateRequest updateRequest(String name, String desc) {
        DepartmentUpdateRequest r = new DepartmentUpdateRequest();
        ReflectionTestUtils.setField(r, "name", name);
        ReflectionTestUtils.setField(r, "description", desc);
        return r;
    }
}
