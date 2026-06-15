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
  FilterInput,
  FilterSelect,
  formatDate,
  PAGE_SIZE,
  textPreview,
  Toolbar,
  usePagedAdminResource,
} from "@/components/admin/admin-page-utils";
import { Button } from "@/components/ui/button";
import { deleteComment, getComments, updateCommentStatus } from "@/lib/api/admin";
import type { Comment, PostStatus } from "@/types/admin";

const postStatuses: PostStatus[] = ["DRAFT", "PUBLISHED", "HIDDEN", "REMOVED"];

type PendingCommentAction =
  | { type: "status"; comment: Comment; status: PostStatus }
  | { type: "delete"; comment: Comment };

export function AdminCommentsPage() {
  const [status, setStatus] = useState<PostStatus | "">("");
  const [blogId, setBlogId] = useState("");
  const [userId, setUserId] = useState("");
  const [pendingAction, setPendingAction] = useState<PendingCommentAction | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const resource = usePagedAdminResource(
    (page, signal) =>
      getComments({ status, blogId, userId, page, size: PAGE_SIZE }, signal),
    [status, blogId, userId],
  );

  const columns = useMemo<AdminTableColumn<Comment>[]>(
    () => [
      {
        header: "Comment",
        cell: (comment) => (
          <div className="max-w-md">
            <p className="font-bold text-espresso">{comment.authorUserName}</p>
            <p className="mt-1 text-sm leading-6 text-muted">
              {textPreview(comment.content)}
            </p>
          </div>
        ),
      },
      { header: "Blog", cell: (comment) => comment.blogId.slice(0, 8) },
      { header: "Status", cell: (comment) => <AdminStatusBadge value={comment.status} /> },
      { header: "Created", cell: (comment) => formatDate(comment.createdAt) },
      {
        header: "Actions",
        className: "w-72",
        cell: (comment) => (
          <div className="flex flex-wrap gap-2">
            {postStatuses
              .filter((nextStatus) => nextStatus !== comment.status)
              .slice(0, 2)
              .map((nextStatus) => (
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  key={nextStatus}
                  onClick={() =>
                    setPendingAction({ type: "status", comment, status: nextStatus })
                  }
                >
                  {nextStatus}
                </Button>
              ))}
            <Button
              type="button"
              variant="destructive"
              size="sm"
              onClick={() => setPendingAction({ type: "delete", comment })}
            >
              Delete
            </Button>
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
      if (pendingAction.type === "status") {
        await updateCommentStatus(pendingAction.comment.id, pendingAction.status);
      } else {
        await deleteComment(pendingAction.comment.id);
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
        title="Comments"
        description="Review comment status by blog or user and remove abusive comments."
      />
      <Toolbar onRefresh={resource.refetch}>
        <FilterSelect
          value={status}
          options={postStatuses}
          placeholder="Status"
          onChange={setStatus}
        />
        <FilterInput value={blogId} placeholder="Blog ID" onChange={setBlogId} />
        <FilterInput value={userId} placeholder="User ID" onChange={setUserId} />
      </Toolbar>
      <AdminDataTable
        columns={columns}
        rows={resource.rows}
        getRowKey={(comment) => comment.id}
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
