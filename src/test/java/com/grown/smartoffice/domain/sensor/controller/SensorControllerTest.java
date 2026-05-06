package com.grown.smartoffice.domain.sensor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grown.smartoffice.domain.sensor.dto.*;
import com.grown.smartoffice.domain.sensor.service.SensorService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SensorController.class)
@Import(TestSecurityConfig.class)
class SensorControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean SensorService sensorService;

    @Test
    @DisplayName("센서 데이터 수신 - 누구나 가능 (SYSTEM/IoT 가정)")
    void recordLog_success() throws Exception {
        SensorLogRequest req = SensorLogRequest.builder()
                .zoneId(1L)
                .deviceId(10L)
                .sensorType("TEMPERATURE")
                .value(new BigDecimal("25.5"))
                .unit("°C")
                .timestamp(LocalDateTime.now())
                .build();

        given(sensorService.recordLog(any())).willReturn(new SensorLogResponse(100L));

        mockMvc.perform(post("/api/v1/sensors/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.logId").value(100L));
    }

    @Test
    @DisplayName("구역별 최신 데이터 조회 - ADMIN 성공")
    @WithMockAdminUser
    void getLatestData_asAdmin_success() throws Exception {
        SensorDataDto data = SensorDataDto.builder()
                .sensorType("TEMPERATURE")
                .value(new BigDecimal("25.5"))
                .unit("°C")
                .timestamp(LocalDateTime.now())
                .build();
        
        given(sensorService.getLatestData(1L)).willReturn(SensorLatestResponse.builder()
                .zoneId(1L)
                .sensorDataList(List.of(data))
                .build());

        mockMvc.perform(get("/api/v1/sensors/latest")
                        .param("zoneId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.searchQuery.zoneId").value(1))
                .andExpect(jsonPath("$.data.sensorDataList[0].sensorType").value("TEMPERATURE"));
    }

    @Test
    @DisplayName("구역별 최신 데이터 조회 - EMPLOYEE 거부")
    @WithMockEmployeeUser
    void getLatestData_asEmployee_forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/sensors/latest")
                        .param("zoneId", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("센서 로그 이력 조회 - ADMIN 성공")
    @WithMockAdminUser
    void getHistory_asAdmin_success() throws Exception {
        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now();

        given(sensorService.getHistory(eq(1L), any(), any())).willReturn(SensorHistoryResponse.builder()
                .zoneId(1L)
                .startDate(start)
                .endDate(end)
                .sensorDataList(List.of())
                .build());

        mockMvc.perform(get("/api/v1/zones/1/sensors/logs")
                        .param("startDate", start.toString())
                        .param("endDate", end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.searchQuery.zoneId").value(1));
    }
}
