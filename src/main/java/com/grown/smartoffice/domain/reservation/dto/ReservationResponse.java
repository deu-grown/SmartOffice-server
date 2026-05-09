package com.grown.smartoffice.domain.reservation.dto;

import com.grown.smartoffice.domain.reservation.entity.Reservation;
import com.grown.smartoffice.domain.reservation.entity.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReservationResponse {

    private Long reservationId;
    private String zoneName;
    private String userName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String purpose;
    private ReservationStatus status;
    private LocalDateTime checkInTime;

    public static ReservationResponse from(Reservation r) {
        return ReservationResponse.builder()
                .reservationId(r.getReservationsId())
                .zoneName(r.getZone().getZoneName())
                .userName(r.getUser().getEmployeeName())
                .startTime(r.getStartAt())
                .endTime(r.getEndAt())
                .purpose(r.getReservationsTitle())
                .status(r.getStatus())
                .checkInTime(r.getCheckedInAt())
                .build();
    }
}
