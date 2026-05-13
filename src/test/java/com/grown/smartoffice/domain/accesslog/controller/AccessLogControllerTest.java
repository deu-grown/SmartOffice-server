package com.grown.smartoffice.domain.accesslog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grown.smartoffice.domain.accesslog.dto.AllAccessLogListResponse;
import com.grown.smartoffice.domain.accesslog.dto.TagEventResponse;
import com.grown.smartoffice.domain.accesslog.service.AccessLogService;
import com.grown.smartoffice.global.common.PageResponse;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccessLogController.class)
@Import(TestSecurityConfig.class)
class AccessLogControllerTest {

    @Autowired MockMvc mockMvc;
    final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean AccessLogService accessLogService;

    @Test
    @DisplayName("POST /access-logs/tag — 인증 없이도 200 (permitAll)")
    void tag_noAuth_200() throws Exception {
        given(accessLogService.processTag(any())).willReturn(TagEventResponse.builder()
                .authResult("APPROVED")
                .userId(1L)
                .taggedAt(LocalDateTime.now())
                .build());

        String body = objectMapper.writeValueAsString(Map.of(
                "deviceId", 1,
                "uid", "ADMIN-CARD-UID-001",
                "direction", "IN"));

        mockMvc.perform(post("/api/v1/access-logs/tag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authResult").value("APPROVED"));
    }

    @Test
    @DisplayName("POST /access-logs/tag — DENIED 응답")
    void tag_denied_response() throws Exception {
        given(accessLogService.processTag(any())).willReturn(TagEventResponse.builder()
                .authResult("DENIED")
                .denyReason("퇴사 처리된 계정")
                .taggedAt(LocalDateTime.now())
                .build());

        String body = objectMapper.writeValueAsString(Map.of(
                "deviceId", 1,
                "uid", "INACTIVE-USER-CARD",
                "direction", "IN"));

        mockMvc.perform(post("/api/v1/access-logs/tag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authResult").value("DENIED"))
                .andExpect(jsonPath("$.data.denyReason").value("퇴사 처리된 계정"));
    }

    @Test
    @DisplayName("POST /access-logs/tag — @NotNull deviceId 누락 → 400")
    void tag_missingDeviceId_400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "uid", "SOME-UID",
                "direction", "IN"));

        mockMvc.perform(post("/api/v1/access-logs/tag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /access-logs/tag — 장치 없으면 404")
    void tag_deviceNotFound_404() throws Exception {
        given(accessLogService.processTag(any()))
                .willThrow(new CustomException(ErrorCode.DEVICE_NOT_FOUND));

        String body = objectMapper.writeValueAsString(Map.of(
                "deviceId", 999,
                "uid", "SOME-UID",
                "direction", "IN"));

        mockMvc.perform(post("/api/v1/access-logs/tag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /access-logs — ADMIN 권한 필요")
    @WithMockAdminUser
    void getAllLogs_admin_200() throws Exception {
        given(accessLogService.getAllAccessLogs(any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .willReturn(AllAccessLogListResponse.from(new PageResponse<>(Collections.emptyList(), 0, 20, 0, 0, true)));

        mockMvc.perform(get("/api/v1/access-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("success"));
    }

    @Test
    @DisplayName("GET /access-logs — 일반 USER는 403")
    @WithMockUser(roles = "USER")
    void getAllLogs_user_403() throws Exception {
        mockMvc.perform(get("/api/v1/access-logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /access-logs/me — USER 권한으로 200")
    @WithMockUser(roles = "USER")
    void getMyLogs_user_200() throws Exception {
        given(accessLogService.getMyAccessLogs(anyString(), any(), any(), any(), anyInt(), anyInt()))
                .willReturn(AllAccessLogListResponse.from(new PageResponse<>(Collections.emptyList(), 0, 20, 0, 0, true)));

        mockMvc.perform(get("/api/v1/access-logs/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("success"));
    }

    @Test
    @DisplayName("GET /access-logs/me — 인증 없음 → 401")
    void getMyLogs_noAuth_401() throws Exception {
        mockMvc.perform(get("/api/v1/access-logs/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /access-logs — 필터·페이지 파라미터가 서비스에 전달")
    @WithMockAdminUser
    void getAllLogs_passesFilters() throws Exception {
        given(accessLogService.getAllAccessLogs(eq(2L), eq(1L), eq("APPROVED"), eq("IN"),
                                                 eq("2026-05-01"), eq("2026-05-13"), eq(1), eq(50)))
                .willReturn(AllAccessLogListResponse.from(new PageResponse<>(Collections.emptyList(), 1, 50, 0, 0, true)));

        mockMvc.perform(get("/api/v1/access-logs")
                        .param("zoneId", "2")
                        .param("userId", "1")
                        .param("authResult", "APPROVED")
                        .param("direction", "IN")
                        .param("startDate", "2026-05-01")
                        .param("endDate", "2026-05-13")
                        .param("page", "1")
                        .param("size", "50"))
                .andExpect(status().isOk());
    }
}
