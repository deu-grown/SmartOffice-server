package com.grown.smartoffice.domain.attendance.schedule;

import com.grown.smartoffice.domain.attendance.service.AttendanceBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceScheduler {

    private final AttendanceBatchService batchService;

    /** 매일 00:05 — 전일 근태 집계 */
    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
    public void dailyBatch() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("[Scheduler] 일별 근태 배치 시작: {}", yesterday);
        batchService.runDailyBatch(yesterday, "SYSTEM");
    }

    /** 매월 1일 00:10 — 전월 월별 집계 */
    @Scheduled(cron = "0 10 0 1 * *", zone = "Asia/Seoul")
    public void monthlyAggregation() {
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        log.info("[Scheduler] 월별 근태 배치 시작: {}-{}", lastMonth.getYear(), lastMonth.getMonthValue());
        batchService.runMonthlyAggregation(lastMonth.getYear(), lastMonth.getMonthValue());
    }
}
