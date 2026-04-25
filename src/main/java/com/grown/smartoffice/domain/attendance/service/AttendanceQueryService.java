package com.grown.smartoffice.domain.attendance.service;

import com.grown.smartoffice.domain.attendance.dto.AttendanceDailyResponse;
import com.grown.smartoffice.domain.attendance.dto.AttendanceMonthlyResponse;
import com.grown.smartoffice.domain.attendance.entity.Attendance;
import com.grown.smartoffice.domain.attendance.entity.MonthlyAttendance;
import com.grown.smartoffice.domain.attendance.repository.AttendanceRepository;
import com.grown.smartoffice.domain.attendance.repository.MonthlyAttendanceRepository;
import com.grown.smartoffice.global.common.PageResponse;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class AttendanceQueryService {

    private final AttendanceRepository attendanceRepository;
    private final MonthlyAttendanceRepository monthlyAttendanceRepository;

    @Transactional(readOnly = true)
    public AttendanceDailyResponse getMyDaily(String email, LocalDate date) {
        return attendanceRepository.findAll().stream()
                .filter(a -> a.getUser().getEmployeeEmail().equals(email) && a.getWorkDate().equals(date))
                .findFirst()
                .map(AttendanceDailyResponse::from)
                .orElseThrow(() -> new CustomException(ErrorCode.ATTENDANCE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public AttendanceMonthlyResponse getMyMonthly(String email, YearMonth yearMonth) {
        MonthlyAttendance monthly = monthlyAttendanceRepository
                .findByEmailAndYearMonth(email, yearMonth.getYear(), yearMonth.getMonthValue())
                .orElseThrow(() -> new CustomException(ErrorCode.ATTENDANCE_NOT_FOUND));
        return AttendanceMonthlyResponse.from(monthly);
    }

    @Transactional(readOnly = true)
    public PageResponse<AttendanceDailyResponse> getAllDaily(LocalDate date, String name, Long deptId, int page, int size) {
        Page<Attendance> p = attendanceRepository.findAllByDateWithFilters(
                date, name, deptId, PageRequest.of(page, size));
        return new PageResponse<>(
                p.getContent().stream().map(AttendanceDailyResponse::from).toList(),
                p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages(), p.isLast());
    }
}
