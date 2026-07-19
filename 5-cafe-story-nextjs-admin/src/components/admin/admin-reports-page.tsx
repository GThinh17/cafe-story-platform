"use client";

import { useEffect, useMemo, useState } from "react";
import {
  BotIcon,
  CheckCircle2Icon,
  ClockIcon,
  EyeIcon,
  LoaderCircleIcon,
  ListChecksIcon,
  SparklesIcon,
  XCircleIcon,
} from "lucide-react";
import { AdminConfirmDialog } from "@/components/admin/admin-confirm-dialog";
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
  getAdminReport,
  getReportAiAutoResolutions,
  getReportAiResolutions,
  getReports,
  resolveReport,
  updateReportStatus,
} from "@/lib/api/admin";
import type { PageResponse } from "@/types/api";
import type {
  AdminReportAiAutoApplyJob,
  AdminReportAiAutoApplyRequest,
  AdminReportAiResolution,
  ContentReport,
  ReportStatus,
  ReportTargetType,
} from "@/types/admin";

const reportStatuses: ReportStatus[] = ["OPEN", "REVIEWING", "RESOLVED", "REJECTED"];
const targetTypes: ReportTargetType[] = ["BLOG", "COMMENT", "USER", "CAFE_PAGE"];
const AI_HISTORY_SIZE = 8;
const BULK_FETCH_SIZE = 100;
const AUTO_APPLY_HISTORY_SIZE = 8;
const AUTO_APPLY_DELAYS = [
  { label: "15m", value: 15 },
  { label: "30m", value: 30 },
  { label: "1h", value: 60 },
  { label: "2h", value: 120 },
  { label: "6h", value: 360 },
  { label: "12h", value: 720 },
];

const REPORT_STATUS_LABELS: Record<ReportStatus, string> = {
  OPEN: "Reopen",
  REVIEWING: "Mark reviewing",
  RESOLVED: "Resolve",
  REJECTED: "Reject",
};

type PendingReportAction =
  | { kind: "resolve"; report: ContentReport }
  | { kind: "status"; report: ContentReport; status: ReportStatus };

type BulkAiMode = "filtered" | "selected";

type BulkAiProgress = {
  done: number;
  failed: number;
  scheduled: number;
  skipped: number;
  total: number;
};

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

function autoApplyRequest(enabled: boolean, delayMinutes: number): AdminReportAiAutoApplyRequest | undefined {
  if (!enabled) {
    return undefined;
  }

  return {
    autoApplyEnabled: true,
    autoApplyDelayMinutes: delayMinutes,
  };
}

function activeAutoApplyJob(jobs: AdminReportAiAutoApplyJob[]) {
  return jobs.find((job) => job.status === "SCHEDULED" || job.status === "APPLYING") ?? null;
}

