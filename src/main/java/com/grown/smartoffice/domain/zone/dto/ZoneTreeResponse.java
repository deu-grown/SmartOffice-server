package com.grown.smartoffice.domain.zone.dto;

import com.grown.smartoffice.domain.zone.entity.Zone;
import com.grown.smartoffice.domain.zone.entity.ZoneType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ZoneTreeResponse {

    private Long id;
    private String name;
    private ZoneType zoneType;
    private String description;
    private List<ZoneTreeResponse> children;

    public static ZoneTreeResponse from(Zone zone) {
        List<ZoneTreeResponse> children = zone.getChildren().stream()
                .map(ZoneTreeResponse::from)
                .toList();

        return ZoneTreeResponse.builder()
                .id(zone.getZoneId())
                .name(zone.getZoneName())
                .zoneType(zone.getZoneType())
                .description(zone.getZoneDescription())
                .children(children)
                .build();
    }
}
