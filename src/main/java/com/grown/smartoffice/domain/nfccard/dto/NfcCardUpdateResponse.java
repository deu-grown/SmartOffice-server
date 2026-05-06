package com.grown.smartoffice.domain.nfccard.dto;

import com.grown.smartoffice.domain.nfccard.entity.NfcCard;
import com.grown.smartoffice.domain.nfccard.entity.NfcCardStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class NfcCardUpdateResponse {
    private Long id;
    private String uid;
    private NfcCardStatus status;
    private LocalDateTime expiredAt;
    private LocalDateTime updatedAt;

    public NfcCardUpdateResponse(NfcCard card) {
        this.id = card.getCardId();
        this.uid = card.getCardUid();
        this.status = card.getCardStatus();
        this.expiredAt = card.getExpiredAt();
        this.updatedAt = card.getUpdatedAt();
    }
}
