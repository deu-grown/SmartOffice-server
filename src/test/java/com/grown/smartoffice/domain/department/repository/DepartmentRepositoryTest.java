package com.grown.smartoffice.domain.department.repository;

import com.grown.smartoffice.domain.department.entity.Department;
import com.grown.smartoffice.support.RepositoryTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DepartmentRepositoryTest extends RepositoryTestSupport {

    @Autowired DepartmentRepository departmentRepository;
    @Autowired TestEntityManager em;

    @Test
    @DisplayName("existsByDeptName: V2 시드 부서명이면 true")
    void existsByDeptName_seeded_true() {
        assertThat(departmentRepository.existsByDeptName("개발팀")).isTrue();
        assertThat(departmentRepository.existsByDeptName("없는팀")).isFalse();
    }

    @Test
    @DisplayName("existsByDeptNameAndDeptIdNot: 자기 자신 ID 제외 시 중복 없음 → false")
    void existsByDeptNameAndDeptIdNot_selfExcluded_false() {
        Department dept = persistDepartment("임시팀");

        boolean duplicate = departmentRepository.existsByDeptNameAndDeptIdNot(
                "임시팀", dept.getDeptId());

        assertThat(duplicate).isFalse();
    }

    @Test
    @DisplayName("existsByDeptNameAndDeptIdNot: 다른 부서가 같은 이름이면 true")
    void existsByDeptNameAndDeptIdNot_otherUses_true() {
        Department dept = persistDepartment("유일팀");

        boolean duplicate = departmentRepository.existsByDeptNameAndDeptIdNot(
                "유일팀", dept.getDeptId() + 9_999L);

        assertThat(duplicate).isTrue();
    }

    @Test
    @DisplayName("findAllByOrderByCreatedAtAsc: 생성 시간 오름차순, 시드 부서를 먼저 반환")
    void findAllByOrderByCreatedAtAsc_ordered() {
        persistDepartment("테스트팀-늦게");

        List<Department> all = departmentRepository.findAllByOrderByCreatedAtAsc();

        // V2 시드 4개 + 방금 추가 1개 이상
        assertThat(all).hasSizeGreaterThanOrEqualTo(5);
        // 가장 마지막 원소가 방금 추가된 부서
        assertThat(all.get(all.size() - 1).getDeptName()).isEqualTo("테스트팀-늦게");
    }

    private Department persistDepartment(String name) {
        Department d = Department.builder().deptName(name).deptDescription("desc").build();
        em.persist(d);
        em.flush();
        return d;
    }
}
