package com.cafestory.config;

import com.cafestory.entity.ReportReason;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.repository.ReportReasonRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.data-initializer.enabled", havingValue = "true", matchIfMissing = true)
public class ReportReasonDataInitializer implements CommandLineRunner {

    private final ReportReasonRepository reportReasonRepository;

    public ReportReasonDataInitializer(ReportReasonRepository reportReasonRepository) {
        this.reportReasonRepository = reportReasonRepository;
    }

    @Override
    public void run(String... args) {
        createReasonIfMissing("DISLIKE_CONTENT", "Chỉ là tôi không thích nội dung này", null, 1, false, 10);
        createReasonIfMissing("BULLYING_OR_UNWANTED_CONTACT", "Bắt nạt hoặc liên hệ theo cách không mong muốn", null, 3, false, 20);
        createReasonIfMissing("SELF_HARM_OR_ABNORMAL_EATING", "Tự tử, tự gây thương tích hoặc ăn uống thất thường", null, 5, true, 30);
        createReasonIfMissing("VIOLENCE_HATE_OR_EXPLOITATION", "Bạo lực, thù ghét hoặc bóc lột", null, 5, true, 40);
        createReasonIfMissing("RESTRICTED_GOODS", "Bán hoặc quảng bá mặt hàng bị hạn chế", null, 4, false, 50);
        createReasonIfMissing("NUDITY_OR_SEXUAL_ACTIVITY", "Ảnh khỏa thân hoặc hoạt động tình dục", null, 5, true, 60);
        createReasonIfMissing("SCAM_FRAUD_OR_SPAM", "Lừa đảo, gian lận hoặc spam", null, 4, false, 70);
        createReasonIfMissing("FALSE_INFORMATION", "Thông tin sai sự thật", ReportTargetType.BLOG, 3, false, 80);
        createReasonIfMissing("INTELLECTUAL_PROPERTY", "Quyền sở hữu trí tuệ", null, 3, true, 90);
    }

    private void createReasonIfMissing(
            String code,
            String labelVi,
            ReportTargetType targetType,
            int severity,
            boolean requiresDescription,
            int sortOrder) {
        if (reportReasonRepository.findByCode(code).isPresent()) {
            return;
        }
        ReportReason reason = new ReportReason();
        reason.setCode(code);
        reason.setLabelVi(labelVi);
        reason.setTargetType(targetType);
        reason.setSeverity(severity);
        reason.setRequiresDescription(requiresDescription);
        reason.setActive(true);
        reason.setSortOrder(sortOrder);
        reportReasonRepository.save(reason);
    }
}
