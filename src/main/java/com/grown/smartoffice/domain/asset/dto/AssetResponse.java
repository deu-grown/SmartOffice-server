package com.grown.smartoffice.domain.asset.dto;

import com.grown.smartoffice.domain.asset.entity.Asset;
import com.grown.smartoffice.domain.asset.entity.AssetStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class AssetResponse {
    private Long assetId;
    private String assetNumber;
    private String assetName;
    private String category;
    private Long assignedUserId;
    private String assignedUserName;
    private String description;
    private AssetStatus assetStatus;
    private LocalDate purchasedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AssetResponse from(Asset a) {
        return AssetResponse.builder()
                .assetId(a.getAssetId())
                .assetNumber(a.getAssetNumber())
                .assetName(a.getAssetName())
                .category(a.getCategory())
                .assignedUserId(a.getAssignedUser() != null ? a.getAssignedUser().getUserId() : null)
                .assignedUserName(a.getAssignedUser() != null ? a.getAssignedUser().getEmployeeName() : null)
                .description(a.getDescription())
                .assetStatus(a.getAssetStatus())
                .purchasedAt(a.getPurchasedAt())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
