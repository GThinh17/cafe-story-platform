package com.cafestory.until.schedule;

import com.cafestory.service.serviceInterface.AdminPayoutService;
import com.cafestory.service.serviceInterface.ReviewerIncomeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;

@Component
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
