package com.grown.smartoffice.domain.control.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grown.smartoffice.domain.control.dto.ControlRequest;
import com.grown.smartoffice.domain.control.dto.ControlResponse;
import com.grown.smartoffice.domain.control.dto.ControlDetailResponse;
import com.grown.smartoffice.domain.control.dto.ControlHistoryResponse;
import com.grown.smartoffice.domain.control.entity.ControlCommand;
import com.grown.smartoffice.domain.control.entity.ControlStatus;
import com.grown.smartoffice.domain.control.service.ControlService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ControlController.class)
@Import(TestSecurityConfig.class)
class ControlControllerTest {

    @Autowired MockMvc mockMvc;
    final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean ControlService controlService;

    @Test
    @DisplayName("제어 명령 발송 - ADMIN 성공")
    @WithMockAdminUser
    void sendCommand_asAdmin_success() throws Exception {
        ControlRequest req = ControlRequest.builder()
                .zoneId(1L)
                .deviceId(10L)
                .command("POWER_ON")
                .value("24")
                .build();

        given(controlService.sendCommand(any())).willReturn(new ControlResponse(1001L));

        mockMvc.perform(post("/api/v1/controls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.controlId").value(1001L));
    }

    @Test
    @DisplayName("제어 명령 발송 - EMPLOYEE 거부")
    @WithMockEmployeeUser
    void sendCommand_asEmployee_forbidden() throws Exception {
        ControlRequest req = ControlRequest.builder()
                .zoneId(1L)
                .deviceId(10L)
                .command("POWER_ON")
                .build();

        mockMvc.perform(post("/api/v1/controls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("제어 명령 상세 조회 - ADMIN 성공")
    @WithMockAdminUser
    void getControlDetail_asAdmin_success() throws Exception {
        ControlCommand command = ControlCommand.builder()
                .commandType(com.grown.smartoffice.domain.control.entity.ControlCommandType.AC)
                .status(ControlStatus.COMPLETED)
                .triggeredAt(LocalDateTime.now())
                .build();
        
        given(controlService.getControlDetail(1001L)).willReturn(new ControlDetailResponse(command));

        mockMvc.perform(get("/api/v1/controls/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("제어 명령 이력 조회 - ADMIN 성공")
    @WithMockAdminUser
    void getHistory_asAdmin_success() throws Exception {
        given(controlService.getHistory(eq(1L), any())).willReturn(ControlHistoryResponse.builder()
                .totalCount(0)
                .controlList(List.of())
                .build());

        mockMvc.perform(get("/api/v1/controls")
                        .param("zoneId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(0));
    }
}
