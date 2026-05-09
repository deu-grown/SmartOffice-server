package com.grown.smartoffice.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grown.smartoffice.domain.auth.dto.LoginResponse;
import com.grown.smartoffice.domain.auth.dto.MeResponse;
import com.grown.smartoffice.domain.auth.dto.TokenRefreshResponse;
import com.grown.smartoffice.domain.auth.service.AuthService;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import com.grown.smartoffice.support.TestSecurityConfig;
import com.grown.smartoffice.support.WithMockAdminUser;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean AuthService authService;

    @Test
    @DisplayName("POST /api/v1/auth/login — 정상 요청 → 200 + success")
    void login_ok() throws Exception {
        LoginResponse stub = LoginResponse.builder()
                .accessToken("at").refreshToken("rt").tokenType("Bearer").expiresIn(1800)
                .user(LoginResponse.UserInfo.builder()
                        .id(1L).name("관리자").email("admin@grown.com")
                        .role("ADMIN").position("팀장").department("개발팀").build())
                .build();
        given(authService.login(any())).willReturn(stub);

        String body = objectMapper.writeValueAsString(Map.of(
                "email", "admin@grown.com",
                "password", "EMP001"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("success"))
                .andExpect(jsonPath("$.data.accessToken").value("at"))
                .andExpect(jsonPath("$.data.user.role").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login — @Valid 실패 시 400")
    void login_validationFail_400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", "not-an-email",
                "password", ""));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("error"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login — 비밀번호 불일치 시 CustomException → 401")
    void login_invalidCreds_401() throws Exception {
        given(authService.login(any()))
                .willThrow(new CustomException(ErrorCode.INVALID_CREDENTIALS));

        String body = objectMapper.writeValueAsString(Map.of(
                "email", "admin@grown.com",
                "password", "wrong"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("error"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh — 정상 요청 → 200")
    void refresh_ok() throws Exception {
        given(authService.refresh(any()))
                .willReturn(TokenRefreshResponse.builder()
                        .accessToken("new-at").tokenType("Bearer").expiresIn(1800).build());

        String body = objectMapper.writeValueAsString(Map.of("refreshToken", "valid-rt"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-at"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout — 인증 없음 → 401")
    void logout_noAuth_401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout — 인증 있음 → 200")
    @WithMockAdminUser
    void logout_ok() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("success"));

        verify(authService).logout("admin@grown.com");
    }

    @Test
    @DisplayName("GET /api/v1/auth/me — 인증 없음 → 401")
    void me_noAuth_401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/auth/me — 인증 있음 → 200")
    @WithMockAdminUser
    void me_ok() throws Exception {
        given(authService.getMe("admin@grown.com"))
                .willReturn(MeResponse.builder()
                        .id(1L).email("admin@grown.com").role("ADMIN").status("ACTIVE")
                        .name("관리자").position("팀장").department("개발팀").build());

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("admin@grown.com"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me — INACTIVE 계정 → 403")
    @WithMockAdminUser(email = "gone@grown.com")
    void me_inactive_403() throws Exception {
        doThrow(new CustomException(ErrorCode.ACCOUNT_INACTIVE))
                .when(authService).getMe(eq("gone@grown.com"));

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("error"));
    }
}
