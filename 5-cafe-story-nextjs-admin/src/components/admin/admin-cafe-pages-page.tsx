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
  FilterInput,
  FilterSelect,
  formatDate,
  PAGE_SIZE,
  Toolbar,
  useAdminDetailResource,
  usePagedAdminResource,
} from "@/components/admin/admin-page-utils";
import { Button } from "@/components/ui/button";
import {
  deleteCafePage,
  getAdminCafePage,
  getCafePages,
  updateCafePageStatus,
} from "@/lib/api/admin";
import type { CafePage, PageStatus } from "@/types/admin";

const pageStatuses: PageStatus[] = ["DRAFT", "ACTIVE", "SUSPENDED"];

type PendingCafeAction =
  | { type: "status"; cafe: CafePage; status: PageStatus }
  | { type: "delete"; cafe: CafePage };

export function AdminCafePagesPage() {
  const [status, setStatus] = useState<PageStatus | "">("");
  const [ownerUserId, setOwnerUserId] = useState("");
  const [pendingAction, setPendingAction] = useState<PendingCafeAction | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const detail = useAdminDetailResource<CafePage>();

  const resource = usePagedAdminResource(
    (page, signal) =>
      getCafePages({ status, ownerUserId, page, size: PAGE_SIZE }, signal),
    [status, ownerUserId],
  );

  const columns = useMemo<AdminTableColumn<CafePage>[]>(
    () => [
      {
        header: "Cafe",
        cell: (cafe) => (
          <div className="max-w-sm">
            <p className="font-bold text-espresso">{cafe.name}</p>
            <p className="mt-1 text-xs text-muted">{cafe.address || cafe.regionCity || "—"}</p>
          </div>
        ),
      },
      { header: "Status", cell: (cafe) => <AdminStatusBadge value={cafe.status} /> },
      { header: "Active", cell: (cafe) => <AdminStatusBadge value={cafe.pageActive} /> },
      {
        header: "Audience",
        cell: (cafe) => (
          <span className="text-muted">
            {cafe.likeCount ?? 0} likes · {cafe.followerCount ?? 0} followers
          </span>
        ),
      },
      { header: "Rating", cell: (cafe) => cafe.ratingScore ?? "—" },
      { header: "Created", cell: (cafe) => formatDate(cafe.createdAt) },
      {
        header: "Actions",
        className: "w-80",
        cell: (cafe) => (
          <div className="flex flex-wrap gap-2">
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => detail.load((signal) => getAdminCafePage(cafe.id, signal))}
            >
              <EyeIcon data-icon="inline-start" />
              View
            </Button>
            {pageStatuses
              .filter((nextStatus) => nextStatus !== cafe.status)
              .map((nextStatus) => (
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  key={nextStatus}
                  onClick={() => setPendingAction({ type: "status", cafe, status: nextStatus })}
                >
                  {nextStatus}
                </Button>
              ))}
            <Button
              type="button"
              variant="destructive"
              size="sm"
              onClick={() => setPendingAction({ type: "delete", cafe })}
            >
              Delete
            </Button>
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
      if (pendingAction.type === "status") {
        const updatedCafe = await updateCafePageStatus(
          pendingAction.cafe.id,
          pendingAction.status,
        );
        if (detail.data?.id === updatedCafe.id) {
          detail.setData(updatedCafe);
        }
      } else {
        await deleteCafePage(pendingAction.cafe.id);
        if (detail.data?.id === pendingAction.cafe.id) {
          detail.setOpen(false);
          detail.setData(null);
        }
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
        title="Cafe Pages"
        description="Manage cafe page status, active packages, audience signals, and removals."
      />
      <Toolbar onRefresh={resource.refetch}>
        <FilterSelect
          label="Status"
          value={status}
          options={pageStatuses}
          placeholder="All statuses"
          onChange={setStatus}
        />
        <FilterInput value={ownerUserId} placeholder="Owner user ID" onChange={setOwnerUserId} />
      </Toolbar>
      <AdminDataTable
        columns={columns}
        rows={resource.rows}
        getRowKey={(cafe) => cafe.id}
        isLoading={resource.isLoading}
        error={resource.error}
      />
      <AdminPagination page={resource.data} onPageChange={resource.setPageNumber} />
      <AdminDetailDialog
        open={detail.open}
        onOpenChange={detail.setOpen}
        title="Cafe page detail"
        description={detail.data ? detail.data.id : "Latest detail from admin API"}
        isLoading={detail.isLoading}
        error={detail.error}
      >
        {detail.data ? (
          <AdminDetailGrid>
            <AdminDetailField label="Name">{detail.data.name}</AdminDetailField>
            <AdminDetailField label="Owner">{detail.data.ownerUserId}</AdminDetailField>
            <AdminDetailField label="Status">
              <AdminStatusBadge value={detail.data.status} />
            </AdminDetailField>
            <AdminDetailField label="Active">
              <AdminStatusBadge value={detail.data.pageActive} />
            </AdminDetailField>
            <AdminDetailField label="Address" className="sm:col-span-2">
              {detail.data.address ||
                [detail.data.regionStreet, detail.data.regionWard, detail.data.regionCity]
                  .filter(Boolean)
                  .join(", ") ||
                "-"}
            </AdminDetailField>
            <AdminDetailField label="Description" className="sm:col-span-2">
              {detail.data.description || "-"}
            </AdminDetailField>
            <AdminDetailField label="Audience">
              {detail.data.likeCount ?? 0} likes / {detail.data.followerCount ?? 0} followers
            </AdminDetailField>
            <AdminDetailField label="Rating">
              {detail.data.ratingScore ?? "-"} ({detail.data.ratingCount ?? 0} ratings)
            </AdminDetailField>
            <AdminDetailField label="Max members">{detail.data.maxMembers ?? "-"}</AdminDetailField>
            <AdminDetailField label="Expires">{formatDate(detail.data.pageExpiresAt)}</AdminDetailField>
            <AdminDetailField label="Created">{formatDate(detail.data.createdAt)}</AdminDetailField>
            <AdminDetailField label="Updated">{formatDate(detail.data.updatedAt)}</AdminDetailField>
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
        title="Confirm cafe page action"
        description="This action updates the selected cafe page immediately."
        confirmLabel="Apply"
        isSubmitting={isSubmitting}
        onConfirm={handleConfirm}
      >
        {actionError ? <p className="text-sm text-accent">{actionError}</p> : null}
      </AdminConfirmDialog>
    </div>
  );
}
