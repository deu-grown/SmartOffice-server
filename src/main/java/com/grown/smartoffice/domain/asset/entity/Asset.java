package com.grown.smartoffice.domain.asset.entity;

import com.grown.smartoffice.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "assets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asset_id")
    private Long assetId;

    @Column(name = "asset_number", nullable = false, unique = true, length = 50)
    private String assetNumber;

    @Column(name = "asset_name", nullable = false, length = 100)
    private String assetName;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_status", nullable = false, length = 10)
    private AssetStatus assetStatus;

    @Column(name = "purchased_at")
    private LocalDate purchasedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Asset(String assetNumber, String assetName, String category,
                 User assignedUser, String description,
                 AssetStatus assetStatus, LocalDate purchasedAt) {
        this.assetNumber  = assetNumber;
        this.assetName    = assetName;
        this.category     = category;
        this.assignedUser = assignedUser;
        this.description  = description;
        this.assetStatus  = assetStatus != null ? assetStatus : AssetStatus.ACTIVE;
        this.purchasedAt  = purchasedAt;
    }

    public void update(String assetNumber, String assetName, String category,
                       User assignedUser, String description,
                       AssetStatus assetStatus, LocalDate purchasedAt) {
        if (assetNumber != null)  this.assetNumber  = assetNumber;
        if (assetName != null)    this.assetName    = assetName;
        if (category != null)     this.category     = category;
        this.assignedUser = assignedUser;
        if (description != null)  this.description  = description;
        if (assetStatus != null)  this.assetStatus  = assetStatus;
        if (purchasedAt != null)  this.purchasedAt  = purchasedAt;
    }
}
