"use client";

import { useEffect, useMemo, useState } from "react";
import {
  BotIcon,
  BookOpenIcon,
  CheckCircle2Icon,
  ClockIcon,
  EyeIcon,
  LoaderCircleIcon,
  ListChecksIcon,
  SparklesIcon,
  XCircleIcon,
} from "lucide-react";
import { AdminConfirmDialog } from "@/components/admin/admin-confirm-dialog";
import { AdminReportAiPolicySheet } from "@/components/admin/admin-report-ai-policy-sheet";
import { AdminReportTargetActions } from "@/components/admin/admin-report-target-actions";
import { UserCell } from "@/components/admin/user-cell";
import {
  AdminDataTable,
  AdminPagination,
  AdminRowActions,
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
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogTitle,
} from "@/components/ui/dialog";
import { Separator } from "@/components/ui/separator";
import {
  cancelReportAiAutoResolution,
  createReportAiResolution,
  getAdminBlog,
  getAdminComment,
  getAdminReport,
  getReportAiAutoResolutions,
  getReportAiResolutions,
  getReports,
  resolveReport,
  updateBlogStatus,
  updateCommentStatus,
  updateReportStatus,
} from "@/lib/api/admin";
import { ApiError } from "@/lib/api/client";
import {
  formatNumber,
  localizeApiError,
  type LocaleTag,
  useEnumLabel,
  useI18n,
  useUiText,
} from "@/features/i18n";
import type { PageResponse } from "@/types/api";
import type {
  AdminReportAiAutoApplyJob,
  AdminReportAiResolution,
  Blog,
  Comment,
  ContentReport,
  PostStatus,
  ReportStatus,
  ReportTargetType,
} from "@/types/admin";

const reportStatuses: ReportStatus[] = ["OPEN", "REVIEWING", "RESOLVED", "REJECTED"];
const targetTypes: ReportTargetType[] = ["BLOG", "COMMENT", "USER", "CAFE_PAGE"];
const AI_HISTORY_SIZE = 8;
const BULK_FETCH_SIZE = 100;
const AUTO_APPLY_HISTORY_SIZE = 8;
const REPORT_STATUS_LABELS: Record<ReportStatus, string> = {
  OPEN: "Reopen",
  REVIEWING: "Mark reviewing",
  RESOLVED: "Resolve",
  REJECTED: "Reject",
};

type PendingReportAction =
  | { kind: "resolve"; report: ContentReport }
  | { kind: "status"; report: ContentReport; status: ReportStatus };

type PendingTargetAction = {
  report: ContentReport;
  target: Blog | Comment;
  status: PostStatus;
};

type BulkAiMode = "filtered" | "selected";

type BulkAiProgress = {
  done: number;
  failed: number;
  recommended: number;
  manualReview: number;
  total: number;
};

type BulkAiFailure = {
  reportId: string;
  reason: string;
};

type AiOperationalError = {
  code: string;
  correlationId: string;
  retryable: boolean;
  stage: string;
};

function reportReason(
  report: ContentReport,
  enumLabel: (value: unknown) => string,
  ui: (phrase: string) => string,
) {
  return report.reasonCode
    ? enumLabel(report.reasonCode)
    : report.reasonLabel || report.reason || ui("Report");
}

