package com.grown.smartoffice.domain.vehicle.entity;

import com.grown.smartoffice.domain.user.entity.User;
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
@Table(name = "vehicle")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "plate_number", nullable = false, unique = true, length = 20)
    private String plateNumber;

    @Column(name = "owner_name", nullable = false, length = 50)
    private String ownerName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User ownerUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 10)
    private VehicleType vehicleType;

    @Column(name = "purpose", length = 200)
    private String purpose;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Vehicle(String plateNumber, String ownerName, User ownerUser,
                   VehicleType vehicleType, String purpose) {
        this.plateNumber = plateNumber;
        this.ownerName = ownerName;
        this.ownerUser = ownerUser;
        this.vehicleType = (vehicleType != null) ? vehicleType : VehicleType.VISITOR;
        this.purpose = purpose;
    }

    public void update(String plateNumber, String ownerName, User ownerUser,
                       VehicleType vehicleType, String purpose) {
        if (plateNumber != null) this.plateNumber = plateNumber;
        if (ownerName != null) this.ownerName = ownerName;
        this.ownerUser = ownerUser;
        if (vehicleType != null) this.vehicleType = vehicleType;
        if (purpose != null) this.purpose = purpose;
    }
}
