package com.grown.smartoffice.domain.nfccard.service;

import com.grown.smartoffice.domain.accesslog.repository.AccessLogRepository;
import com.grown.smartoffice.domain.nfccard.dto.*;
import com.grown.smartoffice.domain.nfccard.entity.NfcCard;
import com.grown.smartoffice.domain.nfccard.entity.NfcCardStatus;
import com.grown.smartoffice.domain.nfccard.repository.NfcCardRepository;
import com.grown.smartoffice.domain.user.entity.User;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NfcCardServiceTest {

    @Mock NfcCardRepository nfcCardRepository;
    @Mock UserRepository userRepository;
    @Mock AccessLogRepository accessLogRepository;
    @InjectMocks NfcCardService nfcCardService;

    @Test
    @DisplayName("NFC 카드 등록 성공")
    void registerCard_success() {
        // given
        NfcCardRegisterRequest req = NfcCardRegisterRequest.builder()
                .userId(1L)
                .uid("UID-001")
                .cardType("EMPLOYEE")
                .build();

        User user = User.builder().employeeName("박성종").build();
        ReflectionTestUtils.setField(user, "userId", 1L);

        given(nfcCardRepository.existsByCardUid("UID-001")).willReturn(false);
        given(nfcCardRepository.existsByUser_UserIdAndCardStatus(1L, NfcCardStatus.ACTIVE)).willReturn(false);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        NfcCard savedCard = NfcCard.builder().user(user).cardUid("UID-001").cardType("EMPLOYEE").build();
        ReflectionTestUtils.setField(savedCard, "cardId", 100L);
        given(nfcCardRepository.save(any(NfcCard.class))).willReturn(savedCard);

        // when
        NfcCardRegisterResponse res = nfcCardService.registerCard(req);

        // then
        assertThat(res.getId()).isEqualTo(100L);
        assertThat(res.getUid()).isEqualTo("UID-001");
        verify(nfcCardRepository).save(any(NfcCard.class));
    }

    @Test
    @DisplayName("NFC 카드 등록 실패 - 중복된 UID")
    void registerCard_duplicateUid() {
        // given
        NfcCardRegisterRequest req = NfcCardRegisterRequest.builder().uid("DUP-UID").build();
        given(nfcCardRepository.existsByCardUid("DUP-UID")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> nfcCardService.registerCard(req))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_NFC_CARD);
    }

    @Test
    @DisplayName("NFC 카드 등록 실패 - 이미 활성 카드 보유")
    void registerCard_alreadyHasActive() {
        // given
        NfcCardRegisterRequest req = NfcCardRegisterRequest.builder().userId(1L).uid("NEW-UID").build();
        given(nfcCardRepository.existsByCardUid("NEW-UID")).willReturn(false);
        given(nfcCardRepository.existsByUser_UserIdAndCardStatus(1L, NfcCardStatus.ACTIVE)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> nfcCardService.registerCard(req))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_HAS_ACTIVE_CARD);
    }

    @Test
    @DisplayName("NFC 카드 수정 성공 - 분실 처리")
    void updateCard_lost() {
        // given
        NfcCard card = NfcCard.builder().cardUid("UID-001").cardStatus(NfcCardStatus.ACTIVE).build();
        given(nfcCardRepository.findById(100L)).willReturn(Optional.of(card));
        
        NfcCardUpdateRequest req = NfcCardUpdateRequest.builder().status(NfcCardStatus.LOST).build();

        // when
        NfcCardUpdateResponse res = nfcCardService.updateCard(100L, req);

        // then
        assertThat(res.getStatus()).isEqualTo(NfcCardStatus.LOST);
        assertThat(card.getCardStatus()).isEqualTo(NfcCardStatus.LOST);
    }

    @Test
    @DisplayName("NFC 카드 삭제 실패 - 출입 로그 존재")
    void deleteCard_hasLogs() {
        // given
        NfcCard card = NfcCard.builder().build();
        given(nfcCardRepository.findById(100L)).willReturn(Optional.of(card));
        given(accessLogRepository.existsByCard_CardId(100L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> nfcCardService.deleteCard(100L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NFC_CARD_HAS_ACCESS_LOGS);
    }
}
