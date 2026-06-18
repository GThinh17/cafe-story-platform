import type {
  ContentReportRequest,
  ContentReportResponse,
  ReportReasonResponse,
  ReportTargetType,
} from "../../types";
import { apiCacheTtl, cachedApiCall } from "./api-cache";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

export function getReportReasons(targetType?: ReportTargetType) {
  const path = apiEndpoints.reportReasons.list(targetType);
  return cachedApiCall(`report-reasons:${targetType ?? "all"}`, apiCacheTtl.reportReasons, () =>
    apiFetch<ReportReasonResponse[]>(path, {
      method: "GET",
    }),
  );
}

export function createContentReport(request: ContentReportRequest) {
  return apiFetch<ContentReportResponse>(apiEndpoints.reports.list, {
    body: request,
    method: "POST",
  });
}
