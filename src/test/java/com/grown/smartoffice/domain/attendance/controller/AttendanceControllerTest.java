package com.grown.smartoffice.domain.attendance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grown.smartoffice.domain.attendance.dto.AttendanceDailyResponse;
import com.grown.smartoffice.domain.attendance.dto.AttendanceMonthlyResponse;
import com.grown.smartoffice.domain.attendance.entity.AttendanceStatus;
import com.grown.smartoffice.domain.attendance.service.AttendanceBatchService;
import com.grown.smartoffice.domain.attendance.service.AttendanceCommandService;
import com.grown.smartoffice.domain.attendance.service.AttendanceQueryService;
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

import java.time.LocalDate;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AttendanceController.class)
@Import(TestSecurityConfig.class)
class AttendanceControllerTest {

    @Autowired MockMvc mockMvc;
    final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean AttendanceQueryService queryService;
    @MockitoBean AttendanceCommandService commandService;
    @MockitoBean AttendanceBatchService batchService;

    @Test
    @DisplayName("GET /attendance/me/daily — 인증 없음 → 401")
    void myDaily_noAuth_401() throws Exception {
        mockMvc.perform(get("/api/v1/attendance/me/daily").param("date", "2026-04-25"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /attendance/me/daily — EMPLOYEE → 200")
    @WithMockEmployeeUser(email = "me@grown.com")
    void myDaily_employee_200() throws Exception {
        given(queryService.getMyDaily(eq("me@grown.com"), any())).willReturn(
                AttendanceDailyResponse.builder()
                        .attendanceId(1L).userId(1L).workDate(LocalDate.of(2026, 4, 25))
                        .attendanceStatus(AttendanceStatus.NORMAL).build());

        mockMvc.perform(get("/api/v1/attendance/me/daily").param("date", "2026-04-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attendanceStatus").value("NORMAL"));
    }

    @Test
    @DisplayName("GET /attendance/daily — EMPLOYEE → 403")
    @WithMockEmployeeUser
    void allDaily_asEmployee_403() throws Exception {
        mockMvc.perform(get("/api/v1/attendance/daily").param("date", "2026-04-25"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /attendance/daily — ADMIN → 200")
    @WithMockAdminUser
    void allDaily_asAdmin_200() throws Exception {
        given(queryService.getAllDaily(any(), any(), any(), eq(0), eq(20))).willReturn(
                new com.grown.smartoffice.global.common.PageResponse<>(
                        java.util.List.of(), 0, 20, 0L, 0, true));

        mockMvc.perform(get("/api/v1/attendance/daily").param("date", "2026-04-25"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /attendance/batch — ADMIN → 200")
    @WithMockAdminUser
    void triggerBatch_asAdmin_200() throws Exception {
        given(batchService.runDailyBatch(any(), any())).willReturn(5);

        String body = objectMapper.writeValueAsString(Map.of("targetDate", "2026-04-25"));

        mockMvc.perform(post("/api/v1/attendance/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(5));
    }
}
