package com.grown.smartoffice.support;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

/**
 * Repository 슬라이스 테스트 베이스.
 * Testcontainers MySQL + Flyway 로 운영과 동일 스키마를 재현.
 * 트랜잭션 롤백 기본 동작으로 테스트 간 격리.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public abstract class RepositoryTestSupport extends AbstractContainerTest {
}
