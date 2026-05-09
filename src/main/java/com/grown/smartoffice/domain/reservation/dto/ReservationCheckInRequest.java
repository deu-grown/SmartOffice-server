package com.grown.smartoffice.domain.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReservationCheckInRequest {

    @NotBlank
    private String nfcTagId;

    private Double latitude;
    private Double longitude;
}
