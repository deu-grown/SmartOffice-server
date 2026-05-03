package com.grown.smartoffice.domain.dashboard.service;

import com.grown.smartoffice.domain.accesslog.entity.AccessLog;
import com.grown.smartoffice.domain.accesslog.repository.AccessLogRepository;
import com.grown.smartoffice.domain.attendance.entity.Attendance;
import com.grown.smartoffice.domain.attendance.entity.AttendanceStatus;
import com.grown.smartoffice.domain.attendance.repository.AttendanceRepository;
import com.grown.smartoffice.domain.dashboard.dto.AttendanceTodayResponse;
import com.grown.smartoffice.domain.dashboard.dto.DashboardSummaryResponse;
import com.grown.smartoffice.domain.dashboard.dto.RecentAccessResponse;
import com.grown.smartoffice.domain.dashboard.dto.SensorCurrentResponse;
import com.grown.smartoffice.domain.device.repository.DeviceRepository;
import com.grown.smartoffice.domain.sensor.entity.SensorLog;
import com.grown.smartoffice.domain.sensor.repository.SensorLogRepository;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final AttendanceRepository attendanceRepository;
    private final AccessLogRepository accessLogRepository;
    private final SensorLogRepository sensorLogRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        int totalUsers    = (int) userRepository.countByStatus(UserStatus.ACTIVE);
        int activeDevices = (int) deviceRepository.countByDeviceStatus("ACTIVE");
        return DashboardSummaryResponse.builder()
                .totalUsers(totalUsers)
                .todayReservations(0)
                .activeDevices(activeDevices)
                .pendingApprovals(0)
                .build();
    }

    @Transactional(readOnly = true)
    public List<SensorCurrentResponse> getCurrentSensors() {
        List<SensorLog> logs = sensorLogRepository.findLatestPerZoneAndType();

        Map<Long, List<SensorLog>> byZone = logs.stream()
                .collect(Collectors.groupingBy(l -> l.getZone().getZoneId()));

        return byZone.entrySet().stream()
                .map(entry -> {
                    List<SensorLog> zoneLogs = entry.getValue();
                    String zoneName = zoneLogs.get(0).getZone().getZoneName();
                    BigDecimal temp = extractValue(zoneLogs, "TEMPERATURE");
                    BigDecimal humi = extractValue(zoneLogs, "HUMIDITY");
                    BigDecimal co2  = extractValue(zoneLogs, "CO2");
                    LocalDateTime updatedAt = zoneLogs.stream()
                            .map(SensorLog::getLoggedAt)
                            .max(Comparator.naturalOrder())
                            .orElse(null);
                    return SensorCurrentResponse.builder()
                            .zoneId(entry.getKey())
                            .zoneName(zoneName)
                            .temp(temp)
                            .humi(humi)
                            .co2(co2)
                            .updatedAt(updatedAt)
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public AttendanceTodayResponse getTodayAttendance() {
        List<Attendance> list = attendanceRepository.findAllByWorkDate(LocalDate.now());
        int totalExpected = (int) userRepository.countByStatus(UserStatus.ACTIVE);
        int presentCount  = (int) list.stream()
                .filter(a -> a.getAttendanceStatus() == AttendanceStatus.NORMAL
                          || a.getAttendanceStatus() == AttendanceStatus.LATE)
                .count();
        int absentCount = (int) list.stream()
                .filter(a -> a.getAttendanceStatus() == AttendanceStatus.ABSENT)
                .count();
        int lateCount = (int) list.stream()
                .filter(a -> a.getAttendanceStatus() == AttendanceStatus.LATE)
                .count();
        return AttendanceTodayResponse.builder()
                .presentCount(presentCount)
                .absentCount(absentCount)
                .lateCount(lateCount)
                .totalExpected(totalExpected)
                .build();
    }

    @Transactional(readOnly = true)
    public List<RecentAccessResponse> getRecentAccess(int limit, String type) {
        String direction = (type != null && !type.isBlank()) ? type.toUpperCase() : null;
        List<AccessLog> logs = accessLogRepository.findRecentWithUserAndZone(
                direction, PageRequest.of(0, limit));
        return logs.stream().map(RecentAccessResponse::from).toList();
    }

    private BigDecimal extractValue(List<SensorLog> logs, String type) {
        return logs.stream()
                .filter(l -> type.equals(l.getSensorType()))
                .map(SensorLog::getSensorValue)
                .findFirst()
                .orElse(null);
    }
}
