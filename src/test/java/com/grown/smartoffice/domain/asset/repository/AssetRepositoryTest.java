package com.grown.smartoffice.domain.asset.repository;

import com.grown.smartoffice.domain.asset.entity.Asset;
import com.grown.smartoffice.domain.asset.entity.AssetStatus;
import com.grown.smartoffice.domain.department.entity.Department;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserRole;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import com.grown.smartoffice.support.RepositoryTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AssetRepositoryTest extends RepositoryTestSupport {

    @Autowired AssetRepository assetRepository;
    @Autowired TestEntityManager em;

    private User userA;

    @BeforeEach
    void setUp() {
        Department dept = Department.builder().deptName("ART팀-" + System.nanoTime()).build();
        em.persist(dept);
        userA = User.builder().department(dept)
                .employeeNumber("ART-U-" + System.nanoTime())
                .employeeName("자산테스터").employeeEmail("art-" + System.nanoTime() + "@grown.com")
                .password("pw").role(UserRole.USER).position("사원")
                .status(UserStatus.ACTIVE).hiredAt(LocalDate.now()).build();
        em.persist(userA);
        em.flush();
    }

    private Asset asset(String num, String category, AssetStatus status, User assignee) {
        return Asset.builder()
                .assetNumber(num).assetName(num + "_name").category(category)
                .assignedUser(assignee).description("d").assetStatus(status).build();
    }

    @Test
    @DisplayName("existsByAssetNumber — 중복 검출")
    void existsByAssetNumber() {
        em.persist(asset("ART-001", "IT기기", AssetStatus.ACTIVE, null));
        em.flush();

        assertThat(assetRepository.existsByAssetNumber("ART-001")).isTrue();
        assertThat(assetRepository.existsByAssetNumber("ART-XYZ")).isFalse();
    }

    @Test
    @DisplayName("existsByAssetNumberAndAssetIdNot — 자기 자신 제외")
    void existsByAssetNumberAndIdNot() {
        Asset a = asset("ART-002", "IT기기", AssetStatus.ACTIVE, null);
        em.persist(a);
        em.flush();

        assertThat(assetRepository.existsByAssetNumberAndAssetIdNot("ART-002", a.getAssetId())).isFalse();
        assertThat(assetRepository.existsByAssetNumberAndAssetIdNot("ART-002", 9999L)).isTrue();
    }

    @Test
    @DisplayName("findByIdWithUser — assignedUser JOIN FETCH로 즉시 로딩")
    void findByIdWithUser() {
        Asset a = asset("ART-003", "가구", AssetStatus.ACTIVE, userA);
        em.persist(a);
        em.flush();
        em.clear();

        assertThat(assetRepository.findByIdWithUser(a.getAssetId()))
                .isPresent()
                .hasValueSatisfying(found -> assertThat(found.getAssignedUser().getEmployeeName())
                        .isEqualTo("자산테스터"));
    }

    @Test
    @DisplayName("findAllWithFilters — 카테고리·상태·assignedUserId·keyword 복합 필터")
    void findAllWithFilters() {
        em.persist(asset("ART-100", "IT기기", AssetStatus.ACTIVE,   userA));
        em.persist(asset("ART-101", "IT기기", AssetStatus.INACTIVE, null));
        em.persist(asset("ART-102", "가구",   AssetStatus.ACTIVE,   userA));
        em.persist(asset("ART-103", "가구",   AssetStatus.LOST,     null));
        em.flush();

        Page<Asset> itActive = assetRepository.findAllWithFilters(
                "IT기기", AssetStatus.ACTIVE, null, null, PageRequest.of(0, 10));
        assertThat(itActive.getContent()).extracting(Asset::getAssetNumber).contains("ART-100");

        Page<Asset> assignedToUserA = assetRepository.findAllWithFilters(
                null, null, userA.getUserId(), null, PageRequest.of(0, 10));
        assertThat(assignedToUserA.getContent()).extracting(Asset::getAssetNumber)
                .containsExactlyInAnyOrder("ART-100", "ART-102");

        Page<Asset> keywordMatch = assetRepository.findAllWithFilters(
                null, null, null, "ART-10", PageRequest.of(0, 10));
        // keyword는 assetName/assetNumber LIKE — name은 "ART-10..._name"
        assertThat(keywordMatch.getContent()).hasSizeGreaterThanOrEqualTo(4);

        Page<Asset> lostOnly = assetRepository.findAllWithFilters(
                null, AssetStatus.LOST, null, null, PageRequest.of(0, 10));
        assertThat(lostOnly.getContent()).extracting(Asset::getAssetNumber).contains("ART-103");
    }

    @Test
    @DisplayName("findAllWithFilters — 페이지 경계 동작")
    void findAllWithFilters_pagination() {
        for (int i = 0; i < 5; i++) {
            em.persist(asset("ART-PG-" + i, "IT기기", AssetStatus.ACTIVE, null));
        }
        em.flush();

        Page<Asset> page0 = assetRepository.findAllWithFilters(
                "IT기기", AssetStatus.ACTIVE, null, "ART-PG", PageRequest.of(0, 2));
        Page<Asset> page1 = assetRepository.findAllWithFilters(
                "IT기기", AssetStatus.ACTIVE, null, "ART-PG", PageRequest.of(1, 2));

        assertThat(page0.getContent()).hasSize(2);
        assertThat(page1.getContent()).hasSizeLessThanOrEqualTo(2);
        assertThat(page0.getTotalElements()).isGreaterThanOrEqualTo(5);
    }
}
