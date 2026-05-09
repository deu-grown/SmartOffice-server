package com.grown.smartoffice.domain.power.service;

import com.grown.smartoffice.domain.power.dto.*;
import com.grown.smartoffice.domain.power.entity.PowerBilling;
import com.grown.smartoffice.domain.power.repository.PowerBillingRepository;
import com.grown.smartoffice.domain.sensor.entity.SensorLog;
import com.grown.smartoffice.domain.sensor.repository.SensorLogRepository;
import com.grown.smartoffice.domain.zone.entity.Zone;
import com.grown.smartoffice.domain.zone.repository.ZoneRepository;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PowerService {

    private final PowerBillingRepository powerBillingRepository;
    private final SensorLogRepository sensorLogRepository;
    private final ZoneRepository zoneRepository;

    @Transactional(readOnly = true)
    public PowerCurrentResponse getCurrentPower(Long zoneId) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new CustomException(ErrorCode.ZONE_NOT_FOUND));
        List<SensorLog> logs = sensorLogRepository.findLatestPowerByZoneId(zoneId);
        return PowerCurrentResponse.from(zone, logs);
    }

    @Transactional(readOnly = true)
    public PowerHourlyResponse getHourlyHistory(Long zoneId, LocalDate startDate, LocalDate endDate, Long deviceId) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new CustomException(ErrorCode.ZONE_NOT_FOUND));

        LocalDate from = startDate != null ? startDate : LocalDate.now().minusDays(7);
        LocalDate to   = endDate   != null ? endDate   : LocalDate.now();

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end   = to.plusDays(1).atStartOfDay().minusNanos(1);

        List<HourlyPowerProjection> projections =
                sensorLogRepository.findHourlyPowerProjection(zoneId, start, end, deviceId);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        List<PowerHourlyResponse.HourlyLog> logs = projections.stream()
                .map(p -> PowerHourlyResponse.HourlyLog.builder()
                        .id(p.getId())
                        .deviceId(p.getDeviceId())
                        .deviceName(p.getDeviceName())
                        .hourAt(LocalDateTime.parse(p.getHourAt(), fmt))
                        .kwh(p.getKwh())
                        .avgWatt(p.getAvgWatt())
                        .peakWatt(p.getPeakWatt())
                        .build())
                .toList();

        return PowerHourlyResponse.builder()
                .zoneId(zone.getZoneId())
                .zoneName(zone.getZoneName())
                .logs(logs)
                .build();
    }

    @Transactional(readOnly = true)
    public PowerBillingAllResponse getAllZonesBilling(Integer year, Integer month) {
        int y = year  != null ? year  : LocalDate.now().getYear();
        int m = month != null ? month : LocalDate.now().getMonthValue();
        List<PowerBilling> records = powerBillingRepository.findAllByYearAndMonthWithZone(y, m);
        return PowerBillingAllResponse.from(y, m, records);
    }

    @Transactional(readOnly = true)
    public PowerBillingZoneResponse getZoneBillingHistory(Long zoneId, Integer year, Integer month) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new CustomException(ErrorCode.ZONE_NOT_FOUND));
        List<PowerBilling> records = powerBillingRepository.findByZoneIdWithFilters(zoneId, year, month);
        return PowerBillingZoneResponse.from(zoneId, zone.getZoneName(), records);
    }

    @Transactional
    public PowerBillingCalculateResponse calculateBilling(PowerBillingCalculateRequest request) {
        int year  = request.getYear();
        int month = request.getMonth();

        List<MonthlyPowerProjection> kwhList = fetchMonthlyKwh(year, month, request.getZoneIds());

        if (kwhList.isEmpty()) {
            throw new CustomException(ErrorCode.POWER_DATA_NOT_FOUND);
        }

        Map<Long, Zone> zoneMap = zoneRepository.findAll().stream()
                .collect(Collectors.toMap(Zone::getZoneId, Function.identity()));

        int totalCount   = kwhList.size();
        int successCount = 0;
        int skipCount    = 0;
        int totalFee     = 0;

        List<PowerBilling> toSave = new ArrayList<>();

        for (MonthlyPowerProjection proj : kwhList) {
            Long zoneId = proj.getZoneId();
            Zone zone   = zoneMap.get(zoneId);
            if (zone == null) {
                skipCount++;
                continue;
            }

            BigDecimal totalKwh = proj.getTotalKwh();
            int powerFee = totalKwh.multiply(BigDecimal.valueOf(request.getUnitPrice())).intValue();
            int fee      = powerFee + request.getBaseFee();

            Optional<PowerBilling> existing =
                    powerBillingRepository.findByZone_ZoneIdAndBillingYearAndBillingMonth(zoneId, year, month);

            if (existing.isPresent()) {
                existing.get().recalculate(totalKwh, request.getUnitPrice(), request.getBaseFee(), powerFee, fee);
                toSave.add(existing.get());
            } else {
                toSave.add(PowerBilling.builder()
                        .zone(zone)
                        .billingYear(year)
                        .billingMonth(month)
                        .totalKwh(totalKwh)
                        .unitPrice(request.getUnitPrice())
                        .baseFee(request.getBaseFee())
                        .powerFee(powerFee)
                        .totalFee(fee)
                        .calculatedAt(LocalDateTime.now())
                        .build());
            }
            totalFee += fee;
            successCount++;
        }

        powerBillingRepository.saveAll(toSave);

        return PowerBillingCalculateResponse.builder()
                .year(year)
                .month(month)
                .totalCount(totalCount)
                .successCount(successCount)
                .skipCount(skipCount)
                .totalFee(totalFee)
                .build();
    }

    private List<MonthlyPowerProjection> fetchMonthlyKwh(int year, int month, List<Long> zoneIds) {
        if (zoneIds == null || zoneIds.isEmpty()) {
            return sensorLogRepository.findMonthlyKwhAllZones(year, month);
        }
        return sensorLogRepository.findMonthlyKwhByZones(year, month, zoneIds);
    }
}
