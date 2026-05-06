package com.grown.smartoffice.domain.nfccard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NfcCardRegisterRequest {

    @NotNull(message = "직원 ID는 필수입니다.")
    private Long userId;

    @NotBlank(message = "NFC 태그 UID는 필수입니다.")
    private String uid;

    @NotBlank(message = "카드 유형은 필수입니다.")
    private String cardType;

    private LocalDateTime expiredAt;

    @Builder
    public NfcCardRegisterRequest(Long userId, String uid, String cardType, LocalDateTime expiredAt) {
        this.userId = userId;
        this.uid = uid;
        this.cardType = cardType;
        this.expiredAt = expiredAt;
    }
}
