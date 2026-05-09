package com.grown.smartoffice.support;

import com.grown.smartoffice.config.JpaAuditingConfig;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Repository 슬라이스 테스트 베이스.
 * Testcontainers MySQL + Flyway 로 운영과 동일 스키마를 재현.
 * 트랜잭션 롤백 기본 동작으로 테스트 간 격리.
 * JpaAuditingConfig: @DataJpaTest 슬라이스에서 @CreatedDate/@LastModifiedDate가 동작하려면 명시적 import 필요.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
public abstract class RepositoryTestSupport extends AbstractContainerTest {
}
