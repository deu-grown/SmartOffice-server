package com.grown.smartoffice.domain.reservation.entity;

import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.zone.entity.Zone;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservations_id")
    private Long reservationsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(name = "reservations_title", nullable = false, length = 200)
    private String reservationsTitle;

    @Column(name = "reservations_start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "reservations_end_at", nullable = false)
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "reservations_status", nullable = false, length = 10)
    private ReservationStatus status;

    @Column(name = "reservations_checked_in_at")
    private LocalDateTime checkedInAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Reservation(User user, Zone zone, String reservationsTitle,
                       LocalDateTime startAt, LocalDateTime endAt) {
        this.user = user;
        this.zone = zone;
        this.reservationsTitle = reservationsTitle;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = ReservationStatus.CONFIRMED;
    }

    public void update(String reservationsTitle, LocalDateTime startAt, LocalDateTime endAt) {
        if (reservationsTitle != null) this.reservationsTitle = reservationsTitle;
        if (startAt != null)           this.startAt = startAt;
        if (endAt != null)             this.endAt = endAt;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    public void checkIn(LocalDateTime checkedInAt) {
        this.status = ReservationStatus.CHECKED_IN;
        this.checkedInAt = checkedInAt;
    }
}
