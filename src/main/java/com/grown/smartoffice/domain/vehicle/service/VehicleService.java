package com.grown.smartoffice.domain.vehicle.service;

import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import com.grown.smartoffice.domain.vehicle.dto.VehicleCreateRequest;
import com.grown.smartoffice.domain.vehicle.dto.VehicleResponse;
import com.grown.smartoffice.domain.vehicle.dto.VehicleUpdateRequest;
import com.grown.smartoffice.domain.vehicle.entity.Vehicle;
import com.grown.smartoffice.domain.vehicle.entity.VehicleType;
import com.grown.smartoffice.domain.vehicle.repository.VehicleRepository;
import com.grown.smartoffice.global.common.PageResponse;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    @Transactional
    public VehicleResponse createVehicle(VehicleCreateRequest request) {
        if (vehicleRepository.existsByPlateNumber(request.getPlateNumber())) {
            throw new CustomException(ErrorCode.DUPLICATE_PLATE_NUMBER);
        }
        User ownerUser = resolveUser(request.getOwnerUserId());

        Vehicle vehicle = Vehicle.builder()
                .plateNumber(request.getPlateNumber())
                .ownerName(request.getOwnerName())
                .ownerUser(ownerUser)
                .vehicleType(parseVehicleType(request.getVehicleType()))
                .purpose(request.getPurpose())
                .build();
        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }

    @Transactional(readOnly = true)
    public PageResponse<VehicleResponse> getVehicles(String vehicleType, String keyword,
                                                     int page, int size) {
        VehicleType type = (vehicleType != null) ? parseVehicleType(vehicleType) : null;
        return PageResponse.from(
                vehicleRepository.findAllWithFilters(type, keyword, PageRequest.of(page, size))
                        .map(VehicleResponse::from));
    }

    @Transactional(readOnly = true)
    public VehicleResponse getVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findByIdWithOwner(id)
                .orElseThrow(() -> new CustomException(ErrorCode.VEHICLE_NOT_FOUND));
        return VehicleResponse.from(vehicle);
    }

    @Transactional
    public VehicleResponse updateVehicle(Long id, VehicleUpdateRequest request) {
        Vehicle vehicle = vehicleRepository.findByIdWithOwner(id)
                .orElseThrow(() -> new CustomException(ErrorCode.VEHICLE_NOT_FOUND));

        if (request.getPlateNumber() != null
                && vehicleRepository.existsByPlateNumberAndVehicleIdNot(request.getPlateNumber(), id)) {
            throw new CustomException(ErrorCode.DUPLICATE_PLATE_NUMBER);
        }
        User ownerUser = resolveUser(request.getOwnerUserId());

        vehicle.update(
                request.getPlateNumber(),
                request.getOwnerName(),
                ownerUser,
                request.getVehicleType() != null ? parseVehicleType(request.getVehicleType()) : null,
                request.getPurpose());
        return VehicleResponse.from(vehicle);
    }

    @Transactional
    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.VEHICLE_NOT_FOUND));
        vehicleRepository.delete(vehicle);
    }

    private User resolveUser(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private VehicleType parseVehicleType(String value) {
        try {
            return VehicleType.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
    }
}
