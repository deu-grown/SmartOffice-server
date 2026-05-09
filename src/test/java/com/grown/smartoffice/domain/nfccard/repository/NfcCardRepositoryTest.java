package com.grown.smartoffice.domain.nfccard.repository;

import com.grown.smartoffice.domain.nfccard.entity.NfcCard;
import com.grown.smartoffice.domain.nfccard.entity.NfcCardStatus;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserRole;
import com.grown.smartoffice.support.RepositoryTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NfcCardRepositoryTest extends RepositoryTestSupport {

    @Autowired NfcCardRepository nfcCardRepository;
    @Autowired TestEntityManager em;

    private User user1;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .employeeNumber("EMPTEST001")
                .employeeName("테스터")
                .employeeEmail("unique_test@example.com")
                .password("password")
                .position("사원")
                .role(UserRole.USER)
                .hiredAt(java.time.LocalDate.now())
                .build();
        // Since we are using H2 and Flyway, we might need to link to existing dept.
        // Or just create one.
        com.grown.smartoffice.domain.department.entity.Department dept = 
            com.grown.smartoffice.domain.department.entity.Department.builder()
                .deptName("테스트부서")
                .build();
        em.persist(dept);
        ReflectionTestUtils.setField(user1, "department", dept);
        em.persist(user1);
        em.flush();
    }

    @Test
    @DisplayName("NFC 카드 저장 및 UID로 조회")
    void saveAndFindByUid() {
        NfcCard card = NfcCard.builder()
                .user(user1)
                .cardUid("UID-123")
                .cardType("EMPLOYEE")
                .build();
        nfcCardRepository.save(card);
        em.flush();
        em.clear();

        NfcCard found = nfcCardRepository.findByCardUidWithUser("UID-123").orElseThrow();
        assertThat(found.getUser().getEmployeeName()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("사용자의 활성 카드 존재 여부 확인")
    void existsActiveCard() {
        NfcCard card = NfcCard.builder()
                .user(user1)
                .cardUid("UID-123")
                .cardStatus(NfcCardStatus.ACTIVE)
                .cardType("EMPLOYEE")
                .build();
        nfcCardRepository.save(card);

        boolean exists = nfcCardRepository.existsByUser_UserIdAndCardStatus(user1.getUserId(), NfcCardStatus.ACTIVE);
        assertThat(exists).isTrue();
    }
}
