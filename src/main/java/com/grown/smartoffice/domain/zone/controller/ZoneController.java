package com.grown.smartoffice.domain.zone.controller;

import com.grown.smartoffice.domain.zone.dto.*;
import com.grown.smartoffice.domain.zone.service.ZoneService;
import com.grown.smartoffice.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Zones", description = "구역 관리 [ADMIN 전용]")
@RestController
@RequestMapping("/api/v1/zones")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ZoneController {

    private final ZoneService zoneService;

    @Operation(summary = "구역 목록 조회 [ADMIN]", description = "parentId, zoneType 필터 지원. 둘 다 없으면 전체 반환.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ZoneListItemResponse>>> getZones(
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) String zoneType) {
        return ResponseEntity.ok(
                ApiResponse.success("정상 조회되었습니다.", zoneService.getZones(parentId, zoneType)));
    }

    @Operation(summary = "구역 트리 조회 [ADMIN]", description = "루트 구역부터 재귀적으로 children을 포함한 트리 구조 반환.")
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<ZoneTreeResponse>>> getZoneTree() {
        return ResponseEntity.ok(
                ApiResponse.success("정상 조회되었습니다.", zoneService.getZoneTree()));
    }

    @Operation(summary = "구역 등록 [ADMIN]")
    @PostMapping
    public ResponseEntity<ApiResponse<ZoneCreateResponse>> createZone(
            @RequestBody @Valid ZoneCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("구역이 등록되었습니다.", zoneService.createZone(request)));
    }

    @Operation(summary = "구역 수정 [ADMIN]", description = "상위 구역 변경 시 순환 참조 검증. clearParent=true면 최상위로 변경.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ZoneUpdateResponse>> updateZone(
            @PathVariable Long id,
            @RequestBody @Valid ZoneUpdateRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("구역 정보가 수정되었습니다.", zoneService.updateZone(id, request)));
    }

    @Operation(summary = "구역 삭제 [ADMIN]", description = "하위 구역 또는 설치 장치가 있으면 409 반환.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteZone(@PathVariable Long id) {
        zoneService.deleteZone(id);
        return ResponseEntity.ok(ApiResponse.success("구역이 삭제되었습니다."));
    }
}
