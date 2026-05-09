package com.grown.smartoffice.domain.nfccard.repository;

import com.grown.smartoffice.domain.nfccard.entity.NfcCard;
import com.grown.smartoffice.domain.nfccard.entity.NfcCardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NfcCardRepository extends JpaRepository<NfcCard, Long> {

    @Query("SELECT c FROM NfcCard c JOIN FETCH c.user WHERE c.cardUid = :uid")
    Optional<NfcCard> findByCardUidWithUser(@Param("uid") String uid);

    boolean existsByUser_UserIdAndCardStatus(Long userId, NfcCardStatus status);

    boolean existsByCardUid(String cardUid);

    @Query("SELECT c FROM NfcCard c JOIN FETCH c.user " +
            "WHERE (:userId IS NULL OR c.user.userId = :userId) " +
            "AND (:cardType IS NULL OR c.cardType = :cardType) " +
            "AND (:status IS NULL OR c.cardStatus = :status)")
    List<NfcCard> findAllWithUser(@Param("userId") Long userId, 
                                  @Param("cardType") String cardType, 
                                  @Param("status") NfcCardStatus status);

    @Query("SELECT c FROM NfcCard c JOIN FETCH c.user " +
            "LEFT JOIN FETCH c.user.department " +
            "WHERE c.cardId = :id")
    Optional<NfcCard> findByIdWithUserAndDept(@Param("id") Long id);
}
