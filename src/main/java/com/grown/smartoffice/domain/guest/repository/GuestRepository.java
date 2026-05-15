package com.grown.smartoffice.domain.guest.repository;

import com.grown.smartoffice.domain.guest.entity.Guest;
import com.grown.smartoffice.domain.guest.entity.GuestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GuestRepository extends JpaRepository<Guest, Long> {

    @Query("SELECT g FROM Guest g LEFT JOIN FETCH g.hostUser WHERE g.guestId = :id")
    Optional<Guest> findByIdWithHost(@Param("id") Long id);

    @Query(value = """
            SELECT g FROM Guest g
            WHERE (:status IS NULL OR g.guestStatus = :status)
              AND (:hostUserId IS NULL OR g.hostUser.userId = :hostUserId)
              AND (:keyword IS NULL
                   OR g.guestName LIKE %:keyword%
                   OR g.company LIKE %:keyword%)
            """,
           countQuery = """
            SELECT COUNT(g) FROM Guest g
            WHERE (:status IS NULL OR g.guestStatus = :status)
              AND (:hostUserId IS NULL OR g.hostUser.userId = :hostUserId)
              AND (:keyword IS NULL
                   OR g.guestName LIKE %:keyword%
                   OR g.company LIKE %:keyword%)
            """)
    Page<Guest> findAllWithFilters(
            @Param("status") GuestStatus status,
            @Param("hostUserId") Long hostUserId,
            @Param("keyword") String keyword,
            Pageable pageable);
}
