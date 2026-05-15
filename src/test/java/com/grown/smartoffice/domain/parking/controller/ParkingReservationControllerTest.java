package com.grown.smartoffice.domain.parking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grown.smartoffice.domain.parking.dto.ParkingReservationResponse;
import com.grown.smartoffice.domain.parking.entity.ParkingReservationStatus;
import com.grown.smartoffice.domain.parking.service.ParkingReservationService;
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

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParkingReservationController.class)
@Import(TestSecurityConfig.class)
class ParkingReservationControllerTest {

    @Autowired MockMvc mockMvc;
    final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean ParkingReservationService parkingReservationService;

    @Test
    @DisplayName("POST /parking/reservations — ADMIN 201")
    @WithMockAdminUser
    void createReservation_admin_201() throws Exception {
        given(parkingReservationService.createReservation(any())).willReturn(
                ParkingReservationResponse.builder().reservationId(1L).vehicleId(1L)
                        .reservationStatus(ParkingReservationStatus.RESERVED).build());

        String body = objectMapper.writeValueAsString(Map.of(
                "vehicleId", 1, "zoneId", 8, "reservedAt", "2026-05-16T09:00:00"));

        mockMvc.perform(post("/api/v1/parking/reservations")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.reservationId").value(1));
    }

    @Test
    @DisplayName("POST /parking/reservations — EMPLOYEE 403")
    @WithMockEmployeeUser
    void createReservation_employee_403() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "vehicleId", 1, "zoneId", 8, "reservedAt", "2026-05-16T09:00:00"));

        mockMvc.perform(post("/api/v1/parking/reservations")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /parking/reservations/{id} — ADMIN 200")
    @WithMockAdminUser
    void getReservation_admin_200() throws Exception {
        given(parkingReservationService.getReservation(eq(1L))).willReturn(
                ParkingReservationResponse.builder().reservationId(1L).vehicleId(1L)
                        .vehiclePlateNumber("12가3456")
                        .reservationStatus(ParkingReservationStatus.PARKED).build());

        mockMvc.perform(get("/api/v1/parking/reservations/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reservationStatus").value("PARKED"));
    }
}
