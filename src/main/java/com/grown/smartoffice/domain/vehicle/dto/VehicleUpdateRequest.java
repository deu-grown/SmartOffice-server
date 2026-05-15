package com.grown.smartoffice.domain.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VehicleUpdateRequest {

    @Size(max = 20)
    @Schema(description = "차량 번호판", example = "78나9012")
    private String plateNumber;

    @Size(max = 50)
    @Schema(description = "소유자명", example = "김방문")
    private String ownerName;

    @Schema(description = "임직원 사용자 ID (해제 시 null)", example = "2")
    private Long ownerUserId;

    @Schema(description = "차량 구분 (STAFF | VISITOR)", example = "VISITOR")
    private String vehicleType;

    @Size(max = 200)
    @Schema(description = "방문 목적", example = "장비 납품")
    private String purpose;
}
