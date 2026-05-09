package com.grown.smartoffice.domain.reservation.repository;

import com.grown.smartoffice.domain.department.entity.Department;
import com.grown.smartoffice.domain.reservation.entity.Reservation;
import com.grown.smartoffice.domain.reservation.entity.ReservationStatus;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationRepositoryTest extends RepositoryTestSupport {

    @Autowired ReservationRepository reservationRepository;
    @Autowired TestEntityManager em;

    private User user1;
    private Zone zone1;

    @BeforeEach
    void setUp() {
        Department dept = Department.builder().deptName("테스트팀").build();
        em.persist(dept);

        user1 = User.builder()
                .department(dept)
                .employeeNumber("TEST001")
                .employeeName("홍길동")
                .employeeEmail("test.user@test.com")
                .password("pw")
                .role(UserRole.USER)
                .position("사원")
                .status(UserStatus.ACTIVE)
                .hiredAt(LocalDate.now())
                .build();
        em.persist(user1);

        zone1 = Zone.builder().zoneName("제1회의실").zoneType(ZoneType.ROOM).build();
        em.persist(zone1);
        em.flush();
    }

    private Reservation reservation(LocalDateTime start, LocalDateTime end) {
        return Reservation.builder()
                .user(user1).zone(zone1)
                .reservationsTitle("테스트 예약")
                .startAt(start).endAt(end)
                .build();
    }

    @Test
    @DisplayName("countConflict: 겹치는 예약 있으면 1 반환")
    void countConflict_hasConflict() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end   = LocalDateTime.now().plusHours(3);
        em.persist(reservation(start, end));
        em.flush();

        long count = reservationRepository.countConflict(
                zone1.getZoneId(), ReservationStatus.CONFIRMED,
                start.plusMinutes(30), end.minusMinutes(30), null);

        assertThat(count).isGreaterThan(0);
    }

    @Test
    @DisplayName("countConflict: 겹치지 않는 예약은 0 반환")
    void countConflict_noConflict() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end   = LocalDateTime.now().plusHours(3);
        em.persist(reservation(start, end));
        em.flush();

        long count = reservationRepository.countConflict(
                zone1.getZoneId(), ReservationStatus.CONFIRMED,
                end.plusMinutes(1), end.plusHours(2), null);

        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("findByUserIdWithDetails: 사용자별 예약 목록 조회")
    void findByUserIdWithDetails() {
        em.persist(reservation(LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3)));
        em.persist(reservation(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2)));
        em.flush();

        List<Reservation> list = reservationRepository.findByUserIdWithDetails(user1.getUserId());

        assertThat(list).hasSize(2);
        assertThat(list).allMatch(r -> r.getUser() != null && r.getZone() != null);
    }

    @Test
    @DisplayName("findByZoneAndDateWithDetails: 구역+날짜 예약 조회")
    void findByZoneAndDateWithDetails() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        em.persist(reservation(todayStart.plusHours(10), todayStart.plusHours(12)));
        em.persist(reservation(todayStart.plusHours(14), todayStart.plusHours(16)));
        em.persist(reservation(todayStart.plusDays(1).plusHours(10), todayStart.plusDays(1).plusHours(12)));
        em.flush();

        List<Reservation> list = reservationRepository.findByZoneAndDateWithDetails(
                zone1.getZoneId(), todayStart, todayStart.plusDays(1).minusNanos(1));

        assertThat(list).hasSize(2);
    }

    @Test
    @DisplayName("countTodayConfirmed: 오늘 CONFIRMED 예약 수 집계")
    void countTodayConfirmed() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long baseline = reservationRepository.countTodayConfirmed(
                ReservationStatus.CONFIRMED, todayStart, todayStart.plusDays(1));

        em.persist(reservation(todayStart.plusHours(10), todayStart.plusHours(12)));
        Reservation cancelled = reservation(todayStart.plusHours(13), todayStart.plusHours(14));
        em.persist(cancelled);
        em.flush();
        cancelled.cancel();
        em.flush();

        long count = reservationRepository.countTodayConfirmed(
                ReservationStatus.CONFIRMED, todayStart, todayStart.plusDays(1));

        assertThat(count).isEqualTo(baseline + 1);
    }

    @Test
    @DisplayName("findByIdWithDetails: ID로 상세 조회")
    void findByIdWithDetails() {
        Reservation r = reservation(LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3));
        em.persist(r);
        em.flush();

        Optional<Reservation> found = reservationRepository.findByIdWithDetails(r.getReservationsId());

        assertThat(found).isPresent();
        assertThat(found.get().getUser().getEmployeeName()).isEqualTo("홍길동");
        assertThat(found.get().getZone().getZoneName()).isEqualTo("제1회의실");
    }
}
