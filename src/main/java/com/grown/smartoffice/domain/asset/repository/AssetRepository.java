package com.grown.smartoffice.domain.asset.repository;

import com.grown.smartoffice.domain.asset.entity.Asset;
import com.grown.smartoffice.domain.asset.entity.AssetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    boolean existsByAssetNumber(String assetNumber);

    boolean existsByAssetNumberAndAssetIdNot(String assetNumber, Long assetId);

    @Query("SELECT a FROM Asset a LEFT JOIN FETCH a.assignedUser WHERE a.assetId = :id")
    Optional<Asset> findByIdWithUser(@Param("id") Long id);

    /**
     * 목록 조회 — Page + JOIN FETCH 혼용 회피(HHH90003004), assignedUser는 Lazy로 처리
     */
    @Query(value = """
            SELECT a FROM Asset a
            WHERE (:category IS NULL OR a.category = :category)
              AND (:status IS NULL OR a.assetStatus = :status)
              AND (:assignedUserId IS NULL OR a.assignedUser.userId = :assignedUserId)
              AND (:keyword IS NULL
                   OR a.assetName LIKE %:keyword%
                   OR a.assetNumber LIKE %:keyword%)
            """,
           countQuery = """
            SELECT COUNT(a) FROM Asset a
            WHERE (:category IS NULL OR a.category = :category)
              AND (:status IS NULL OR a.assetStatus = :status)
              AND (:assignedUserId IS NULL OR a.assignedUser.userId = :assignedUserId)
              AND (:keyword IS NULL
                   OR a.assetName LIKE %:keyword%
                   OR a.assetNumber LIKE %:keyword%)
            """)
    Page<Asset> findAllWithFilters(
            @Param("category") String category,
            @Param("status") AssetStatus status,
            @Param("assignedUserId") Long assignedUserId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
