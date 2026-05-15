package com.grown.smartoffice.domain.zone.service;

import com.grown.smartoffice.domain.zone.dto.*;
import com.grown.smartoffice.domain.zone.entity.Zone;
import com.grown.smartoffice.domain.zone.entity.ZoneType;
import com.grown.smartoffice.domain.zone.repository.ZoneRepository;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ZoneServiceImpl implements ZoneService {

    private final ZoneRepository zoneRepository;

    @Override
    @Transactional
    public ZoneCreateResponse createZone(ZoneCreateRequest request) {
        Zone parent = null;
        if (request.getParentId() != null) {
            parent = zoneRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PARENT_ZONE_NOT_FOUND));
        }

        validateDuplicateName(request.getParentId(), request.getName());

        Zone zone = Zone.builder()
                .parent(parent)
                .zoneName(request.getName())
                .zoneType(request.getZoneType())
                .zoneDescription(request.getDescription())
                .build();

        return ZoneCreateResponse.from(zoneRepository.save(zone));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZoneListItemResponse> getZones(Long parentId, String zoneType) {
        List<Zone> zones;

        ZoneType type = zoneType != null ? ZoneType.valueOf(zoneType) : null;

        if (parentId != null && type != null) {
            zones = zoneRepository.findAllByParent_ZoneIdAndZoneType(parentId, type);
        } else if (parentId != null) {
            zones = zoneRepository.findAllByParent_ZoneId(parentId);
        } else if (type != null) {
            zones = zoneRepository.findAllByZoneType(type);
        } else {
            zones = zoneRepository.findAll();
        }

        return zones.stream().map(ZoneListItemResponse::from).toList();
    }

    @Override
    @Transactional
    public ZoneUpdateResponse updateZone(Long id, ZoneUpdateRequest request) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ZONE_NOT_FOUND));

        Zone newParent = null;
        Long newParentId = request.getParentId();

        if (newParentId != null) {
            newParent = zoneRepository.findById(newParentId)
                    .orElseThrow(() -> new CustomException(ErrorCode.PARENT_ZONE_NOT_FOUND));
            validateNoCircularReference(id, newParent);
        }

        String newName = request.getName() != null ? request.getName() : zone.getZoneName();
        Long currentParentId = (Boolean.TRUE.equals(request.getClearParent()) || newParentId != null)
                ? newParentId
                : (zone.getParent() != null ? zone.getParent().getZoneId() : null);

        validateDuplicateNameExcludingSelf(currentParentId, newName, id);

        zone.update(request.getName(), request.getZoneType(), newParent, request.getDescription());
        return ZoneUpdateResponse.from(zone);
    }

    @Override
    @Transactional
    public void deleteZone(Long id) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ZONE_NOT_FOUND));

        if (zoneRepository.existsByParent_ZoneId(id)) {
            throw new CustomException(ErrorCode.ZONE_HAS_CHILDREN);
        }
        if (zoneRepository.hasDevices(id)) {
            throw new CustomException(ErrorCode.ZONE_HAS_DEVICES);
        }

        zoneRepository.delete(zone);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZoneTreeResponse> getZoneTree() {
        return zoneRepository.findRootsWithChildren().stream()
                .map(ZoneTreeResponse::from)
                .toList();
    }

    private void validateDuplicateName(Long parentId, String name) {
        boolean dup = parentId != null
                ? zoneRepository.existsByParent_ZoneIdAndZoneName(parentId, name)
                : zoneRepository.existsByParentIsNullAndZoneName(name);
        if (dup) throw new CustomException(ErrorCode.DUPLICATE_ZONE_NAME);
    }

    private void validateDuplicateNameExcludingSelf(Long parentId, String name, Long selfId) {
        List<Zone> siblings = parentId != null
                ? zoneRepository.findAllByParent_ZoneId(parentId)
                : zoneRepository.findAllRoots();

        boolean dup = siblings.stream()
                .anyMatch(z -> z.getZoneName().equals(name) && !z.getZoneId().equals(selfId));
        if (dup) throw new CustomException(ErrorCode.DUPLICATE_ZONE_NAME);
    }

    private void validateNoCircularReference(Long targetId, Zone candidateParent) {
        Zone cursor = candidateParent;
        while (cursor != null) {
            if (cursor.getZoneId().equals(targetId)) {
                throw new CustomException(ErrorCode.INVALID_ZONE_HIERARCHY);
            }
            cursor = cursor.getParent();
        }
    }
}
