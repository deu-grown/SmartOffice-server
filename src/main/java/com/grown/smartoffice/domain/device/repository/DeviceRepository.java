package com.grown.smartoffice.domain.device.repository;

import com.grown.smartoffice.domain.device.entity.Device;
import com.grown.smartoffice.domain.device.entity.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    @Query("SELECT d FROM Device d JOIN FETCH d.zone")
    List<Device> findAllWithZone();

    @Query("SELECT d FROM Device d JOIN FETCH d.zone WHERE d.devicesId = :id")
    Optional<Device> findByIdWithZone(@Param("id") Long id);

    long countByDeviceStatus(DeviceStatus deviceStatus);

    Optional<Device> findByDeviceName(String deviceName);

    boolean existsByDeviceName(String deviceName);

    boolean existsBySerialNumber(String serialNumber);

    List<Device> findByZone_ZoneIdAndDeviceType(Long zoneId, String deviceType);
}