function canRequestAi(report: ContentReport) {
  return report.status === "OPEN" || report.status === "REVIEWING";
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

function scoreLabel(value: number | null | undefined, localeTag: LocaleTag) {
  return typeof value === "number"
    ? formatNumber(value, localeTag, { minimumFractionDigits: 1, maximumFractionDigits: 1 })
    : "—";
}

function activeAutoApplyJob(jobs: AdminReportAiAutoApplyJob[]) {
  return jobs.find((job) => job.status === "SCHEDULED" || job.status === "APPLYING") ?? null;
}

function countdownLabel(scheduledAt: string, nowMs: number, localeTag: LocaleTag, dueNow: string) {
  const remainingMs = new Date(scheduledAt).getTime() - nowMs;

  if (remainingMs <= 0) {
    return dueNow;
  }

  const totalSeconds = Math.ceil(remainingMs / 1000);
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;

  if (hours > 0) {
    return `${formatNumber(hours, localeTag)}h ${formatNumber(minutes, localeTag)}m`;
  }

  if (minutes > 0) {
    return `${formatNumber(minutes, localeTag)}m ${formatNumber(seconds, localeTag)}s`;
  }

  return `${formatNumber(seconds, localeTag)}s`;
}

function mergeAiHistory(
  current: AdminReportAiResolution[],
  created: AdminReportAiResolution,
) {
  return [created, ...current.filter((item) => item.id !== created.id)].slice(0, AI_HISTORY_SIZE);
}

export function AdminReportsPage() {
  const { locale, localeTag, t } = useI18n();
  const ui = useUiText();
  const enumLabel = useEnumLabel();
  const [status, setStatus] = useState<ReportStatus | "">("");
  const [targetType, setTargetType] = useState<ReportTargetType | "">("");
  const [pendingAction, setPendingAction] = useState<PendingReportAction | null>(null);
  const [pendingTargetAction, setPendingTargetAction] = useState<PendingTargetAction | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [aiActionError, setAiActionError] = useState<string | null>(null);
  const [aiOperationalError, setAiOperationalError] =
    useState<AiOperationalError | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isTargetSubmitting, setIsTargetSubmitting] = useState(false);
  const [targetContent, setTargetContent] = useState<Blog | Comment | null>(null);
  const [targetContentLoading, setTargetContentLoading] = useState(false);
  const [targetContentError, setTargetContentError] = useState<string | null>(null);
  const [targetActionSuccess, setTargetActionSuccess] = useState<string | null>(null);
  const [recommendationStale, setRecommendationStale] = useState(false);
  const [policySheetOpen, setPolicySheetOpen] = useState(false);
  const [aiLoadingReportId, setAiLoadingReportId] = useState<string | null>(null);
  const [aiHistory, setAiHistory] = useState<AdminReportAiResolution[]>([]);
  const [aiHistoryPage, setAiHistoryPage] =
    useState<PageResponse<AdminReportAiResolution> | null>(null);
  const [aiHistoryLoading, setAiHistoryLoading] = useState(false);
  const [aiHistoryError, setAiHistoryError] = useState<string | null>(null);
  const [autoApplyJobs, setAutoApplyJobs] = useState<AdminReportAiAutoApplyJob[]>([]);
  const [autoApplyJobsLoading, setAutoApplyJobsLoading] = useState(false);
  const [autoApplyJobsError, setAutoApplyJobsError] = useState<string | null>(null);
  const [autoApplyJobsPage, setAutoApplyJobsPage] =
    useState<PageResponse<AdminReportAiAutoApplyJob> | null>(null);
  const [askAiDialogOpen, setAskAiDialogOpen] = useState(false);
  const [askAiReport, setAskAiReport] = useState<ContentReport | null>(null);
  const [bulkDialogOpen, setBulkDialogOpen] = useState(false);
  const [bulkMode, setBulkMode] = useState<BulkAiMode>("filtered");
  const [selectedBulkReportIds, setSelectedBulkReportIds] = useState<Set<string>>(
    () => new Set(),
  );
  const [bulkError, setBulkError] = useState<string | null>(null);
  const [bulkFailures, setBulkFailures] = useState<BulkAiFailure[]>([]);
  const [bulkRunning, setBulkRunning] = useState(false);
  const [bulkProgress, setBulkProgress] = useState<BulkAiProgress>({
    done: 0,
    failed: 0,
    recommended: 0,
    manualReview: 0,
    total: 0,
  });
  const [nowMs, setNowMs] = useState(() => Date.now());
  const detail = useAdminDetailResource<ContentReport>();

  const hasActiveAutoApplyJob = useMemo(
    () => activeAutoApplyJob(autoApplyJobs) !== null,
    [autoApplyJobs],
  );

  useEffect(() => {
    if (!hasActiveAutoApplyJob) return;
    const intervalId = window.setInterval(() => setNowMs(Date.now()), 1000);
    return () => window.clearInterval(intervalId);
  }, [hasActiveAutoApplyJob]);

  const resource = usePagedAdminResource(
    (page, signal) =>
      getReports({ status, targetType, page, size: PAGE_SIZE }, signal),
    [status, targetType],
  );

  const currentPageEligibleReports = useMemo(
    () => resource.rows.filter(canRequestAi),
    [resource.rows],
  );
  const currentPageReportIds = useMemo(
    () => currentPageEligibleReports.map((report) => report.id),
    [currentPageEligibleReports],
  );
  const selectedBulkReports = useMemo(
    () => currentPageEligibleReports.filter((report) => selectedBulkReportIds.has(report.id)),
    [currentPageEligibleReports, selectedBulkReportIds],
  );
  const allCurrentPageSelected =
    currentPageReportIds.length > 0 &&
    currentPageReportIds.every((reportId) => selectedBulkReportIds.has(reportId));

  function openBulkDialog() {
    setBulkDialogOpen(true);
    setBulkMode("filtered");
    setBulkError(null);
    setBulkFailures([]);
    setBulkProgress({ done: 0, failed: 0, recommended: 0, manualReview: 0, total: 0 });
    setSelectedBulkReportIds(new Set(currentPageReportIds));
  }

  function toggleBulkReport(reportId: string) {
    setSelectedBulkReportIds((current) => {
      const next = new Set(current);

      if (next.has(reportId)) {
        next.delete(reportId);
      } else {
        next.add(reportId);
      }

      return next;
    });
  }

  function toggleCurrentPageSelection() {
    setSelectedBulkReportIds((current) => {
      const next = new Set(current);

      if (allCurrentPageSelected) {
        currentPageReportIds.forEach((reportId) => next.delete(reportId));
      } else {
        currentPageReportIds.forEach((reportId) => next.add(reportId));
      }

      return next;
    });
  }

  async function getAllFilteredReports() {
    const firstPage = await getReports({
      status,
      targetType,
      page: 0,
      size: BULK_FETCH_SIZE,
    });
    const reports = [...firstPage.content];

    for (let page = 1; page < firstPage.totalPages; page += 1) {
      const nextPage = await getReports({
        status,
        targetType,
        page,
        size: BULK_FETCH_SIZE,
      });
      reports.push(...nextPage.content);
    }

    return reports.filter(canRequestAi);
  }

  async function handleBulkAskAi() {
    setBulkRunning(true);
    setBulkError(null);
    setBulkFailures([]);
    setAiActionError(null);
    setBulkProgress({ done: 0, failed: 0, recommended: 0, manualReview: 0, total: 0 });

    try {
      const reports =
        bulkMode === "filtered" ? await getAllFilteredReports() : selectedBulkReports;

      if (!reports.length) {
        setBulkError(
          bulkMode === "filtered"
            ? ui("No open or reviewing reports match the current filters.")
            : ui("Select at least one open or reviewing report."),
        );
        return;
      }

      setBulkProgress({ done: 0, failed: 0, recommended: 0, manualReview: 0, total: reports.length });

      // Bounded concurrency: 4 in-flight AI calls at a time. Previous implementation
      // ran serially with a 250 ms sleep between each report — for N reports that was
      // (N × latency) + (N × 250 ms). This drops to roughly (N / 4 × latency).
      const CONCURRENCY = 4;
      let cursor = 0;
      const processOne = async (report: (typeof reports)[number]) => {
        try {
          const createdResolution = await createReportAiResolution(report.id);

          if (detail.data?.id === report.id) {
            setAiHistory((current) => mergeAiHistory(current, createdResolution));
            if (createdResolution.autoApplyJob) {
              setAutoApplyJobs((current) => [
                createdResolution.autoApplyJob!,
                ...current.filter((job) => job.id !== createdResolution.autoApplyJob?.id),
              ].slice(0, AUTO_APPLY_HISTORY_SIZE));
            }
          }

          setBulkProgress((current) => ({
            ...current,
            done: current.done + 1,
            recommended:
              current.recommended +
              (createdResolution.reportDecision === "NEEDS_MANUAL_REVIEW" ? 0 : 1),
            manualReview:
              current.manualReview +
              (createdResolution.reportDecision === "NEEDS_MANUAL_REVIEW" ? 1 : 0),
          }));
        } catch (requestError) {
          const reason = requestError instanceof Error
            ? requestError.message
            : "AI recommendation failed.";
          setBulkFailures((current) => [
            ...current,
            {
              reportId: report.id,
              reason: locale === "en" ? reason : t("common.error.action"),
            },
          ]);
          setBulkProgress((current) => ({
            ...current,
            done: current.done + 1,
            failed: current.failed + 1,
          }));
        }
      };
      const workers = Array.from({ length: Math.min(CONCURRENCY, reports.length) }, async () => {
        while (cursor < reports.length) {
          const index = cursor++;
          await processOne(reports[index]);
        }
      });
      await Promise.all(workers);

      if (detail.data) {
        await loadAiHistory(detail.data.id);
        await loadAutoApplyJobs(detail.data.id);
      }

      resource.refetch();
    } catch (requestError) {
      setBulkError(localizeApiError(requestError, locale, t, "common.error.action"));
    } finally {
      setBulkRunning(false);
    }
  }

  async function loadAutoApplyJobs(reportId: string, signal?: AbortSignal) {
    setAutoApplyJobsLoading(true);
    setAutoApplyJobsError(null);

    try {
      const response = await getReportAiAutoResolutions(
        reportId,
        { page: 0, size: AUTO_APPLY_HISTORY_SIZE },
        signal,
      );
      setAutoApplyJobsPage(response);
      setAutoApplyJobs(response.content);
    } catch (requestError) {
      if (signal?.aborted) {
        return;
      }

      setAutoApplyJobs([]);
      setAutoApplyJobsPage(null);
      setAutoApplyJobsError(localizeApiError(requestError, locale, t));
    } finally {
      if (!signal?.aborted) {
        setAutoApplyJobsLoading(false);
      }
    }
  }

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
      setAiHistoryError(localizeApiError(requestError, locale, t));
    } finally {
      if (!signal?.aborted) {
        setAiHistoryLoading(false);
      }
    }
  }

  async function loadTargetContent(report: ContentReport, signal?: AbortSignal) {
    if (report.targetType !== "BLOG" && report.targetType !== "COMMENT") {
      setTargetContent(null);
      setTargetContentError(null);
      return;
    }
    setTargetContentLoading(true);
    setTargetContentError(null);
    try {
      const targetId = report.targetType === "BLOG"
        ? report.blogId || report.targetId
        : report.commentId || report.targetId;
      const target = report.targetType === "BLOG"
        ? await getAdminBlog(targetId, signal)
        : await getAdminComment(targetId, signal);
      setTargetContent(target);
    } catch (requestError) {
      if (!signal?.aborted) {
        setTargetContent(null);
        setTargetContentError(localizeApiError(requestError, locale, t, "common.error.loadDetail"));
      }
    } finally {
      if (!signal?.aborted) setTargetContentLoading(false);
    }
  }

  function openReportDetail(report: ContentReport) {
    setAiActionError(null);
    setAiHistory([]);
    setAiHistoryPage(null);
    setAutoApplyJobs([]);
    setAutoApplyJobsPage(null);
    setTargetContent(null);
    setTargetContentError(null);
    setTargetActionSuccess(null);
    setRecommendationStale(false);
    setPolicySheetOpen(false);
    void detail.load((signal) => getAdminReport(report.id, signal));
    void loadAiHistory(report.id);
    void loadAutoApplyJobs(report.id);
    void loadTargetContent(report);
  }

  function openAskAiDialog(report: ContentReport) {
    setAskAiReport(report);
    setAiActionError(null);
    setAiOperationalError(null);
    setAskAiDialogOpen(true);
  }

  async function handleAskAi(report: ContentReport) {
    setAiLoadingReportId(report.id);
    setAiActionError(null);
    setAiOperationalError(null);

    try {
      const createdResolution = await createReportAiResolution(report.id);

      if (detail.data?.id === report.id) {
        setRecommendationStale(false);
        setAiHistory((current) => mergeAiHistory(current, createdResolution));
        if (createdResolution.autoApplyJob) {
          setAutoApplyJobs((current) => [
            createdResolution.autoApplyJob!,
            ...current.filter((job) => job.id !== createdResolution.autoApplyJob?.id),
          ].slice(0, AUTO_APPLY_HISTORY_SIZE));
        }
        await loadAiHistory(report.id);
        await loadAutoApplyJobs(report.id);
      }
      if (createdResolution.autoApplyWarning) {
        setAiActionError(
          locale === "en"
            ? createdResolution.autoApplyWarning
            : ui("AI recommendation created, but the legacy auto-apply warning could not be localized."),
        );
      }
      setAskAiDialogOpen(false);
    } catch (requestError) {
      setAiActionError(localizeApiError(requestError, locale, t, "common.error.action"));
      setAiOperationalError(
        requestError instanceof ApiError &&
          requestError.code &&
          requestError.correlationId &&
          requestError.retryable !== null &&
          requestError.stage
          ? {
              code: requestError.code,
              correlationId: requestError.correlationId,
              retryable: requestError.retryable,
              stage: requestError.stage,
            }
          : null,
      );
    } finally {
      setAiLoadingReportId(null);
    }
  }

  async function handleAskAiDialogSubmit() {
    if (!askAiReport) {
      return;
    }

    await handleAskAi(askAiReport);
  }

  async function handleCancelAutoApply(job: AdminReportAiAutoApplyJob) {
    setAiActionError(null);

    try {
      const cancelledJob = await cancelReportAiAutoResolution(job.id);
      setAutoApplyJobs((current) =>
        current.map((item) => (item.id === cancelledJob.id ? cancelledJob : item)),
      );
      if (detail.data) {
        await loadAutoApplyJobs(detail.data.id);
      }
    } catch (requestError) {
      setAiActionError(localizeApiError(requestError, locale, t, "common.error.action"));
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
        lines: 2,
        maxWidth: 420,
        cell: (report) => (
          <div>
            <div className="flex flex-wrap items-center gap-2">
              <p className="font-semibold text-espresso">{reportReason(report, enumLabel, ui)}</p>
              <Badge className={severityClassName(report.reasonSeverity)}>
                {ui("Severity {severity}", {
                  severity:
                    report.reasonSeverity == null
                      ? "—"
                      : formatNumber(report.reasonSeverity, localeTag),
                })}
              </Badge>
            </div>
            <p className="mt-1 text-sm leading-6 text-muted">
              {report.description ?? "—"}
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
      { header: "Created", cell: (report) => formatDate(report.createdAt, localeTag) },
      {
        header: "",
        className: "w-12 text-right",
        cell: (report) => (
          <AdminRowActions
            actions={[
              {
                label: "View detail",
                icon: EyeIcon,
                onSelect: () => openReportDetail(report),
              },
              {
                label: ui("Ask AI"),
                icon: SparklesIcon,
                disabled: aiLoadingReportId === report.id || !canRequestAi(report),
                onSelect: () => openAskAiDialog(report),
              },
              ...(report.status !== "RESOLVED"
                ? [
                    {
                label: ui("Resolve"),
                      icon: CheckCircle2Icon,
                      onSelect: () =>
                        setPendingAction({ kind: "resolve", report }),
                    },
                  ]
                : []),
            ]}
          />
        ),
      },
    ],
    [aiLoadingReportId, detail.data?.id, enumLabel, localeTag, ui],
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
      setActionError(localizeApiError(requestError, locale, t, "common.error.action"));
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleTargetConfirm() {
    if (!pendingTargetAction) return;
    setIsTargetSubmitting(true);
    setTargetContentError(null);
    setTargetActionSuccess(null);
    try {
      const updatedTarget = pendingTargetAction.report.targetType === "BLOG"
        ? await updateBlogStatus(pendingTargetAction.target.id, pendingTargetAction.status)
        : await updateCommentStatus(pendingTargetAction.target.id, pendingTargetAction.status);
      setTargetContent(updatedTarget);
      setRecommendationStale(Boolean(latestAiResolution));
      setTargetActionSuccess(
        ui("Target status changed from {oldStatus} to {newStatus}. The report remains {reportStatus}.", {
          oldStatus: enumLabel(pendingTargetAction.target.status),
          newStatus: enumLabel(updatedTarget.status),
          reportStatus: enumLabel(pendingTargetAction.report.status),
        }),
      );
      setPendingTargetAction(null);
    } catch (requestError) {
      setTargetContentError(localizeApiError(requestError, locale, t, "common.error.action"));
    } finally {
      setIsTargetSubmitting(false);
    }
  }

  const latestAiResolution = aiHistory[0] ?? null;
  const detailReport = detail.data;
  const suggestedTargetStatus: PostStatus | null = latestAiResolution
    ? ({ KEEP_VISIBLE: "PUBLISHED", HIDE: "HIDDEN", REMOVE: "REMOVED" } as const)[
        latestAiResolution.targetAction as "KEEP_VISIBLE" | "HIDE" | "REMOVE"
      ] ?? null
    : null;
  const confirmTitle =
    pendingAction?.kind === "resolve"
      ? "Resolve report"
      : pendingAction
        ? ui("{action} report", { action: ui(REPORT_STATUS_LABELS[pendingAction.status]) })
        : "Update report";
  const confirmDescription =
    pendingAction?.kind === "resolve"
      ? "This closes the report only. It will not hide, remove, or suspend the target content."
      : pendingAction
        ? ui("Change this report's status to {status}.", {
            status: enumLabel(pendingAction.status),
          })
        : "Confirm the report action.";
  const confirmLabel =
    pendingAction?.kind === "resolve"
      ? "Resolve report"
      : pendingAction
        ? ui(REPORT_STATUS_LABELS[pendingAction.status])
        : "Confirm";
  const bulkRemaining = Math.max(
    bulkProgress.total - bulkProgress.done,
    0,
  );
  const bulkSuccess = Math.max(bulkProgress.done - bulkProgress.failed, 0);
  const currentAutoApplyJob = activeAutoApplyJob(autoApplyJobs);

  return (
    <div className="flex flex-col gap-4">
      <AdminPageHeader
        title="Reports"
        description="Review user reports, request AI recommendations, and resolve reports separately from moderation actions."
      />
      <Toolbar
        onRefresh={resource.refetch}
        actions={
          <Button
            type="button"
            variant="outline"
            size="sm"
            disabled={resource.isLoading || !resource.rows.length}
            onClick={openBulkDialog}
          >
            <ListChecksIcon data-icon="inline-start" />
            {ui("Generate AI recommendations")}
          </Button>
        }
      >
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
      <Dialog open={bulkDialogOpen} onOpenChange={(open) => {
        if (!bulkRunning) {
          setBulkDialogOpen(open);
        }
      }}>
        <DialogContent className="max-h-[86vh] w-[94vw] max-w-3xl grid-rows-[auto_minmax(0,1fr)_auto] p-0">
          <div className="border-b border-border px-5 py-4">
            <DialogTitle>{ui("Generate AI recommendations")}</DialogTitle>
            <DialogDescription className="mt-1">
              {ui("Create AI recommendations in bulk. This does not resolve reports or change target content.")}
            </DialogDescription>
          </div>
          <div className="overflow-y-auto px-5 py-4">
            <div className="grid gap-3 sm:grid-cols-2">
              <button
                type="button"
                className={`rounded-md border p-4 text-left transition ${
                  bulkMode === "filtered"
                    ? "border-primary bg-primary/5"
                    : "border-border bg-background hover:border-primary/60"
                }`}
                disabled={bulkRunning}
                onClick={() => setBulkMode("filtered")}
              >
                <span className="text-sm font-bold text-espresso">
                  {ui("All matching filters")}
                </span>
                <span className="mt-2 block text-sm leading-6 text-muted">
                  {ui("Run AI for every report matching status and target filters, across all pages.")}
                </span>
                <span className="mt-3 block text-xs text-muted">
                  {ui("Current filters: {status} / {target}", {
                    status: status ? enumLabel(status) : ui("all statuses"),
                    target: targetType ? enumLabel(targetType) : ui("all targets"),
                  })}
                </span>
              </button>
              <button
                type="button"
                className={`rounded-md border p-4 text-left transition ${
                  bulkMode === "selected"
                    ? "border-primary bg-primary/5"
                    : "border-border bg-background hover:border-primary/60"
                }`}
                disabled={bulkRunning}
                onClick={() => setBulkMode("selected")}
              >
                <span className="text-sm font-bold text-espresso">
                  {ui("Selected reports")}
                </span>
                <span className="mt-2 block text-sm leading-6 text-muted">
                  {ui("Run AI only for reports selected from the current page.")}
                </span>
                <span className="mt-3 block text-xs text-muted">
                  {ui("Selected: {selected} of {eligible} eligible", {
                    selected: formatNumber(selectedBulkReports.length, localeTag),
                    eligible: formatNumber(currentPageEligibleReports.length, localeTag),
                  })}
                </span>
              </button>
            </div>

            <div className="mt-5 rounded-md border border-primary/20 bg-primary/5 p-4">
              <p className="text-sm font-bold text-espresso">
                {ui("Recommendation only — automation is disabled")}
              </p>
              <p className="mt-1 text-sm leading-6 text-muted">
                {ui("This bulk run creates review records only. It never resolves a report or changes a target.")}
              </p>
            </div>

            <div className="mt-5 rounded-md border border-border bg-background">
              <div className="flex flex-wrap items-center justify-between gap-3 border-b border-border px-4 py-3">
                <div>
                  <p className="text-sm font-bold text-espresso">{ui("Current page selection")}</p>
                  <p className="mt-1 text-xs text-muted">
                    {ui("Selection is used when the Selected reports mode is active.")}
                  </p>
                </div>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  disabled={bulkRunning || !currentPageEligibleReports.length}
                  onClick={toggleCurrentPageSelection}
                >
                  {ui(allCurrentPageSelected ? "Clear page" : "Select page")}
                </Button>
              </div>
              <div className="max-h-72 overflow-y-auto">
                {resource.rows.map((report) => (
                  <label
                    className={`flex items-start gap-3 border-b border-border px-4 py-3 last:border-b-0 ${
                      canRequestAi(report)
                        ? "cursor-pointer hover:bg-surface-muted/40"
                        : "cursor-not-allowed bg-surface-muted/30 opacity-60"
                    }`}
                    key={report.id}
                  >
                    <input
                      type="checkbox"
                      className="mt-1 size-4 accent-primary"
                      checked={selectedBulkReportIds.has(report.id)}
                      disabled={bulkRunning || !canRequestAi(report)}
                      onChange={() => toggleBulkReport(report.id)}
                    />
                    <span className="min-w-0 flex-1">
                      <span className="flex flex-wrap items-center gap-2">
                        <span className="font-semibold text-espresso">
                         {reportReason(report, enumLabel, ui)}
                        </span>
                        <AdminStatusBadge value={report.status} />
                        <AdminStatusBadge value={report.targetType} />
                      </span>
                      <span className="mt-1 block truncate text-sm text-muted">
                        {textPreview(report.description, 140)}
                      </span>
                    </span>
                  </label>
                ))}
                {!resource.rows.length ? (
                  <div className="p-4 text-sm text-muted">
                    {ui("No reports on this page.")}
                  </div>
                ) : null}
              </div>
            </div>

            {bulkProgress.total > 0 ? (
              <div className="mt-5 rounded-md border border-border bg-background p-4">
                <div className="flex flex-wrap gap-4 text-sm text-muted">
                  <span>{ui("Completed: {count}", { count: formatNumber(bulkSuccess, localeTag) })}</span>
                  <span>{ui("Recommended: {count}", { count: formatNumber(bulkProgress.recommended, localeTag) })}</span>
                  <span>{ui("Needs manual review: {count}", { count: formatNumber(bulkProgress.manualReview, localeTag) })}</span>
                  <span>{ui("Failed: {count}", { count: formatNumber(bulkProgress.failed, localeTag) })}</span>
                  <span>{ui("Remaining: {count}", { count: formatNumber(bulkRemaining, localeTag) })}</span>
                  <span>{ui("Total: {count}", { count: formatNumber(bulkProgress.total, localeTag) })}</span>
                </div>
                <div className="mt-3 h-2 overflow-hidden rounded-full bg-surface-muted">
                  <div
                    className="h-full bg-primary transition-all"
                    style={{
                      width: `${Math.min(
                        100,
                        Math.round((bulkProgress.done / bulkProgress.total) * 100),
                      )}%`,
                    }}
                  />
                </div>
              </div>
            ) : null}

            {bulkError ? (
              <p role="alert" className="mt-4 rounded-md border border-accent/30 bg-accent/10 p-3 text-sm text-accent">
                {bulkError}
              </p>
            ) : null}
            {bulkFailures.length ? (
              <div role="alert" className="mt-4 rounded-md border border-accent/30 bg-accent/10 p-3">
                <p className="text-sm font-semibold text-accent">
                  {ui("{count} reports failed. Successful recommendations were kept.", {
                    count: formatNumber(bulkFailures.length, localeTag),
                  })}
                </p>
                <ul className="mt-2 max-h-32 space-y-1 overflow-y-auto text-sm text-foreground">
                  {bulkFailures.map((failure) => (
                    <li className="break-words" key={failure.reportId}>
                      <span className="font-mono text-xs">{shortId(failure.reportId)}</span>
                      {": "}{failure.reason}
                    </li>
                  ))}
                </ul>
              </div>
            ) : null}
          </div>
          <div className="flex flex-wrap justify-end gap-2 border-t border-border px-5 py-4">
            <Button
              type="button"
              variant="outline"
              disabled={bulkRunning}
              onClick={() => setBulkDialogOpen(false)}
            >
              {ui("Close")}
            </Button>
            <Button
              type="button"
              disabled={
                bulkRunning ||
                (bulkMode === "selected" && selectedBulkReports.length === 0)
              }
              onClick={() => void handleBulkAskAi()}
            >
              {bulkRunning ? (
                <LoaderCircleIcon className="animate-spin" data-icon="inline-start" />
              ) : (
                <SparklesIcon data-icon="inline-start" />
              )}
              {ui(bulkRunning ? "Running..." : "Run AI")}
            </Button>
          </div>
        </DialogContent>
      </Dialog>
      <Dialog
        open={askAiDialogOpen}
        onOpenChange={(open) => {
          if (!aiLoadingReportId) {
            setAskAiDialogOpen(open);
          }
        }}
      >
        <DialogContent className="w-[94vw] max-w-xl p-0">
          <div className="border-b border-border px-5 py-4">
            <DialogTitle>{ui("Ask AI for report resolution")}</DialogTitle>
            <DialogDescription className="mt-1">
              {ui("Build an evidence-based recommendation. No report or target action is applied.")}
            </DialogDescription>
          </div>
          <div className="px-5 py-4">
            {askAiReport ? (
              <div className="rounded-md border border-border bg-surface p-4">
                <div className="flex flex-wrap items-center gap-2">
                  <span className="font-semibold text-espresso">
                     {reportReason(askAiReport, enumLabel, ui)}
                  </span>
                  <AdminStatusBadge value={askAiReport.status} />
                  <AdminStatusBadge value={askAiReport.targetType} />
                </div>
                <p className="mt-2 text-sm text-muted">
                  {textPreview(askAiReport.description, 160)}
                </p>
              </div>
            ) : null}

            <div className="mt-4 rounded-md border border-primary/20 bg-primary/5 p-4 text-sm leading-6 text-foreground">
              <p className="font-bold text-espresso">{ui("A0 recommendation-only mode")}</p>
              <p className="mt-1 text-muted">
                {ui("AI findings support an admin review. Confidence, likelihood, severity, or action risk never authorize an automatic action.")}
              </p>
            </div>
            {aiActionError ? (
              <div role="alert" className="mt-4 rounded-md border border-accent/30 bg-accent/10 p-3 text-sm text-accent">
                <p className="font-semibold">{ui("AI recommendation was not created.")}</p>
                <p className="mt-1 break-words">{aiActionError}</p>
                {aiOperationalError ? (
                  <div className="mt-2 space-y-1 text-foreground">
                    <p>{ui("Error code: {code}", { code: aiOperationalError.code })}</p>
                    <p>{ui("Stage: {stage}", { stage: aiOperationalError.stage })}</p>
                    <p>{ui("Support reference: {reference}", { reference: aiOperationalError.correlationId })}</p>
                    <p>{ui(aiOperationalError.retryable ? "Retry available" : "Retry unavailable")}</p>
                  </div>
                ) : null}
                <p className="mt-1 text-foreground">
                  {ui("Check the service status, then select Ask AI to retry this report.")}
                </p>
              </div>
            ) : null}
          </div>
          <div className="flex flex-wrap justify-end gap-2 border-t border-border px-5 py-4">
            <Button
              type="button"
              variant="outline"
              disabled={Boolean(aiLoadingReportId)}
              onClick={() => setAskAiDialogOpen(false)}
            >
              {ui("Close")}
            </Button>
            <Button
              type="button"
              disabled={Boolean(aiLoadingReportId) || !askAiReport}
              onClick={() => void handleAskAiDialogSubmit()}
            >
              {aiLoadingReportId ? (
                <LoaderCircleIcon className="animate-spin" data-icon="inline-start" />
              ) : (
                <SparklesIcon data-icon="inline-start" />
              )}
              {ui(aiLoadingReportId ? "Running..." : "Ask AI")}
            </Button>
          </div>
        </DialogContent>
      </Dialog>
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
        description={
          detailReport
            ? ui("Report {id}", { id: detailReport.id })
            : "Latest detail from admin API"
        }
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
                onClick={() => setPolicySheetOpen(true)}
              >
                <BookOpenIcon data-icon="inline-start" />
                {ui("View policy")}
              </Button>
              <Button
                type="button"
                variant="outline"
                size="sm"
                disabled={aiLoadingReportId === detailReport.id || !canRequestAi(detailReport)}
                onClick={() => openAskAiDialog(detailReport)}
              >
                {aiLoadingReportId === detailReport.id ? (
                  <LoaderCircleIcon className="animate-spin" data-icon="inline-start" />
                ) : (
                  <SparklesIcon data-icon="inline-start" />
                )}
                {ui("Ask AI")}
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
                    {ui(REPORT_STATUS_LABELS[nextStatus])}
                  </Button>
                ))}
              {detailReport.status !== "RESOLVED" ? (
                <Button
                  type="button"
                  size="sm"
                  onClick={() => setPendingAction({ kind: "resolve", report: detailReport })}
                >
                  <CheckCircle2Icon data-icon="inline-start" />
                  {ui("Resolve report")}
                </Button>
              ) : null}
            </div>
          ) : null
        }
      >
        {detailReport ? (
          <div className="flex flex-col gap-5">
            <div className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_360px]">
              <div
                className="flex min-w-0 flex-col gap-4"
                data-testid="report-detail-main-column"
              >
                <AdminDetailGrid>
                <AdminDetailField label="Reason" className="sm:col-span-2">
                  <div className="flex flex-wrap items-center gap-2">
                    <span>{reportReason(detailReport, enumLabel, ui)}</span>
                    <Badge className={severityClassName(detailReport.reasonSeverity)}>
                      {ui("Severity {severity}", {
                        severity:
                          detailReport.reasonSeverity == null
                            ? "—"
                            : formatNumber(detailReport.reasonSeverity, localeTag),
                      })}
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
                  {formatDate(detailReport.createdAt, localeTag)}
                </AdminDetailField>
                <AdminDetailField label="Resolved">
                  {formatDate(detailReport.resolvedAt, localeTag)}
                </AdminDetailField>
                <AdminDetailField label="Description" className="sm:col-span-2">
                  <p className="whitespace-pre-wrap leading-6">
                    {detailReport.description || "-"}
                  </p>
                </AdminDetailField>
                </AdminDetailGrid>

                {detailReport.targetType === "BLOG" || detailReport.targetType === "COMMENT" ? (
                  <AdminReportTargetActions
                    report={detailReport}
                    target={targetContent}
                    isLoading={targetContentLoading}
                    error={targetContentError}
                    isMutating={
                      isTargetSubmitting || aiLoadingReportId === detailReport.id
                    }
                    suggestedStatus={suggestedTargetStatus}
                    recommendationStale={recommendationStale}
                    successMessage={targetActionSuccess}
                    onRetry={() => void loadTargetContent(detailReport)}
                    onRequestStatus={(status) => {
                      if (targetContent) {
                        setTargetActionSuccess(null);
                        setPendingTargetAction({
                          report: detailReport,
                          target: targetContent,
                          status,
                        });
                      }
                    }}
                  />
                ) : null}

                <Separator />

                <section data-testid="report-ai-history">
                  <div className="flex flex-wrap items-center justify-between gap-3">
                    <div>
                      <p className="text-xs font-black uppercase tracking-[0.08em] text-muted">
                         {ui("AI recommendation history")}
                      </p>
                      <p className="mt-1 text-sm text-muted">
                         {ui("Showing {shown} of {total} saved recommendations.", {
                           shown: formatNumber(aiHistory.length, localeTag),
                           total: formatNumber(
                             aiHistoryPage?.totalElements ?? aiHistory.length,
                             localeTag,
                           ),
                         })}
                      </p>
                    </div>
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      onClick={() => {
                        void loadAiHistory(detailReport.id);
                        void loadAutoApplyJobs(detailReport.id);
                      }}
                    >
                       {ui("Refresh history")}
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
                              <Badge variant="outline">
                                {resolution.contractVersion === "2.0" ? "CONTRACT V2" : "LEGACY V1"}
                              </Badge>
                              {resolution.contractVersion !== "2.0" && resolution.ruleCode ? (
                                <Badge variant="outline">{resolution.ruleCode}</Badge>
                              ) : null}
                            </div>
                             <span className="text-xs text-muted">{formatDate(resolution.createdAt, localeTag)}</span>
                          </div>
                          <p className="mt-3 text-sm font-semibold text-espresso">
                             {enumLabel(resolution.reportDecision)} / {enumLabel(resolution.targetAction)}
                          </p>
                          <p className="mt-2 whitespace-pre-wrap text-sm leading-6 text-muted">
                            {resolution.explanation || "-"}
                          </p>
                          <div className="mt-3 flex flex-wrap gap-3 text-xs text-muted">
                            {resolution.contractVersion === "2.0" ? (
                              <>
                                 <span>{ui("Evidence {value}", { value: enumLabel(resolution.evidenceSufficiency || "UNKNOWN") })}</span>
                                 <span>{ui("Likelihood {value}", { value: enumLabel(resolution.violationLikelihood || "UNKNOWN") })}</span>
                                 <span>{ui("Action risk {value}", { value: enumLabel(resolution.actionRisk || "UNKNOWN") })}</span>
                              </>
                            ) : (
                              <span>
                                 {ui("Confidence {confidence} · risk {risk} — uncalibrated legacy values", {
                                   confidence: scoreLabel(resolution.confidenceScore, localeTag),
                                   risk: scoreLabel(resolution.riskScore, localeTag),
                                 })}
                              </span>
                            )}
                             <span>{resolution.modelName || ui("Unknown model")}</span>
                          </div>
                        </div>
                      ))
                    ) : (
                      <div className="rounded-md border border-dashed border-border p-4 text-sm text-muted">
                         {ui("No saved AI recommendations for this report.")}
                      </div>
                    )}
                  </div>
                </section>
              </div>

              <section
                className="rounded-md border border-border bg-background p-4"
                data-testid="report-ai-evidence-column"
              >
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <p className="text-xs font-black uppercase tracking-[0.08em] text-muted">
                       {ui("Latest AI recommendation")}
                    </p>
                    <p className="mt-1 text-sm text-muted">
                       {ui("Recommendation only. Admin still applies the final action.")}
                    </p>
                  </div>
                  <BotIcon className="size-5 shrink-0 text-primary" />
                </div>
                {aiHistoryLoading ? (
                  <div className="mt-5 flex items-center gap-2 text-sm text-muted">
                    <LoaderCircleIcon className="size-4 animate-spin" />
                     {ui("Loading AI history...")}
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
                      <Badge variant="outline">
                        {latestAiResolution.contractVersion === "2.0" ? "CONTRACT V2" : "LEGACY V1"}
                      </Badge>
                    </div>
                    <div className="rounded-md border border-primary/20 bg-primary/5 p-3 text-sm leading-6">
                      <p className="font-bold text-espresso">
                         {ui("AI recommendation only — no action has been applied")}
                      </p>
                      <p className="mt-1 text-muted">
                         {ui("An admin must make and execute the final moderation decision.")}
                      </p>
                    </div>
                    {latestAiResolution.blockedReasons?.length ? (
                      <div className="rounded-md border border-rating/30 bg-rating/10 p-3">
                        <p className="text-xs font-black uppercase text-espresso">
                           {ui("Why manual review is required")}
                        </p>
                        <ul className="mt-2 space-y-1 text-sm text-foreground">
                          {latestAiResolution.blockedReasons.map((reason) => (
                            <li className="font-mono text-xs" key={reason}>• {reason}</li>
                          ))}
                        </ul>
                      </div>
                    ) : null}
                    {latestAiResolution.contractVersion === "2.0" ? (
                      <div className="grid grid-cols-2 gap-2 text-sm">
                        <div className="rounded-md bg-surface p-3">
                           <p className="text-xs font-black uppercase text-muted">{ui("Evidence")}</p>
                          <p className="mt-1 font-semibold text-espresso">
                             {enumLabel(latestAiResolution.evidenceSufficiency || "UNKNOWN")}
                          </p>
                          <p className="mt-1 text-xs text-muted">
                             {ui("Quality {quality}", {
                               quality: enumLabel(latestAiResolution.evidenceQuality || "UNKNOWN"),
                             })}
                          </p>
                        </div>
                        <div className="rounded-md bg-surface p-3">
                           <p className="text-xs font-black uppercase text-muted">{ui("Assessment")}</p>
                          <p className="mt-1 font-semibold text-espresso">
                             {ui("Likelihood {likelihood}", {
                               likelihood: enumLabel(latestAiResolution.violationLikelihood || "UNKNOWN"),
                             })}
                          </p>
                          <p className="mt-1 text-xs text-muted">
                             {ui("Harm {harm} · Action risk {risk}", {
                               harm: enumLabel(latestAiResolution.harmSeverity || "UNKNOWN"),
                               risk: enumLabel(latestAiResolution.actionRisk || "UNKNOWN"),
                             })}
                          </p>
                        </div>
                      </div>
                    ) : (
                      <div className="rounded-md border border-dashed border-border p-3 text-sm text-muted">
                         {ui("Legacy confidence {confidence} · risk {risk}. These uncalibrated values are not action authority.", {
                           confidence: scoreLabel(latestAiResolution.confidenceScore, localeTag),
                           risk: scoreLabel(latestAiResolution.riskScore, localeTag),
                         })}
                      </div>
                    )}
                    {latestAiResolution.findings?.length ? (
                      <div>
                         <p className="text-xs font-black uppercase text-muted">{ui("Policy findings")}</p>
                        <div className="mt-2 space-y-2">
                          {latestAiResolution.findings.map((finding) => (
                            <div className="rounded-md border border-border p-3" key={`${finding.ruleId}-${finding.ruleVersion}`}>
                              <div className="flex flex-wrap items-center gap-2">
                                <Badge variant="outline">{finding.ruleId}</Badge>
                                <span className="text-xs text-muted">{finding.ruleVersion}</span>
                                <AdminStatusBadge value={finding.outcome} />
                              </div>
                              <p className="mt-2 text-sm text-foreground">{finding.rationale}</p>
                              <p className="mt-2 text-xs text-muted">
                                 {ui("Evidence: {ids}", { ids: finding.evidenceIds.join(", ") || ui("none") })}
                              </p>
                              {finding.missingEvidenceIds.length ? (
                                <p className="mt-1 text-xs text-rating">
                                   {ui("Missing: {ids}", { ids: finding.missingEvidenceIds.join(", ") })}
                                </p>
                              ) : null}
                            </div>
                          ))}
                        </div>
                      </div>
                    ) : null}
                    <div className="grid grid-cols-3 gap-2 text-sm">
                      <div className="rounded-md bg-surface p-3">
                         <p className="text-xs font-black uppercase text-muted">{ui("Used evidence")}</p>
                        <p className="mt-1 font-semibold text-espresso">
                           {formatNumber(latestAiResolution.evidenceSummary?.usedEvidenceIds?.length ?? 0, localeTag)}
                        </p>
                      </div>
                      <div className="rounded-md bg-surface p-3">
                         <p className="text-xs font-black uppercase text-muted">{ui("Counter")}</p>
                        <p className="mt-1 font-semibold text-espresso">
                           {formatNumber(latestAiResolution.evidenceSummary?.counterEvidenceIds?.length ?? 0, localeTag)}
                        </p>
                      </div>
                      <div className="rounded-md bg-surface p-3">
                         <p className="text-xs font-black uppercase text-muted">{ui("Missing")}</p>
                        <p className="mt-1 font-semibold text-espresso">
                           {formatNumber(latestAiResolution.evidenceSummary?.missingEvidenceIds?.length ?? 0, localeTag)}
                        </p>
                      </div>
                    </div>
                    <div>
                      <p className="text-xs font-black uppercase text-muted">
                         {ui("AI rationale — not evidence")}
                      </p>
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
                         <span className="text-sm text-muted">{ui("No labels")}</span>
                      )}
                    </div>
                    <p className="text-xs text-muted">
                       {latestAiResolution.modelName || ui("Unknown model")} -{" "}
                       {formatDate(latestAiResolution.createdAt, localeTag)}
                    </p>
                    {latestAiResolution.contractVersion === "2.0" ? (
                      <div className="rounded-md bg-surface p-3 font-mono text-[11px] leading-5 text-muted">
                         <p>{ui("Policy {value}", { value: latestAiResolution.policyVersion || "—" })}</p>
                         <p>{ui("Rules {value}", { value: latestAiResolution.ruleCatalogVersion || "—" })}</p>
                         <p>{ui("Prompt {value}", { value: latestAiResolution.promptVersion || "—" })}</p>
                         <p>{ui("Workflow {value}", { value: latestAiResolution.workflowVersion || "—" })}</p>
                         <p>{ui("Correlation {value}", { value: latestAiResolution.correlationId || "—" })}</p>
                      </div>
                    ) : null}
                  </div>
                ) : (
                  <div className="mt-5 rounded-md border border-dashed border-border p-4 text-sm text-muted">
                     {ui("No AI recommendation yet. Use Ask AI to request one from the admin resolution workflow.")}
                  </div>
                )}
                <div className="mt-5 rounded-md border border-border bg-surface p-4">
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <p className="text-xs font-black uppercase tracking-[0.08em] text-muted">
                         {ui("Legacy auto-apply history")}
                      </p>
                      <p className="mt-1 text-sm text-muted">
                         {ui("New jobs are disabled in A0. Existing scheduled jobs remain visible so an admin can cancel them.")}
                      </p>
                    </div>
                    <ClockIcon className="size-5 shrink-0 text-primary" />
                  </div>
                  {autoApplyJobsLoading ? (
                    <div className="mt-4 flex items-center gap-2 text-sm text-muted">
                      <LoaderCircleIcon className="size-4 animate-spin" />
                       {ui("Loading auto apply jobs...")}
                    </div>
                  ) : autoApplyJobsError ? (
                    <p className="mt-4 rounded-md border border-accent/30 bg-accent/10 p-3 text-sm text-accent">
                      {autoApplyJobsError}
                    </p>
                  ) : currentAutoApplyJob ? (
                    <div className="mt-4 rounded-md border border-primary/20 bg-primary/5 p-3">
                      <div className="flex flex-wrap items-center gap-2">
                        <AdminStatusBadge value={currentAutoApplyJob.status} />
                        <AdminStatusBadge value={currentAutoApplyJob.reportDecision} />
                        <AdminStatusBadge value={currentAutoApplyJob.targetAction} />
                      </div>
                      <p className="mt-3 text-lg font-bold text-espresso">
                        {currentAutoApplyJob.status === "SCHEDULED"
                           ? countdownLabel(
                               currentAutoApplyJob.scheduledAt,
                               nowMs,
                               localeTag,
                               ui("Due now"),
                             )
                           : ui("Applying now")}
                      </p>
                      <p className="mt-1 text-xs text-muted">
                         {ui("Scheduled at {date}", {
                           date: formatDate(currentAutoApplyJob.scheduledAt, localeTag),
                         })}
                      </p>
                      {currentAutoApplyJob.status === "SCHEDULED" ? (
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          className="mt-3"
                          onClick={() => void handleCancelAutoApply(currentAutoApplyJob)}
                        >
                          <XCircleIcon data-icon="inline-start" />
                           {ui("Cancel auto apply")}
                        </Button>
                      ) : null}
                    </div>
                  ) : autoApplyJobs.length ? (
                    <div className="mt-4 rounded-md border border-border bg-background p-3">
                      <div className="flex flex-wrap items-center gap-2">
                        <AdminStatusBadge value={autoApplyJobs[0].status} />
                        <AdminStatusBadge value={autoApplyJobs[0].targetAction} />
                      </div>
                      <p className="mt-2 text-sm text-muted">
                         {ui("Latest job: {date}", {
                           date: formatDate(autoApplyJobs[0].createdAt, localeTag),
                         })}
                      </p>
                      {autoApplyJobs[0].lastError ? (
                        <p className="mt-2 text-sm text-accent">{autoApplyJobs[0].lastError}</p>
                      ) : null}
                    </div>
                  ) : (
                    <div className="mt-4 rounded-md border border-dashed border-border p-3 text-sm text-muted">
                       {ui("No legacy auto-apply job exists for this report.")}
                    </div>
                  )}
                </div>
                {aiActionError ? (
                  <p className="mt-4 rounded-md border border-accent/30 bg-accent/10 p-3 text-sm text-accent">
                    {aiActionError}
                  </p>
                ) : null}
              </section>
            </div>

          </div>
        ) : null}
      </AdminDetailDialog>
      <AdminReportAiPolicySheet
        open={policySheetOpen}
        report={detailReport}
        latestResolution={latestAiResolution}
        onOpenChange={setPolicySheetOpen}
      />
      <AdminConfirmDialog
        open={Boolean(pendingTargetAction)}
        onOpenChange={(open) => {
          if (!open) {
            setPendingTargetAction(null);
          }
        }}
        title={ui("Change {targetType} content status?", {
          targetType: pendingTargetAction
            ? enumLabel(pendingTargetAction.report.targetType)
            : ui("target"),
        })}
        description={ui("Confirm the target and status transition. This action does not close the report.")}
        confirmLabel={pendingTargetAction
          ? ui("Confirm {status}", { status: enumLabel(pendingTargetAction.status) })
          : ui("Confirm")}
        isSubmitting={isTargetSubmitting}
        onConfirm={handleTargetConfirm}
      >
        {pendingTargetAction ? (
          <div className="rounded-md border border-border bg-surface p-3 text-sm">
            <p className="font-mono text-xs text-muted">{pendingTargetAction.target.id}</p>
            <div className="mt-3 flex flex-wrap items-center gap-2">
              <AdminStatusBadge value={pendingTargetAction.target.status} />
              <span aria-hidden="true">→</span>
              <AdminStatusBadge value={pendingTargetAction.status} />
            </div>
            <p className="mt-3 font-semibold text-espresso">
              {ui("The report remains {status}.", {
                status: enumLabel(pendingTargetAction.report.status),
              })}
            </p>
          </div>
        ) : null}
      </AdminConfirmDialog>
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
