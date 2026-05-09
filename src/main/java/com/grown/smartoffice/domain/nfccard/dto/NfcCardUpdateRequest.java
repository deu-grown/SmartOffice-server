package com.grown.smartoffice.domain.nfccard.dto;

import com.grown.smartoffice.domain.nfccard.entity.NfcCardStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NfcCardUpdateRequest {
    private NfcCardStatus status;
    private LocalDateTime expiredAt;

    @Builder
    public NfcCardUpdateRequest(NfcCardStatus status, LocalDateTime expiredAt) {
        this.status = status;
        this.expiredAt = expiredAt;
    }
}
