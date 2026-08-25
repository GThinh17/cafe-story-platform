import type {
  ContentReportRequest,
  ContentReportResponse,
  ReportReasonResponse,
  ReportTargetType,
} from "@/types/report";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

export function getReportReasons(targetType?: ReportTargetType) {
  return apiFetch<ReportReasonResponse[]>(
    apiEndpoints.reportReasons.list(targetType),
    { method: "GET" },
  );
}

export function createContentReport(request: ContentReportRequest) {
  return apiFetch<ContentReportResponse>(apiEndpoints.reports.list, {
    body: request,
    method: "POST",
  });
}
