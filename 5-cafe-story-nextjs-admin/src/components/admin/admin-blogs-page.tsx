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
import { createBlogRankingOverride, deleteBlog, getBlogs, updateBlogStatus } from "@/lib/api/admin";
import type { Blog, PostStatus } from "@/types/admin";

const postStatuses: PostStatus[] = ["DRAFT", "PUBLISHED", "HIDDEN", "REMOVED"];

type PendingBlogAction =
  | { type: "status"; blog: Blog; status: PostStatus }
  | { type: "delete"; blog: Blog }
  | { type: "rank"; blog: Blog };

export function AdminBlogsPage() {
  const [status, setStatus] = useState<PostStatus | "">("");
  const [authorUserId, setAuthorUserId] = useState("");
  const [pageId, setPageId] = useState("");
  const [pendingAction, setPendingAction] = useState<PendingBlogAction | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const resource = usePagedAdminResource(
    (page, signal) =>
      getBlogs({ status, authorUserId, pageId, page, size: PAGE_SIZE }, signal),
    [status, authorUserId, pageId],
  );

  const columns = useMemo<AdminTableColumn<Blog>[]>(
    () => [
      {
        header: "Content",
        cell: (blog) => (
          <div className="max-w-md">
            <p className="font-bold text-espresso">
              {blog.displayName || blog.authorUserName}
            </p>
            <p className="mt-1 text-sm leading-6 text-muted">
              {textPreview(blog.content)}
            </p>
          </div>
        ),
      },
      { header: "Page", cell: (blog) => blog.pageName || "Personal" },
      { header: "Status", cell: (blog) => <AdminStatusBadge value={blog.status} /> },
      {
        header: "Signals",
        cell: (blog) => (
          <span className="text-muted">
            {blog.likeCount ?? 0} likes · {blog.commentCount ?? 0} comments
          </span>
        ),
      },
      { header: "Created", cell: (blog) => formatDate(blog.createdAt) },
      {
        header: "Actions",
        className: "w-80",
        cell: (blog) => (
          <div className="flex flex-wrap gap-2">
            {postStatuses
              .filter((nextStatus) => nextStatus !== blog.status)
              .slice(0, 2)
              .map((nextStatus) => (
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  key={nextStatus}
                  onClick={() => setPendingAction({ type: "status", blog, status: nextStatus })}
                >
                  {nextStatus}
                </Button>
              ))}
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => setPendingAction({ type: "rank", blog })}
            >
              Boost
            </Button>
            <Button
              type="button"
              variant="destructive"
              size="sm"
              onClick={() => setPendingAction({ type: "delete", blog })}
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
        await updateBlogStatus(pendingAction.blog.id, pendingAction.status);
      } else if (pendingAction.type === "delete") {
        await deleteBlog(pendingAction.blog.id);
      } else {
        const now = new Date();
        const end = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000);
        await createBlogRankingOverride(pendingAction.blog.id, {
          boost_score: 1,
          is_pinned: true,
          reason: "Admin dashboard boost",
          start_at: now.toISOString(),
          end_at: end.toISOString(),
        });
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
        title="Blogs"
        description="Moderate blog content, update post status, delete abusive content, and apply ranking overrides."
      />
      <Toolbar onRefresh={resource.refetch}>
        <FilterSelect
          value={status}
          options={postStatuses}
          placeholder="Status"
          onChange={setStatus}
        />
        <FilterInput value={authorUserId} placeholder="Author user ID" onChange={setAuthorUserId} />
        <FilterInput value={pageId} placeholder="Cafe page ID" onChange={setPageId} />
      </Toolbar>
      <AdminDataTable
        columns={columns}
        rows={resource.rows}
        getRowKey={(blog) => blog.id}
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
        title="Confirm blog action"
        description="This action updates live blog data."
        confirmLabel="Apply"
        isSubmitting={isSubmitting}
        onConfirm={handleConfirm}
      >
        {actionError ? <p className="text-sm text-accent">{actionError}</p> : null}
      </AdminConfirmDialog>
    </div>
  );
}
