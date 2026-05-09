package com.grown.smartoffice.domain.nfccard.dto;

import com.grown.smartoffice.domain.nfccard.entity.NfcCard;
import com.grown.smartoffice.domain.nfccard.entity.NfcCardStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class NfcCardListItemResponse {
    private Long id;
    private String uid;
    private String cardType;
    private NfcCardStatus status;
    private Long userId;
    private String userName;
    private String employeeNumber;
    private LocalDateTime issuedAt;
    private LocalDateTime expiredAt;

    public NfcCardListItemResponse(NfcCard card) {
        this.id = card.getCardId();
        this.uid = card.getCardUid();
        this.cardType = card.getCardType();
        this.status = card.getCardStatus();
        this.userId = card.getUser().getUserId();
        this.userName = card.getUser().getEmployeeName();
        this.employeeNumber = card.getUser().getEmployeeNumber();
        this.issuedAt = card.getIssuedAt();
        this.expiredAt = card.getExpiredAt();
    }
}
