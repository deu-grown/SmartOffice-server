package com.grown.smartoffice.domain.device.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grown.smartoffice.domain.device.dto.*;
import com.grown.smartoffice.domain.device.entity.DeviceStatus;
import com.grown.smartoffice.domain.device.service.DeviceService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceController.class)
@Import(TestSecurityConfig.class)
class DeviceControllerTest {

    @Autowired MockMvc mockMvc;
    final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean DeviceService deviceService;

    @Test
    @DisplayName("전체 장치 목록 조회 - ADMIN 권한 필요")
    @WithMockAdminUser
    void getAllDevices_success() throws Exception {
        DeviceListItemResponse item = DeviceListItemResponse.builder()
                .id(1L)
                .name("Test Device")
                .deviceType("NFC_READER")
                .status(DeviceStatus.ACTIVE)
                .zoneName("1층 대회의실")
                .build();
        given(deviceService.getAllDevices()).willReturn(List.of(item));

        mockMvc.perform(get("/api/v1/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Test Device"));
    }

    @Test
    @DisplayName("장치 등록 - ADMIN 권한 성공")
    @WithMockAdminUser
    void registerDevice_success() throws Exception {
        DeviceCreateRequest req = DeviceCreateRequest.builder()
                .name("New Device")
                .deviceType("SENSOR")
                .zoneId(1L)
                .status(DeviceStatus.ACTIVE)
                .build();
        
        given(deviceService.registerDevice(any())).willReturn(DeviceCreateResponse.builder()
                .id(10L)
                .name("New Device")
                .build());

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("New Device"));
    }

    @Test
    @DisplayName("장치 등록 - EMPLOYEE 권한 거부")
    @WithMockEmployeeUser
    void registerDevice_forbidden() throws Exception {
        DeviceCreateRequest req = DeviceCreateRequest.builder()
                .name("New Device")
                .deviceType("SENSOR")
                .zoneId(1L)
                .build();

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("장치 상세 조회 - 성공")
    @WithMockAdminUser
    void getDeviceDetail_success() throws Exception {
        given(deviceService.getDeviceDetail(1L)).willReturn(DeviceDetailResponse.builder()
                .id(1L)
                .name("Device 1")
                .build());

        mockMvc.perform(get("/api/v1/devices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Device 1"));
    }

    @Test
    @DisplayName("장치 수정 - 성공")
    @WithMockAdminUser
    void updateDevice_success() throws Exception {
        DeviceUpdateRequest req = DeviceUpdateRequest.builder()
                .name("Updated Name")
                .build();

        given(deviceService.updateDevice(eq(1L), any())).willReturn(DeviceUpdateResponse.builder()
                .id(1L)
                .name("Updated Name")
                .build());

        mockMvc.perform(put("/api/v1/devices/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Name"));
    }

    @Test
    @DisplayName("장치 삭제 - 성공")
    @WithMockAdminUser
    void deleteDevice_success() throws Exception {
        mockMvc.perform(delete("/api/v1/devices/1"))
                .andExpect(status().isOk());
    }
}
