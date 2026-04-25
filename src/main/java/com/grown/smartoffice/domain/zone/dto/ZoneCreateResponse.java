package com.grown.smartoffice.domain.zone.dto;

import com.grown.smartoffice.domain.zone.entity.Zone;
import com.grown.smartoffice.domain.zone.entity.ZoneType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ZoneCreateResponse {

    private Long id;
    private String name;
    private ZoneType zoneType;
    private Long parentId;
    private String description;
    private LocalDateTime createdAt;

    public static ZoneCreateResponse from(Zone zone) {
        return ZoneCreateResponse.builder()
                .id(zone.getZoneId())
                .name(zone.getZoneName())
                .zoneType(zone.getZoneType())
                .parentId(zone.getParent() != null ? zone.getParent().getZoneId() : null)
                .description(zone.getZoneDescription())
                .createdAt(zone.getCreatedAt())
                .build();
    }
}
