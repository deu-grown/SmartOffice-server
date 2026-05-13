package com.grown.smartoffice.domain.salary.repository;

import com.grown.smartoffice.domain.salary.entity.SalarySetting;
import com.grown.smartoffice.support.RepositoryTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SalarySettingRepositoryTest extends RepositoryTestSupport {

    @Autowired SalarySettingRepository salarySettingRepository;
    @Autowired TestEntityManager em;

    private SalarySetting setting(String position, int base, LocalDate from, LocalDate to) {
        SalarySetting s = SalarySetting.builder()
                .salsetPosition(position).baseSalary(base)
                .overtimeRate(new BigDecimal("1.5")).nightRate(new BigDecimal("2.0"))
                .effectiveFrom(from).build();
        if (to != null) s.closeAt(to);
        return s;
    }

    @Test
    @DisplayName("findActiveByPosition — effectiveTo IS NULL 인 최신 기준")
    void findActiveByPosition() {
        String position = "SSR-A-" + System.nanoTime();
        em.persist(setting(position, 3000000, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31)));
        em.persist(setting(position, 4000000, LocalDate.of(2026, 1, 1), null));
        em.flush();

        Optional<SalarySetting> active = salarySettingRepository.findActiveByPosition(position);
        assertThat(active).isPresent();
        assertThat(active.get().getBaseSalary()).isEqualTo(4000000);
    }

    @Test
    @DisplayName("findApplicableByPositionAndDate — 특정 일자에 효력 있는 기준")
    void findApplicableByDate() {
        String position = "SSR-B-" + System.nanoTime();
        em.persist(setting(position, 3000000, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31)));
        em.persist(setting(position, 4000000, LocalDate.of(2026, 1, 1), null));
        em.flush();

        Optional<SalarySetting> old = salarySettingRepository.findApplicableByPositionAndDate(position, LocalDate.of(2025, 6, 1));
        assertThat(old).hasValueSatisfying(s -> assertThat(s.getBaseSalary()).isEqualTo(3000000));

        Optional<SalarySetting> current = salarySettingRepository.findApplicableByPositionAndDate(position, LocalDate.of(2026, 5, 1));
        assertThat(current).hasValueSatisfying(s -> assertThat(s.getBaseSalary()).isEqualTo(4000000));

        Optional<SalarySetting> tooOld = salarySettingRepository.findApplicableByPositionAndDate(position, LocalDate.of(2020, 1, 1));
        assertThat(tooOld).isEmpty();
    }

    @Test
    @DisplayName("findAllBySalsetPosition — 직급별 전체 이력 반환")
    void findAllByPosition() {
        String position = "SSR-C-" + System.nanoTime();
        em.persist(setting(position, 3000000, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31)));
        em.persist(setting(position, 4000000, LocalDate.of(2026, 1, 1), null));
        em.flush();

        List<SalarySetting> all = salarySettingRepository.findAllBySalsetPosition(position);
        assertThat(all).hasSize(2);
    }
}
