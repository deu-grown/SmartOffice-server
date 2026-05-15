package com.grown.smartoffice.domain.guest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grown.smartoffice.domain.guest.dto.GuestResponse;
import com.grown.smartoffice.domain.guest.entity.GuestStatus;
import com.grown.smartoffice.domain.guest.service.GuestService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GuestController.class)
@Import(TestSecurityConfig.class)
class GuestControllerTest {

    @Autowired MockMvc mockMvc;
    final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean GuestService guestService;

    @Test
    @DisplayName("POST /guests — ADMIN 201")
    @WithMockAdminUser
    void createGuest_admin_201() throws Exception {
        given(guestService.createGuest(any())).willReturn(
                GuestResponse.builder().guestId(1L).guestName("김방문")
                        .guestStatus(GuestStatus.SCHEDULED).build());

        String body = objectMapper.writeValueAsString(Map.of(
                "guestName", "김방문", "scheduledEntryAt", "2026-05-16T14:00:00"));

        mockMvc.perform(post("/api/v1/guests")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.guestStatus").value("SCHEDULED"));
    }

    @Test
    @DisplayName("POST /guests — EMPLOYEE 403")
    @WithMockEmployeeUser
    void createGuest_employee_403() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "guestName", "김방문", "scheduledEntryAt", "2026-05-16T14:00:00"));

        mockMvc.perform(post("/api/v1/guests")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /guests/{id}/check-in — ADMIN 200")
    @WithMockAdminUser
    void checkIn_admin_200() throws Exception {
        given(guestService.checkIn(eq(1L))).willReturn(
                GuestResponse.builder().guestId(1L).guestName("김방문")
                        .guestStatus(GuestStatus.VISITING).build());

        mockMvc.perform(post("/api/v1/guests/{id}/check-in", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.guestStatus").value("VISITING"));
    }

    @Test
    @DisplayName("POST /guests/{id}/check-out — ADMIN 200")
    @WithMockAdminUser
    void checkOut_admin_200() throws Exception {
        given(guestService.checkOut(eq(1L))).willReturn(
                GuestResponse.builder().guestId(1L).guestName("김방문")
                        .guestStatus(GuestStatus.COMPLETED).build());

        mockMvc.perform(post("/api/v1/guests/{id}/check-out", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.guestStatus").value("COMPLETED"));
    }
}
