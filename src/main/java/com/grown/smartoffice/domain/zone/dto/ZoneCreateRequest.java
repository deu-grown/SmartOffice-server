package com.grown.smartoffice.domain.zone.dto;

import com.grown.smartoffice.domain.zone.entity.ZoneType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ZoneCreateRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "구역 이름", example = "1층")
    private String name;

    @NotNull
    @Schema(description = "구역 유형 (FLOOR | AREA)", example = "FLOOR")
    private ZoneType zoneType;

    @Schema(description = "상위 구역 ID (최상위이면 null)", example = "null")
    private Long parentId;

    @Size(max = 255)
    @Schema(description = "구역 설명", example = "본관 1층 전체")
    private String description;
}
