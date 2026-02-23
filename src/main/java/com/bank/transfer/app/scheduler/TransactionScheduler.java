package com.bank.transfer.app.scheduler;


import com.bank.transfer.app.service.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Scheduled jobs for the bank transfer service.
 *
 * Shedlock ensures only ONE instance runs the job in a Kubernetes cluster.
 * Both jobs run at different times to avoid resource contention.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionScheduler {

    private final TransferService transferService;

    /**
     * Commission processing job — runs at 1:00 AM daily.
     * Marks successful transactions as commission-worthy and computes commission value.
     */
    @Scheduled(cron = "${app.scheduler.commission-cron}")
    @SchedulerLock(name = "commissionProcessingJob",
            lockAtMostFor = "PT20M",
            lockAtLeastFor = "PT5M")
    public void processCommissions() {
        log.info("Commission job triggered at {}", LocalDate.now());
        try {
            transferService.processCommissions();
        } catch (Exception e) {
            log.error("Commission processing job failed", e);
        }
    }

    /**
     * Daily summary job — runs at 1:30 AM daily.
     * Aggregates previous day's transaction statistics.
     */
    @Scheduled(cron = "${app.scheduler.summary-cron}")
    @SchedulerLock(name = "dailySummaryJob",
            lockAtMostFor = "PT20M",
            lockAtLeastFor = "PT5M")
    public void generatePreviousDaySummary() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Daily summary job triggered for date: {}", yesterday);
        try {
            transferService.generateDailySummary(yesterday);
        } catch (Exception e) {
            log.error("Daily summary job failed for {}", yesterday, e);
        }
    }
}
