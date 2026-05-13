package com.grown.smartoffice.domain.power.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grown.smartoffice.domain.power.dto.PowerBillingAllResponse;
import com.grown.smartoffice.domain.power.dto.PowerBillingCalculateResponse;
import com.grown.smartoffice.domain.power.dto.PowerBillingZoneResponse;
import com.grown.smartoffice.domain.power.dto.PowerCurrentResponse;
import com.grown.smartoffice.domain.power.dto.PowerHourlyResponse;
import com.grown.smartoffice.domain.power.service.PowerService;
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

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PowerController.class)
@Import(TestSecurityConfig.class)
class PowerControllerTest {

    @Autowired MockMvc mockMvc;
    final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean PowerService powerService;

    @Test
    @DisplayName("GET /power/zones/{zoneId}/current — ADMIN 200")
    @WithMockAdminUser
    void current_admin_200() throws Exception {
        given(powerService.getCurrentPower(eq(2L))).willReturn(
                PowerCurrentResponse.builder().zoneId(2L).zoneName("회의실A")
                        .devices(Collections.emptyList()).build());

        mockMvc.perform(get("/api/v1/power/zones/{zoneId}/current", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.zoneId").value(2));
    }

    @Test
    @DisplayName("GET /power/zones/{zoneId}/current — EMPLOYEE 403")
    @WithMockEmployeeUser
    void current_employee_403() throws Exception {
        mockMvc.perform(get("/api/v1/power/zones/{zoneId}/current", 2))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /power/zones/{zoneId}/hourly — ADMIN 200, 날짜·deviceId 파라미터 전달")
    @WithMockAdminUser
    void hourly_admin_200() throws Exception {
        given(powerService.getHourlyHistory(eq(2L), any(), any(), eq(7L))).willReturn(
                PowerHourlyResponse.builder().zoneId(2L).logs(Collections.emptyList()).build());

        mockMvc.perform(get("/api/v1/power/zones/{zoneId}/hourly", 2)
                        .param("startDate", "2026-05-01")
                        .param("endDate", "2026-05-13")
                        .param("deviceId", "7"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /power/billing — ADMIN 200")
    @WithMockAdminUser
    void billing_admin_200() throws Exception {
        given(powerService.getAllZonesBilling(eq(2026), eq(4))).willReturn(
                PowerBillingAllResponse.builder().year(2026).month(4).zones(Collections.emptyList()).build());

        mockMvc.perform(get("/api/v1/power/billing").param("year", "2026").param("month", "4"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /power/billing/calculate — ADMIN 200")
    @WithMockAdminUser
    void calculate_admin_200() throws Exception {
        given(powerService.calculateBilling(any())).willReturn(
                PowerBillingCalculateResponse.builder().year(2026).month(4).totalCount(4).successCount(4).build());

        String body = objectMapper.writeValueAsString(Map.of("year", 2026, "month", 4, "unitPrice", 150, "baseFee", 6000));

        mockMvc.perform(post("/api/v1/power/billing/calculate")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(4));
    }

    @Test
    @DisplayName("POST /power/billing/calculate — EMPLOYEE 403")
    @WithMockEmployeeUser
    void calculate_employee_403() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("year", 2026, "month", 4, "unitPrice", 150, "baseFee", 6000));
        mockMvc.perform(post("/api/v1/power/billing/calculate")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }
}
