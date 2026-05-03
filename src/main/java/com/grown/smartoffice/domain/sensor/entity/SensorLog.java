package com.grown.smartoffice.domain.sensor.entity;

import com.grown.smartoffice.domain.device.entity.Device;
import com.grown.smartoffice.domain.zone.entity.Zone;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class SensorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sensor_logs_id")
    private Long sensorLogsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devices_id", nullable = false)
    private Device device;

    @Column(name = "sensor_type", nullable = false, length = 15)
    private String sensorType;

    @Column(name = "sensor_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal sensorValue;

    @Column(name = "sensor_unit", nullable = false, length = 20)
    private String sensorUnit;

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
