package com.cafestory.until.schedule;

import com.cafestory.entity.enums.RankingPeriodType;
import com.cafestory.service.serviceInterface.ReviewerRankingSnapshotService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReviewerRankingSnapshotJob {

    private final ReviewerRankingSnapshotService snapshotService;

    public ReviewerRankingSnapshotJob(ReviewerRankingSnapshotService snapshotService) {
        this.snapshotService = snapshotService;
    }

    // Daily at 2:00 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void generateDailySnapshot() {
        snapshotService.generateSnapshot(RankingPeriodType.DAILY);
    }

    // Weekly on Monday at 3:00 AM
    @Scheduled(cron = "0 0 3 * * MON")
    public void generateWeeklySnapshot() {
        snapshotService.generateSnapshot(RankingPeriodType.WEEKLY);
    }

    // Monthly on the 1st at 4:00 AM
    @Scheduled(cron = "0 0 4 1 * *")
    public void generateMonthlySnapshot() {
        snapshotService.generateSnapshot(RankingPeriodType.MONTHLY);
    }
}
