package com.grown.smartoffice.util;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 해시 생성 유틸리티 테스트
 * Spring Context 없이 단독 실행 가능 — DB/Redis 불필요
 * V2 seed SQL에 넣을 해시값 출력용 (1회 실행 후 삭제해도 무방)
 */
class BcryptHashGeneratorTest {

    @Test
    @Disabled("V2 seed SQL 해시 생성용 유틸 — CI에서 실행 불필요")
    void printAdminPasswordHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("EMP001");

        System.out.println("\n==== ADMIN PASSWORD HASH ====");
        System.out.println(hash);
        System.out.println("=============================\n");
    }
}
