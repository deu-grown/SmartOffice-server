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
import com.grown.smartoffice.domain.device.entity.DeviceStatus;
import com.grown.smartoffice.domain.device.repository.DeviceRepository;
import com.grown.smartoffice.domain.reservation.entity.ReservationStatus;
import com.grown.smartoffice.domain.reservation.repository.ReservationRepository;
import com.grown.smartoffice.domain.sensor.entity.SensorLog;
import com.grown.smartoffice.domain.sensor.repository.SensorLogRepository;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserRole;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import com.grown.smartoffice.domain.zone.entity.Zone;
import com.grown.smartoffice.domain.zone.entity.ZoneType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock UserRepository userRepository;
    @Mock DeviceRepository deviceRepository;
    @Mock AttendanceRepository attendanceRepository;
    @Mock AccessLogRepository accessLogRepository;
    @Mock SensorLogRepository sensorLogRepository;
    @Mock ReservationRepository reservationRepository;
    @InjectMocks DashboardService dashboardService;

    private Zone zone;
    private User user;

    @BeforeEach
    void setUp() {
        zone = Zone.builder().zoneName("개발팀").zoneType(ZoneType.AREA).build();
        ReflectionTestUtils.setField(zone, "zoneId", 5L);

        user = User.builder()
                .employeeNumber("EMP-D").employeeName("대시").employeeEmail("d@grown.com")
                .password("p").role(UserRole.USER).position("사원")
                .status(UserStatus.ACTIVE).hiredAt(LocalDate.now()).build();
        ReflectionTestUtils.setField(user, "userId", 1L);
    }

    @Test
    @DisplayName("getSummary — 총 직원/장치/오늘 예약 집계 정상")
    void getSummary() {
        given(userRepository.countByStatus(UserStatus.ACTIVE)).willReturn(12L);
        given(deviceRepository.countByDeviceStatus(DeviceStatus.ACTIVE)).willReturn(15L);
        given(reservationRepository.countTodayConfirmed(eq(ReservationStatus.CONFIRMED), any(), any()))
                .willReturn(5L);

        DashboardSummaryResponse res = dashboardService.getSummary();

        assertThat(res.getTotalUsers()).isEqualTo(12);
        assertThat(res.getActiveDevices()).isEqualTo(15);
        assertThat(res.getTodayReservations()).isEqualTo(5);
        assertThat(res.getPendingApprovals()).isZero();
    }

    @Test
    @DisplayName("getSummary — 데이터 없음 케이스 (모든 카운트 0L → 4 필드 모두 0)")
    void getSummary_emptyData() {
        given(userRepository.countByStatus(UserStatus.ACTIVE)).willReturn(0L);
        given(deviceRepository.countByDeviceStatus(DeviceStatus.ACTIVE)).willReturn(0L);
        given(reservationRepository.countTodayConfirmed(eq(ReservationStatus.CONFIRMED), any(), any()))
                .willReturn(0L);

        DashboardSummaryResponse res = dashboardService.getSummary();

        assertThat(res.getTotalUsers()).isZero();
        assertThat(res.getActiveDevices()).isZero();
        assertThat(res.getTodayReservations()).isZero();
        assertThat(res.getPendingApprovals()).isZero();
    }

    @Test
    @DisplayName("getCurrentSensors — zone별 TEMP/HUMI/CO2 추출, 최신 시각 반영")
    void getCurrentSensors() {
        SensorLog temp = SensorLog.builder().zone(zone).device(null).sensorType("TEMPERATURE")
                .sensorValue(new BigDecimal("23.50")).sensorUnit("°C")
                .loggedAt(LocalDateTime.of(2026, 5, 13, 12, 0)).build();
        SensorLog humi = SensorLog.builder().zone(zone).device(null).sensorType("HUMIDITY")
                .sensorValue(new BigDecimal("48.20")).sensorUnit("%")
                .loggedAt(LocalDateTime.of(2026, 5, 13, 13, 0)).build();
        given(sensorLogRepository.findLatestPerZoneAndType()).willReturn(List.of(temp, humi));

        List<SensorCurrentResponse> res = dashboardService.getCurrentSensors();

        assertThat(res).hasSize(1);
        SensorCurrentResponse z = res.get(0);
        assertThat(z.getZoneName()).isEqualTo("개발팀");
        assertThat(z.getTemp()).isEqualByComparingTo("23.50");
        assertThat(z.getHumi()).isEqualByComparingTo("48.20");
        assertThat(z.getCo2()).isNull();   // CO2 데이터 없음
        assertThat(z.getUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 5, 13, 13, 0));
    }

    @Test
    @DisplayName("getCurrentSensors — 빈 결과")
    void getCurrentSensors_empty() {
        given(sensorLogRepository.findLatestPerZoneAndType()).willReturn(List.of());
        assertThat(dashboardService.getCurrentSensors()).isEmpty();
    }

    @Test
    @DisplayName("getTodayAttendance — NORMAL+LATE=present, LATE=late, ABSENT=absent")
    void getTodayAttendance() {
        Attendance normal = attendance(AttendanceStatus.NORMAL);
        Attendance late   = attendance(AttendanceStatus.LATE);
        Attendance absent = attendance(AttendanceStatus.ABSENT);
        given(attendanceRepository.findAllByWorkDate(any())).willReturn(List.of(normal, late, absent));
        given(userRepository.countByStatus(UserStatus.ACTIVE)).willReturn(10L);

        AttendanceTodayResponse res = dashboardService.getTodayAttendance();
        assertThat(res.getTotalExpected()).isEqualTo(10);
        assertThat(res.getPresentCount()).isEqualTo(2);   // NORMAL + LATE
        assertThat(res.getAbsentCount()).isEqualTo(1);
        assertThat(res.getLateCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("getRecentAccess — type 빈 문자열은 null로 전달, limit 페이지 적용")
    void getRecentAccess_blankTypeBecomesNull() {
        given(accessLogRepository.findRecentWithUserAndZone(eq(null), eq(PageRequest.of(0, 5))))
                .willReturn(List.<AccessLog>of());

        List<RecentAccessResponse> res = dashboardService.getRecentAccess(5, "");
        assertThat(res).isEmpty();
    }

    @Test
    @DisplayName("getRecentAccess — type 대문자로 정규화")
    void getRecentAccess_upperCases() {
        given(accessLogRepository.findRecentWithUserAndZone(eq("IN"), any()))
                .willReturn(List.<AccessLog>of());

        List<RecentAccessResponse> res = dashboardService.getRecentAccess(20, "in");
        assertThat(res).isEmpty();
    }

    private Attendance attendance(AttendanceStatus status) {
        Attendance a = Attendance.builder().user(user)
                .workDate(LocalDate.now())
                .checkIn(LocalDateTime.now())
                .attendanceStatus(status).build();
        return a;
    }
}
