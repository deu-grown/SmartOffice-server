package com.grown.smartoffice.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grown.smartoffice.domain.auth.dto.TestLoginResponse;
import com.grown.smartoffice.domain.auth.service.TestAuthService;
import com.grown.smartoffice.support.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TestAuthController.class,
        properties = "spring.profiles.active=test")
@Import(TestSecurityConfig.class)
class TestAuthControllerTest {

    @Autowired MockMvc mockMvc;
    final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean TestAuthService testAuthService;

    private TestLoginResponse sampleResponse(boolean autoCreated) {
        return TestLoginResponse.builder()
                .accessToken("ACCESS").refreshToken("REFRESH").tokenType("Bearer")
                .expiresIn(1800).autoCreated(autoCreated)
                .user(TestLoginResponse.UserInfo.builder()
                        .id(1L).name("관리자").email("admin@grown.com")
                        .role("ADMIN").position("팀장").department("개발팀").build())
                .build();
    }

    @Test
    @DisplayName("POST /auth/test-login — body 없이 호출 가능 (permitAll)")
    void noBody_ok() throws Exception {
        given(testAuthService.testLogin(isNull(), isNull())).willReturn(sampleResponse(false));

        mockMvc.perform(post("/api/v1/auth/test-login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("ACCESS"))
                .andExpect(jsonPath("$.data.autoCreated").value(false))
                .andExpect(jsonPath("$.message").value("테스트 로그인 성공"));
    }

    @Test
    @DisplayName("POST /auth/test-login — autoCreated 시 메시지 변경")
    void autoCreated_messageDifferent() throws Exception {
        given(testAuthService.testLogin(eq("new@grown.com"), eq("USER")))
                .willReturn(sampleResponse(true));

        String body = objectMapper.writeValueAsString(Map.of(
                "email", "new@grown.com", "role", "USER"));

        mockMvc.perform(post("/api/v1/auth/test-login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.autoCreated").value(true))
                .andExpect(jsonPath("$.message").value("테스트 로그인 성공 (계정 자동 생성)"));
    }

    @Test
    @DisplayName("POST /auth/test-login — email/role 파라미터 서비스에 전달")
    void paramsPassThrough() throws Exception {
        given(testAuthService.testLogin(eq("admin@grown.com"), eq("ADMIN")))
                .willReturn(sampleResponse(false));

        String body = objectMapper.writeValueAsString(Map.of(
                "email", "admin@grown.com", "role", "ADMIN"));

        mockMvc.perform(post("/api/v1/auth/test-login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }
}
