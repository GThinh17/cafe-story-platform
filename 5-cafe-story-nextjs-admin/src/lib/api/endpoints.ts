function pathId(id: string) {
  return encodeURIComponent(id);
}

export const apiEndpoints = {
  auth: {
    login: "/api/auth/login",
    me: "/api/auth/me",
  },
  admin: {
    dashboardSummary: "/api/admin/dashboard/summary",
    users: "/api/admin/users",
    user: (userId: string) => `/api/admin/users/${pathId(userId)}`,
    userStatus: (userId: string) => `/api/admin/users/${pathId(userId)}/status`,
    userRoles: (userId: string) => `/api/admin/users/${pathId(userId)}/roles`,
    blogs: "/api/admin/blogs",
    blog: (blogId: string) => `/api/admin/blogs/${pathId(blogId)}`,
    blogStatus: (blogId: string) => `/api/admin/blogs/${pathId(blogId)}/status`,
    blogRankingOverride: (blogId: string) =>
      `/api/admin/blogs/${pathId(blogId)}/ranking-override`,
    cafePages: "/api/admin/cafe-pages",
    cafePage: (pageId: string) => `/api/admin/cafe-pages/${pathId(pageId)}`,
    cafePageStatus: (pageId: string) =>
      `/api/admin/cafe-pages/${pathId(pageId)}/status`,
    comments: "/api/admin/comments",
    comment: (commentId: string) => `/api/admin/comments/${pathId(commentId)}`,
    commentStatus: (commentId: string) =>
      `/api/admin/comments/${pathId(commentId)}/status`,
    moderationResults: "/api/admin/moderation/results",
    moderationQueue: "/api/admin/moderation/queue",
    moderationResult: (resultId: string) =>
      `/api/admin/moderation/results/${pathId(resultId)}`,
    moderationResolve: (resultId: string) =>
      `/api/admin/moderation/results/${pathId(resultId)}/resolve`,
    reports: "/api/admin/reports",
    report: (reportId: string) => `/api/admin/reports/${pathId(reportId)}`,
    reportStatus: (reportId: string) =>
      `/api/admin/reports/${pathId(reportId)}/status`,
    payments: "/api/admin/payments",
    payment: (paymentId: string) => `/api/admin/payments/${pathId(paymentId)}`,
    markBankTransferPaid: (paymentId: string) =>
      `/api/admin/payments/${pathId(paymentId)}/bank-transfer/mark-paid`,
    refundPayment: (paymentId: string) =>
      `/api/admin/payments/${pathId(paymentId)}/refund`,
    extraFees: "/api/admin/extra-fees",
    extraFee: (extraFeeId: string) =>
      `/api/admin/extra-fees/${pathId(extraFeeId)}`,
    extraFeeStatus: (extraFeeId: string) =>
      `/api/admin/extra-fees/${pathId(extraFeeId)}/status`,
    reviewerRanking: "/api/admin/reviewer-config/ranking",
    generateReviewerRanking: "/api/admin/reviewer-config/ranking/generate",
    formulas: "/api/admin/formulas",
    formulaActivate: (id: string) => `/api/admin/formulas/${pathId(id)}/activate`,
    formulaThresholds: (id: string) => `/api/admin/formulas/${pathId(id)}/thresholds`,
    payoutIncome: "/api/admin/payout/income",
    generatePayoutIncome: "/api/admin/payout/income/generate",
    payoutMonthly: "/api/admin/payout/monthly",
    generatePayoutMonthly: "/api/admin/payout/monthly/generate",
    payoutMonthlyStatus: (id: string) =>
      `/api/admin/payout/monthly/${pathId(id)}/status`,
  },
} as const;
