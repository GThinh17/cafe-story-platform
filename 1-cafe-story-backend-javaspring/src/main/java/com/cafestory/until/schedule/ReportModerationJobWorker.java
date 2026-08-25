package com.cafestory.until.schedule;

import com.cafestory.service.serviceInterface.ReportModerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class ReportModerationJobWorker {

    private static final Logger log = LoggerFactory.getLogger(ReportModerationJobWorker.class);

    private final ReportModerationService reportModerationService;
    private final int batchSize;

    public ReportModerationJobWorker(
            ReportModerationService reportModerationService,
            @Value("${report.moderation.worker.batch-size:10}") int batchSize) {
        this.reportModerationService = reportModerationService;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 15000)
    public void processDueJobs() {
        int processed = reportModerationService.processDueJobs(batchSize);
        if (processed > 0) {
            log.info("ReportModerationJobWorker: processed {} report moderation job(s)", processed);
        }
    }
}
