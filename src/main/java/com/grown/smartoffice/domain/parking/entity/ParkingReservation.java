package com.grown.smartoffice.domain.parking.entity;

import com.grown.smartoffice.domain.vehicle.entity.Vehicle;
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
@Table(name = "parking_reservation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ParkingReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long reservationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id")
    private ParkingSpot spot;

    @Column(name = "reserved_at", nullable = false)
    private LocalDateTime reservedAt;

    @Column(name = "entry_at")
    private LocalDateTime entryAt;

    @Column(name = "exit_at")
    private LocalDateTime exitAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status", nullable = false, length = 10)
    private ParkingReservationStatus reservationStatus;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public ParkingReservation(Vehicle vehicle, Zone zone, ParkingSpot spot,
                              LocalDateTime reservedAt, LocalDateTime entryAt, LocalDateTime exitAt,
                              ParkingReservationStatus reservationStatus) {
        this.vehicle = vehicle;
        this.zone = zone;
        this.spot = spot;
        this.reservedAt = (reservedAt != null) ? reservedAt : LocalDateTime.now();
        this.entryAt = entryAt;
        this.exitAt = exitAt;
        this.reservationStatus = (reservationStatus != null) ? reservationStatus : ParkingReservationStatus.RESERVED;
    }

    public void update(ParkingSpot spot, LocalDateTime entryAt, LocalDateTime exitAt,
                       ParkingReservationStatus reservationStatus) {
        this.spot = spot;
        if (entryAt != null) this.entryAt = entryAt;
        if (exitAt != null) this.exitAt = exitAt;
        if (reservationStatus != null) this.reservationStatus = reservationStatus;
    }
}
