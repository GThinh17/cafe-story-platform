package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.AdminPayoutStatus;
import com.cafestory.entity.enums.ReviewerBadge;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Thu nhập theo tháng mà chính reviewer đọc được.
 *
 * <p>Ghép hai nguồn: admin_payout cho số tiền chốt (base, hệ số badge, final,
 * trạng thái duyệt/chi) và reviewer_income cho phần bóc tách theo loại tương
 * tác. Số tiền từng loại được suy ra từ count nhân đơn giá của công thức đang
 * active, nên tổng ba khoản bằng totalBaseAmount chứ chưa nhân hệ số badge.
 */
@Data
public class ReviewerEarningsResponseDTO {

    private UUID id;

    private UUID reviewerId;

    private String payoutMonth;

    private long likeCount;

    private long shareCount;

    private long commentCount;

    private long likeAmount;

    private long shareAmount;

    private long commentAmount;

    private long totalBaseAmount;

    private ReviewerBadge badge;

    private BigDecimal badgeMultiplier;

    private long totalFinalAmount;

    private AdminPayoutStatus status;

    private LocalDateTime paidAt;
}
