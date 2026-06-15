"use client";

import { useMemo, useState } from "react";
import { AdminConfirmDialog } from "@/components/admin/admin-confirm-dialog";
import {
  AdminDataTable,
  AdminPagination,
  type AdminTableColumn,
} from "@/components/admin/admin-data-table";
import { AdminPageHeader } from "@/components/admin/admin-page-header";
import { AdminStatusBadge } from "@/components/admin/admin-status-badge";
import {
  FilterSelect,
  formatDate,
  PAGE_SIZE,
  textPreview,
  Toolbar,
  usePagedAdminResource,
} from "@/components/admin/admin-page-utils";
import { Button } from "@/components/ui/button";
import { getReports, updateReportStatus } from "@/lib/api/admin";
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
        className: "w-72",
        cell: (report) => (
          <div className="flex flex-wrap gap-2">
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
    [],
  );

  async function handleConfirm() {
    if (!pendingAction) {
      return;
    }

    setIsSubmitting(true);
    setActionError(null);

    try {
      await updateReportStatus(pendingAction.report.id, pendingAction.status);
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
          value={status}
          options={reportStatuses}
          placeholder="Status"
          onChange={setStatus}
        />
        <FilterSelect
          value={targetType}
          options={targetTypes}
          placeholder="Target"
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
