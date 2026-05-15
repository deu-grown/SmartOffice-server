package com.grown.smartoffice.domain.parking.service;

import com.grown.smartoffice.domain.parking.dto.ParkingReservationCreateRequest;
import com.grown.smartoffice.domain.parking.dto.ParkingReservationResponse;
import com.grown.smartoffice.domain.parking.dto.ParkingReservationUpdateRequest;
import com.grown.smartoffice.domain.parking.entity.ParkingReservation;
import com.grown.smartoffice.domain.parking.entity.ParkingReservationStatus;
import com.grown.smartoffice.domain.parking.entity.ParkingSpot;
import com.grown.smartoffice.domain.parking.repository.ParkingReservationRepository;
import com.grown.smartoffice.domain.parking.repository.ParkingSpotRepository;
import com.grown.smartoffice.domain.vehicle.entity.Vehicle;
import com.grown.smartoffice.domain.vehicle.repository.VehicleRepository;
import com.grown.smartoffice.domain.zone.entity.Zone;
import com.grown.smartoffice.domain.zone.repository.ZoneRepository;
import com.grown.smartoffice.global.common.PageResponse;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParkingReservationService {

    private final ParkingReservationRepository parkingReservationRepository;
    private final VehicleRepository vehicleRepository;
    private final ZoneRepository zoneRepository;
    private final ParkingSpotRepository parkingSpotRepository;

    @Transactional
    public ParkingReservationResponse createReservation(ParkingReservationCreateRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new CustomException(ErrorCode.VEHICLE_NOT_FOUND));
        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new CustomException(ErrorCode.ZONE_NOT_FOUND));
        ParkingSpot spot = resolveSpot(request.getSpotId());

        ParkingReservation reservation = ParkingReservation.builder()
                .vehicle(vehicle)
                .zone(zone)
                .spot(spot)
                .reservedAt(request.getReservedAt())
                .reservationStatus(ParkingReservationStatus.RESERVED)
                .build();
        return ParkingReservationResponse.from(parkingReservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public PageResponse<ParkingReservationResponse> getReservations(Long zoneId, String status,
                                                                    int page, int size) {
        ParkingReservationStatus st = (status != null) ? parseStatus(status) : null;
        return PageResponse.from(
                parkingReservationRepository.findAllWithFilters(zoneId, st, PageRequest.of(page, size))
                        .map(ParkingReservationResponse::from));
    }

    @Transactional(readOnly = true)
    public ParkingReservationResponse getReservation(Long id) {
        ParkingReservation reservation = parkingReservationRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PARKING_RESERVATION_NOT_FOUND));
        return ParkingReservationResponse.from(reservation);
    }

    @Transactional
    public ParkingReservationResponse updateReservation(Long id, ParkingReservationUpdateRequest request) {
        ParkingReservation reservation = parkingReservationRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PARKING_RESERVATION_NOT_FOUND));

        ParkingSpot spot = resolveSpot(request.getSpotId());
        reservation.update(
                spot,
                request.getEntryAt(),
                request.getExitAt(),
                request.getReservationStatus() != null ? parseStatus(request.getReservationStatus()) : null);
        return ParkingReservationResponse.from(reservation);
    }

    @Transactional
    public void deleteReservation(Long id) {
        ParkingReservation reservation = parkingReservationRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PARKING_RESERVATION_NOT_FOUND));
        parkingReservationRepository.delete(reservation);
    }

    private ParkingSpot resolveSpot(Long spotId) {
        if (spotId == null) return null;
        return parkingSpotRepository.findById(spotId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARKING_SPOT_NOT_FOUND));
    }

    private ParkingReservationStatus parseStatus(String value) {
        try {
            return ParkingReservationStatus.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
    }
}
