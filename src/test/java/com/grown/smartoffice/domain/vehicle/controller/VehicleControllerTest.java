package com.grown.smartoffice.domain.vehicle.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grown.smartoffice.domain.vehicle.dto.VehicleResponse;
import com.grown.smartoffice.domain.vehicle.entity.VehicleType;
import com.grown.smartoffice.domain.vehicle.service.VehicleService;
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

@WebMvcTest(VehicleController.class)
@Import(TestSecurityConfig.class)
class VehicleControllerTest {

    @Autowired MockMvc mockMvc;
    final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean VehicleService vehicleService;

    @Test
    @DisplayName("POST /vehicles — ADMIN 201")
    @WithMockAdminUser
    void createVehicle_admin_201() throws Exception {
        given(vehicleService.createVehicle(any())).willReturn(
                VehicleResponse.builder().vehicleId(1L).plateNumber("12가3456")
                        .vehicleType(VehicleType.VISITOR).build());

        String body = objectMapper.writeValueAsString(Map.of(
                "plateNumber", "12가3456", "ownerName", "박성종", "vehicleType", "VISITOR"));

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.plateNumber").value("12가3456"));
    }

    @Test
    @DisplayName("POST /vehicles — EMPLOYEE 403")
    @WithMockEmployeeUser
    void createVehicle_employee_403() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "plateNumber", "12가3456", "ownerName", "박성종", "vehicleType", "VISITOR"));

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /vehicles/{id} — ADMIN 200")
    @WithMockAdminUser
    void getVehicle_admin_200() throws Exception {
        given(vehicleService.getVehicle(eq(1L))).willReturn(
                VehicleResponse.builder().vehicleId(1L).plateNumber("12가3456")
                        .vehicleType(VehicleType.STAFF).ownerName("박성종").build());

        mockMvc.perform(get("/api/v1/vehicles/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.vehicleId").value(1))
                .andExpect(jsonPath("$.data.vehicleType").value("STAFF"));
    }
}
