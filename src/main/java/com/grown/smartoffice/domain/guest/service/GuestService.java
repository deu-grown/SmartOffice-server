package com.grown.smartoffice.domain.guest.service;

import com.grown.smartoffice.domain.guest.dto.GuestCreateRequest;
import com.grown.smartoffice.domain.guest.dto.GuestResponse;
import com.grown.smartoffice.domain.guest.dto.GuestUpdateRequest;
import com.grown.smartoffice.domain.guest.entity.Guest;
import com.grown.smartoffice.domain.guest.entity.GuestStatus;
import com.grown.smartoffice.domain.guest.repository.GuestRepository;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import com.grown.smartoffice.global.common.PageResponse;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;
    private final UserRepository userRepository;

    @Transactional
    public GuestResponse createGuest(GuestCreateRequest request) {
        User hostUser = resolveUser(request.getHostUserId());

        Guest guest = Guest.builder()
                .guestName(request.getGuestName())
                .company(request.getCompany())
                .hostUser(hostUser)
                .purpose(request.getPurpose())
                .contactPhone(request.getContactPhone())
                .guestStatus(GuestStatus.SCHEDULED)
                .scheduledEntryAt(request.getScheduledEntryAt())
                .build();
        return GuestResponse.from(guestRepository.save(guest));
    }

    @Transactional(readOnly = true)
    public PageResponse<GuestResponse> getGuests(String status, Long hostUserId, String keyword,
                                                 int page, int size) {
        GuestStatus guestStatus = (status != null) ? parseStatus(status) : null;
        return PageResponse.from(
                guestRepository.findAllWithFilters(guestStatus, hostUserId, keyword,
                                PageRequest.of(page, size))
                        .map(GuestResponse::from));
    }

    @Transactional(readOnly = true)
    public GuestResponse getGuest(Long id) {
        return GuestResponse.from(findGuest(id));
    }

    @Transactional
    public GuestResponse updateGuest(Long id, GuestUpdateRequest request) {
        Guest guest = findGuest(id);
        User hostUser = resolveUser(request.getHostUserId());

        guest.update(
                request.getGuestName(),
                request.getCompany(),
                hostUser,
                request.getPurpose(),
                request.getContactPhone(),
                request.getGuestStatus() != null ? parseStatus(request.getGuestStatus()) : null,
                request.getScheduledEntryAt());
        return GuestResponse.from(guest);
    }

    @Transactional
    public void deleteGuest(Long id) {
        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.GUEST_NOT_FOUND));
        guestRepository.delete(guest);
    }

    @Transactional
    public GuestResponse checkIn(Long id) {
        Guest guest = findGuest(id);
        if (guest.getGuestStatus() != GuestStatus.SCHEDULED) {
            throw new CustomException(ErrorCode.GUEST_CHECK_IN_NOT_ALLOWED);
        }
        guest.checkIn(LocalDateTime.now());
        return GuestResponse.from(guest);
    }

    @Transactional
    public GuestResponse checkOut(Long id) {
        Guest guest = findGuest(id);
        if (guest.getGuestStatus() != GuestStatus.VISITING) {
            throw new CustomException(ErrorCode.GUEST_CHECK_OUT_NOT_ALLOWED);
        }
        guest.checkOut(LocalDateTime.now());
        return GuestResponse.from(guest);
    }

    private Guest findGuest(Long id) {
        return guestRepository.findByIdWithHost(id)
                .orElseThrow(() -> new CustomException(ErrorCode.GUEST_NOT_FOUND));
    }

    private User resolveUser(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private GuestStatus parseStatus(String value) {
        try {
            return GuestStatus.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
    }
}
