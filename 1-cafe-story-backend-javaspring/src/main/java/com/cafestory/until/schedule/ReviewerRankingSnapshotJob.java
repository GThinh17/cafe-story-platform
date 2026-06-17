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

    // Daily at 2:00 AM — recalculate daily snapshot AND update current month's ranking
    @Scheduled(cron = "0 0 2 * * *")
    public void generateDailySnapshot() {
        snapshotService.generateSnapshot(RankingPeriodType.DAILY);
        snapshotService.generateSnapshot(RankingPeriodType.MONTHLY);
    }

    // Weekly on Monday at 3:00 AM — recalculate weekly snapshot
    @Scheduled(cron = "0 0 3 * * MON")
    public void generateWeeklySnapshot() {
        snapshotService.generateSnapshot(RankingPeriodType.WEEKLY);
    }
}
