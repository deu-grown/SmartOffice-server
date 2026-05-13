package com.grown.smartoffice.domain.accesslog.repository;

import com.grown.smartoffice.domain.accesslog.entity.AccessLog;
import com.grown.smartoffice.domain.department.entity.Department;
import com.grown.smartoffice.domain.device.entity.Device;
import com.grown.smartoffice.domain.device.entity.DeviceStatus;
import com.grown.smartoffice.domain.nfccard.entity.NfcCard;
import com.grown.smartoffice.domain.nfccard.entity.NfcCardStatus;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserRole;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import com.grown.smartoffice.domain.zone.entity.Zone;
import com.grown.smartoffice.domain.zone.entity.ZoneType;
import com.grown.smartoffice.support.RepositoryTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AccessLogRepositoryTest extends RepositoryTestSupport {

    @Autowired AccessLogRepository accessLogRepository;
    @Autowired TestEntityManager em;

    private User userA;
    private User userB;
    private Zone zoneA;
    private Zone zoneB;
    private Device deviceA;
    private Device deviceB;
    private NfcCard cardA;
    private NfcCard cardB;

    @BeforeEach
    void setUp() {
        Department d = Department.builder().deptName("ALR팀-" + System.nanoTime()).build();
        em.persist(d);

        userA = newUser(d, "ALR-A");
        userB = newUser(d, "ALR-B");
        em.persist(userA);
        em.persist(userB);

        zoneA = Zone.builder().zoneName("ALR-Z-A-" + System.nanoTime()).zoneType(ZoneType.AREA).build();
        zoneB = Zone.builder().zoneName("ALR-Z-B-" + System.nanoTime()).zoneType(ZoneType.AREA).build();
        em.persist(zoneA);
        em.persist(zoneB);

        deviceA = Device.builder().zone(zoneA).deviceName("ALR-DEV-A").deviceType("NFC_READER")
                .deviceStatus(DeviceStatus.ACTIVE).build();
        deviceB = Device.builder().zone(zoneB).deviceName("ALR-DEV-B").deviceType("NFC_READER")
                .deviceStatus(DeviceStatus.ACTIVE).build();
        em.persist(deviceA);
        em.persist(deviceB);

        cardA = NfcCard.builder().user(userA).cardUid("ALR-CARD-A-" + System.nanoTime())
                .cardType("EMPLOYEE").cardStatus(NfcCardStatus.ACTIVE)
                .issuedAt(LocalDateTime.now()).build();
        cardB = NfcCard.builder().user(userB).cardUid("ALR-CARD-B-" + System.nanoTime())
                .cardType("EMPLOYEE").cardStatus(NfcCardStatus.ACTIVE)
                .issuedAt(LocalDateTime.now()).build();
        em.persist(cardA);
        em.persist(cardB);
        em.flush();
    }

    private User newUser(Department d, String prefix) {
        return User.builder().department(d)
                .employeeNumber(prefix + "-" + System.nanoTime())
                .employeeName(prefix).employeeEmail(prefix + "-" + System.nanoTime() + "@grown.com")
                .password("pw").role(UserRole.USER).position("사원")
                .status(UserStatus.ACTIVE).hiredAt(LocalDate.now()).build();
    }

    private AccessLog log(User u, NfcCard c, Device dev, Zone z, String dir,
                          String result, LocalDateTime taggedAt) {
        return AccessLog.builder().user(u).card(c).device(dev).zone(z)
                .readUid(c.getCardUid()).direction(dir).authResult(result)
                .taggedAt(taggedAt).build();
    }

    @Test
    @DisplayName("existsByCard_CardId — 출입 로그 보유 카드 검출")
    void existsByCardId() {
        em.persist(log(userA, cardA, deviceA, zoneA, "IN", "APPROVED", LocalDateTime.now().minusHours(1)));
        em.flush();

        assertThat(accessLogRepository.existsByCard_CardId(cardA.getCardId())).isTrue();
        assertThat(accessLogRepository.existsByCard_CardId(cardB.getCardId())).isFalse();
    }

    @Test
    @DisplayName("findAllWithFilters — zoneId 필터 적용")
    void findAllWithFilters_byZone() {
        em.persist(log(userA, cardA, deviceA, zoneA, "IN", "APPROVED", LocalDateTime.now().minusHours(2)));
        em.persist(log(userB, cardB, deviceB, zoneB, "IN", "APPROVED", LocalDateTime.now().minusHours(1)));
        em.flush();
        em.clear();

        Page<AccessLog> zoneAOnly = accessLogRepository.findAllWithFilters(
                zoneA.getZoneId(), null, null, null, null, null,
                PageRequest.of(0, 10, Sort.by("taggedAt").descending()));
        assertThat(zoneAOnly.getContent()).hasSize(1);
        assertThat(zoneAOnly.getContent().get(0).getZone().getZoneName()).isEqualTo(zoneA.getZoneName());
    }

    @Test
    @DisplayName("findAllWithFilters — authResult 필터")
    void findAllWithFilters_byAuthResult() {
        em.persist(log(userA, cardA, deviceA, zoneA, "IN",  "APPROVED", LocalDateTime.now().minusHours(3)));
        em.persist(log(userA, cardA, deviceA, zoneA, "IN",  "DENIED",   LocalDateTime.now().minusHours(2)));
        em.persist(log(userA, cardA, deviceA, zoneA, "OUT", "BLOCKED",  LocalDateTime.now().minusHours(1)));
        em.flush();
        em.clear();

        Page<AccessLog> deniedOnly = accessLogRepository.findAllWithFilters(
                null, userA.getUserId(), "DENIED", null, null, null,
                PageRequest.of(0, 10, Sort.by("taggedAt").descending()));
        assertThat(deniedOnly.getContent()).extracting(AccessLog::getAuthResult).containsOnly("DENIED");
    }

    @Test
    @DisplayName("findAllWithFilters — 날짜 범위 필터 + 페이지 경계")
    void findAllWithFilters_dateRangeAndPagination() {
        // DATETIME 정밀도(초)에 안전하도록 fractional 없는 명시적 시각 사용
        LocalDateTime base = LocalDateTime.of(2025, 12, 1, 0, 0, 0);
        for (int i = 0; i < 6; i++) {
            em.persist(log(userA, cardA, deviceA, zoneA, "IN", "APPROVED", base.plusDays(i)));
        }
        em.flush();
        em.clear();

        Page<AccessLog> rangeOnly = accessLogRepository.findAllWithFilters(
                null, userA.getUserId(), null, null,
                base.plusDays(1), base.plusDays(4),
                PageRequest.of(0, 2, Sort.by("taggedAt").descending()));
        assertThat(rangeOnly.getContent()).hasSize(2);
        // day1~day4 inclusive = 4건. 페이지 size 2 → 1페이지에 2건
        assertThat(rangeOnly.getTotalElements()).isEqualTo(4);
    }

    @Test
    @DisplayName("findAllWithFilters — 결과 0건 → 빈 페이지")
    void findAllWithFilters_empty() {
        Page<AccessLog> empty = accessLogRepository.findAllWithFilters(
                null, userA.getUserId(), null, null, null, null,
                PageRequest.of(0, 10, Sort.by("taggedAt").descending()));
        assertThat(empty.getContent()).isEmpty();
        assertThat(empty.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("findRecentWithUserAndZone — direction 필터 + 최신순 정렬")
    void findRecentWithUserAndZone() {
        em.persist(log(userA, cardA, deviceA, zoneA, "IN",  "APPROVED", LocalDateTime.now().minusHours(3)));
        em.persist(log(userA, cardA, deviceA, zoneA, "OUT", "APPROVED", LocalDateTime.now().minusHours(2)));
        em.persist(log(userB, cardB, deviceB, zoneB, "IN",  "APPROVED", LocalDateTime.now().minusHours(1)));
        em.flush();
        em.clear();

        var ins = accessLogRepository.findRecentWithUserAndZone("IN", PageRequest.of(0, 10));
        assertThat(ins).extracting(AccessLog::getDirection).containsOnly("IN");
        // 최신순 — userB(zoneB) 가 더 최근
        assertThat(ins.get(0).getUser().getEmployeeName()).isEqualTo("ALR-B");
    }
}
