package com.grown.smartoffice.domain.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VehicleCreateRequest {

    @NotBlank(message = "차량 번호판은 필수입니다.")
    @Size(max = 20)
    @Schema(description = "차량 번호판", example = "12가3456")
    private String plateNumber;

    @NotBlank(message = "소유자명은 필수입니다.")
    @Size(max = 50)
    @Schema(description = "소유자명", example = "박성종")
    private String ownerName;

    @Schema(description = "임직원 사용자 ID (임직원 차량일 때만, 방문객은 null)", example = "1")
    private Long ownerUserId;

    @NotNull(message = "차량 구분은 필수입니다.")
    @Schema(description = "차량 구분 (STAFF | VISITOR)", example = "STAFF")
    private String vehicleType;

    @Size(max = 200)
    @Schema(description = "방문 목적 (방문객 차량 등)", example = "협력사 미팅")
    private String purpose;
}