function countdownLabel(scheduledAt: string, nowMs: number) {
  const remainingMs = new Date(scheduledAt).getTime() - nowMs;

  if (remainingMs <= 0) {
    return "Due now";
  }

  const totalSeconds = Math.ceil(remainingMs / 1000);
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;

  if (hours > 0) {
    return `${hours}h ${minutes}m`;
  }

  if (minutes > 0) {
    return `${minutes}m ${seconds}s`;
  }

  return `${seconds}s`;
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

function sleep(ms: number) {
  return new Promise((resolve) => window.setTimeout(resolve, ms));
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
  const [autoApplyJobs, setAutoApplyJobs] = useState<AdminReportAiAutoApplyJob[]>([]);
  const [autoApplyJobsLoading, setAutoApplyJobsLoading] = useState(false);
  const [autoApplyJobsError, setAutoApplyJobsError] = useState<string | null>(null);
  const [autoApplyJobsPage, setAutoApplyJobsPage] =
    useState<PageResponse<AdminReportAiAutoApplyJob> | null>(null);
  const [askAiDialogOpen, setAskAiDialogOpen] = useState(false);
  const [askAiReport, setAskAiReport] = useState<ContentReport | null>(null);
  const [askAiAutoApplyEnabled, setAskAiAutoApplyEnabled] = useState(false);
  const [askAiDelayMinutes, setAskAiDelayMinutes] = useState(15);
  const [bulkDialogOpen, setBulkDialogOpen] = useState(false);
  const [bulkMode, setBulkMode] = useState<BulkAiMode>("filtered");
  const [bulkAutoApplyEnabled, setBulkAutoApplyEnabled] = useState(false);
  const [bulkDelayMinutes, setBulkDelayMinutes] = useState(15);
  const [bulkAutoApplyConfirmed, setBulkAutoApplyConfirmed] = useState(false);
  const [selectedBulkReportIds, setSelectedBulkReportIds] = useState<Set<string>>(
    () => new Set(),
  );
  const [bulkError, setBulkError] = useState<string | null>(null);
  const [bulkRunning, setBulkRunning] = useState(false);
  const [bulkProgress, setBulkProgress] = useState<BulkAiProgress>({
    done: 0,
    failed: 0,
    scheduled: 0,
    skipped: 0,
    total: 0,
  });
  const [nowMs, setNowMs] = useState(() => Date.now());
  const detail = useAdminDetailResource<ContentReport>();

  useEffect(() => {
    const intervalId = window.setInterval(() => setNowMs(Date.now()), 1000);

    return () => window.clearInterval(intervalId);
  }, []);

  const resource = usePagedAdminResource(
    (page, signal) =>
      getReports({ status, targetType, page, size: PAGE_SIZE }, signal),
    [status, targetType],
  );

  const currentPageReportIds = useMemo(
    () => resource.rows.map((report) => report.id),
    [resource.rows],
  );
  const selectedBulkReports = useMemo(
    () => resource.rows.filter((report) => selectedBulkReportIds.has(report.id)),
    [resource.rows, selectedBulkReportIds],
  );
  const allCurrentPageSelected =
    currentPageReportIds.length > 0 &&
    currentPageReportIds.every((reportId) => selectedBulkReportIds.has(reportId));

  function openBulkDialog() {
    setBulkDialogOpen(true);
    setBulkMode("filtered");
    setBulkAutoApplyEnabled(false);
    setBulkDelayMinutes(15);
    setBulkAutoApplyConfirmed(false);
    setBulkError(null);
    setBulkProgress({ done: 0, failed: 0, scheduled: 0, skipped: 0, total: 0 });
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

    return reports;
  }

  async function handleBulkAskAi() {
    setBulkRunning(true);
    setBulkError(null);
    setAiActionError(null);
    setBulkProgress({ done: 0, failed: 0, scheduled: 0, skipped: 0, total: 0 });

    try {
      const reports =
        bulkMode === "filtered" ? await getAllFilteredReports() : selectedBulkReports;

      if (!reports.length) {
        setBulkError(
          bulkMode === "filtered"
            ? "No reports match the current filters."
            : "Select at least one report.",
        );
        return;
      }

      setBulkProgress({ done: 0, failed: 0, scheduled: 0, skipped: 0, total: reports.length });

      for (const report of reports) {
        try {
          const createdResolution = await createReportAiResolution(
            report.id,
            autoApplyRequest(bulkAutoApplyEnabled, bulkDelayMinutes),
          );

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
            scheduled: current.scheduled + (createdResolution.autoApplyJob ? 1 : 0),
            skipped: current.skipped + (createdResolution.autoApplyWarning ? 1 : 0),
          }));
        } catch {
          setBulkProgress((current) => ({
            ...current,
            done: current.done + 1,
            failed: current.failed + 1,
          }));
        }

        await sleep(250);
      }

      if (detail.data) {
        await loadAiHistory(detail.data.id);
        await loadAutoApplyJobs(detail.data.id);
      }

      resource.refetch();
    } catch (requestError) {
      setBulkError(
        requestError instanceof Error
          ? requestError.message
          : "Bulk AI recommendation failed.",
      );
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
      setAutoApplyJobsError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to load auto apply jobs.",
      );
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
    setAutoApplyJobs([]);
    setAutoApplyJobsPage(null);
    void detail.load((signal) => getAdminReport(report.id, signal));
    void loadAiHistory(report.id);
    void loadAutoApplyJobs(report.id);
  }

  function openAskAiDialog(report: ContentReport) {
    setAskAiReport(report);
    setAskAiAutoApplyEnabled(false);
    setAskAiDelayMinutes(15);
    setAiActionError(null);
    setAskAiDialogOpen(true);
  }

  async function handleAskAi(report: ContentReport, request?: AdminReportAiAutoApplyRequest) {
    setAiLoadingReportId(report.id);
    setAiActionError(null);

    try {
      const createdResolution = await createReportAiResolution(report.id, request);

      if (detail.data?.id === report.id) {
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
        setAiActionError(createdResolution.autoApplyWarning);
      }
      setAskAiDialogOpen(false);
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

  async function handleAskAiDialogSubmit() {
    if (!askAiReport) {
      return;
    }

    await handleAskAi(
      askAiReport,
      autoApplyRequest(askAiAutoApplyEnabled, askAiDelayMinutes),
    );
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
      setAiActionError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to cancel auto apply job.",
      );
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
                label: "Ask AI",
                icon: SparklesIcon,
                disabled: aiLoadingReportId === report.id,
                onSelect: () => openAskAiDialog(report),
              },
              ...(report.status !== "RESOLVED"
                ? [
                    {
                      label: "Resolve",
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
            AI resolve all
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
            <DialogTitle>AI resolve reports</DialogTitle>
            <DialogDescription className="mt-1">
              Create AI recommendations in bulk. This does not resolve reports or change target content.
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
                  All matching filters
                </span>
                <span className="mt-2 block text-sm leading-6 text-muted">
                  Run AI for every report matching status and target filters, across all pages.
                </span>
                <span className="mt-3 block text-xs text-muted">
                  Current filters: {status || "all statuses"} / {targetType || "all targets"}
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
                  Selected reports
                </span>
                <span className="mt-2 block text-sm leading-6 text-muted">
                  Run AI only for reports selected from the current page.
                </span>
                <span className="mt-3 block text-xs text-muted">
                  Selected: {selectedBulkReports.length} of {resource.rows.length}
                </span>
              </button>
            </div>

            <div className="mt-5 rounded-md border border-border bg-background p-4">
              <label className="flex items-start gap-3">
                <input
                  type="checkbox"
                  className="mt-1 size-4 accent-primary"
                  checked={bulkAutoApplyEnabled}
                  disabled={bulkRunning}
                  onChange={(event) => {
                    setBulkAutoApplyEnabled(event.target.checked);
                    setBulkAutoApplyConfirmed(false);
                  }}
                />
                <span>
                  <span className="block text-sm font-bold text-espresso">
                    Auto apply after delay
                  </span>
                  <span className="mt-1 block text-sm leading-6 text-muted">
                    Only high-confidence recommendations are scheduled. Admin can cancel before the countdown ends.
                  </span>
                </span>
              </label>
              {bulkAutoApplyEnabled ? (
                <div className="mt-4 flex flex-col gap-3">
                  <div className="flex flex-wrap gap-2">
                    {AUTO_APPLY_DELAYS.map((option) => (
                      <Button
                        type="button"
                        variant={bulkDelayMinutes === option.value ? "default" : "outline"}
                        size="sm"
                        disabled={bulkRunning}
                        key={option.value}
                        onClick={() => setBulkDelayMinutes(option.value)}
                      >
                        {option.label}
                      </Button>
                    ))}
                  </div>
                  <label className="flex items-start gap-3 rounded-md border border-accent/20 bg-accent/5 p-3">
                    <input
                      type="checkbox"
                      className="mt-1 size-4 accent-primary"
                      checked={bulkAutoApplyConfirmed}
                      disabled={bulkRunning}
                      onChange={(event) => setBulkAutoApplyConfirmed(event.target.checked)}
                    />
                    <span className="text-sm leading-6 text-foreground">
                      I understand this may schedule target actions for every matching report in this bulk run.
                    </span>
                  </label>
                </div>
              ) : null}
            </div>

            <div className="mt-5 rounded-md border border-border bg-background">
              <div className="flex flex-wrap items-center justify-between gap-3 border-b border-border px-4 py-3">
                <div>
                  <p className="text-sm font-bold text-espresso">Current page selection</p>
                  <p className="mt-1 text-xs text-muted">
                    Selection is used when the Selected reports mode is active.
                  </p>
                </div>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  disabled={bulkRunning || !resource.rows.length}
                  onClick={toggleCurrentPageSelection}
                >
                  {allCurrentPageSelected ? "Clear page" : "Select page"}
                </Button>
              </div>
              <div className="max-h-72 overflow-y-auto">
                {resource.rows.map((report) => (
                  <label
                    className="flex cursor-pointer items-start gap-3 border-b border-border px-4 py-3 last:border-b-0 hover:bg-surface-muted/40"
                    key={report.id}
                  >
                    <input
                      type="checkbox"
                      className="mt-1 size-4 accent-primary"
                      checked={selectedBulkReportIds.has(report.id)}
                      disabled={bulkRunning}
                      onChange={() => toggleBulkReport(report.id)}
                    />
                    <span className="min-w-0 flex-1">
                      <span className="flex flex-wrap items-center gap-2">
                        <span className="font-semibold text-espresso">
                          {reportReason(report)}
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
                    No reports on this page.
                  </div>
                ) : null}
              </div>
            </div>

            {bulkProgress.total > 0 ? (
              <div className="mt-5 rounded-md border border-border bg-background p-4">
                <div className="flex flex-wrap gap-4 text-sm text-muted">
                  <span>Success: {bulkSuccess}</span>
                  <span>Scheduled: {bulkProgress.scheduled}</span>
                  <span>Skipped: {bulkProgress.skipped}</span>
                  <span>Failed: {bulkProgress.failed}</span>
                  <span>Remaining: {bulkRemaining}</span>
                  <span>Total: {bulkProgress.total}</span>
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
              <p className="mt-4 rounded-md border border-accent/30 bg-accent/10 p-3 text-sm text-accent">
                {bulkError}
              </p>
            ) : null}
          </div>
          <div className="flex flex-wrap justify-end gap-2 border-t border-border px-5 py-4">
            <Button
              type="button"
              variant="outline"
              disabled={bulkRunning}
              onClick={() => setBulkDialogOpen(false)}
            >
              Close
            </Button>
            <Button
              type="button"
              disabled={
                bulkRunning ||
                (bulkMode === "selected" && selectedBulkReports.length === 0) ||
                (bulkAutoApplyEnabled && !bulkAutoApplyConfirmed)
              }
              onClick={() => void handleBulkAskAi()}
            >
              {bulkRunning ? (
                <LoaderCircleIcon className="animate-spin" data-icon="inline-start" />
              ) : (
                <SparklesIcon data-icon="inline-start" />
              )}
              {bulkRunning ? "Running..." : "Run AI"}
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
            <DialogTitle>Ask AI for report resolution</DialogTitle>
            <DialogDescription className="mt-1">
              Create a recommendation now. Auto apply is optional and can be cancelled before the scheduled time.
            </DialogDescription>
          </div>
          <div className="px-5 py-4">
            {askAiReport ? (
              <div className="rounded-md border border-border bg-surface p-4">
                <div className="flex flex-wrap items-center gap-2">
                  <span className="font-semibold text-espresso">
                    {reportReason(askAiReport)}
                  </span>
                  <AdminStatusBadge value={askAiReport.status} />
                  <AdminStatusBadge value={askAiReport.targetType} />
                </div>
                <p className="mt-2 text-sm text-muted">
                  {textPreview(askAiReport.description, 160)}
                </p>
              </div>
            ) : null}

            <div className="mt-4 rounded-md border border-border bg-background p-4">
              <label className="flex items-start gap-3">
                <input
                  type="checkbox"
                  className="mt-1 size-4 accent-primary"
                  checked={askAiAutoApplyEnabled}
                  disabled={Boolean(aiLoadingReportId)}
                  onChange={(event) => setAskAiAutoApplyEnabled(event.target.checked)}
                />
                <span>
                  <span className="block text-sm font-bold text-espresso">
                    Auto apply after delay
                  </span>
                  <span className="mt-1 block text-sm leading-6 text-muted">
                    BE schedules the countdown. Closing this tab will not cancel the job.
                  </span>
                </span>
              </label>
              {askAiAutoApplyEnabled ? (
                <div className="mt-4 flex flex-wrap gap-2">
                  {AUTO_APPLY_DELAYS.map((option) => (
                    <Button
                      type="button"
                      variant={askAiDelayMinutes === option.value ? "default" : "outline"}
                      size="sm"
                      disabled={Boolean(aiLoadingReportId)}
                      key={option.value}
                      onClick={() => setAskAiDelayMinutes(option.value)}
                    >
                      {option.label}
                    </Button>
                  ))}
                </div>
              ) : null}
            </div>

            <div className="mt-4 rounded-md border border-dashed border-border p-3 text-sm leading-6 text-muted">
              Auto apply only schedules high-confidence decisions. Low confidence or manual-review results are saved as recommendations without a scheduled action.
            </div>
          </div>
          <div className="flex flex-wrap justify-end gap-2 border-t border-border px-5 py-4">
            <Button
              type="button"
              variant="outline"
              disabled={Boolean(aiLoadingReportId)}
              onClick={() => setAskAiDialogOpen(false)}
            >
              Close
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
              {aiLoadingReportId ? "Running..." : "Ask AI"}
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
              onClick={() => openAskAiDialog(detailReport)}
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
                <div className="mt-5 rounded-md border border-border bg-surface p-4">
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <p className="text-xs font-black uppercase tracking-[0.08em] text-muted">
                        Auto apply
                      </p>
                      <p className="mt-1 text-sm text-muted">
                        Scheduled actions run in backend and can be cancelled before they apply.
                      </p>
                    </div>
                    <ClockIcon className="size-5 shrink-0 text-primary" />
                  </div>
                  {autoApplyJobsLoading ? (
                    <div className="mt-4 flex items-center gap-2 text-sm text-muted">
                      <LoaderCircleIcon className="size-4 animate-spin" />
                      Loading auto apply jobs...
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
                          ? countdownLabel(currentAutoApplyJob.scheduledAt, nowMs)
                          : "Applying now"}
                      </p>
                      <p className="mt-1 text-xs text-muted">
                        Scheduled at {formatDate(currentAutoApplyJob.scheduledAt)}
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
                          Cancel auto apply
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
                        Latest job: {formatDate(autoApplyJobs[0].createdAt)}
                      </p>
                      {autoApplyJobs[0].lastError ? (
                        <p className="mt-2 text-sm text-accent">{autoApplyJobs[0].lastError}</p>
                      ) : null}
                    </div>
                  ) : (
                    <div className="mt-4 rounded-md border border-dashed border-border p-3 text-sm text-muted">
                      No auto apply job has been scheduled for this report.
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
                  onClick={() => {
                    void loadAiHistory(detailReport.id);
                    void loadAutoApplyJobs(detailReport.id);
                  }}
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
