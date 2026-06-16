"use client";

import { useMemo, useState } from "react";
import { EyeIcon } from "lucide-react";
import { AdminConfirmDialog } from "@/components/admin/admin-confirm-dialog";
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
  textPreview,
  Toolbar,
  useAdminDetailResource,
  usePagedAdminResource,
} from "@/components/admin/admin-page-utils";
import { Button } from "@/components/ui/button";
import { getAdminReport, getReports, updateReportStatus } from "@/lib/api/admin";
import type { ContentReport, ReportStatus, ReportTargetType } from "@/types/admin";

const reportStatuses: ReportStatus[] = ["OPEN", "REVIEWING", "RESOLVED", "REJECTED"];
const targetTypes: ReportTargetType[] = ["BLOG", "COMMENT", "USER", "CAFE_PAGE"];

type PendingReportAction = { report: ContentReport; status: ReportStatus };

export function AdminReportsPage() {
  const [status, setStatus] = useState<ReportStatus | "">("");
  const [targetType, setTargetType] = useState<ReportTargetType | "">("");
  const [pendingAction, setPendingAction] = useState<PendingReportAction | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const detail = useAdminDetailResource<ContentReport>();

  const resource = usePagedAdminResource(
    (page, signal) =>
      getReports({ status, targetType, page, size: PAGE_SIZE }, signal),
    [status, targetType],
  );

  const columns = useMemo<AdminTableColumn<ContentReport>[]>(
    () => [
      {
        header: "Report",
        cell: (report) => (
          <div className="max-w-md">
            <p className="font-bold text-espresso">{report.reason}</p>
            <p className="mt-1 text-sm leading-6 text-muted">
              {textPreview(report.description)}
            </p>
          </div>
        ),
      },
      { header: "Reporter", cell: (report) => report.reporterUserName || report.reporterUserId.slice(0, 8) },
      { header: "Target", cell: (report) => report.targetType },
      { header: "Status", cell: (report) => <AdminStatusBadge value={report.status} /> },
      { header: "Created", cell: (report) => formatDate(report.createdAt) },
      {
        header: "Actions",
        className: "w-80",
        cell: (report) => (
          <div className="flex flex-wrap gap-2">
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => detail.load((signal) => getAdminReport(report.id, signal))}
            >
              <EyeIcon data-icon="inline-start" />
              View
            </Button>
            {reportStatuses
              .filter((nextStatus) => nextStatus !== report.status)
              .slice(0, 3)
              .map((nextStatus) => (
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  key={nextStatus}
                  onClick={() => setPendingAction({ report, status: nextStatus })}
                >
                  {nextStatus}
                </Button>
              ))}
          </div>
        ),
      },
    ],
    [detail],
  );

  async function handleConfirm() {
    if (!pendingAction) {
      return;
    }

    setIsSubmitting(true);
    setActionError(null);

    try {
      const updatedReport = await updateReportStatus(
        pendingAction.report.id,
        pendingAction.status,
      );
      if (detail.data?.id === updatedReport.id) {
        detail.setData(updatedReport);
      }
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

  return (
    <div className="flex flex-col gap-6">
      <AdminPageHeader
        title="Reports"
        description="Review user reports by target type and resolution status."
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
      <AdminDataTable
        columns={columns}
        rows={resource.rows}
        getRowKey={(report) => report.id}
        isLoading={resource.isLoading}
        error={resource.error}
      />
      <AdminPagination page={resource.data} onPageChange={resource.setPageNumber} />
      <AdminDetailDialog
        open={detail.open}
        onOpenChange={detail.setOpen}
        title="Report detail"
        description={detail.data ? detail.data.id : "Latest detail from admin API"}
        isLoading={detail.isLoading}
        error={detail.error}
      >
        {detail.data ? (
          <AdminDetailGrid>
            <AdminDetailField label="Reason">{detail.data.reason}</AdminDetailField>
            <AdminDetailField label="Status">
              <AdminStatusBadge value={detail.data.status} />
            </AdminDetailField>
            <AdminDetailField label="Reporter">
              {detail.data.reporterUserName || detail.data.reporterUserId}
            </AdminDetailField>
            <AdminDetailField label="Reporter ID">{detail.data.reporterUserId}</AdminDetailField>
            <AdminDetailField label="Target type">{detail.data.targetType}</AdminDetailField>
            <AdminDetailField label="Target ID">{detail.data.targetId}</AdminDetailField>
            <AdminDetailField label="Blog">{detail.data.blogId || "-"}</AdminDetailField>
            <AdminDetailField label="Comment">{detail.data.commentId || "-"}</AdminDetailField>
            <AdminDetailField label="Reported user">{detail.data.reportedUserId || "-"}</AdminDetailField>
            <AdminDetailField label="Cafe page">{detail.data.cafePageId || "-"}</AdminDetailField>
            <AdminDetailField label="Created">{formatDate(detail.data.createdAt)}</AdminDetailField>
            <AdminDetailField label="Resolved">{formatDate(detail.data.resolvedAt)}</AdminDetailField>
            <AdminDetailField label="Description" className="sm:col-span-2">
              <p className="whitespace-pre-wrap leading-6">{detail.data.description || "-"}</p>
            </AdminDetailField>
          </AdminDetailGrid>
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
        title="Update report status"
        description="This updates the selected content report."
        confirmLabel="Update"
        isSubmitting={isSubmitting}
        onConfirm={handleConfirm}
      >
        {actionError ? <p className="text-sm text-accent">{actionError}</p> : null}
      </AdminConfirmDialog>
    </div>
  );
}
