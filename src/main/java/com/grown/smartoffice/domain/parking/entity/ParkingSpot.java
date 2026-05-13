package com.grown.smartoffice.domain.parking.entity;

import com.grown.smartoffice.domain.device.entity.Device;
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
@Table(name = "parking_spots")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ParkingSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "spot_id")
    private Long spotId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(name = "spot_number", nullable = false, length = 20)
    private String spotNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "spot_type", nullable = false, length = 10)
    private SpotType spotType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", unique = true)
    private Device device;

    @Column(name = "position_x")
    private Integer positionX;

    @Column(name = "position_y")
    private Integer positionY;

    @Column(name = "is_occupied", nullable = false)
    private boolean occupied;

    @Enumerated(EnumType.STRING)
    @Column(name = "spot_status", nullable = false, length = 10)
    private SpotStatus spotStatus;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public ParkingSpot(Zone zone, String spotNumber, SpotType spotType, Device device,
                       Integer positionX, Integer positionY, Boolean occupied, SpotStatus spotStatus) {
        this.zone = zone;
        this.spotNumber = spotNumber;
        this.spotType = (spotType != null) ? spotType : SpotType.REGULAR;
        this.device = device;
        this.positionX = positionX;
        this.positionY = positionY;
        this.occupied = occupied != null && occupied;
        this.spotStatus = (spotStatus != null) ? spotStatus : SpotStatus.ACTIVE;
    }

    public void update(String spotNumber, SpotType spotType, Device device,
                       Integer positionX, Integer positionY, SpotStatus spotStatus) {
        if (spotNumber != null) this.spotNumber = spotNumber;
        if (spotType != null) this.spotType = spotType;
        this.device = device;
        if (positionX != null) this.positionX = positionX;
        if (positionY != null) this.positionY = positionY;
        if (spotStatus != null) this.spotStatus = spotStatus;
    }

    public void updateOccupancy(boolean occupied) {
        this.occupied = occupied;
    }
}
