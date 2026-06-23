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

    // Weekly on Monday at 3:00 AM — snapshot for previous week (full week data)
    @Scheduled(cron = "0 0 3 * * MON")
    public void generateWeeklySnapshot() {
        snapshotService.generateSnapshot(RankingPeriodType.WEEKLY);
    }
}
