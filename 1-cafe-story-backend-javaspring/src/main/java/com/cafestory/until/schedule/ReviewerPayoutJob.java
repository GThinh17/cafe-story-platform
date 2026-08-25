package com.cafestory.until.schedule;

import com.cafestory.service.serviceInterface.AdminPayoutService;
import com.cafestory.service.serviceInterface.ReviewerIncomeService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Chuỗi job sinh thu nhập reviewer. Thứ tự chạy là ràng buộc thật, không phải
 * tình cờ — đổi giờ một job mà không xét cả chuỗi sẽ làm badge tụt về IRON:
 *
 * <pre>
 *   02:00 hằng ngày   ReviewerRankingSnapshotJob  snapshot DAILY của hôm qua
 *   02:00 ngày 1      ReviewerRankingSnapshotJob  snapshot MONTHLY tháng trước
 *                                                 + reviewer_badge_history
 *   02:30 thứ 2       ReviewerRankingSnapshotJob  snapshot WEEKLY tuần trước
 *   03:00 hằng ngày   ReviewerPayoutJob           reviewer_income của hôm qua
 *                                                 (đọc snapshot DAILY để lấy badge)
 *   04:00 ngày 1      ReviewerPayoutJob           admin_payout tháng trước
 *                                                 (đọc snapshot MONTHLY + gom
 *                                                  reviewer_income cả tháng)
 * </pre>
 *
 * Payout tháng còn tự backfill những ngày thiếu reviewer_income, nên nếu job
 * 03:00 lỡ một hôm thì số tháng vẫn đúng.
 */
@Component
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class ReviewerPayoutJob {

    private final ReviewerIncomeService incomeService;
    private final AdminPayoutService payoutService;

    public ReviewerPayoutJob(ReviewerIncomeService incomeService,
                             AdminPayoutService payoutService) {
        this.incomeService = incomeService;
        this.payoutService = payoutService;
    }

    // Daily at 3:00 AM — generate income for yesterday (full 24h window)
    @Scheduled(cron = "0 0 3 * * *")
    public void generateDailyIncome() {
        incomeService.generateDailyIncome(LocalDate.now().minusDays(1));
    }

    // 1st of each month at 4:00 AM — generate payout for previous month
    // Runs after: monthly ranking snapshot (2AM) and last-day income (3AM)
    @Scheduled(cron = "0 0 4 1 * *")
    public void generateMonthlyPayout() {
        String previousMonth = YearMonth.now().minusMonths(1).toString();
        payoutService.generateMonthlyPayout(previousMonth);
    }
}
