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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NfcCardService {

    private final NfcCardRepository nfcCardRepository;
    private final UserRepository userRepository;
    private final AccessLogRepository accessLogRepository;

    @Transactional
    public NfcCardRegisterResponse registerCard(NfcCardRegisterRequest request) {
        if (nfcCardRepository.existsByCardUid(request.getUid())) {
            throw new CustomException(ErrorCode.DUPLICATE_NFC_CARD);
        }

        if (nfcCardRepository.existsByUser_UserIdAndCardStatus(request.getUserId(), NfcCardStatus.ACTIVE)) {
            throw new CustomException(ErrorCode.ALREADY_HAS_ACTIVE_CARD);
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        NfcCard card = NfcCard.builder()
                .user(user)
                .cardUid(request.getUid())
                .cardType(request.getCardType())
                .cardStatus(NfcCardStatus.ACTIVE)
                .expiredAt(request.getExpiredAt())
                .build();

        return new NfcCardRegisterResponse(nfcCardRepository.save(card));
    }

    public List<NfcCardListItemResponse> getAllCards(Long userId, String cardType, NfcCardStatus status) {
        return nfcCardRepository.findAllWithUser(userId, cardType, status).stream()
                .map(NfcCardListItemResponse::new)
                .collect(Collectors.toList());
    }

    public NfcCardDetailResponse getCardDetail(Long id) {
        return nfcCardRepository.findByIdWithUserAndDept(id)
                .map(NfcCardDetailResponse::new)
                .orElseThrow(() -> new CustomException(ErrorCode.NFC_CARD_NOT_FOUND));
    }

    @Transactional
    public NfcCardUpdateResponse updateCard(Long id, NfcCardUpdateRequest request) {
        NfcCard card = nfcCardRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NFC_CARD_NOT_FOUND));

        if (request.getStatus() != null) {
            card.updateStatus(request.getStatus());
        }
        if (request.getExpiredAt() != null) {
            card.updateExpiration(request.getExpiredAt());
        }

        return new NfcCardUpdateResponse(card);
    }

    @Transactional
    public void deleteCard(Long id) {
        NfcCard card = nfcCardRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NFC_CARD_NOT_FOUND));

        if (accessLogRepository.existsByCard_CardId(id)) {
            throw new CustomException(ErrorCode.NFC_CARD_HAS_ACCESS_LOGS);
        }

        nfcCardRepository.delete(card);
    }
}
