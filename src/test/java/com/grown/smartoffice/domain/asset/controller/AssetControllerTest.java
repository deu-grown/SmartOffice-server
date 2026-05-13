package com.grown.smartoffice.domain.asset.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grown.smartoffice.domain.asset.dto.AssetResponse;
import com.grown.smartoffice.domain.asset.entity.AssetStatus;
import com.grown.smartoffice.domain.asset.service.AssetService;
import com.grown.smartoffice.global.common.PageResponse;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import com.grown.smartoffice.support.TestSecurityConfig;
import com.grown.smartoffice.support.WithMockAdminUser;
import com.grown.smartoffice.support.WithMockEmployeeUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssetController.class)
@Import(TestSecurityConfig.class)
class AssetControllerTest {

    @Autowired MockMvc mockMvc;
    final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean AssetService assetService;

    private AssetResponse sample() {
        return AssetResponse.builder()
                .assetId(1L).assetNumber("AST-2026-001").assetName("MacBook Pro 16\"")
                .category("IT기기").assignedUserId(1L).assignedUserName("관리자")
                .description("M3 Max").assetStatus(AssetStatus.ACTIVE)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /assets — ADMIN 201")
    @WithMockAdminUser
    void create_admin_201() throws Exception {
        given(assetService.createAsset(any())).willReturn(sample());

        String body = objectMapper.writeValueAsString(Map.of(
                "assetNumber", "AST-2026-001",
                "assetName", "MacBook Pro 16\"",
                "category", "IT기기"));

        mockMvc.perform(post("/api/v1/assets")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.assetId").value(1));
    }

    @Test
    @DisplayName("POST /assets — EMPLOYEE 403")
    @WithMockEmployeeUser
    void create_employee_403() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "assetNumber", "AST-X", "assetName", "x", "category", "x"));

        mockMvc.perform(post("/api/v1/assets")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /assets — ADMIN 200, 필터 파라미터 전달")
    @WithMockAdminUser
    void list_admin_200() throws Exception {
        given(assetService.getAssets(eq("IT기기"), eq("ACTIVE"), eq(1L), eq("Mac"), eq(0), eq(20)))
                .willReturn(new PageResponse<>(List.of(sample()), 0, 20, 1, 1, true));

        mockMvc.perform(get("/api/v1/assets")
                        .param("category", "IT기기").param("status", "ACTIVE")
                        .param("assignedUserId", "1").param("keyword", "Mac"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].assetNumber").value("AST-2026-001"));
    }

    @Test
    @DisplayName("GET /assets/{id} — 존재하지 않으면 404")
    @WithMockAdminUser
    void detail_notFound_404() throws Exception {
        given(assetService.getAsset(999L)).willThrow(new CustomException(ErrorCode.ASSET_NOT_FOUND));

        mockMvc.perform(get("/api/v1/assets/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /assets/{id} — EMPLOYEE 403")
    @WithMockEmployeeUser
    void delete_employee_403() throws Exception {
        mockMvc.perform(delete("/api/v1/assets/{id}", 1))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /assets/{id} — ADMIN 200")
    @WithMockAdminUser
    void update_admin_200() throws Exception {
        given(assetService.updateAsset(eq(1L), any())).willReturn(sample());

        mockMvc.perform(put("/api/v1/assets/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assetId").value(1));
    }
}
