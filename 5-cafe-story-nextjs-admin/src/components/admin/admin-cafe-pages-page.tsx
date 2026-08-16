"use client";

import { useMemo, useState } from "react";
import { EyeIcon, ShieldOffIcon } from "lucide-react";
import { AdminConfirmDialog } from "@/components/admin/admin-confirm-dialog";
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
  FilterInput,
  FilterSelect,
  formatDate,
  PAGE_SIZE,
  Toolbar,
  useAdminDetailResource,
  usePagedAdminResource,
} from "@/components/admin/admin-page-utils";
import {
  getAdminCafePage,
  getCafePages,
  updateCafePageStatus,
} from "@/lib/api/admin";
import {
  formatNumber,
  localizeApiError,
  useEnumLabel,
  useI18n,
  useUiText,
} from "@/features/i18n";
import type { CafePage, PageStatus } from "@/types/admin";

const pageStatuses: PageStatus[] = ["DRAFT", "ACTIVE", "SUSPENDED"];

type PendingCafeAction = { type: "status"; cafe: CafePage; status: PageStatus };

export function AdminCafePagesPage() {
  const { locale, localeTag, t } = useI18n();
  const ui = useUiText();
  const enumLabel = useEnumLabel();
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
            {ui("{likes} likes · {followers} followers", {
              likes: formatNumber(cafe.likeCount ?? 0, localeTag),
              followers: formatNumber(cafe.followerCount ?? 0, localeTag),
            })}
          </span>
        ),
      },
      { header: "Rating", cell: (cafe) => cafe.ratingScore ?? "—" },
      { header: "Created", cell: (cafe) => formatDate(cafe.createdAt, localeTag) },
      {
        header: "",
        className: "w-12 text-right",
        cell: (cafe) => (
          <AdminRowActions
            actions={[
              {
                label: "View detail",
                icon: EyeIcon,
                onSelect: () =>
                  detail.load((signal) => getAdminCafePage(cafe.id, signal)),
              },
              ...pageStatuses
                .filter((nextStatus) => nextStatus !== cafe.status)
                .map((nextStatus) => ({
                  label: ui("Set status: {status}", {
                    status: enumLabel(nextStatus),
                  }),
                  icon: nextStatus === "SUSPENDED" ? ShieldOffIcon : undefined,
                  destructive: nextStatus === "SUSPENDED",
                  onSelect: () =>
                    setPendingAction({ type: "status", cafe, status: nextStatus }),
                })),
            ]}
          />
        ),
      },
    ],
    [detail, enumLabel, localeTag, ui],
  );

  async function handleConfirm() {
    if (!pendingAction) {
      return;
    }

    setIsSubmitting(true);
    setActionError(null);

    try {
      const updatedCafe = await updateCafePageStatus(
        pendingAction.cafe.id,
        pendingAction.status,
      );
      if (detail.data?.id === updatedCafe.id) {
        detail.setData(updatedCafe);
      }

      setPendingAction(null);
      resource.refetch();
    } catch (requestError) {
      setActionError(localizeApiError(requestError, locale, t, "common.error.action"));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="flex flex-col gap-4">
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
        <FilterInput label="Owner user ID" value={ownerUserId} placeholder="Enter user ID" onChange={setOwnerUserId} />
      </Toolbar>
      <AdminDataTable
        columns={columns}
        rows={resource.rows}
        getRowKey={(cafe) => cafe.id}
        isLoading={resource.isLoading}
        error={resource.error}
        onRowClick={(cafe) =>
          detail.load((signal) => getAdminCafePage(cafe.id, signal))
        }
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
              {ui("{likes} likes / {followers} followers", {
                likes: formatNumber(detail.data.likeCount ?? 0, localeTag),
                followers: formatNumber(detail.data.followerCount ?? 0, localeTag),
              })}
            </AdminDetailField>
            <AdminDetailField label="Rating">
              {ui("{rating} ({count} ratings)", {
                rating: detail.data.ratingScore ?? "—",
                count: formatNumber(detail.data.ratingCount ?? 0, localeTag),
              })}
            </AdminDetailField>
            <AdminDetailField label="Max members">{detail.data.maxMembers ?? "-"}</AdminDetailField>
            <AdminDetailField label="Expires">{formatDate(detail.data.pageExpiresAt, localeTag)}</AdminDetailField>
            <AdminDetailField label="Created">{formatDate(detail.data.createdAt, localeTag)}</AdminDetailField>
            <AdminDetailField label="Updated">{formatDate(detail.data.updatedAt, localeTag)}</AdminDetailField>
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
