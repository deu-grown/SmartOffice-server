package com.grown.smartoffice.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 테스트 전역 공유 컨테이너 (Singleton Pattern).
 * JVM 당 MySQL/Redis 컨테이너를 한 번만 기동하여 전체 테스트 러닝 타임을 단축한다.
 */
public abstract class AbstractContainerTest {

    static final MySQLContainer<?> MYSQL;
    static final GenericContainer<?> REDIS;

    static {
        MYSQL = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                .withDatabaseName("smartoffice_test")
                .withUsername("test")
                .withPassword("test");
        REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                .withExposedPorts(6379);
        MYSQL.start();
        REDIS.start();
    }

    @DynamicPropertySource
    static void registerContainerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }
}
