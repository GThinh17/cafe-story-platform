package com.cafestory.until.schedule;

import com.cafestory.entity.enums.RankingPeriodType;
import com.cafestory.service.serviceInterface.ReviewerRankingSnapshotService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class ReviewerRankingSnapshotJob {

    private final ReviewerRankingSnapshotService snapshotService;

    public ReviewerRankingSnapshotJob(ReviewerRankingSnapshotService snapshotService) {
        this.snapshotService = snapshotService;
    }

    // Daily at 2:00 AM — snapshot for yesterday (full day data)
    @Scheduled(cron = "0 0 2 * * *")
    public void generateDailySnapshot() {
        snapshotService.generateSnapshot(RankingPeriodType.DAILY, LocalDate.now().minusDays(1));
    }

    // 1st of each month at 2:00 AM — snapshot for previous month (full month data)
    @Scheduled(cron = "0 0 2 1 * *")
    public void generateMonthlySnapshot() {
        snapshotService.generateSnapshot(RankingPeriodType.MONTHLY, LocalDate.now().minusDays(1));
    }

    // Weekly on Monday at 2:30 AM — snapshot for previous week (full week data).
    //
    // Reference date must be yesterday (Sunday), same as the daily/monthly jobs:
    // resolveDateRange(WEEKLY) does date.with(MONDAY), so passing today (Monday)
    // would snapshot the week that started two hours ago instead of the one that
    // just closed. 2:30 also keeps this clear of the 3:00 daily income job.
    @Scheduled(cron = "0 30 2 * * MON")
    public void generateWeeklySnapshot() {
        snapshotService.generateSnapshot(RankingPeriodType.WEEKLY, LocalDate.now().minusDays(1));
    }
}
