package com.grown.smartoffice.domain.device.entity;

import com.grown.smartoffice.domain.zone.entity.Zone;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "devices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "devices_id")
    private Long devicesId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(name = "device_name", nullable = false, length = 100)
    private String deviceName;

    @Column(name = "device_type", nullable = false, length = 20)
    private String deviceType;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "mqtt_topic", length = 255)
    private String mqttTopic;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_status", nullable = false, length = 10)
    private DeviceStatus deviceStatus;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @lombok.Builder
    public Device(Zone zone, String deviceName, String deviceType, String serialNumber, DeviceStatus deviceStatus) {
        this.zone = zone;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.serialNumber = serialNumber;
        this.deviceStatus = (deviceStatus != null) ? deviceStatus : DeviceStatus.ACTIVE;
    }

    public void updateInfo(String deviceName, String deviceType, String serialNumber, DeviceStatus deviceStatus, Zone zone) {
        if (deviceName != null) this.deviceName = deviceName;
        if (deviceType != null) this.deviceType = deviceType;
        if (serialNumber != null) this.serialNumber = serialNumber;
        if (deviceStatus != null) this.deviceStatus = deviceStatus;
        if (zone != null) this.zone = zone;
    }

    public void updateMqttTopic(String mqttTopic) {
        this.mqttTopic = mqttTopic;
    }
}
