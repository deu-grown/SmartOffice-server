package com.grown.smartoffice.domain.power.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PowerZoneListResponse {

    private Long zoneId;
    private String zoneName;
    private Long meterCount;
}
