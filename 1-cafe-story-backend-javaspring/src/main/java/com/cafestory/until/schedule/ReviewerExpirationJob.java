package com.cafestory.until.schedule;

import com.cafestory.entity.Reviewer;
import com.cafestory.repository.ReviewerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Hạ cờ reviewer_active khi gói đã quá hạn.
 *
 * <p>Trước đây không chỗ nào trong hệ thống gọi setReviewerActive(false):
 * PaymentServiceImpl bật cờ lúc thanh toán thành công rồi thôi. Hệ quả là gói
 * reviewer trở thành vĩnh viễn — người đã hết hạn vẫn onboard được Stripe, và
 * số "tổng reviewer đang hoạt động" trên dashboard admin luôn phình lên.
 *
 * <p>Chỉ hạ cờ, KHÔNG thu hồi ROLE_REVIEWER: người dùng vẫn cần vào dashboard
 * xem lịch sử thu nhập các tháng trước và gia hạn.
 */
@Component
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class ReviewerExpirationJob {

    private static final Logger log = LoggerFactory.getLogger(ReviewerExpirationJob.class);

    private final ReviewerRepository reviewerRepository;

    public ReviewerExpirationJob(ReviewerRepository reviewerRepository) {
        this.reviewerRepository = reviewerRepository;
    }

    // 01:00 hằng ngày — chạy trước chuỗi snapshot 02:00 / income 03:00 để các
    // job đó nhìn thấy trạng thái đã cập nhật.
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void expireLapsedReviewers() {
        List<Reviewer> lapsed = reviewerRepository
                .findByReviewerActiveTrueAndReviewerExpiresAtBefore(LocalDateTime.now());

        if (lapsed.isEmpty()) {
            return;
        }

        for (Reviewer reviewer : lapsed) {
            reviewer.setReviewerActive(false);
        }
        reviewerRepository.saveAll(lapsed);
        log.info("ReviewerExpirationJob: hạ cờ active cho {} reviewer hết hạn", lapsed.size());
    }
}
