package com.grown.smartoffice.domain.reservation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ReservationUpdateRequest {

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String purpose;
}
