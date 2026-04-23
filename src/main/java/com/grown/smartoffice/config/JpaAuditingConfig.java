package com.grown.smartoffice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 감사(@CreatedDate/@LastModifiedDate) 활성화 전용 설정.
 * Slice 테스트(@WebMvcTest 등)에서는 JPA 컨텍스트가 없어 감사 빈 생성이 실패하므로
 * 메인 Application 클래스에서 분리해둔다.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
