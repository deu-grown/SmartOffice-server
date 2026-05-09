package com.grown.smartoffice.domain.reservation.dto;

import com.grown.smartoffice.domain.reservation.entity.Reservation;
import com.grown.smartoffice.domain.reservation.entity.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ReservationListResponse {

    private Integer totalCount;
    private List<ReservationListItem> reservationList;

    @Getter
    @Builder
    public static class ReservationListItem {
        private Long id;
        private String userName;
        private String zoneName;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private ReservationStatus status;

        public static ReservationListItem from(Reservation r) {
            return ReservationListItem.builder()
                    .id(r.getReservationsId())
                    .userName(r.getUser().getEmployeeName())
                    .zoneName(r.getZone().getZoneName())
                    .startTime(r.getStartAt())
                    .endTime(r.getEndAt())
                    .status(r.getStatus())
                    .build();
        }
    }

    public static ReservationListResponse from(List<Reservation> reservations) {
        List<ReservationListItem> items = reservations.stream()
                .map(ReservationListItem::from)
                .toList();
        return ReservationListResponse.builder()
                .totalCount(items.size())
                .reservationList(items)
                .build();
    }
}
