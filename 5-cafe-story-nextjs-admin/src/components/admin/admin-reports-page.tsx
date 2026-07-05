"use client";

import { useMemo, useState } from "react";
import {
  BotIcon,
  CheckCircle2Icon,
  EyeIcon,
  LoaderCircleIcon,
  SparklesIcon,
} from "lucide-react";
import { AdminConfirmDialog } from "@/components/admin/admin-confirm-dialog";
import { UserCell } from "@/components/admin/user-cell";
import {
  AdminDataTable,
  AdminPagination,
  type AdminTableColumn,
} from "@/components/admin/admin-data-table";
import {
  AdminDetailDialog,
  AdminDetailField,
  AdminDetailGrid,
} from "@/components/admin/admin-detail-dialog";
import { AdminPageHeader } from "@/components/admin/admin-page-header";
import { AdminStatusBadge } from "@/components/admin/admin-status-badge";
import {
  FilterSelect,
  formatDate,
  PAGE_SIZE,
  shortId,
  textPreview,
  Toolbar,
  useAdminDetailResource,
  usePagedAdminResource,
} from "@/components/admin/admin-page-utils";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import {
  createReportAiResolution,
  getAdminReport,
  getReportAiResolutions,
  getReports,
  resolveReport,
  updateReportStatus,
} from "@/lib/api/admin";
import type { PageResponse } from "@/types/api";
import type {
  AdminReportAiResolution,
  ContentReport,
  ReportStatus,
  ReportTargetType,
} from "@/types/admin";

const reportStatuses: ReportStatus[] = ["OPEN", "REVIEWING", "RESOLVED", "REJECTED"];
const targetTypes: ReportTargetType[] = ["BLOG", "COMMENT", "USER", "CAFE_PAGE"];
const AI_HISTORY_SIZE = 8;

const REPORT_STATUS_LABELS: Record<ReportStatus, string> = {
  OPEN: "Reopen",
  REVIEWING: "Mark reviewing",
  RESOLVED: "Resolve",
  REJECTED: "Reject",
};

type PendingReportAction =
  | { kind: "resolve"; report: ContentReport }
  | { kind: "status"; report: ContentReport; status: ReportStatus };

function reportReason(report: ContentReport) {
  return report.reasonLabel || report.reason || report.reasonCode || "Report";
}

function severityClassName(severity: number | null | undefined) {
  if (severity === null || severity === undefined) {
    return "bg-surface-muted text-coffee-muted";
  }

  if (severity >= 8) {
    return "bg-accent/10 text-accent";
  }

  if (severity >= 5) {
    return "bg-rating/10 text-rating";
  }

  return "bg-primary/10 text-primary-strong";
}

function scoreLabel(value: number | null | undefined) {
  return typeof value === "number" ? value.toFixed(1) : "-";
}

function resolutionSummary(resolution: AdminReportAiResolution) {
  return `${resolution.reportDecision} / ${resolution.targetAction}`;
}

function mergeAiHistory(
  current: AdminReportAiResolution[],
  created: AdminReportAiResolution,
) {
  return [created, ...current.filter((item) => item.id !== created.id)].slice(0, AI_HISTORY_SIZE);
}

