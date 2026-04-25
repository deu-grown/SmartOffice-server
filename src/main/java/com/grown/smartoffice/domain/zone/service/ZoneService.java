package com.grown.smartoffice.domain.zone.service;

import com.grown.smartoffice.domain.zone.dto.*;

import java.util.List;

public interface ZoneService {

    ZoneCreateResponse createZone(ZoneCreateRequest request);

    List<ZoneListItemResponse> getZones(Long parentId, String zoneType);

    ZoneUpdateResponse updateZone(Long id, ZoneUpdateRequest request);

    void deleteZone(Long id);

    List<ZoneTreeResponse> getZoneTree();
}
