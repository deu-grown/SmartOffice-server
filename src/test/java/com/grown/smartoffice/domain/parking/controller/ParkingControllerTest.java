package com.grown.smartoffice.domain.parking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grown.smartoffice.domain.parking.dto.*;
import com.grown.smartoffice.domain.parking.entity.SpotStatus;
import com.grown.smartoffice.domain.parking.entity.SpotType;
import com.grown.smartoffice.domain.parking.service.ParkingService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParkingController.class)
@Import(TestSecurityConfig.class)
class ParkingControllerTest {

    @Autowired MockMvc mockMvc;
    final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean ParkingService parkingService;

    private ParkingSpotResponse sampleSpotResponse() {
        return ParkingSpotResponse.builder()
                .spotId(100L).zoneId(8L).zoneName("지하1층")
                .spotNumber("B1-001").spotType(SpotType.REGULAR)
                .deviceId(11L).deviceName("초음파-001")
                .positionX(100).positionY(200)
                .occupied(false).spotStatus(SpotStatus.ACTIVE)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("주차면 등록 — ADMIN 성공")
    @WithMockAdminUser
    void createSpot_admin_success() throws Exception {
        ParkingSpotCreateRequest req = new ParkingSpotCreateRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(req, "zoneId", 8L);
        org.springframework.test.util.ReflectionTestUtils.setField(req, "spotNumber", "B1-001");
        org.springframework.test.util.ReflectionTestUtils.setField(req, "spotType", "REGULAR");

        given(parkingService.createSpot(any())).willReturn(sampleSpotResponse());

        mockMvc.perform(post("/api/v1/parking/spots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.spotId").value(100))
                .andExpect(jsonPath("$.data.spotNumber").value("B1-001"));
    }

    @Test
    @DisplayName("주차면 등록 — EMPLOYEE 거부")
    @WithMockEmployeeUser
    void createSpot_employee_forbidden() throws Exception {
        ParkingSpotCreateRequest req = new ParkingSpotCreateRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(req, "zoneId", 8L);
        org.springframework.test.util.ReflectionTestUtils.setField(req, "spotNumber", "B1-001");
        org.springframework.test.util.ReflectionTestUtils.setField(req, "spotType", "REGULAR");

        mockMvc.perform(post("/api/v1/parking/spots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("주차면 목록 조회 — ADMIN 성공")
    @WithMockAdminUser
    void getSpots_admin_success() throws Exception {
        given(parkingService.getSpots(eq(8L), eq("REGULAR"), eq("ACTIVE")))
                .willReturn(List.of(sampleSpotResponse()));

        mockMvc.perform(get("/api/v1/parking/spots")
                        .param("zoneId", "8")
                        .param("spotType", "REGULAR")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].spotId").value(100));
    }

    @Test
    @DisplayName("주차면 목록 조회 — EMPLOYEE 거부")
    @WithMockEmployeeUser
    void getSpots_employee_forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/parking/spots"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("주차장 전체 현황 조회 — 인증 사용자 성공")
    @WithMockEmployeeUser
    void getZoneSummary_employee_success() throws Exception {
        ParkingZoneSummaryResponse res = ParkingZoneSummaryResponse.builder()
                .zoneId(8L).zoneName("지하1층")
                .totalSpots(5).occupiedSpots(2).availableSpots(3)
                .spots(List.of(sampleSpotResponse()))
                .build();
        given(parkingService.getZoneSummary(8L)).willReturn(res);

        mockMvc.perform(get("/api/v1/parking/zones/{zoneId}/spots", 8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalSpots").value(5))
                .andExpect(jsonPath("$.data.occupiedSpots").value(2))
                .andExpect(jsonPath("$.data.availableSpots").value(3));
    }

    @Test
    @DisplayName("주차장 지도 조회 — 인증 사용자 성공")
    @WithMockEmployeeUser
    void getZoneMap_employee_success() throws Exception {
        ParkingZoneMapResponse res = ParkingZoneMapResponse.builder()
                .zoneId(8L).zoneName("지하1층")
                .spots(List.of(ParkingSpotMapResponse.builder()
                        .spotId(100L).spotNumber("B1-001").spotType(SpotType.REGULAR)
                        .positionX(100).positionY(200).occupied(false).build()))
                .build();
        given(parkingService.getZoneMap(8L)).willReturn(res);

        mockMvc.perform(get("/api/v1/parking/zones/{zoneId}/map", 8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.spots[0].positionX").value(100));
    }

    @Test
    @DisplayName("IoT 점유 상태 업데이트 — 익명 호출 통과 + 성공")
    void updateStatus_anonymous_success() throws Exception {
        ParkingStatusUpdateRequest req = new ParkingStatusUpdateRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(req, "deviceId", 11L);
        org.springframework.test.util.ReflectionTestUtils.setField(req, "occupied", true);

        given(parkingService.updateOccupancyFromIot(eq(100L), any()))
                .willReturn(ParkingStatusUpdateResponse.builder()
                        .spotId(100L).occupied(true).updatedAt(LocalDateTime.now()).build());

        mockMvc.perform(post("/api/v1/parking/spots/{spotId}/status", 100)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.occupied").value(true));
    }

    @Test
    @DisplayName("IoT 점유 상태 업데이트 — deviceId 불일치 시 400(DEVICE_SPOT_MISMATCH)")
    void updateStatus_deviceMismatch() throws Exception {
        ParkingStatusUpdateRequest req = new ParkingStatusUpdateRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(req, "deviceId", 99L);
        org.springframework.test.util.ReflectionTestUtils.setField(req, "occupied", true);

        willThrow(new CustomException(ErrorCode.DEVICE_SPOT_MISMATCH))
                .given(parkingService).updateOccupancyFromIot(eq(100L), any());

        mockMvc.perform(post("/api/v1/parking/spots/{spotId}/status", 100)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주차면 삭제 — ADMIN 성공")
    @WithMockAdminUser
    void deleteSpot_admin_success() throws Exception {
        mockMvc.perform(delete("/api/v1/parking/spots/{spotId}", 100))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("success"));
    }
}
