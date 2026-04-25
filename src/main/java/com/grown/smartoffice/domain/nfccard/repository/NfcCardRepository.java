package com.grown.smartoffice.domain.nfccard.repository;

import com.grown.smartoffice.domain.nfccard.entity.NfcCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NfcCardRepository extends JpaRepository<NfcCard, Long> {

    @Query("SELECT c FROM NfcCard c JOIN FETCH c.user WHERE c.cardUid = :uid")
    Optional<NfcCard> findByCardUidWithUser(@Param("uid") String uid);
}
