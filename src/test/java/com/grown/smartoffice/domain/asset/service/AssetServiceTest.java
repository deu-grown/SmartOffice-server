package com.grown.smartoffice.domain.asset.service;

import com.grown.smartoffice.domain.asset.dto.AssetCreateRequest;
import com.grown.smartoffice.domain.asset.dto.AssetResponse;
import com.grown.smartoffice.domain.asset.dto.AssetUpdateRequest;
import com.grown.smartoffice.domain.asset.entity.Asset;
import com.grown.smartoffice.domain.asset.entity.AssetStatus;
import com.grown.smartoffice.domain.asset.repository.AssetRepository;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserRole;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock AssetRepository assetRepository;
    @Mock UserRepository userRepository;
    @InjectMocks AssetService assetService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .employeeNumber("EMP001").employeeName("관리자").employeeEmail("admin@grown.com")
                .password("pw").role(UserRole.ADMIN).position("팀장")
                .status(UserStatus.ACTIVE).hiredAt(LocalDate.now()).build();
        ReflectionTestUtils.setField(user, "userId", 1L);
    }

    @Test
    @DisplayName("자산 등록 성공 — assignedUser null도 허용")
    void createAsset_success() {
        AssetCreateRequest req = new AssetCreateRequest();
        ReflectionTestUtils.setField(req, "assetNumber", "AST-X-001");
        ReflectionTestUtils.setField(req, "assetName", "노트북");
        ReflectionTestUtils.setField(req, "category", "IT기기");

        given(assetRepository.existsByAssetNumber("AST-X-001")).willReturn(false);
        given(assetRepository.save(any(Asset.class))).willAnswer(inv -> {
            Asset saved = inv.getArgument(0);
            ReflectionTestUtils.setField(saved, "assetId", 100L);
            return saved;
        });

        AssetResponse res = assetService.createAsset(req);
        assertThat(res.getAssetId()).isEqualTo(100L);
        assertThat(res.getAssetStatus()).isEqualTo(AssetStatus.ACTIVE);  // 기본값
    }

    @Test
    @DisplayName("자산 등록 — 중복 assetNumber 시 DUPLICATE_ASSET_NUMBER")
    void createAsset_duplicate() {
        AssetCreateRequest req = new AssetCreateRequest();
        ReflectionTestUtils.setField(req, "assetNumber", "AST-2026-001");
        ReflectionTestUtils.setField(req, "assetName", "x");
        ReflectionTestUtils.setField(req, "category", "x");

        given(assetRepository.existsByAssetNumber("AST-2026-001")).willReturn(true);

        assertThatThrownBy(() -> assetService.createAsset(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_ASSET_NUMBER);
    }

    @Test
    @DisplayName("자산 등록 — assignedUserId 미존재 시 USER_NOT_FOUND")
    void createAsset_userNotFound() {
        AssetCreateRequest req = new AssetCreateRequest();
        ReflectionTestUtils.setField(req, "assetNumber", "AST-Y");
        ReflectionTestUtils.setField(req, "assetName", "x");
        ReflectionTestUtils.setField(req, "category", "x");
        ReflectionTestUtils.setField(req, "assignedUserId", 999L);

        given(assetRepository.existsByAssetNumber("AST-Y")).willReturn(false);
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> assetService.createAsset(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("자산 단건 조회 — ASSET_NOT_FOUND")
    void getAsset_notFound() {
        given(assetRepository.findByIdWithUser(999L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> assetService.getAsset(999L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.ASSET_NOT_FOUND);
    }

    @Test
    @DisplayName("자산 수정 — 중복 assetNumber(자기 자신 제외)")
    void updateAsset_duplicateNumber() {
        Asset asset = Asset.builder()
                .assetNumber("AST-A").assetName("a").category("c").assetStatus(AssetStatus.ACTIVE).build();
        ReflectionTestUtils.setField(asset, "assetId", 50L);

        AssetUpdateRequest req = new AssetUpdateRequest();
        ReflectionTestUtils.setField(req, "assetNumber", "AST-B");

        given(assetRepository.findByIdWithUser(50L)).willReturn(Optional.of(asset));
        given(assetRepository.existsByAssetNumberAndAssetIdNot("AST-B", 50L)).willReturn(true);

        assertThatThrownBy(() -> assetService.updateAsset(50L, req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_ASSET_NUMBER);
    }

    @Test
    @DisplayName("자산 수정 — assignedUser 변경 + 상태 전이 ACTIVE→LOST 성공")
    void updateAsset_success() {
        Asset asset = Asset.builder()
                .assetNumber("AST-A").assetName("a").category("c").assetStatus(AssetStatus.ACTIVE).build();
        ReflectionTestUtils.setField(asset, "assetId", 50L);

        AssetUpdateRequest req = new AssetUpdateRequest();
        ReflectionTestUtils.setField(req, "assignedUserId", 1L);
        ReflectionTestUtils.setField(req, "assetStatus", "LOST");

        given(assetRepository.findByIdWithUser(50L)).willReturn(Optional.of(asset));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        AssetResponse res = assetService.updateAsset(50L, req);
        assertThat(res.getAssetStatus()).isEqualTo(AssetStatus.LOST);
        assertThat(asset.getAssignedUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("자산 삭제 — 존재하지 않으면 ASSET_NOT_FOUND")
    void deleteAsset_notFound() {
        given(assetRepository.findById(999L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> assetService.deleteAsset(999L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.ASSET_NOT_FOUND);
    }
}
