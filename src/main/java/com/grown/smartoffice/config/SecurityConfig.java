package com.grown.smartoffice.config;

import com.grown.smartoffice.global.security.JwtAccessDeniedHandler;
import com.grown.smartoffice.global.security.JwtAuthenticationEntryPoint;
import com.grown.smartoffice.jwt.JwtAuthenticationFilter;
import com.grown.smartoffice.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // @PreAuthorize("hasRole('ADMIN')") 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)  // 401
                    .accessDeniedHandler(jwtAccessDeniedHandler))            // 403
            .authorizeHttpRequests(auth -> auth
                    // 인증 불필요
                    .requestMatchers("/api/v1/auth/login").permitAll()
                    .requestMatchers("/api/v1/auth/refresh").permitAll()
                    .requestMatchers("/h2-console/**").permitAll()           // 로컬 개발용
                    .requestMatchers("/actuator/health").permitAll()
                    // 그 외 모든 요청 인증 필요
                    .anyRequest().authenticated()
            )
            // H2 콘솔 iframe 허용 (로컬 개발용)
            .headers(headers -> headers
                    .frameOptions(frame -> frame.sameOrigin()))
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
