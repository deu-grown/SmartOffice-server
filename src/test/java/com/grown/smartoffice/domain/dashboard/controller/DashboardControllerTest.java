package com.grown.smartoffice.domain.dashboard.controller;

import com.grown.smartoffice.domain.dashboard.dto.AttendanceTodayResponse;
import com.grown.smartoffice.domain.dashboard.dto.DashboardSummaryResponse;
import com.grown.smartoffice.domain.dashboard.dto.RecentAccessResponse;
import com.grown.smartoffice.domain.dashboard.dto.SensorCurrentResponse;
import com.grown.smartoffice.domain.dashboard.service.DashboardService;
import com.grown.smartoffice.support.TestSecurityConfig;
import com.grown.smartoffice.support.WithMockAdminUser;
import com.grown.smartoffice.support.WithMockEmployeeUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@Import(TestSecurityConfig.class)
class DashboardControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean DashboardService dashboardService;

    @Test
    @DisplayName("GET /dashboard/summary — ADMIN 200")
    @WithMockAdminUser
    void summary_admin_200() throws Exception {
        given(dashboardService.getSummary()).willReturn(
                DashboardSummaryResponse.builder()
                        .totalUsers(12).todayReservations(5).activeDevices(15).pendingApprovals(2)
                        .build());

        mockMvc.perform(get("/api/v1/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").value(12));
    }

    @Test
    @DisplayName("GET /dashboard/summary — EMPLOYEE 403")
    @WithMockEmployeeUser
    void summary_employee_403() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/summary"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /dashboard/sensors/current — ADMIN 200, 빈 배열 허용")
    @WithMockAdminUser
    void sensorsCurrent_admin_200_emptyOk() throws Exception {
        given(dashboardService.getCurrentSensors()).willReturn(Collections.<SensorCurrentResponse>emptyList());

        mockMvc.perform(get("/api/v1/dashboard/sensors/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /dashboard/attendance/today — ADMIN 200")
    @WithMockAdminUser
    void todayAttendance_admin_200() throws Exception {
        given(dashboardService.getTodayAttendance()).willReturn(
                AttendanceTodayResponse.builder()
                        .totalExpected(12).presentCount(8).absentCount(2).lateCount(2).build());

        mockMvc.perform(get("/api/v1/dashboard/attendance/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalExpected").value(12));
    }

    @Test
    @DisplayName("GET /dashboard/access/recent — ADMIN 200 (limit, type 전달)")
    @WithMockAdminUser
    void recentAccess_admin_200() throws Exception {
        given(dashboardService.getRecentAccess(anyInt(), any()))
                .willReturn(List.<RecentAccessResponse>of());

        mockMvc.perform(get("/api/v1/dashboard/access/recent")
                        .param("limit", "50").param("type", "IN"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /dashboard/access/recent — EMPLOYEE 403")
    @WithMockEmployeeUser
    void recentAccess_employee_403() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/access/recent"))
                .andExpect(status().isForbidden());
    }
}
