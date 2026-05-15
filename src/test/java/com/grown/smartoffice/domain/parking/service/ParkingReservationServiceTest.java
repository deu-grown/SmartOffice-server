package com.grown.smartoffice.domain.parking.service;

import com.grown.smartoffice.domain.parking.dto.ParkingReservationCreateRequest;
import com.grown.smartoffice.domain.parking.dto.ParkingReservationResponse;
import com.grown.smartoffice.domain.parking.entity.ParkingReservation;
import com.grown.smartoffice.domain.parking.entity.ParkingReservationStatus;
import com.grown.smartoffice.domain.parking.repository.ParkingReservationRepository;
import com.grown.smartoffice.domain.parking.repository.ParkingSpotRepository;
import com.grown.smartoffice.domain.vehicle.entity.Vehicle;
import com.grown.smartoffice.domain.vehicle.entity.VehicleType;
import com.grown.smartoffice.domain.vehicle.repository.VehicleRepository;
import com.grown.smartoffice.domain.zone.entity.Zone;
import com.grown.smartoffice.domain.zone.entity.ZoneType;
import com.grown.smartoffice.domain.zone.repository.ZoneRepository;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ParkingReservationServiceTest {

    @Mock ParkingReservationRepository parkingReservationRepository;
    @Mock VehicleRepository vehicleRepository;
    @Mock ZoneRepository zoneRepository;
    @Mock ParkingSpotRepository parkingSpotRepository;
    @InjectMocks ParkingReservationService parkingReservationService;

    private Vehicle vehicle() {
        Vehicle v = Vehicle.builder().plateNumber("12가3456").ownerName("박성종")
                .vehicleType(VehicleType.STAFF).build();
        ReflectionTestUtils.setField(v, "vehicleId", 1L);
        return v;
    }

    private Zone zone() {
        Zone z = Zone.builder().zoneName("지하1층").zoneType(ZoneType.FLOOR).build();
        ReflectionTestUtils.setField(z, "zoneId", 8L);
        return z;
    }

    private ParkingReservationCreateRequest createRequest() {
        ParkingReservationCreateRequest req = new ParkingReservationCreateRequest();
        ReflectionTestUtils.setField(req, "vehicleId", 1L);
        ReflectionTestUtils.setField(req, "zoneId", 8L);
        ReflectionTestUtils.setField(req, "reservedAt", LocalDateTime.of(2026, 5, 16, 9, 0));
        return req;
    }

    @Test
    @DisplayName("주차 예약 등록 성공 (spot 미배정)")
    void createReservation_success() {
        ParkingReservationCreateRequest req = createRequest();
        given(vehicleRepository.findById(1L)).willReturn(Optional.of(vehicle()));
        given(zoneRepository.findById(8L)).willReturn(Optional.of(zone()));
        given(parkingReservationRepository.save(any(ParkingReservation.class)))
                .willAnswer(inv -> inv.getArgument(0));

        ParkingReservationResponse res = parkingReservationService.createReservation(req);

        assertThat(res.getVehiclePlateNumber()).isEqualTo("12가3456");
        assertThat(res.getZoneName()).isEqualTo("지하1층");
        assertThat(res.getReservationStatus()).isEqualTo(ParkingReservationStatus.RESERVED);
        assertThat(res.getSpotId()).isNull();
    }

    @Test
    @DisplayName("주차 예약 등록 — 차량 부재 시 VEHICLE_NOT_FOUND")
    void createReservation_vehicleNotFound() {
        ParkingReservationCreateRequest req = createRequest();
        given(vehicleRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> parkingReservationService.createReservation(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.VEHICLE_NOT_FOUND);
    }

    @Test
    @DisplayName("주차 예약 상세 조회 — 부재 시 PARKING_RESERVATION_NOT_FOUND")
    void getReservation_notFound() {
        given(parkingReservationRepository.findByIdWithRelations(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> parkingReservationService.getReservation(99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.PARKING_RESERVATION_NOT_FOUND);
    }

    @Test
    @DisplayName("주차 예약 삭제 — 부재 시 PARKING_RESERVATION_NOT_FOUND")
    void deleteReservation_notFound() {
        given(parkingReservationRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> parkingReservationService.deleteReservation(99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.PARKING_RESERVATION_NOT_FOUND);
    }
}
