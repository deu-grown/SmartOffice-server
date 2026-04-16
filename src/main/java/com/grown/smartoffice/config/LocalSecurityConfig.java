package com.grown.smartoffice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 로컬 개발 환경 전용 Security 설정
 * H2 콘솔 접근 허용 — prod 프로파일에서는 로드되지 않음
 */
@Configuration
@Profile("local")
public class LocalSecurityConfig {

    @Bean
    @Order(1)   // 메인 SecurityFilterChain(Order 기본값)보다 먼저 적용
    public SecurityFilterChain h2ConsoleFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/h2-console/**")
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers
                    .frameOptions(frame -> frame.sameOrigin()))
            .authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll());

        return http.build();
    }
}