export function AdminReportsPage() {
  const [status, setStatus] = useState<ReportStatus | "">("");
  const [targetType, setTargetType] = useState<ReportTargetType | "">("");
  const [pendingAction, setPendingAction] = useState<PendingReportAction | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [aiActionError, setAiActionError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [aiLoadingReportId, setAiLoadingReportId] = useState<string | null>(null);
  const [aiHistory, setAiHistory] = useState<AdminReportAiResolution[]>([]);
  const [aiHistoryPage, setAiHistoryPage] =
    useState<PageResponse<AdminReportAiResolution> | null>(null);
  const [aiHistoryLoading, setAiHistoryLoading] = useState(false);
  const [aiHistoryError, setAiHistoryError] = useState<string | null>(null);
  const detail = useAdminDetailResource<ContentReport>();

  const resource = usePagedAdminResource(
    (page, signal) =>
      getReports({ status, targetType, page, size: PAGE_SIZE }, signal),
    [status, targetType],
  );

  async function loadAiHistory(reportId: string, signal?: AbortSignal) {
    setAiHistoryLoading(true);
    setAiHistoryError(null);

    try {
      const response = await getReportAiResolutions(
        reportId,
        { page: 0, size: AI_HISTORY_SIZE },
        signal,
      );
      setAiHistoryPage(response);
      setAiHistory(response.content);
    } catch (requestError) {
      if (signal?.aborted) {
        return;
      }

      setAiHistory([]);
      setAiHistoryPage(null);
      setAiHistoryError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to load AI recommendations.",
      );
    } finally {
      if (!signal?.aborted) {
        setAiHistoryLoading(false);
      }
    }
  }

  function openReportDetail(report: ContentReport) {
    setAiActionError(null);
    setAiHistory([]);
    setAiHistoryPage(null);
    void detail.load((signal) => getAdminReport(report.id, signal));
    void loadAiHistory(report.id);
  }

  async function handleAskAi(report: ContentReport) {
    setAiLoadingReportId(report.id);
    setAiActionError(null);

    try {
      const createdResolution = await createReportAiResolution(report.id);

      if (detail.data?.id === report.id) {
        setAiHistory((current) => mergeAiHistory(current, createdResolution));
        await loadAiHistory(report.id);
      }
    } catch (requestError) {
      setAiActionError(
        requestError instanceof Error
          ? requestError.message
          : "AI recommendation failed.",
      );
    } finally {
      setAiLoadingReportId(null);
    }
  }

  function applyReportUpdate(updatedReport: ContentReport) {
    resource.updateRow(
      (report) => report.id === updatedReport.id,
      () => updatedReport,
    );

    if (detail.data?.id === updatedReport.id) {
      detail.setData(updatedReport);
    }
  }

  const columns = useMemo<AdminTableColumn<ContentReport>[]>(
    () => [
      {
        header: "Report",
        cell: (report) => (
          <div className="max-w-md">
            <div className="flex flex-wrap items-center gap-2">
              <p className="font-bold text-espresso">{reportReason(report)}</p>
              <Badge className={severityClassName(report.reasonSeverity)}>
                Severity {report.reasonSeverity ?? "-"}
              </Badge>
            </div>
            <p className="mt-1 text-sm leading-6 text-muted">
              {textPreview(report.description)}
            </p>
            {report.reasonCode ? (
              <p className="mt-1 font-mono text-xs text-muted">{report.reasonCode}</p>
            ) : null}
          </div>
        ),
      },
      {
        header: "Reporter",
        cell: (report) => (
          <UserCell
            name={report.reporterUserName}
            avatar={report.reporterUserAvatar}
            subtitle={shortId(report.reporterUserId)}
          />
        ),
      },
      {
        header: "Target",
        cell: (report) => (
          <div className="flex flex-col gap-1">
            <AdminStatusBadge value={report.targetType} />
            <span className="font-mono text-xs text-muted">{shortId(report.targetId)}</span>
          </div>
        ),
      },
      { header: "Status", cell: (report) => <AdminStatusBadge value={report.status} /> },
      { header: "Created", cell: (report) => formatDate(report.createdAt) },
      {
        header: "Actions",
        className: "w-72",
        cell: (report) => (
          <div className="flex flex-wrap gap-2">
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={(event) => {
                event.stopPropagation();
                openReportDetail(report);
              }}
            >
              <EyeIcon data-icon="inline-start" />
              View
            </Button>
            <Button
              type="button"
              variant="outline"
              size="sm"
              disabled={aiLoadingReportId === report.id}
              onClick={(event) => {
                event.stopPropagation();
                void handleAskAi(report);
              }}
            >
              {aiLoadingReportId === report.id ? (
                <LoaderCircleIcon className="animate-spin" data-icon="inline-start" />
              ) : (
                <SparklesIcon data-icon="inline-start" />
              )}
              Ask AI
            </Button>
            {report.status !== "RESOLVED" ? (
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={(event) => {
                  event.stopPropagation();
                  setPendingAction({ kind: "resolve", report });
                }}
              >
                <CheckCircle2Icon data-icon="inline-start" />
                Resolve
              </Button>
            ) : null}
          </div>
        ),
      },
    ],
    [aiLoadingReportId, detail.data?.id],
  );

  async function handleConfirm() {
    if (!pendingAction) {
      return;
    }

    setIsSubmitting(true);
    setActionError(null);

    try {
      const updatedReport =
        pendingAction.kind === "resolve"
          ? await resolveReport(pendingAction.report.id)
          : await updateReportStatus(pendingAction.report.id, pendingAction.status);

      applyReportUpdate(updatedReport);
      setPendingAction(null);
      resource.refetch();
    } catch (requestError) {
      setActionError(
        requestError instanceof Error ? requestError.message : "Action failed.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  const latestAiResolution = aiHistory[0] ?? null;
  const detailReport = detail.data;
  const confirmTitle =
    pendingAction?.kind === "resolve"
      ? "Resolve report"
      : pendingAction
        ? `${REPORT_STATUS_LABELS[pendingAction.status]} report`
        : "Update report";
  const confirmDescription =
    pendingAction?.kind === "resolve"
      ? "This closes the report only. It will not hide, remove, or suspend the target content."
      : pendingAction
        ? `Change this report's status to "${pendingAction.status.toLowerCase().replace("_", " ")}".`
        : "Confirm the report action.";
  const confirmLabel =
    pendingAction?.kind === "resolve"
      ? "Resolve report"
      : pendingAction
        ? REPORT_STATUS_LABELS[pendingAction.status]
        : "Confirm";

  return (
    <div className="flex flex-col gap-6">
      <AdminPageHeader
        title="Reports"
        description="Review user reports, request AI recommendations, and resolve reports separately from moderation actions."
      />
      <Toolbar onRefresh={resource.refetch}>
        <FilterSelect
          label="Status"
          value={status}
          options={reportStatuses}
          placeholder="All statuses"
          onChange={setStatus}
        />
        <FilterSelect
          label="Target type"
          value={targetType}
          options={targetTypes}
          placeholder="All targets"
          onChange={setTargetType}
        />
      </Toolbar>
      {aiActionError ? (
        <div className="rounded-md border border-accent/30 bg-accent/10 px-4 py-3 text-sm text-accent">
          {aiActionError}
        </div>
      ) : null}
      <AdminDataTable
        columns={columns}
        rows={resource.rows}
        getRowKey={(report) => report.id}
        isLoading={resource.isLoading}
        error={resource.error}
        emptyTitle="No reports found"
        emptyDescription="No reports match the current status and target filters."
        onRowClick={openReportDetail}
      />
      <AdminPagination page={resource.data} onPageChange={resource.setPageNumber} />
      <AdminDetailDialog
        open={detail.open}
        onOpenChange={detail.setOpen}
        title="Report detail"
        description={detailReport ? `Report ${detailReport.id}` : "Latest detail from admin API"}
        isLoading={detail.isLoading}
        error={detail.error}
        className="max-w-6xl"
        footer={
          detailReport ? (
            <div className="flex flex-wrap justify-end gap-2">
              <Button
                type="button"
                variant="outline"
                size="sm"
                disabled={aiLoadingReportId === detailReport.id}
                onClick={() => void handleAskAi(detailReport)}
              >
                {aiLoadingReportId === detailReport.id ? (
                  <LoaderCircleIcon className="animate-spin" data-icon="inline-start" />
                ) : (
                  <SparklesIcon data-icon="inline-start" />
                )}
                Ask AI
              </Button>
              {reportStatuses
                .filter((nextStatus) => nextStatus !== detailReport.status && nextStatus !== "RESOLVED")
                .map((nextStatus) => (
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    key={nextStatus}
                    onClick={() =>
                      setPendingAction({
                        kind: "status",
                        report: detailReport,
                        status: nextStatus,
                      })
                    }
                  >
                    {REPORT_STATUS_LABELS[nextStatus]}
                  </Button>
                ))}
              {detailReport.status !== "RESOLVED" ? (
                <Button
                  type="button"
                  size="sm"
                  onClick={() => setPendingAction({ kind: "resolve", report: detailReport })}
                >
                  <CheckCircle2Icon data-icon="inline-start" />
                  Resolve report
                </Button>
              ) : null}
            </div>
          ) : null
        }
      >
        {detailReport ? (
          <div className="flex flex-col gap-5">
            <div className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_360px]">
              <AdminDetailGrid>
                <AdminDetailField label="Reason" className="sm:col-span-2">
                  <div className="flex flex-wrap items-center gap-2">
                    <span>{reportReason(detailReport)}</span>
                    <Badge className={severityClassName(detailReport.reasonSeverity)}>
                      Severity {detailReport.reasonSeverity ?? "-"}
                    </Badge>
                  </div>
                </AdminDetailField>
                <AdminDetailField label="Reason code">
                  {detailReport.reasonCode || "-"}
                </AdminDetailField>
                <AdminDetailField label="Reason ID">
                  {detailReport.reasonId || "-"}
                </AdminDetailField>
                <AdminDetailField label="Status">
                  <AdminStatusBadge value={detailReport.status} />
                </AdminDetailField>
                <AdminDetailField label="Reporter">
                  <UserCell
                    name={detailReport.reporterUserName || detailReport.reporterUserId}
                    avatar={detailReport.reporterUserAvatar}
                    subtitle={detailReport.reporterUserId}
                  />
                </AdminDetailField>
                <AdminDetailField label="Target type">
                  <AdminStatusBadge value={detailReport.targetType} />
                </AdminDetailField>
                <AdminDetailField label="Target ID">{detailReport.targetId}</AdminDetailField>
                <AdminDetailField label="Blog">{detailReport.blogId || "-"}</AdminDetailField>
                <AdminDetailField label="Comment">
                  {detailReport.commentId || "-"}
                </AdminDetailField>
                <AdminDetailField label="Reported user">
                  {detailReport.reportedUserId || "-"}
                </AdminDetailField>
                <AdminDetailField label="Cafe page">
                  {detailReport.cafePageId || "-"}
                </AdminDetailField>
                <AdminDetailField label="Created">
                  {formatDate(detailReport.createdAt)}
                </AdminDetailField>
                <AdminDetailField label="Resolved">
                  {formatDate(detailReport.resolvedAt)}
                </AdminDetailField>
                <AdminDetailField label="Description" className="sm:col-span-2">
                  <p className="whitespace-pre-wrap leading-6">
                    {detailReport.description || "-"}
                  </p>
                </AdminDetailField>
              </AdminDetailGrid>

              <section className="rounded-md border border-border bg-background p-4">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <p className="text-xs font-black uppercase tracking-[0.08em] text-muted">
                      Latest AI recommendation
                    </p>
                    <p className="mt-1 text-sm text-muted">
                      Recommendation only. Admin still applies the final action.
                    </p>
                  </div>
                  <BotIcon className="size-5 shrink-0 text-primary" />
                </div>
                {aiHistoryLoading ? (
                  <div className="mt-5 flex items-center gap-2 text-sm text-muted">
                    <LoaderCircleIcon className="size-4 animate-spin" />
                    Loading AI history...
                  </div>
                ) : aiHistoryError ? (
                  <p className="mt-5 rounded-md border border-accent/30 bg-accent/10 p-3 text-sm text-accent">
                    {aiHistoryError}
                  </p>
                ) : latestAiResolution ? (
                  <div className="mt-5 flex flex-col gap-4">
                    <div className="flex flex-wrap gap-2">
                      <AdminStatusBadge value={latestAiResolution.reportDecision} />
                      <AdminStatusBadge value={latestAiResolution.targetAction} />
                    </div>
                    <div className="grid grid-cols-2 gap-2 text-sm">
                      <div className="rounded-md bg-surface p-3">
                        <p className="text-xs font-black uppercase text-muted">Confidence</p>
                        <p className="mt-1 font-semibold text-espresso">
                          {scoreLabel(latestAiResolution.confidenceScore)}
                        </p>
                      </div>
                      <div className="rounded-md bg-surface p-3">
                        <p className="text-xs font-black uppercase text-muted">Risk</p>
                        <p className="mt-1 font-semibold text-espresso">
                          {scoreLabel(latestAiResolution.riskScore)}
                        </p>
                      </div>
                    </div>
                    <div>
                      <p className="text-xs font-black uppercase text-muted">Rule</p>
                      <p className="mt-1 text-sm text-foreground">
                        {latestAiResolution.ruleCode || "-"}
                      </p>
                    </div>
                    <div>
                      <p className="text-xs font-black uppercase text-muted">Explanation</p>
                      <p className="mt-1 whitespace-pre-wrap text-sm leading-6 text-foreground">
                        {latestAiResolution.explanation || "-"}
                      </p>
                    </div>
                    <div className="flex flex-wrap gap-1">
                      {latestAiResolution.labels.length ? (
                        latestAiResolution.labels.map((label) => (
                          <Badge variant="secondary" key={label}>
                            {label}
                          </Badge>
                        ))
                      ) : (
                        <span className="text-sm text-muted">No labels</span>
                      )}
                    </div>
                    <p className="text-xs text-muted">
                      {latestAiResolution.modelName || "Unknown model"} -{" "}
                      {formatDate(latestAiResolution.createdAt)}
                    </p>
                  </div>
                ) : (
                  <div className="mt-5 rounded-md border border-dashed border-border p-4 text-sm text-muted">
                    No AI recommendation yet. Use Ask AI to request one from the admin
                    resolution workflow.
                  </div>
                )}
                {aiActionError ? (
                  <p className="mt-4 rounded-md border border-accent/30 bg-accent/10 p-3 text-sm text-accent">
                    {aiActionError}
                  </p>
                ) : null}
              </section>
            </div>

            <Separator />

            <section>
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <p className="text-xs font-black uppercase tracking-[0.08em] text-muted">
                    AI recommendation history
                  </p>
                  <p className="mt-1 text-sm text-muted">
                    Showing {aiHistory.length} of {aiHistoryPage?.totalElements ?? aiHistory.length}
                    {" "}saved recommendations.
                  </p>
                </div>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => void loadAiHistory(detailReport.id)}
                >
                  Refresh history
                </Button>
              </div>
              <div className="mt-3 grid gap-3">
                {aiHistory.length ? (
                  aiHistory.map((resolution) => (
                    <div
                      className="rounded-md border border-border bg-background p-4"
                      key={resolution.id}
                    >
                      <div className="flex flex-wrap items-center justify-between gap-3">
                        <div className="flex flex-wrap gap-2">
                          <AdminStatusBadge value={resolution.reportDecision} />
                          <AdminStatusBadge value={resolution.targetAction} />
                          {resolution.ruleCode ? (
                            <Badge variant="outline">{resolution.ruleCode}</Badge>
                          ) : null}
                        </div>
                        <span className="text-xs text-muted">{formatDate(resolution.createdAt)}</span>
                      </div>
                      <p className="mt-3 text-sm font-semibold text-espresso">
                        {resolutionSummary(resolution)}
                      </p>
                      <p className="mt-2 whitespace-pre-wrap text-sm leading-6 text-muted">
                        {resolution.explanation || "-"}
                      </p>
                      <div className="mt-3 flex flex-wrap gap-3 text-xs text-muted">
                        <span>Confidence {scoreLabel(resolution.confidenceScore)}</span>
                        <span>Risk {scoreLabel(resolution.riskScore)}</span>
                        <span>{resolution.modelName || "Unknown model"}</span>
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="rounded-md border border-dashed border-border p-4 text-sm text-muted">
                    No saved AI recommendations for this report.
                  </div>
                )}
              </div>
            </section>
          </div>
        ) : null}
      </AdminDetailDialog>
      <AdminConfirmDialog
        open={Boolean(pendingAction)}
        onOpenChange={(open) => {
          if (!open) {
            setPendingAction(null);
            setActionError(null);
          }
        }}
        title={confirmTitle}
        description={confirmDescription}
        confirmLabel={confirmLabel}
        isSubmitting={isSubmitting}
        onConfirm={handleConfirm}
      >
        {actionError ? <p className="text-sm text-accent">{actionError}</p> : null}
      </AdminConfirmDialog>
    </div>
  );
}
