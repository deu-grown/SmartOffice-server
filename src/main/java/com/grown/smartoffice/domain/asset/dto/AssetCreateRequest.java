package com.grown.smartoffice.domain.asset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class AssetCreateRequest {

    @NotBlank(message = "자산 번호는 필수입니다.")
    @Size(max = 50)
    private String assetNumber;

    @NotBlank(message = "자산명은 필수입니다.")
    @Size(max = 100)
    private String assetName;

    @NotBlank(message = "카테고리는 필수입니다.")
    @Size(max = 50)
    private String category;

    private Long assignedUserId;

    @Size(max = 500)
    private String description;

    private String assetStatus;

    private LocalDate purchasedAt;
}
