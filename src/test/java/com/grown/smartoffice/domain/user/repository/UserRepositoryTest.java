package com.grown.smartoffice.domain.user.repository;

import com.grown.smartoffice.domain.department.entity.Department;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserRole;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import com.grown.smartoffice.support.RepositoryTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends RepositoryTestSupport {

    @Autowired UserRepository userRepository;
    @Autowired TestEntityManager em;

    private Department dept1;
    private Department dept2;

    @BeforeEach
    void setUp() {
        dept1 = persistDepartment("QA팀");
        dept2 = persistDepartment("Infra팀");
    }

    // ── findBy* ───────────────────────────────────────────

    @Test
    @DisplayName("findByEmployeeEmail: V2 시드 관리자 계정을 이메일로 조회할 수 있다")
    void findByEmployeeEmail_seededAdmin() {
        Optional<User> user = userRepository.findByEmployeeEmail("admin@grown.com");

        assertThat(user).isPresent();
        assertThat(user.get().getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("findByEmployeeEmail: 미등록 이메일 → empty")
    void findByEmployeeEmail_unknown_returnsEmpty() {
        assertThat(userRepository.findByEmployeeEmail("missing@grown.com")).isEmpty();
    }

    @Test
    @DisplayName("findByEmployeeEmailWithDept: JOIN FETCH 로 Department 를 즉시 로딩")
    void findByEmployeeEmailWithDept_eagerLoadsDept() {
        Optional<User> user = userRepository.findByEmployeeEmailWithDept("admin@grown.com");

        assertThat(user).isPresent();
        assertThat(user.get().getDepartment().getDeptName()).isEqualTo("개발팀");
    }

    @Test
    @DisplayName("findByIdWithDept: JOIN FETCH 로 Department 동반 조회")
    void findByIdWithDept_eagerLoadsDept() {
        User created = persistUser("EMP777", "detail@grown.com", UserRole.USER, UserStatus.ACTIVE, dept1);

        Optional<User> found = userRepository.findByIdWithDept(created.getUserId());

        assertThat(found).isPresent();
        assertThat(found.get().getDepartment().getDeptName()).isEqualTo("QA팀");
    }

    // ── existsBy ──────────────────────────────────────────

    @Test
    @DisplayName("existsByEmployeeEmail: V2 시드 이메일 존재 → true")
    void existsByEmail_seededAdmin_true() {
        assertThat(userRepository.existsByEmployeeEmail("admin@grown.com")).isTrue();
        assertThat(userRepository.existsByEmployeeEmail("nothere@grown.com")).isFalse();
    }

    @Test
    @DisplayName("existsByEmployeeNumber: V2 시드 사번 존재 → true")
    void existsByEmployeeNumber_seededEmp_true() {
        assertThat(userRepository.existsByEmployeeNumber("EMP001")).isTrue();
        assertThat(userRepository.existsByEmployeeNumber("EMP999")).isFalse();
    }

    // ── 집계 (countGroupedByDeptId / getActiveCountByDeptId) ──

    @Test
    @DisplayName("getActiveCountByDeptId: INACTIVE 는 제외, 부서별 ACTIVE 수만 Map 으로 반환")
    void getActiveCountByDeptId_excludesInactive() {
        persistUser("EMP100", "a@grown.com", UserRole.USER, UserStatus.ACTIVE, dept1);
        persistUser("EMP101", "b@grown.com", UserRole.USER, UserStatus.ACTIVE, dept1);
        persistUser("EMP102", "c@grown.com", UserRole.USER, UserStatus.INACTIVE, dept1);
        persistUser("EMP103", "d@grown.com", UserRole.USER, UserStatus.ACTIVE, dept2);

        Map<Long, Long> counts = userRepository.getActiveCountByDeptId();

        assertThat(counts.get(dept1.getDeptId())).isEqualTo(2L);
        assertThat(counts.get(dept2.getDeptId())).isEqualTo(1L);
    }

    @Test
    @DisplayName("countByDeptIdAndStatus: 단일 부서+상태 집계")
    void countByDeptIdAndStatus() {
        persistUser("EMP200", "x@grown.com", UserRole.USER, UserStatus.ACTIVE, dept1);
        persistUser("EMP201", "y@grown.com", UserRole.USER, UserStatus.ACTIVE, dept1);
        persistUser("EMP202", "z@grown.com", UserRole.USER, UserStatus.INACTIVE, dept1);

        long active = userRepository.countByDeptIdAndStatus(dept1.getDeptId(), UserStatus.ACTIVE);
        long inactive = userRepository.countByDeptIdAndStatus(dept1.getDeptId(), UserStatus.INACTIVE);

        assertThat(active).isEqualTo(2L);
        assertThat(inactive).isEqualTo(1L);
    }

    // ── findAllWithFilters ────────────────────────────────

    @Test
    @DisplayName("findAllWithFilters: 필터 없음 → 전체(시드 포함) 페이지 반환")
    void findAllWithFilters_noFilter_returnsAll() {
        persistUser("EMP300", "f1@grown.com", UserRole.USER, UserStatus.ACTIVE, dept1);

        Page<User> page = userRepository.findAllWithFilters(null, null, null, PageRequest.of(0, 20));

        // 시드 admin(EMP001) + 추가 1명 이상 포함
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2L);
    }

    @Test
    @DisplayName("findAllWithFilters: 부서 필터 적용")
    void findAllWithFilters_byDept() {
        persistUser("EMP310", "q1@grown.com", UserRole.USER, UserStatus.ACTIVE, dept1);
        persistUser("EMP311", "q2@grown.com", UserRole.USER, UserStatus.ACTIVE, dept1);
        persistUser("EMP312", "i1@grown.com", UserRole.USER, UserStatus.ACTIVE, dept2);

        Page<User> page = userRepository.findAllWithFilters(
                dept1.getDeptId(), null, null, PageRequest.of(0, 20));

        assertThat(page.getContent())
                .extracting(User::getEmployeeEmail)
                .containsExactlyInAnyOrder("q1@grown.com", "q2@grown.com");
    }

    @Test
    @DisplayName("findAllWithFilters: 상태 필터 적용")
    void findAllWithFilters_byStatus() {
        persistUser("EMP320", "s1@grown.com", UserRole.USER, UserStatus.INACTIVE, dept1);
        persistUser("EMP321", "s2@grown.com", UserRole.USER, UserStatus.ACTIVE, dept1);

        Page<User> page = userRepository.findAllWithFilters(
                dept1.getDeptId(), UserStatus.INACTIVE, null, PageRequest.of(0, 20));

        assertThat(page.getContent())
                .extracting(User::getEmployeeEmail)
                .containsExactly("s1@grown.com");
    }

    @Test
    @DisplayName("findAllWithFilters: 키워드(이름 부분 일치) 필터 적용")
    void findAllWithFilters_byKeyword() {
        persistUserWithName("EMP330", "k1@grown.com", "김철수", dept1);
        persistUserWithName("EMP331", "k2@grown.com", "박영희", dept1);
        persistUserWithName("EMP332", "k3@grown.com", "김영수", dept1);

        Page<User> page = userRepository.findAllWithFilters(
                dept1.getDeptId(), null, "김", PageRequest.of(0, 20));

        assertThat(page.getContent())
                .extracting(User::getEmployeeEmail)
                .containsExactlyInAnyOrder("k1@grown.com", "k3@grown.com");
    }

    // ── 헬퍼 ──────────────────────────────────────────────

    private Department persistDepartment(String name) {
        Department d = Department.builder().deptName(name).deptDescription(name + " desc").build();
        em.persist(d);
        em.flush();
        return d;
    }

    private User persistUser(String empNumber, String email,
                             UserRole role, UserStatus status, Department dept) {
        return persistUserWithName(empNumber, email, "직원-" + empNumber, dept, role, status);
    }

    private User persistUserWithName(String empNumber, String email, String name, Department dept) {
        return persistUserWithName(empNumber, email, name, dept, UserRole.USER, UserStatus.ACTIVE);
    }

    private User persistUserWithName(String empNumber, String email, String name,
                                      Department dept, UserRole role, UserStatus status) {
        User u = User.builder()
                .department(dept)
                .employeeNumber(empNumber)
                .employeeName(name)
                .employeeEmail(email)
                .password("$2a$10$dummy")
                .role(role)
                .position("사원")
                .phone("010-0000-0000")
                .status(status)
                .hiredAt(LocalDate.of(2026, 3, 2))
                .build();
        em.persist(u);
        em.flush();
        return u;
    }
}
