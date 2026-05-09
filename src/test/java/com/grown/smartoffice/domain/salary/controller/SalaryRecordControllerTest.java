package com.grown.smartoffice.domain.salary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grown.smartoffice.domain.salary.dto.SalaryCalculateResponse;
import com.grown.smartoffice.domain.salary.dto.SalaryRecordResponse;
import com.grown.smartoffice.domain.salary.entity.SalaryStatus;
import com.grown.smartoffice.domain.salary.service.SalaryCalculationService;
import com.grown.smartoffice.domain.salary.service.SalaryRecordService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SalaryRecordController.class)
@Import(TestSecurityConfig.class)
class SalaryRecordControllerTest {

    @Autowired MockMvc mockMvc;
    final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean SalaryCalculationService calculationService;
    @MockitoBean SalaryRecordService recordService;

    @Test
    @DisplayName("POST /salary/records/calculate — 인증 없음 → 401")
    void calculate_noAuth_401() throws Exception {
        mockMvc.perform(post("/api/v1/salary/records/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /salary/records/calculate — EMPLOYEE → 403")
    @WithMockEmployeeUser
    void calculate_asEmployee_403() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("year", 2026, "month", 4));

        mockMvc.perform(post("/api/v1/salary/records/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /salary/records/calculate — ADMIN → 200")
    @WithMockAdminUser
    void calculate_asAdmin_200() throws Exception {
        given(calculationService.calculate(any())).willReturn(
                SalaryCalculateResponse.builder()
                        .totalCount(1).successCount(1).skipCount(0).records(List.of()).build());

        String body = objectMapper.writeValueAsString(Map.of("year", 2026, "month", 4));

        mockMvc.perform(post("/api/v1/salary/records/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(1));
    }

    @Test
    @DisplayName("PUT /salary/records/{id}/confirm — ADMIN → 200")
    @WithMockAdminUser
    void confirm_asAdmin_200() throws Exception {
        given(recordService.confirm(1L)).willReturn(
                SalaryRecordResponse.builder()
                        .id(1L).userId(1L).year(2026).month(4)
                        .baseSalary(2500000).overtimePay(0).totalPay(2500000)
                        .status(SalaryStatus.CONFIRMED).build());

        mockMvc.perform(put("/api/v1/salary/records/1/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("PUT /salary/records/{id}/confirm — 이미 확정 → 409")
    @WithMockAdminUser
    void confirm_alreadyConfirmed_409() throws Exception {
        given(recordService.confirm(1L))
                .willThrow(new CustomException(ErrorCode.SALARY_RECORD_ALREADY_CONFIRMED));

        mockMvc.perform(put("/api/v1/salary/records/1/confirm"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /salary/records/me — EMPLOYEE → 200, CONFIRMED만")
    @WithMockEmployeeUser(email = "me@grown.com")
    void getMy_employee_200() throws Exception {
        given(recordService.getMy(eq("me@grown.com"), eq(2026), eq(4))).willReturn(
                SalaryRecordResponse.builder()
                        .id(1L).userId(1L).year(2026).month(4)
                        .baseSalary(2500000).overtimePay(50000).totalPay(2550000)
                        .status(SalaryStatus.CONFIRMED).build());

        mockMvc.perform(get("/api/v1/salary/records/me")
                        .param("year", "2026").param("month", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalPay").value(2550000));
    }
}
