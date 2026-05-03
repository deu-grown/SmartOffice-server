package com.grown.smartoffice.domain.asset.controller;

import com.grown.smartoffice.domain.asset.dto.AssetCreateRequest;
import com.grown.smartoffice.domain.asset.dto.AssetResponse;
import com.grown.smartoffice.domain.asset.dto.AssetUpdateRequest;
import com.grown.smartoffice.domain.asset.service.AssetService;
import com.grown.smartoffice.global.common.ApiResponse;
import com.grown.smartoffice.global.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Assets", description = "자산 관리 대장 [ADMIN 전용]")
@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @Operation(summary = "자산 등록 [ADMIN]")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AssetResponse>> createAsset(
            @RequestBody @Valid AssetCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("자산이 등록되었습니다.", assetService.createAsset(request)));
    }

    @Operation(summary = "자산 목록 조회 [ADMIN]",
               description = "필터: category, status(ACTIVE|INACTIVE|LOST), assignedUserId, keyword")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<AssetResponse>>> getAssets(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long assignedUserId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("정상 조회되었습니다.",
                assetService.getAssets(category, status, assignedUserId, keyword, page, size)));
    }

    @Operation(summary = "자산 상세 조회 [ADMIN]")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AssetResponse>> getAsset(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("정상 조회되었습니다.", assetService.getAsset(id)));
    }

    @Operation(summary = "자산 수정 [ADMIN]")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AssetResponse>> updateAsset(
            @PathVariable Long id,
            @RequestBody @Valid AssetUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("자산 정보가 수정되었습니다.",
                assetService.updateAsset(id, request)));
    }

    @Operation(summary = "자산 삭제 [ADMIN]")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return ResponseEntity.ok(ApiResponse.success("자산이 삭제되었습니다."));
    }
}
