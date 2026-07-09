export type ReportTargetType = "BLOG" | "COMMENT" | "USER" | "CAFE_PAGE";

export type ReportReasonResponse = {
  code: string;
  createdAt: string | null;
  descriptionVi: string | null;
  id: string;
  isActive: boolean | null;
  labelVi: string;
  requiresDescription: boolean | null;
  severity: number | null;
  sortOrder: number | null;
  targetType: ReportTargetType | null;
  updatedAt: string | null;
};

export type ContentReportRequest = {
  description?: string;
  reasonId: string;
  targetId: string;
  targetType: ReportTargetType;
};

export type ContentReportResponse = {
  blogId: string | null;
  cafePageId: string | null;
  commentId: string | null;
  createdAt: string | null;
  description: string | null;
  id: string;
  reason: string | null;
  reasonCode: string | null;
  reasonId: string;
  reasonLabel: string | null;
  reasonSeverity: number | null;
  reportedUserId: string | null;
  reporterUserId: string;
  reporterUserName: string | null;
  resolvedAt: string | null;
  status: string | null;
  targetId: string;
  targetType: ReportTargetType;
};
