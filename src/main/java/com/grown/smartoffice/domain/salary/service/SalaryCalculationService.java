package com.grown.smartoffice.domain.salary.service;

import com.grown.smartoffice.domain.attendance.entity.MonthlyAttendance;
import com.grown.smartoffice.domain.attendance.repository.MonthlyAttendanceRepository;
import com.grown.smartoffice.domain.salary.dto.SalaryCalculateRequest;
import com.grown.smartoffice.domain.salary.dto.SalaryCalculateResponse;
import com.grown.smartoffice.domain.salary.dto.SalaryRecordResponse;
import com.grown.smartoffice.domain.salary.entity.SalaryRecord;
import com.grown.smartoffice.domain.salary.entity.SalaryStatus;
import com.grown.smartoffice.domain.salary.entity.SalarySetting;
import com.grown.smartoffice.domain.salary.repository.SalaryRecordRepository;
import com.grown.smartoffice.domain.salary.repository.SalarySettingRepository;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryCalculationService {

    private static final int STANDARD_MONTHLY_HOURS = 160;

    private final UserRepository userRepository;
    private final MonthlyAttendanceRepository monthlyAttendanceRepository;
    private final SalarySettingRepository salarySettingRepository;
    private final SalaryRecordRepository salaryRecordRepository;

    @Transactional
    public SalaryCalculateResponse calculate(SalaryCalculateRequest request) {
        int year = request.getYear();
        int month = request.getMonth();
        LocalDate lastDayOfMonth = LocalDate.of(year, month, 1).withDayOfMonth(
                LocalDate.of(year, month, 1).lengthOfMonth());

        List<User> targets = resolveTargets(request.getUserIds());
        int totalCount = targets.size();
        int successCount = 0;
        int skipCount = 0;
        List<SalaryRecordResponse> records = new ArrayList<>();

        for (User user : targets) {
            Optional<MonthlyAttendance> monatOpt =
                    monthlyAttendanceRepository.findByUser_UserIdAndMonatYearAndMonatMonth(
                            user.getUserId(), year, month);

            if (monatOpt.isEmpty()) {
                log.debug("[SalaryCalc] 월 근태 없음 — user={}, {}-{}", user.getUserId(), year, month);
                skipCount++;
                continue;
            }

            Optional<SalarySetting> settingOpt =
                    salarySettingRepository.findApplicableByPositionAndDate(user.getPosition(), lastDayOfMonth);

            if (settingOpt.isEmpty()) {
                log.debug("[SalaryCalc] 급여 기준 없음 — user={}, position={}", user.getUserId(), user.getPosition());
                skipCount++;
                continue;
            }

            MonthlyAttendance monat = monatOpt.get();
            SalarySetting setting = settingOpt.get();

            int[] pay = computePay(setting, monat.getMonatOvertimeMinutes());

            Optional<SalaryRecord> existing =
                    salaryRecordRepository.findByUser_UserIdAndSalrecYearAndSalrecMonth(user.getUserId(), year, month);

            SalaryRecord record;
            if (existing.isPresent()) {
                if (existing.get().getSalrecStatus() == SalaryStatus.CONFIRMED) {
                    skipCount++;
                    continue;
                }
                existing.get().overwrite(monat, setting, pay[0], pay[1], pay[2]);
                record = existing.get();
            } else {
                record = SalaryRecord.builder()
                        .user(user)
                        .monthlyAttendance(monat)
                        .salarySetting(setting)
                        .salrecYear(year)
                        .salrecMonth(month)
                        .salrecBaseSalary(pay[0])
                        .overtimePay(pay[1])
                        .totalPay(pay[2])
                        .build();
                salaryRecordRepository.save(record);
            }

            records.add(SalaryRecordResponse.from(record));
            successCount++;
        }

        return SalaryCalculateResponse.builder()
                .totalCount(totalCount)
                .successCount(successCount)
                .skipCount(skipCount)
                .records(records)
                .build();
    }

    private List<User> resolveTargets(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return userRepository.findAll().stream()
                    .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                    .toList();
        }
        return userRepository.findAllById(userIds);
    }

    private int[] computePay(SalarySetting setting, int overtimeMinutes) {
        int baseSalary = setting.getBaseSalary();
        BigDecimal hourlyRate = BigDecimal.valueOf(baseSalary).divide(BigDecimal.valueOf(STANDARD_MONTHLY_HOURS), 2, RoundingMode.HALF_UP);
        BigDecimal overtimeHours = BigDecimal.valueOf(overtimeMinutes).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        int overtimePay = overtimeHours.multiply(hourlyRate).multiply(setting.getOvertimeRate()).setScale(0, RoundingMode.HALF_UP).intValue();
        int totalPay = baseSalary + overtimePay;
        return new int[]{baseSalary, overtimePay, totalPay};
    }
}
