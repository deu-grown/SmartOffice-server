package com.grown.smartoffice.domain.reservation.service;

import com.grown.smartoffice.domain.reservation.dto.*;
import com.grown.smartoffice.domain.reservation.entity.Reservation;
import com.grown.smartoffice.domain.reservation.entity.ReservationStatus;
import com.grown.smartoffice.domain.reservation.repository.ReservationRepository;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserRole;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import com.grown.smartoffice.domain.zone.entity.Zone;
import com.grown.smartoffice.domain.zone.entity.ZoneType;
import com.grown.smartoffice.domain.zone.repository.ZoneRepository;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock ReservationRepository reservationRepository;
    @Mock UserRepository userRepository;
    @Mock ZoneRepository zoneRepository;
    @InjectMocks ReservationService reservationService;

    private User user;
    private Zone zone;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .employeeNumber("EMP001")
                .employeeName("홍길동")
                .employeeEmail("test@test.com")
                .password("pw")
                .role(UserRole.USER)
                .position("사원")
                .status(UserStatus.ACTIVE)
                .hiredAt(java.time.LocalDate.now())
                .build();
        ReflectionTestUtils.setField(user, "userId", 1L);

        zone = Zone.builder().zoneName("제1회의실").zoneType(ZoneType.ROOM).build();
        ReflectionTestUtils.setField(zone, "zoneId", 1L);
    }

    @Test
    @DisplayName("예약 생성 성공")
    void createReservation_success() {
        ReservationCreateRequest req = new ReservationCreateRequest();
        ReflectionTestUtils.setField(req, "zoneId", 1L);
        ReflectionTestUtils.setField(req, "startTime", LocalDateTime.now().plusHours(1));
        ReflectionTestUtils.setField(req, "endTime", LocalDateTime.now().plusHours(3));
        ReflectionTestUtils.setField(req, "purpose", "팀 미팅");

        Reservation saved = Reservation.builder()
                .user(user).zone(zone)
                .reservationsTitle("팀 미팅")
                .startAt(LocalDateTime.now().plusHours(1))
                .endAt(LocalDateTime.now().plusHours(3))
                .build();
        ReflectionTestUtils.setField(saved, "reservationsId", 100L);

        given(userRepository.findByEmployeeEmail("test@test.com")).willReturn(Optional.of(user));
        given(zoneRepository.findById(1L)).willReturn(Optional.of(zone));
        given(reservationRepository.countConflict(eq(1L), eq(ReservationStatus.CONFIRMED), any(), any(), isNull()))
                .willReturn(0L);
        given(reservationRepository.save(any())).willReturn(saved);

        ReservationResponse res = reservationService.createReservation(req, "test@test.com");

        assertThat(res.getReservationId()).isEqualTo(100L);
        assertThat(res.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        verify(reservationRepository).save(any());
    }

    @Test
    @DisplayName("예약 생성 — 시간 중복 시 RESERVATION_TIME_CONFLICT")
    void createReservation_conflict() {
        ReservationCreateRequest req = new ReservationCreateRequest();
        ReflectionTestUtils.setField(req, "zoneId", 1L);
        ReflectionTestUtils.setField(req, "startTime", LocalDateTime.now().plusHours(1));
        ReflectionTestUtils.setField(req, "endTime", LocalDateTime.now().plusHours(3));

        given(userRepository.findByEmployeeEmail("test@test.com")).willReturn(Optional.of(user));
        given(zoneRepository.findById(1L)).willReturn(Optional.of(zone));
        given(reservationRepository.countConflict(eq(1L), eq(ReservationStatus.CONFIRMED), any(), any(), isNull()))
                .willReturn(1L);

        assertThatThrownBy(() -> reservationService.createReservation(req, "test@test.com"))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.RESERVATION_TIME_CONFLICT));
    }

    @Test
    @DisplayName("예약 생성 — 종료 시각이 시작 시각 이전 시 RESERVATION_END_BEFORE_START")
    void createReservation_endBeforeStart() {
        ReservationCreateRequest req = new ReservationCreateRequest();
        ReflectionTestUtils.setField(req, "zoneId", 1L);
        ReflectionTestUtils.setField(req, "startTime", LocalDateTime.now().plusHours(3));
        ReflectionTestUtils.setField(req, "endTime", LocalDateTime.now().plusHours(1));

        given(userRepository.findByEmployeeEmail("test@test.com")).willReturn(Optional.of(user));
        given(zoneRepository.findById(1L)).willReturn(Optional.of(zone));

        assertThatThrownBy(() -> reservationService.createReservation(req, "test@test.com"))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.RESERVATION_END_BEFORE_START));
    }

    @Test
    @DisplayName("예약 취소 성공")
    void cancelReservation_success() {
        Reservation reservation = Reservation.builder()
                .user(user).zone(zone)
                .reservationsTitle("팀 미팅")
                .startAt(LocalDateTime.now().plusHours(1))
                .endAt(LocalDateTime.now().plusHours(3))
                .build();
        ReflectionTestUtils.setField(reservation, "reservationsId", 50L);

        given(reservationRepository.findByIdWithDetails(50L)).willReturn(Optional.of(reservation));
        given(userRepository.findByEmployeeEmail("test@test.com")).willReturn(Optional.of(user));

        Long cancelledId = reservationService.cancelReservation(50L, "test@test.com");

        assertThat(cancelledId).isEqualTo(50L);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    @DisplayName("예약 취소 — 이미 취소된 예약")
    void cancelReservation_alreadyCancelled() {
        Reservation reservation = Reservation.builder()
                .user(user).zone(zone)
                .reservationsTitle("팀 미팅")
                .startAt(LocalDateTime.now().plusHours(1))
                .endAt(LocalDateTime.now().plusHours(3))
                .build();
        ReflectionTestUtils.setField(reservation, "reservationsId", 50L);
        reservation.cancel();

        given(reservationRepository.findByIdWithDetails(50L)).willReturn(Optional.of(reservation));
        given(userRepository.findByEmployeeEmail("test@test.com")).willReturn(Optional.of(user));

        assertThatThrownBy(() -> reservationService.cancelReservation(50L, "test@test.com"))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.RESERVATION_ALREADY_CANCELLED));
    }

    @Test
    @DisplayName("NFC 체크인 성공")
    void checkIn_success() {
        LocalDateTime start = LocalDateTime.now().plusMinutes(5);
        LocalDateTime end   = LocalDateTime.now().plusHours(2);

        Reservation reservation = Reservation.builder()
                .user(user).zone(zone)
                .reservationsTitle("팀 미팅")
                .startAt(start).endAt(end)
                .build();
        ReflectionTestUtils.setField(reservation, "reservationsId", 50L);

        ReservationCheckInRequest req = new ReservationCheckInRequest();
        ReflectionTestUtils.setField(req, "nfcTagId", "NFC_ZONE_01");

        given(reservationRepository.findByIdWithDetails(50L)).willReturn(Optional.of(reservation));
        given(userRepository.findByEmployeeEmail("test@test.com")).willReturn(Optional.of(user));

        ReservationCheckInResponse res = reservationService.checkIn(50L, req, "test@test.com");

        assertThat(res.getStatus()).isEqualTo(ReservationStatus.CHECKED_IN);
        assertThat(reservation.getCheckedInAt()).isNotNull();
    }

    @Test
    @DisplayName("NFC 체크인 — 체크인 시간 범위 밖이면 RESERVATION_CHECK_IN_NOT_ALLOWED")
    void checkIn_tooEarly() {
        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end   = LocalDateTime.now().plusHours(4);

        Reservation reservation = Reservation.builder()
                .user(user).zone(zone)
                .reservationsTitle("팀 미팅")
                .startAt(start).endAt(end)
                .build();
        ReflectionTestUtils.setField(reservation, "reservationsId", 50L);

        ReservationCheckInRequest req = new ReservationCheckInRequest();
        ReflectionTestUtils.setField(req, "nfcTagId", "NFC_ZONE_01");

        given(reservationRepository.findByIdWithDetails(50L)).willReturn(Optional.of(reservation));
        given(userRepository.findByEmployeeEmail("test@test.com")).willReturn(Optional.of(user));

        assertThatThrownBy(() -> reservationService.checkIn(50L, req, "test@test.com"))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.RESERVATION_CHECK_IN_NOT_ALLOWED));
    }
}
