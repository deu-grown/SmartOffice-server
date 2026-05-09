package com.grown.smartoffice.domain.control.entity;

import com.grown.smartoffice.domain.device.entity.Device;
import com.grown.smartoffice.domain.zone.entity.Zone;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "control_commands")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ControlCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "control_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devices_id", nullable = false)
    private Device device;

    @Column(name = "command_type", nullable = false, length = 15)
    private String commandType;

    @Column(name = "control_payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "control_status", nullable = false, length = 10)
    private ControlStatus status;

    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ControlCommand(Zone zone, Device device, String commandType, String payload, ControlStatus status, LocalDateTime triggeredAt) {
        this.zone = zone;
        this.device = device;
        this.commandType = commandType;
        this.payload = payload;
        this.status = (status != null) ? status : ControlStatus.PENDING;
        this.triggeredAt = (triggeredAt != null) ? triggeredAt : LocalDateTime.now();
    }

    public void updateStatus(ControlStatus status) {
        this.status = status;
    }
}
