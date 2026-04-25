package com.grown.smartoffice.domain.accesslog.entity;

import com.grown.smartoffice.domain.device.entity.Device;
import com.grown.smartoffice.domain.nfccard.entity.NfcCard;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.zone.entity.Zone;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "access_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "access_log_id")
    private Long accessLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private NfcCard card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devices_id", nullable = false)
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(name = "read_uid", nullable = false, length = 100)
    private String readUid;

    @Column(name = "direction", nullable = false, length = 5)
    private String direction;

    @Column(name = "auth_result", nullable = false, length = 10)
    private String authResult;

    @Column(name = "deny_reason", length = 255)
    private String denyReason;

    @Column(name = "tagged_at", nullable = false)
    private LocalDateTime taggedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public AccessLog(User user, NfcCard card, Device device, Zone zone,
                     String readUid, String direction, String authResult,
                     String denyReason, LocalDateTime taggedAt) {
        this.user = user;
        this.card = card;
        this.device = device;
        this.zone = zone;
        this.readUid = readUid;
        this.direction = direction;
        this.authResult = authResult;
        this.denyReason = denyReason;
        this.taggedAt = taggedAt;
    }
}
