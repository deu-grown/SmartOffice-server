package com.grown.smartoffice.domain.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grown.smartoffice.domain.reservation.dto.ReservationListResponse;
import com.grown.smartoffice.domain.reservation.dto.ReservationResponse;
import com.grown.smartoffice.global.common.PageResponse;
import com.grown.smartoffice.domain.reservation.entity.ReservationStatus;
import com.grown.smartoffice.domain.reservation.service.ReservationService;
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
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
@Import(TestSecurityConfig.class)
class ReservationControllerTest {

    @Autowired MockMvc mockMvc;
    final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean ReservationService reservationService;

    private ReservationResponse sample() {
        return ReservationResponse.builder()
                .reservationId(100L).zoneName("회의실A").userName("관리자")
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .purpose("주간 회의")
                .status(ReservationStatus.CONFIRMED)
                .build();
    }

    @Test
    @DisplayName("POST /reservations — EMPLOYEE 201 (본인이 생성)")
    @WithMockEmployeeUser(email = "me@grown.com")
    void create_employee_201() throws Exception {
        given(reservationService.createReservation(any(), eq("me@grown.com"))).willReturn(sample());

        String body = objectMapper.writeValueAsString(Map.of(
                "zoneId", 2,
                "startTime", LocalDateTime.now().plusHours(1).toString(),
                "endTime", LocalDateTime.now().plusHours(2).toString(),
                "purpose", "주간 회의"));

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.reservationId").value(100));
    }

    @Test
    @DisplayName("POST /reservations — 인증 없음 → 401")
    void create_noAuth_401() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "zoneId", 2,
                "startTime", LocalDateTime.now().plusHours(1).toString(),
                "endTime", LocalDateTime.now().plusHours(2).toString(),
                "purpose", "x"));

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /reservations — ADMIN 200")
    @WithMockAdminUser
    void listAll_admin_200() throws Exception {
        given(reservationService.getAllReservations(any(), anyInt(), anyInt()))
                .willReturn(new PageResponse<>(Collections.emptyList(), 0, 20, 0, 0, true));

        mockMvc.perform(get("/api/v1/reservations"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /reservations — EMPLOYEE 403")
    @WithMockEmployeeUser
    void listAll_employee_403() throws Exception {
        mockMvc.perform(get("/api/v1/reservations"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /reservations/me — EMPLOYEE 200")
    @WithMockEmployeeUser(email = "me@grown.com")
    void listMine_employee_200() throws Exception {
        given(reservationService.getMyReservations(eq("me@grown.com")))
                .willReturn(ReservationListResponse.builder()
                        .totalCount(0).reservationList(Collections.emptyList()).build());

        mockMvc.perform(get("/api/v1/reservations/me"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /reservations/{id} — 없는 예약 → 404")
    @WithMockEmployeeUser(email = "me@grown.com")
    void detail_notFound_404() throws Exception {
        given(reservationService.getReservation(eq(999L), eq("me@grown.com")))
                .willThrow(new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        mockMvc.perform(get("/api/v1/reservations/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /reservations/{id} — 본인 예약 상세 200 (호출자 email 서비스 전달)")
    @WithMockEmployeeUser(email = "me@grown.com")
    void detail_employee_200() throws Exception {
        given(reservationService.getReservation(eq(100L), eq("me@grown.com"))).willReturn(sample());

        mockMvc.perform(get("/api/v1/reservations/{id}", 100))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reservationId").value(100));
    }

    @Test
    @DisplayName("DELETE /reservations/{id} — EMPLOYEE 본인 취소 200")
    @WithMockEmployeeUser(email = "me@grown.com")
    void cancel_employee_200() throws Exception {
        given(reservationService.cancelReservation(eq(100L), eq("me@grown.com"))).willReturn(100L);

        mockMvc.perform(delete("/api/v1/reservations/{id}", 100))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(100));
    }
}
