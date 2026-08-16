"use client";

import { useMemo, useState } from "react";
import { EyeIcon } from "lucide-react";
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
  FilterInput,
  FilterSelect,
  formatDate,
  PAGE_SIZE,
  Toolbar,
  useAdminDetailResource,
  usePagedAdminResource,
} from "@/components/admin/admin-page-utils";
import {
  getAdminComment,
  getComments,
  updateCommentStatus,
} from "@/lib/api/admin";
import { localizeApiError, useEnumLabel, useI18n, useUiText } from "@/features/i18n";
import type { Comment, PostStatus } from "@/types/admin";

const postStatuses: PostStatus[] = ["DRAFT", "PUBLISHED", "HIDDEN", "REMOVED"];

type PendingCommentAction = {
  type: "status";
  comment: Comment;
  status: PostStatus;
};

export function AdminCommentsPage() {
  const { locale, localeTag, t } = useI18n();
  const ui = useUiText();
  const enumLabel = useEnumLabel();
  const [status, setStatus] = useState<PostStatus | "">("");
  const [blogId, setBlogId] = useState("");
  const [userId, setUserId] = useState("");
  const [pendingAction, setPendingAction] = useState<PendingCommentAction | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const detail = useAdminDetailResource<Comment>();

  const resource = usePagedAdminResource(
    (page, signal) =>
      getComments({ status, blogId, userId, page, size: PAGE_SIZE }, signal),
    [status, blogId, userId],
  );

  const columns = useMemo<AdminTableColumn<Comment>[]>(
    () => [
      {
        header: "User",
        cell: (comment) => (
          <UserCell name={comment.authorUserName} avatar={comment.authorUserAvatar} subtitle={comment.userId.slice(0, 8)} />
        ),
      },
      { header: "ID", cell: (comment) => comment.userId },
      {
        header: "Comment",
        lines: 2,
        maxWidth: 420,
        cell: (comment) => (
          <p className="text-sm leading-6 text-muted">{comment.content ?? "—"}</p>
        ),
      },
      { header: "Blog", cell: (comment) => comment.blogId.slice(0, 8) },
      { header: "Status", cell: (comment) => <AdminStatusBadge value={comment.status} /> },
      { header: "Created", cell: (comment) => formatDate(comment.createdAt, localeTag) },
      {
        header: "",
        className: "w-12 text-right",
        cell: (comment) => (
          <AdminRowActions
            actions={[
              {
                label: "View detail",
                icon: EyeIcon,
                onSelect: () =>
                  detail.load((signal) => getAdminComment(comment.id, signal)),
              },
              ...postStatuses
                .filter((nextStatus) => nextStatus !== comment.status)
                .map((nextStatus) => ({
                  label: ui("Set status: {status}", {
                    status: enumLabel(nextStatus),
                  }),
                  destructive: nextStatus === "REMOVED",
                  onSelect: () =>
                    setPendingAction({ type: "status", comment, status: nextStatus }),
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
      const updatedComment = await updateCommentStatus(
        pendingAction.comment.id,
        pendingAction.status,
      );
      if (detail.data?.id === updatedComment.id) {
        detail.setData(updatedComment);
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
        title="Comments"
        description="Review comment status by blog or user and remove abusive comments."
      />
      <Toolbar onRefresh={resource.refetch}>
        <FilterSelect
          label="Status"
          value={status}
          options={postStatuses}
          placeholder="All statuses"
          onChange={setStatus}
        />
        <FilterInput label="Blog ID" value={blogId} placeholder="Enter blog ID" onChange={setBlogId} />
        <FilterInput label="User ID" value={userId} placeholder="Enter user ID" onChange={setUserId} />
      </Toolbar>
      <AdminDataTable
        columns={columns}
        rows={resource.rows}
        getRowKey={(comment) => comment.id}
        isLoading={resource.isLoading}
        error={resource.error}
        onRowClick={(comment) =>
          detail.load((signal) => getAdminComment(comment.id, signal))
        }
      />
      <AdminPagination page={resource.data} onPageChange={resource.setPageNumber} />
      <AdminDetailDialog
        open={detail.open}
        onOpenChange={detail.setOpen}
        title="Comment detail"
        description={detail.data ? detail.data.id : "Latest detail from admin API"}
        isLoading={detail.isLoading}
        error={detail.error}
      >
        {detail.data ? (
          <div className="flex flex-col gap-4">
            <AdminDetailGrid>
              <AdminDetailField label="Author">{detail.data.authorUserName}</AdminDetailField>
              <AdminDetailField label="User">{detail.data.userId}</AdminDetailField>
              <AdminDetailField label="Blog">{detail.data.blogId}</AdminDetailField>
              <AdminDetailField label="Parent">{detail.data.parentCommentId || "-"}</AdminDetailField>
              <AdminDetailField label="Status">
                <AdminStatusBadge value={detail.data.status} />
              </AdminDetailField>
              <AdminDetailField label="Created">{formatDate(detail.data.createdAt, localeTag)}</AdminDetailField>
              <AdminDetailField label="Updated">{formatDate(detail.data.updatedAt, localeTag)}</AdminDetailField>
              <AdminDetailField label="Content" className="sm:col-span-2">
                <p className="whitespace-pre-wrap leading-6">{detail.data.content || "-"}</p>
              </AdminDetailField>
            </AdminDetailGrid>
            {detail.data.imageUrls?.length ? (
              <div className="grid gap-3 sm:grid-cols-2">
                {detail.data.imageUrls.map((url) => (
                  <img
                    alt={ui("Comment attachment")}
                    className="max-h-72 w-full rounded-md border border-border object-contain"
                    key={url}
                    src={url}
                  />
                ))}
              </div>
            ) : null}
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
        title="Confirm comment action"
        description="This action updates the selected comment immediately."
        confirmLabel="Apply"
        isSubmitting={isSubmitting}
        onConfirm={handleConfirm}
      >
        {actionError ? <p className="text-sm text-accent">{actionError}</p> : null}
      </AdminConfirmDialog>
    </div>
  );
}
