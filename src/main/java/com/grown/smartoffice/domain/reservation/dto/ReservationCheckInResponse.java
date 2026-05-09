package com.grown.smartoffice.domain.reservation.dto;

import com.grown.smartoffice.domain.reservation.entity.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReservationCheckInResponse {

    private LocalDateTime checkInTime;
    private ReservationStatus status;
}
