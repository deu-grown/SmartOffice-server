package com.grown.smartoffice.domain.zone.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "zones")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "zone_id")
    private Long zoneId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_parent_id")
    private Zone parent;

    @OneToMany(mappedBy = "parent")
    private List<Zone> children = new ArrayList<>();

    @Column(name = "zone_name", nullable = false, length = 100)
    private String zoneName;

    @Enumerated(EnumType.STRING)
    @Column(name = "zone_type", nullable = false, length = 10)
    private ZoneType zoneType;

    @Column(name = "zone_description", length = 255)
    private String zoneDescription;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Zone(Zone parent, String zoneName, ZoneType zoneType, String zoneDescription) {
        this.parent = parent;
        this.zoneName = zoneName;
        this.zoneType = zoneType;
        this.zoneDescription = zoneDescription;
    }

    public void update(String zoneName, ZoneType zoneType, Zone parent, String zoneDescription) {
        if (zoneName != null) this.zoneName = zoneName;
        if (zoneType != null) this.zoneType = zoneType;
        if (parent != null) this.parent = parent;
        if (zoneDescription != null) this.zoneDescription = zoneDescription;
    }
}
