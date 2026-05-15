package com.grown.smartoffice.domain.vehicle.service;

import com.grown.smartoffice.domain.user.repository.UserRepository;
import com.grown.smartoffice.domain.vehicle.dto.VehicleCreateRequest;
import com.grown.smartoffice.domain.vehicle.dto.VehicleResponse;
import com.grown.smartoffice.domain.vehicle.entity.Vehicle;
import com.grown.smartoffice.domain.vehicle.entity.VehicleType;
import com.grown.smartoffice.domain.vehicle.repository.VehicleRepository;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock VehicleRepository vehicleRepository;
    @Mock UserRepository userRepository;
    @InjectMocks VehicleService vehicleService;

    private VehicleCreateRequest createRequest(String plate, String type) {
        VehicleCreateRequest req = new VehicleCreateRequest();
        ReflectionTestUtils.setField(req, "plateNumber", plate);
        ReflectionTestUtils.setField(req, "ownerName", "박성종");
        ReflectionTestUtils.setField(req, "vehicleType", type);
        ReflectionTestUtils.setField(req, "purpose", "협력사 미팅");
        return req;
    }

    @Test
    @DisplayName("차량 등록 성공 (VISITOR)")
    void createVehicle_success() {
        VehicleCreateRequest req = createRequest("12가3456", "VISITOR");
        given(vehicleRepository.existsByPlateNumber("12가3456")).willReturn(false);
        given(vehicleRepository.save(any(Vehicle.class))).willAnswer(inv -> inv.getArgument(0));

        VehicleResponse res = vehicleService.createVehicle(req);

        assertThat(res.getPlateNumber()).isEqualTo("12가3456");
        assertThat(res.getVehicleType()).isEqualTo(VehicleType.VISITOR);
        assertThat(res.getOwnerName()).isEqualTo("박성종");
    }

    @Test
    @DisplayName("차량 등록 — 번호판 중복 시 DUPLICATE_PLATE_NUMBER")
    void createVehicle_duplicatePlate() {
        VehicleCreateRequest req = createRequest("12가3456", "STAFF");
        given(vehicleRepository.existsByPlateNumber("12가3456")).willReturn(true);

        assertThatThrownBy(() -> vehicleService.createVehicle(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_PLATE_NUMBER);
    }

    @Test
    @DisplayName("차량 등록 — 잘못된 vehicleType 시 INVALID_INPUT")
    void createVehicle_invalidType() {
        VehicleCreateRequest req = createRequest("12가3456", "UNKNOWN");
        given(vehicleRepository.existsByPlateNumber("12가3456")).willReturn(false);

        assertThatThrownBy(() -> vehicleService.createVehicle(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("차량 상세 조회 — 부재 시 VEHICLE_NOT_FOUND")
    void getVehicle_notFound() {
        given(vehicleRepository.findByIdWithOwner(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.getVehicle(99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.VEHICLE_NOT_FOUND);
    }

    @Test
    @DisplayName("차량 삭제 — 부재 시 VEHICLE_NOT_FOUND")
    void deleteVehicle_notFound() {
        given(vehicleRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.deleteVehicle(99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.VEHICLE_NOT_FOUND);
    }
}
