package com.grown.smartoffice.domain.asset.service;

import com.grown.smartoffice.domain.asset.dto.AssetCreateRequest;
import com.grown.smartoffice.domain.asset.dto.AssetResponse;
import com.grown.smartoffice.domain.asset.dto.AssetUpdateRequest;
import com.grown.smartoffice.domain.asset.entity.Asset;
import com.grown.smartoffice.domain.asset.entity.AssetStatus;
import com.grown.smartoffice.domain.asset.repository.AssetRepository;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import com.grown.smartoffice.global.common.PageResponse;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final UserRepository userRepository;

    @Transactional
    public AssetResponse createAsset(AssetCreateRequest request) {
        if (assetRepository.existsByAssetNumber(request.getAssetNumber())) {
            throw new CustomException(ErrorCode.DUPLICATE_ASSET_NUMBER);
        }
        User assignedUser = resolveUser(request.getAssignedUserId());
        AssetStatus status = request.getAssetStatus() != null
                ? AssetStatus.valueOf(request.getAssetStatus())
                : AssetStatus.ACTIVE;

        Asset asset = Asset.builder()
                .assetNumber(request.getAssetNumber())
                .assetName(request.getAssetName())
                .category(request.getCategory())
                .assignedUser(assignedUser)
                .description(request.getDescription())
                .assetStatus(status)
                .purchasedAt(request.getPurchasedAt())
                .build();
        return AssetResponse.from(assetRepository.save(asset));
    }

    @Transactional(readOnly = true)
    public PageResponse<AssetResponse> getAssets(String category, String status,
                                                  Long assignedUserId, String keyword,
                                                  int page, int size) {
        AssetStatus assetStatus = status != null ? AssetStatus.valueOf(status) : null;
        return PageResponse.from(
                assetRepository.findAllWithFilters(
                        category, assetStatus, assignedUserId, keyword,
                        PageRequest.of(page, size)
                ).map(AssetResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public AssetResponse getAsset(Long id) {
        Asset asset = assetRepository.findByIdWithUser(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ASSET_NOT_FOUND));
        return AssetResponse.from(asset);
    }

    @Transactional
    public AssetResponse updateAsset(Long id, AssetUpdateRequest request) {
        Asset asset = assetRepository.findByIdWithUser(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ASSET_NOT_FOUND));

        if (request.getAssetNumber() != null
                && assetRepository.existsByAssetNumberAndAssetIdNot(request.getAssetNumber(), id)) {
            throw new CustomException(ErrorCode.DUPLICATE_ASSET_NUMBER);
        }
        User assignedUser = resolveUser(request.getAssignedUserId());
        AssetStatus assetStatus = request.getAssetStatus() != null
                ? AssetStatus.valueOf(request.getAssetStatus())
                : null;

        asset.update(request.getAssetNumber(), request.getAssetName(), request.getCategory(),
                assignedUser, request.getDescription(), assetStatus, request.getPurchasedAt());
        return AssetResponse.from(asset);
    }

    @Transactional
    public void deleteAsset(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ASSET_NOT_FOUND));
        assetRepository.delete(asset);
    }

    private User resolveUser(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
