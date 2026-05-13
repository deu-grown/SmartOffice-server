package com.grown.smartoffice.domain.attendance.repository;

import com.grown.smartoffice.domain.attendance.entity.Attendance;
import com.grown.smartoffice.domain.attendance.entity.AttendanceStatus;
import com.grown.smartoffice.domain.department.entity.Department;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserRole;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import com.grown.smartoffice.support.RepositoryTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AttendanceRepositoryTest extends RepositoryTestSupport {

    @Autowired AttendanceRepository attendanceRepository;
    @Autowired TestEntityManager em;

    private User userA;
    private User userB;
    private Department deptA;
    private Department deptB;

    @BeforeEach
    void setUp() {
        deptA = Department.builder().deptName("ATD-DEPT-A-" + System.nanoTime()).build();
        deptB = Department.builder().deptName("ATD-DEPT-B-" + System.nanoTime()).build();
        em.persist(deptA);
        em.persist(deptB);

        userA = newUser(deptA, "ATD-A");
        userB = newUser(deptB, "ATD-B");
        em.persist(userA);
        em.persist(userB);
        em.flush();
    }

    private User newUser(Department dept, String prefix) {
        return User.builder().department(dept)
                .employeeNumber(prefix + "-" + System.nanoTime())
                .employeeName(prefix).employeeEmail(prefix + "-" + System.nanoTime() + "@grown.com")
                .password("p").role(UserRole.USER).position("사원")
                .status(UserStatus.ACTIVE).hiredAt(LocalDate.now()).build();
    }

    private Attendance attendance(User u, LocalDate date, LocalDateTime in) {
        return Attendance.builder().user(u).workDate(date).checkIn(in)
                .attendanceStatus(AttendanceStatus.NORMAL).build();
    }

    @Test
    @DisplayName("findByUser_UserIdAndWorkDate — 본인+날짜 유일 조회")
    void findByUserAndDate() {
        LocalDate d = LocalDate.of(2025, 6, 1);
        em.persist(attendance(userA, d, d.atTime(9, 0)));
        em.flush();

        Optional<Attendance> found =
                attendanceRepository.findByUser_UserIdAndWorkDate(userA.getUserId(), d);
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getUserId()).isEqualTo(userA.getUserId());

        // 다른 사용자 / 다른 날짜는 미발견
        assertThat(attendanceRepository.findByUser_UserIdAndWorkDate(userB.getUserId(), d)).isEmpty();
        assertThat(attendanceRepository.findByUser_UserIdAndWorkDate(userA.getUserId(), d.plusDays(1))).isEmpty();
    }

    @Test
    @DisplayName("findAllByWorkDate — 같은 날의 전체 사용자 반환")
    void findAllByWorkDate() {
        LocalDate d = LocalDate.of(2025, 6, 2);
        em.persist(attendance(userA, d, d.atTime(9, 0)));
        em.persist(attendance(userB, d, d.atTime(9, 5)));
        em.persist(attendance(userA, d.plusDays(1), d.plusDays(1).atTime(9, 0)));
        em.flush();

        List<Attendance> result = attendanceRepository.findAllByWorkDate(d);
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("findAllByDateWithFilters — 이름 부분 일치 + 부서 필터")
    void findAllByDateWithFilters() {
        LocalDate d = LocalDate.of(2025, 6, 3);
        em.persist(attendance(userA, d, d.atTime(9, 0)));
        em.persist(attendance(userB, d, d.atTime(9, 0)));
        em.flush();
        em.clear();

        Page<Attendance> deptAOnly = attendanceRepository.findAllByDateWithFilters(
                d, null, deptA.getDeptId(), PageRequest.of(0, 10));
        assertThat(deptAOnly.getContent()).hasSize(1);

        Page<Attendance> nameMatch = attendanceRepository.findAllByDateWithFilters(
                d, "ATD-A", null, PageRequest.of(0, 10));
        assertThat(nameMatch.getContent()).hasSize(1);

        Page<Attendance> none = attendanceRepository.findAllByDateWithFilters(
                d.plusYears(10), null, null, PageRequest.of(0, 10));
        assertThat(none.getContent()).isEmpty();
    }
}
