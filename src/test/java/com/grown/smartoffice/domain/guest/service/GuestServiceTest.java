package com.grown.smartoffice.domain.guest.service;

import com.grown.smartoffice.domain.guest.dto.GuestCreateRequest;
import com.grown.smartoffice.domain.guest.dto.GuestResponse;
import com.grown.smartoffice.domain.guest.entity.Guest;
import com.grown.smartoffice.domain.guest.entity.GuestStatus;
import com.grown.smartoffice.domain.guest.repository.GuestRepository;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GuestServiceTest {

    @Mock GuestRepository guestRepository;
    @Mock UserRepository userRepository;
    @InjectMocks GuestService guestService;

    private GuestCreateRequest createRequest() {
        GuestCreateRequest req = new GuestCreateRequest();
        ReflectionTestUtils.setField(req, "guestName", "김방문");
        ReflectionTestUtils.setField(req, "company", "그로운파트너스");
        ReflectionTestUtils.setField(req, "purpose", "협력사 미팅");
        ReflectionTestUtils.setField(req, "scheduledEntryAt", LocalDateTime.of(2026, 5, 16, 14, 0));
        return req;
    }

    private Guest guestWithStatus(GuestStatus status) {
        return Guest.builder()
                .guestName("김방문")
                .guestStatus(status)
                .scheduledEntryAt(LocalDateTime.of(2026, 5, 16, 14, 0))
                .build();
    }

    @Test
    @DisplayName("방문객 등록 성공 — SCHEDULED 상태로 생성")
    void createGuest_success() {
        given(guestRepository.save(any(Guest.class))).willAnswer(inv -> inv.getArgument(0));

        GuestResponse res = guestService.createGuest(createRequest());

        assertThat(res.getGuestName()).isEqualTo("김방문");
        assertThat(res.getGuestStatus()).isEqualTo(GuestStatus.SCHEDULED);
    }

    @Test
    @DisplayName("방문객 상세 조회 — 부재 시 GUEST_NOT_FOUND")
    void getGuest_notFound() {
        given(guestRepository.findByIdWithHost(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> guestService.getGuest(99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.GUEST_NOT_FOUND);
    }

    @Test
    @DisplayName("방문객 삭제 — 부재 시 GUEST_NOT_FOUND")
    void deleteGuest_notFound() {
        given(guestRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> guestService.deleteGuest(99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.GUEST_NOT_FOUND);
    }

    @Test
    @DisplayName("체크인 성공 — SCHEDULED → VISITING, 입실 시각 기록")
    void checkIn_success() {
        given(guestRepository.findByIdWithHost(1L))
                .willReturn(Optional.of(guestWithStatus(GuestStatus.SCHEDULED)));

        GuestResponse res = guestService.checkIn(1L);

        assertThat(res.getGuestStatus()).isEqualTo(GuestStatus.VISITING);
        assertThat(res.getActualEntryAt()).isNotNull();
    }

    @Test
    @DisplayName("체크인 — SCHEDULED 아닌 상태 시 GUEST_CHECK_IN_NOT_ALLOWED")
    void checkIn_notAllowed() {
        given(guestRepository.findByIdWithHost(1L))
                .willReturn(Optional.of(guestWithStatus(GuestStatus.VISITING)));

        assertThatThrownBy(() -> guestService.checkIn(1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.GUEST_CHECK_IN_NOT_ALLOWED);
    }

    @Test
    @DisplayName("체크아웃 성공 — VISITING → COMPLETED, 퇴실 시각 기록")
    void checkOut_success() {
        given(guestRepository.findByIdWithHost(1L))
                .willReturn(Optional.of(guestWithStatus(GuestStatus.VISITING)));

        GuestResponse res = guestService.checkOut(1L);

        assertThat(res.getGuestStatus()).isEqualTo(GuestStatus.COMPLETED);
        assertThat(res.getActualExitAt()).isNotNull();
    }

    @Test
    @DisplayName("체크아웃 — VISITING 아닌 상태 시 GUEST_CHECK_OUT_NOT_ALLOWED")
    void checkOut_notAllowed() {
        given(guestRepository.findByIdWithHost(1L))
                .willReturn(Optional.of(guestWithStatus(GuestStatus.SCHEDULED)));

        assertThatThrownBy(() -> guestService.checkOut(1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.GUEST_CHECK_OUT_NOT_ALLOWED);
    }
}
