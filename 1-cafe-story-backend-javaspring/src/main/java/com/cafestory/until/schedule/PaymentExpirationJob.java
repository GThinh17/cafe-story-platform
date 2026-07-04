package com.cafestory.until.schedule;

import com.cafestory.service.serviceInterface.AdminPaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class PaymentExpirationJob {

    private static final Logger log = LoggerFactory.getLogger(PaymentExpirationJob.class);

    private final AdminPaymentService adminPaymentService;

    public PaymentExpirationJob(AdminPaymentService adminPaymentService) {
        this.adminPaymentService = adminPaymentService;
    }

    @Scheduled(fixedRate = 60000)
    public void expireStalePayments() {
        int count = adminPaymentService.expireStalePayments();
        if (count > 0) {
            log.info("PaymentExpirationJob: expired {} stale payment(s)", count);
        }
    }
}
