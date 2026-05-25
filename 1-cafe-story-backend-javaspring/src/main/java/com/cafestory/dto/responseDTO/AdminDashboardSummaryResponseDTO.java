package com.cafestory.dto.responseDTO;

import lombok.Data;

@Data
public class AdminDashboardSummaryResponseDTO {

    private long totalUsers;

    private long activeUsers;

    private long totalCafePages;

    private long activeCafePages;

    private long totalBlogs;

    private long publishedBlogs;

    private long hiddenBlogs;

    private long totalComments;

    private long totalPayments;

    private long paidPayments;

    private long pendingPayments;

    private long failedPayments;

    private long totalReviewers;

    private long pendingModerationItems;
}
