package com.grown.smartoffice.domain.zone.dto;

import com.grown.smartoffice.domain.zone.entity.ZoneType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ZoneUpdateRequest {

    @Size(max = 100)
    @Schema(description = "변경할 구역 이름", example = "2층")
    private String name;

    @Schema(description = "변경할 구역 유형 (FLOOR | AREA)", example = "FLOOR")
    private ZoneType zoneType;

    @Schema(description = "변경할 상위 구역 ID (null이면 최상위)")
    private Long parentId;

    @Schema(description = "변경 여부를 명시적으로 전달하기 위해 parentId와 함께 사용")
    private Boolean clearParent;

    @Size(max = 255)
    @Schema(description = "변경할 구역 설명")
    private String description;
}
