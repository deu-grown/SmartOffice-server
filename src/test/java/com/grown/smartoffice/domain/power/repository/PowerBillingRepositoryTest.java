package com.grown.smartoffice.domain.power.repository;

import com.grown.smartoffice.domain.power.entity.PowerBilling;
import com.grown.smartoffice.domain.zone.entity.Zone;
import com.grown.smartoffice.domain.zone.entity.ZoneType;
import com.grown.smartoffice.support.RepositoryTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PowerBillingRepositoryTest extends RepositoryTestSupport {

    @Autowired PowerBillingRepository powerBillingRepository;
    @Autowired TestEntityManager em;

    private Zone zone1;
    private Zone zone2;

    @BeforeEach
    void setUp() {
        zone1 = Zone.builder().zoneName("1층").zoneType(ZoneType.FLOOR).build();
        zone2 = Zone.builder().zoneName("2층").zoneType(ZoneType.FLOOR).build();
        em.persist(zone1);
        em.persist(zone2);
        em.flush();
    }

    private PowerBilling billing(Zone zone, int year, int month, BigDecimal kwh, int total) {
        return PowerBilling.builder()
                .zone(zone).billingYear(year).billingMonth(month)
                .totalKwh(kwh).unitPrice(120).baseFee(6160)
                .powerFee(total - 6160).totalFee(total)
                .calculatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("findAllByYearAndMonthWithZone: 연월별 전체 구역 조회")
    void findAllByYearAndMonth() {
        em.persist(billing(zone1, 2025, 4, new BigDecimal("183.24"), 28148));
        em.persist(billing(zone2, 2025, 4, new BigDecimal("412.57"), 55628));
        em.persist(billing(zone1, 2025, 3, new BigDecimal("100.00"), 18160));
        em.flush();

        List<PowerBilling> result = powerBillingRepository.findAllByYearAndMonthWithZone(2025, 4);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(b -> b.getBillingYear() == 2025 && b.getBillingMonth() == 4);
    }

    @Test
    @DisplayName("findByZone_ZoneIdAndBillingYearAndBillingMonth: 구역+연월 단건 조회")
    void findByZoneAndYearMonth() {
        em.persist(billing(zone1, 2025, 4, new BigDecimal("183.24"), 28148));
        em.flush();

        Optional<PowerBilling> result =
                powerBillingRepository.findByZone_ZoneIdAndBillingYearAndBillingMonth(
                        zone1.getZoneId(), 2025, 4);

        assertThat(result).isPresent();
        assertThat(result.get().getTotalKwh()).isEqualByComparingTo(new BigDecimal("183.24"));
    }

    @Test
    @DisplayName("findByZoneIdWithFilters: 구역별 이력 필터 조회")
    void findByZoneIdWithFilters() {
        em.persist(billing(zone1, 2025, 3, new BigDecimal("100.00"), 18160));
        em.persist(billing(zone1, 2025, 4, new BigDecimal("183.24"), 28148));
        em.persist(billing(zone1, 2025, 5, new BigDecimal("200.00"), 30160));
        em.flush();

        List<PowerBilling> all = powerBillingRepository.findByZoneIdWithFilters(zone1.getZoneId(), null, null);
        assertThat(all).hasSize(3);

        List<PowerBilling> filtered = powerBillingRepository.findByZoneIdWithFilters(zone1.getZoneId(), 2025, 4);
        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).getBillingMonth()).isEqualTo(4);
    }

    @Test
    @DisplayName("recalculate: 기존 레코드 덮어쓰기")
    void recalculate() {
        PowerBilling b = billing(zone1, 2025, 4, new BigDecimal("183.24"), 28148);
        em.persist(b);
        em.flush();

        b.recalculate(new BigDecimal("200.00"), 120, 6160, 24000, 30160);
        em.flush();

        Optional<PowerBilling> updated =
                powerBillingRepository.findByZone_ZoneIdAndBillingYearAndBillingMonth(
                        zone1.getZoneId(), 2025, 4);
        assertThat(updated).isPresent();
        assertThat(updated.get().getTotalKwh()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(updated.get().getTotalFee()).isEqualTo(30160);
    }
}
