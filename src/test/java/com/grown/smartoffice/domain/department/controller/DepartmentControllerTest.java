package com.grown.smartoffice.domain.department.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grown.smartoffice.domain.department.dto.DepartmentCreateResponse;
import com.grown.smartoffice.domain.department.dto.DepartmentListResponse;
import com.grown.smartoffice.domain.department.dto.DepartmentUpdateResponse;
import com.grown.smartoffice.domain.department.service.DepartmentService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DepartmentController.class)
@Import(TestSecurityConfig.class)
class DepartmentControllerTest {

    @Autowired MockMvc mockMvc;
    final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean DepartmentService departmentService;

    // ── 조회 ──────────────────────────────────────────────

    @Test
    @DisplayName("GET /departments — 인증 없음 → 401")
    void list_noAuth_401() throws Exception {
        mockMvc.perform(get("/api/v1/departments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /departments — EMPLOYEE → 403")
    @WithMockEmployeeUser
    void list_asEmployee_403() throws Exception {
        mockMvc.perform(get("/api/v1/departments"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /departments — ADMIN → 200, userCount 포함")
    @WithMockAdminUser
    void list_asAdmin_200() throws Exception {
        given(departmentService.getDepartments()).willReturn(List.of(
                DepartmentListResponse.builder()
                        .id(1L).name("개발팀").description("설명").userCount(3L)
                        .createdAt(LocalDateTime.now()).build()));

        mockMvc.perform(get("/api/v1/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("개발팀"))
                .andExpect(jsonPath("$.data[0].userCount").value(3));
    }

    // ── 등록 ──────────────────────────────────────────────

    @Test
    @DisplayName("POST /departments — ADMIN → 201")
    @WithMockAdminUser
    void create_asAdmin_201() throws Exception {
        given(departmentService.createDepartment(any())).willReturn(
                DepartmentCreateResponse.builder()
                        .id(10L).name("신규팀").description("설명")
                        .createdAt(LocalDateTime.now()).build());

        String body = objectMapper.writeValueAsString(Map.of(
                "name", "신규팀",
                "description", "설명"));

        mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(10));
    }

    @Test
    @DisplayName("POST /departments — EMPLOYEE → 403")
    @WithMockEmployeeUser
    void create_asEmployee_403() throws Exception {
        String validBody = objectMapper.writeValueAsString(Map.of("name", "임시팀"));

        mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /departments — @NotBlank 검증 실패 → 400")
    @WithMockAdminUser
    void create_blankName_400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("name", "", "description", ""));

        mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── 수정 ──────────────────────────────────────────────

    @Test
    @DisplayName("PUT /departments/{id} — ADMIN → 200")
    @WithMockAdminUser
    void update_asAdmin_200() throws Exception {
        given(departmentService.updateDepartment(eq(1L), any())).willReturn(
                DepartmentUpdateResponse.builder()
                        .id(1L).name("변경").description("desc").updatedAt(LocalDateTime.now()).build());

        String body = objectMapper.writeValueAsString(Map.of(
                "name", "변경",
                "description", "desc"));

        mockMvc.perform(put("/api/v1/departments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("변경"));
    }

    // ── 삭제 ──────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /departments/{id} — ADMIN → 200")
    @WithMockAdminUser
    void delete_asAdmin_200() throws Exception {
        mockMvc.perform(delete("/api/v1/departments/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("success"));

        verify(departmentService).deleteDepartment(7L);
    }

    @Test
    @DisplayName("DELETE /departments/{id} — 소속 직원 존재 시 409")
    @WithMockAdminUser
    void delete_hasUsers_409() throws Exception {
        doThrow(new CustomException(ErrorCode.DEPARTMENT_HAS_USERS))
                .when(departmentService).deleteDepartment(anyLong());

        mockMvc.perform(delete("/api/v1/departments/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("error"));
    }
}
