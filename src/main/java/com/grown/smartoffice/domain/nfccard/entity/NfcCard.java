package com.grown.smartoffice.domain.nfccard.entity;

import com.grown.smartoffice.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "nfc_cards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class NfcCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    private Long cardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "card_uid", nullable = false, length = 100, unique = true)
    private String cardUid;

    @Column(name = "card_type", nullable = false, length = 10)
    private String cardType;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_status", nullable = false, length = 10)
    private NfcCardStatus cardStatus;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @lombok.Builder
    public NfcCard(User user, String cardUid, String cardType, NfcCardStatus cardStatus, LocalDateTime issuedAt, LocalDateTime expiredAt) {
        this.user = user;
        this.cardUid = cardUid;
        this.cardType = cardType;
        this.cardStatus = (cardStatus != null) ? cardStatus : NfcCardStatus.ACTIVE;
        this.issuedAt = (issuedAt != null) ? issuedAt : LocalDateTime.now();
        this.expiredAt = expiredAt;
    }

    public void updateStatus(NfcCardStatus status) {
        this.cardStatus = status;
    }

    public void updateExpiration(LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }

    public boolean isExpired() {
        return expiredAt != null && LocalDateTime.now().isAfter(expiredAt);
    }
}
