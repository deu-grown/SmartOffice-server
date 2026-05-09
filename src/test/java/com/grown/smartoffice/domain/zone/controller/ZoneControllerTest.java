package com.grown.smartoffice.domain.zone.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grown.smartoffice.domain.zone.dto.*;
import com.grown.smartoffice.domain.zone.entity.ZoneType;
import com.grown.smartoffice.domain.zone.service.ZoneService;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import com.grown.smartoffice.support.TestSecurityConfig;
import com.grown.smartoffice.support.WithMockAdminUser;
import com.grown.smartoffice.support.WithMockEmployeeUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ZoneController.class)
@Import(TestSecurityConfig.class)
class ZoneControllerTest {

    @Autowired MockMvc mockMvc;
    final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean ZoneService zoneService;

    // ── 인증·권한 ──────────────────────────────────────────

    @Test
    @DisplayName("GET /zones — 인증 없음 → 401")
    void list_noAuth_401() throws Exception {
        mockMvc.perform(get("/api/v1/zones"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /zones — EMPLOYEE 권한 → 403")
    @WithMockEmployeeUser
    void list_asEmployee_403() throws Exception {
        mockMvc.perform(get("/api/v1/zones"))
                .andExpect(status().isForbidden());
    }

    // ── 목록 조회 ──────────────────────────────────────────

    @Test
    @DisplayName("GET /zones — ADMIN → 200, 목록 반환")
    @WithMockAdminUser
    void list_asAdmin_200() throws Exception {
        given(zoneService.getZones(null, null)).willReturn(List.of(
                ZoneListItemResponse.builder().id(1L).name("1층").zoneType(ZoneType.FLOOR).build()));

        mockMvc.perform(get("/api/v1/zones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("1층"));
    }

    // ── 트리 조회 ──────────────────────────────────────────

    @Test
    @DisplayName("GET /zones/tree — ADMIN → 200, children 포함")
    @WithMockAdminUser
    void tree_asAdmin_200() throws Exception {
        ZoneTreeResponse child = ZoneTreeResponse.builder()
                .id(2L).name("회의실A").zoneType(ZoneType.AREA).children(List.of()).build();
        ZoneTreeResponse root = ZoneTreeResponse.builder()
                .id(1L).name("1층").zoneType(ZoneType.FLOOR).children(List.of(child)).build();
        given(zoneService.getZoneTree()).willReturn(List.of(root));

        mockMvc.perform(get("/api/v1/zones/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].children[0].name").value("회의실A"));
    }

    // ── 등록 ──────────────────────────────────────────────

    @Test
    @DisplayName("POST /zones — ADMIN → 201")
    @WithMockAdminUser
    void create_asAdmin_201() throws Exception {
        given(zoneService.createZone(any())).willReturn(
                ZoneCreateResponse.builder().id(1L).name("1층").zoneType(ZoneType.FLOOR).build());

        String body = objectMapper.writeValueAsString(Map.of("name", "1층", "zoneType", "FLOOR"));

        mockMvc.perform(post("/api/v1/zones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("POST /zones — @NotBlank name 누락 → 400")
    @WithMockAdminUser
    void create_missingName_400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("zoneType", "FLOOR"));

        mockMvc.perform(post("/api/v1/zones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── 삭제 ──────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /zones/{id} — ADMIN → 200")
    @WithMockAdminUser
    void delete_asAdmin_200() throws Exception {
        mockMvc.perform(delete("/api/v1/zones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("success"));

        verify(zoneService).deleteZone(1L);
    }

    @Test
    @DisplayName("DELETE /zones/{id} — 하위 구역 존재 시 409")
    @WithMockAdminUser
    void delete_hasChildren_409() throws Exception {
        doThrow(new CustomException(ErrorCode.ZONE_HAS_CHILDREN))
                .when(zoneService).deleteZone(1L);

        mockMvc.perform(delete("/api/v1/zones/1"))
                .andExpect(status().isConflict());
    }
}
