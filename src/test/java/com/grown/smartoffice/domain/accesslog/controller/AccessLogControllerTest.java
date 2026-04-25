package com.grown.smartoffice.domain.accesslog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grown.smartoffice.domain.accesslog.dto.TagEventResponse;
import com.grown.smartoffice.domain.accesslog.service.AccessLogService;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import com.grown.smartoffice.support.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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
}
