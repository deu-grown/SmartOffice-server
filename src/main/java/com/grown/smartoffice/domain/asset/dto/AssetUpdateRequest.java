package com.grown.smartoffice.domain.asset.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class AssetUpdateRequest {

    @Size(max = 50)
    private String assetNumber;

    @Size(max = 100)
    private String assetName;

    @Size(max = 50)
    private String category;

    private Long assignedUserId;

    @Size(max = 500)
    private String description;

    private String assetStatus;

    private LocalDate purchasedAt;
}
