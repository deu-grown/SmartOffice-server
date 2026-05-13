package com.grown.smartoffice.domain.salary.repository;

import com.grown.smartoffice.domain.attendance.entity.MonthlyAttendance;
import com.grown.smartoffice.domain.department.entity.Department;
import com.grown.smartoffice.domain.salary.entity.SalaryRecord;
import com.grown.smartoffice.domain.salary.entity.SalaryStatus;
import com.grown.smartoffice.domain.salary.entity.SalarySetting;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SalaryRecordRepositoryTest extends RepositoryTestSupport {

    @Autowired SalaryRecordRepository salaryRecordRepository;
    @Autowired TestEntityManager em;

    private User user;
    private SalarySetting setting;
    private MonthlyAttendance monat;

    @BeforeEach
    void setUp() {
        Department d = Department.builder().deptName("SRR-DEPT-" + System.nanoTime()).build();
        em.persist(d);
        user = User.builder().department(d)
                .employeeNumber("SRR-" + System.nanoTime()).employeeName("샐러리")
                .employeeEmail("srr-" + System.nanoTime() + "@grown.com")
                .password("p").role(UserRole.USER).position("개발자")
                .status(UserStatus.ACTIVE).hiredAt(LocalDate.now()).build();
        em.persist(user);

        setting = SalarySetting.builder().salsetPosition("개발자").baseSalary(4000000)
                .overtimeRate(new BigDecimal("1.5")).nightRate(new BigDecimal("2.0"))
                .effectiveFrom(LocalDate.of(2026, 1, 1)).build();
        em.persist(setting);

        monat = MonthlyAttendance.builder().user(user).monatYear(2026).monatMonth(5)
                .monatTotalWorkMinutes(2700).monatOvertimeMinutes(0)
                .lateCount(0).earlyLeaveCount(0).absentCount(0).build();
        em.persist(monat);
        em.flush();
    }

    private SalaryRecord record(int year, int month, SalaryStatus status) {
        SalaryRecord r = SalaryRecord.builder()
                .user(user).monthlyAttendance(monat).salarySetting(setting)
                .salrecYear(year).salrecMonth(month)
                .salrecBaseSalary(4000000).overtimePay(0).totalPay(4000000).build();
        if (status != SalaryStatus.DRAFT) ReflectionTestUtils.setField(r, "salrecStatus", status);
        return r;
    }

    @Test
    @DisplayName("findByUser_UserIdAndSalrecYearAndSalrecMonth — 본인+연월 유일")
    void findByUserAndYearMonth() {
        em.persist(record(2026, 4, SalaryStatus.DRAFT));
        em.persist(record(2026, 5, SalaryStatus.CONFIRMED));
        em.flush();

        Optional<SalaryRecord> april = salaryRecordRepository
                .findByUser_UserIdAndSalrecYearAndSalrecMonth(user.getUserId(), 2026, 4);
        assertThat(april).hasValueSatisfying(r -> assertThat(r.getSalrecStatus()).isEqualTo(SalaryStatus.DRAFT));

        assertThat(salaryRecordRepository
                .findByUser_UserIdAndSalrecYearAndSalrecMonth(user.getUserId(), 2026, 6))
                .isEmpty();
    }

    @Test
    @DisplayName("existsBySalarySetting_SalsetId — 사용 중인 기준 검출")
    void existsBySalsetId() {
        em.persist(record(2026, 5, SalaryStatus.DRAFT));
        em.flush();

        assertThat(salaryRecordRepository.existsBySalarySetting_SalsetId(setting.getSalsetId())).isTrue();
        assertThat(salaryRecordRepository.existsBySalarySetting_SalsetId(99999L)).isFalse();
    }

    @Test
    @DisplayName("findMyConfirmed — CONFIRMED 상태만 매치")
    void findMyConfirmed() {
        em.persist(record(2026, 5, SalaryStatus.DRAFT));
        em.persist(record(2026, 4, SalaryStatus.CONFIRMED));
        em.flush();
        em.clear();

        assertThat(salaryRecordRepository.findMyConfirmed(user.getEmployeeEmail(), 2026, 5)).isEmpty();
        assertThat(salaryRecordRepository.findMyConfirmed(user.getEmployeeEmail(), 2026, 4)).isPresent();
    }

    @Test
    @DisplayName("findAllByYearMonthFiltered — userId/status 필터 + 페이지")
    void findAllByYearMonthFiltered() {
        em.persist(record(2026, 5, SalaryStatus.DRAFT));
        em.persist(record(2026, 4, SalaryStatus.CONFIRMED));
        em.flush();
        em.clear();

        Page<SalaryRecord> mayDraft = salaryRecordRepository.findAllByYearMonthFiltered(
                2026, 5, user.getUserId(), SalaryStatus.DRAFT, PageRequest.of(0, 10));
        assertThat(mayDraft.getContent()).hasSize(1);

        Page<SalaryRecord> mayConfirmed = salaryRecordRepository.findAllByYearMonthFiltered(
                2026, 5, user.getUserId(), SalaryStatus.CONFIRMED, PageRequest.of(0, 10));
        assertThat(mayConfirmed.getContent()).isEmpty();

        Page<SalaryRecord> myMay = salaryRecordRepository.findAllByYearMonthFiltered(
                2026, 5, user.getUserId(), null, PageRequest.of(0, 10));
        assertThat(myMay.getContent()).hasSize(1);
    }
}
