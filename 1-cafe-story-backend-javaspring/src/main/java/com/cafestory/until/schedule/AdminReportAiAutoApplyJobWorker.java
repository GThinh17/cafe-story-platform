package com.cafestory.until.schedule;

import com.cafestory.service.serviceInterface.AdminReportAiAutoApplyJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class AdminReportAiAutoApplyJobWorker {

    private static final Logger log = LoggerFactory.getLogger(AdminReportAiAutoApplyJobWorker.class);

    private final AdminReportAiAutoApplyJobService autoApplyJobService;
    private final int batchSize;

    public AdminReportAiAutoApplyJobWorker(
            AdminReportAiAutoApplyJobService autoApplyJobService,
            @Value("${admin.report.ai.auto-apply.worker.batch-size:10}") int batchSize) {
        this.autoApplyJobService = autoApplyJobService;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 15000)
    public void processDueJobs() {
        int processed = autoApplyJobService.processDueJobs(batchSize);
        if (processed > 0) {
            log.info("AdminReportAiAutoApplyJobWorker: processed {} auto apply job(s)", processed);
        }
    }
}
