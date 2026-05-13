package com.grown.smartoffice.domain.salary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grown.smartoffice.domain.salary.dto.SalarySettingResponse;
import com.grown.smartoffice.domain.salary.service.SalarySettingService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SalarySettingController.class)
@Import(TestSecurityConfig.class)
class SalarySettingControllerTest {

    @Autowired MockMvc mockMvc;
    final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean SalarySettingService salarySettingService;

    private SalarySettingResponse sample() {
        return SalarySettingResponse.builder()
                .id(1L).position("개발자").baseSalary(4000000)
                .overtimeRate(new BigDecimal("1.5")).nightRate(new BigDecimal("2.0"))
                .effectiveFrom(LocalDate.of(2026, 1, 1)).effectiveTo(null)
                .build();
    }

    @Test
    @DisplayName("POST /salary/settings — ADMIN 201")
    @WithMockAdminUser
    void create_admin_201() throws Exception {
        given(salarySettingService.create(any())).willReturn(sample());

        String body = objectMapper.writeValueAsString(Map.of(
                "position", "개발자", "baseSalary", 4000000,
                "overtimeRate", 1.5, "nightRate", 2.0,
                "effectiveFrom", "2026-01-01"));

        mockMvc.perform(post("/api/v1/salary/settings")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("POST /salary/settings — EMPLOYEE 403")
    @WithMockEmployeeUser
    void create_employee_403() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "position", "x", "baseSalary", 1000000,
                "overtimeRate", 1.0, "nightRate", 1.0,
                "effectiveFrom", "2026-01-01"));

        mockMvc.perform(post("/api/v1/salary/settings")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /salary/settings — ADMIN 200, position 필터 전달")
    @WithMockAdminUser
    void list_admin_200() throws Exception {
        given(salarySettingService.list(eq("개발자"))).willReturn(Collections.singletonList(sample()));

        mockMvc.perform(get("/api/v1/salary/settings").param("position", "개발자"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].position").value("개발자"));
    }

    @Test
    @DisplayName("GET /salary/settings — EMPLOYEE 403")
    @WithMockEmployeeUser
    void list_employee_403() throws Exception {
        mockMvc.perform(get("/api/v1/salary/settings"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /salary/settings/{id} — 이미 사용 중인 기준 409")
    @WithMockAdminUser
    void update_inUse_409() throws Exception {
        given(salarySettingService.update(eq(1L), any()))
                .willThrow(new CustomException(ErrorCode.SALARY_SETTING_USED));

        mockMvc.perform(put("/api/v1/salary/settings/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"baseSalary\":5000000}"))
                .andExpect(status().isConflict());
    }
}
