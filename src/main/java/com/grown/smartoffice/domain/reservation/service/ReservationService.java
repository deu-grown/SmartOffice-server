package com.grown.smartoffice.domain.reservation.service;

import com.grown.smartoffice.domain.reservation.dto.*;
import com.grown.smartoffice.domain.reservation.entity.Reservation;
import com.grown.smartoffice.domain.reservation.entity.ReservationStatus;
import com.grown.smartoffice.domain.reservation.repository.ReservationRepository;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserRole;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import com.grown.smartoffice.domain.zone.entity.Zone;
import com.grown.smartoffice.domain.zone.repository.ZoneRepository;
import com.grown.smartoffice.global.common.PageResponse;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ZoneRepository zoneRepository;

    @Transactional
    public ReservationResponse createReservation(ReservationCreateRequest request, String email) {
        User user = findUserByEmail(email);
        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new CustomException(ErrorCode.ZONE_NOT_FOUND));

        validateTimes(request.getStartTime(), request.getEndTime());
        checkConflict(zone.getZoneId(), request.getStartTime(), request.getEndTime(), null);

        Reservation reservation = Reservation.builder()
                .user(user)
                .zone(zone)
                .reservationsTitle(request.getPurpose() != null ? request.getPurpose() : "")
                .startAt(request.getStartTime())
                .endAt(request.getEndTime())
                .build();

        Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservation(Long id, String email) {
        Reservation r = reservationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
        // 단건 조회도 수정·취소와 동일하게 본인/ADMIN 만 허용 (서비스 레이어 일관 검증)
        checkOwnership(r, findUserByEmail(email));
        return ReservationResponse.from(r);
    }

    @Transactional
    public ReservationResponse updateReservation(Long id, ReservationUpdateRequest request, String email) {
        Reservation r = reservationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        User caller = findUserByEmail(email);
        checkOwnership(r, caller);

        if (r.getStatus() == ReservationStatus.CANCELLED) {
            throw new CustomException(ErrorCode.RESERVATION_ALREADY_CANCELLED);
        }

        LocalDateTime newStart = request.getStartTime() != null ? request.getStartTime() : r.getStartAt();
        LocalDateTime newEnd   = request.getEndTime()   != null ? request.getEndTime()   : r.getEndAt();
        validateTimes(newStart, newEnd);
        checkConflict(r.getZone().getZoneId(), newStart, newEnd, id);

        r.update(request.getPurpose(), request.getStartTime(), request.getEndTime());
        return ReservationResponse.from(r);
    }

    @Transactional
    public Long cancelReservation(Long id, String email) {
        Reservation r = reservationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        User caller = findUserByEmail(email);
        checkOwnership(r, caller);

        if (r.getStatus() == ReservationStatus.CANCELLED) {
            throw new CustomException(ErrorCode.RESERVATION_ALREADY_CANCELLED);
        }

        r.cancel();
        return r.getReservationsId();
    }

    @Transactional(readOnly = true)
    public PageResponse<ReservationListResponse.ReservationListItem> getAllReservations(
            String statusStr, int page, int size) {
        ReservationStatus status = statusStr != null ? ReservationStatus.valueOf(statusStr) : null;
        return PageResponse.from(
                reservationRepository.findAllWithDetailsFiltered(status, PageRequest.of(page, size))
                        .map(ReservationListResponse.ReservationListItem::from)
        );
    }

    @Transactional(readOnly = true)
    public ReservationListResponse getMyReservations(String email) {
        User user = findUserByEmail(email);
        List<Reservation> list = reservationRepository.findByUserIdWithDetails(user.getUserId());
        return ReservationListResponse.from(list);
    }

    @Transactional(readOnly = true)
    public ReservationListResponse getZoneReservations(Long zoneId, LocalDate date) {
        if (!zoneRepository.existsById(zoneId)) {
            throw new CustomException(ErrorCode.ZONE_NOT_FOUND);
        }
        LocalDate target = date != null ? date : LocalDate.now();
        LocalDateTime start = target.atStartOfDay();
        LocalDateTime end   = target.plusDays(1).atStartOfDay().minusNanos(1);

        List<Reservation> list = reservationRepository.findByZoneAndDateWithDetails(zoneId, start, end);
        return ReservationListResponse.from(list);
    }

    @Transactional
    public ReservationCheckInResponse checkIn(Long id, ReservationCheckInRequest request, String email) {
        Reservation r = reservationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        User caller = findUserByEmail(email);
        checkOwnership(r, caller);

        if (r.getStatus() == ReservationStatus.CANCELLED) {
            throw new CustomException(ErrorCode.RESERVATION_ALREADY_CANCELLED);
        }

        LocalDateTime now   = LocalDateTime.now();
        LocalDateTime earliest = r.getStartAt().minusMinutes(10);
        if (now.isBefore(earliest) || now.isAfter(r.getEndAt())) {
            throw new CustomException(ErrorCode.RESERVATION_CHECK_IN_NOT_ALLOWED);
        }

        r.checkIn(now);
        return ReservationCheckInResponse.builder()
                .checkInTime(now)
                .status(r.getStatus())
                .build();
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmployeeEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateTimes(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            throw new CustomException(ErrorCode.RESERVATION_END_BEFORE_START);
        }
    }

    private void checkConflict(Long zoneId, LocalDateTime start, LocalDateTime end, Long excludeId) {
        if (reservationRepository.countConflict(zoneId, ReservationStatus.CONFIRMED, start, end, excludeId) > 0) {
            throw new CustomException(ErrorCode.RESERVATION_TIME_CONFLICT);
        }
    }

    private void checkOwnership(Reservation r, User caller) {
        boolean isAdmin = caller.getRole() == UserRole.ADMIN;
        if (!isAdmin && !r.getUser().getUserId().equals(caller.getUserId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }
}
