package com.grown.smartoffice.domain.attendance.repository;

import com.grown.smartoffice.domain.attendance.entity.MonthlyAttendance;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class MonthlyAttendanceRepositoryTest extends RepositoryTestSupport {

    @Autowired MonthlyAttendanceRepository monthlyAttendanceRepository;
    @Autowired TestEntityManager em;

    private User userA;

    @BeforeEach
    void setUp() {
        Department d = Department.builder().deptName("MAR-DEPT-" + System.nanoTime()).build();
        em.persist(d);
        userA = User.builder().department(d)
                .employeeNumber("MAR-" + System.nanoTime()).employeeName("월간")
                .employeeEmail("mar-" + System.nanoTime() + "@grown.com")
                .password("p").role(UserRole.USER).position("사원")
                .status(UserStatus.ACTIVE).hiredAt(LocalDate.now()).build();
        em.persist(userA);
        em.flush();
    }

    private MonthlyAttendance monthly(User u, int year, int month, int totalMin) {
        return MonthlyAttendance.builder().user(u)
                .monatYear(year).monatMonth(month)
                .monatTotalWorkMinutes(totalMin).monatOvertimeMinutes(0)
                .lateCount(0).earlyLeaveCount(0).absentCount(0).build();
    }

    @Test
    @DisplayName("findByUser_UserIdAndMonatYearAndMonatMonth — 본인+연월 유일")
    void findByUserAndYearMonth() {
        em.persist(monthly(userA, 2026, 3, 2700));
        em.persist(monthly(userA, 2026, 4, 2700));
        em.flush();

        assertThat(monthlyAttendanceRepository
                .findByUser_UserIdAndMonatYearAndMonatMonth(userA.getUserId(), 2026, 3))
                .isPresent();
        assertThat(monthlyAttendanceRepository
                .findByUser_UserIdAndMonatYearAndMonatMonth(userA.getUserId(), 2026, 5))
                .isEmpty();
    }

    @Test
    @DisplayName("findAllByYearAndMonth — JOIN FETCH user 즉시 로딩")
    void findAllByYearAndMonth() {
        em.persist(monthly(userA, 2026, 3, 2700));
        em.flush();
        em.clear();

        List<MonthlyAttendance> result = monthlyAttendanceRepository.findAllByYearAndMonth(2026, 3);
        assertThat(result).isNotEmpty();
        // user fetch 검증 — Lazy proxy가 아닌 실제 객체
        assertThat(result.get(0).getUser().getEmployeeName()).isEqualTo("월간");
    }

    @Test
    @DisplayName("findByEmailAndYearMonth — 이메일+연월로 단건")
    void findByEmailAndYearMonth() {
        em.persist(monthly(userA, 2026, 4, 2700));
        em.flush();

        Optional<MonthlyAttendance> found = monthlyAttendanceRepository
                .findByEmailAndYearMonth(userA.getEmployeeEmail(), 2026, 4);
        assertThat(found).isPresent();

        assertThat(monthlyAttendanceRepository.findByEmailAndYearMonth("not-exist@grown.com", 2026, 4))
                .isEmpty();
    }
}
