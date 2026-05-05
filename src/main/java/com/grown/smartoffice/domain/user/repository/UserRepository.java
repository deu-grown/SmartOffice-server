package com.grown.smartoffice.domain.user.repository;

import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserRole;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u JOIN FETCH u.department WHERE u.employeeEmail = :email")
    Optional<User> findByEmployeeEmailWithDept(@Param("email") String email);

    Optional<User> findByEmployeeEmail(String employeeEmail);

    @Query("SELECT u FROM User u JOIN FETCH u.department WHERE u.userId = :id")
    Optional<User> findByIdWithDept(@Param("id") Long id);

    boolean existsByEmployeeEmail(String employeeEmail);

    boolean existsByEmployeeNumber(String employeeNumber);

    Optional<User> findFirstByRoleAndStatus(UserRole role, UserStatus status);

    /** 전체 상태별 직원 수 */
    long countByStatus(UserStatus status);

    /** 특정 부서의 상태별 직원 수 (단건 조회용) */
    @Query("SELECT COUNT(u) FROM User u WHERE u.department.deptId = :deptId AND u.status = :status")
    long countByDeptIdAndStatus(@Param("deptId") Long deptId, @Param("status") UserStatus status);

    /** 부서별 ACTIVE 직원 수 — N+1 방지용 단일 집계 쿼리 */
    @Query("SELECT u.department.deptId, COUNT(u) FROM User u WHERE u.status = :status GROUP BY u.department.deptId")
    List<Object[]> countGroupedByDeptId(@Param("status") UserStatus status);

    /** countGroupedByDeptId 결과를 Map<deptId, count> 으로 변환하는 default 메서드 */
    default Map<Long, Long> getActiveCountByDeptId() {
        return countGroupedByDeptId(UserStatus.ACTIVE).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    /**
     * 직원 목록 페이지 조회 — 다중 필터 + 페이지네이션
     *
     * JOIN FETCH 미사용: Page + JOIN FETCH 조합 시 Hibernate가 메모리 페이지네이션을 적용하는 문제(HHH90003004) 방지
     * 부서명 lazy loading은 @Transactional 서비스 메서드 안에서 안전하게 실행됨
     */
    @Query(value = "SELECT u FROM User u " +
                   "WHERE (:deptId IS NULL OR u.department.deptId = :deptId) " +
                   "AND (:status IS NULL OR u.status = :status) " +
                   "AND (:keyword IS NULL OR u.employeeName LIKE %:keyword% OR u.employeeNumber LIKE %:keyword%)",
           countQuery = "SELECT COUNT(u) FROM User u " +
                        "WHERE (:deptId IS NULL OR u.department.deptId = :deptId) " +
                        "AND (:status IS NULL OR u.status = :status) " +
                        "AND (:keyword IS NULL OR u.employeeName LIKE %:keyword% OR u.employeeNumber LIKE %:keyword%)")
    Page<User> findAllWithFilters(
            @Param("deptId") Long deptId,
            @Param("status") UserStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
