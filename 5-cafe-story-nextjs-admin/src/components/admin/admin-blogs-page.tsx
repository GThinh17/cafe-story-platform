"use client";

import { useMemo, useState } from "react";
import {
  ChevronLeftIcon,
  ChevronRightIcon,
  EyeIcon,
  PinIcon,
  Trash2Icon,
} from "lucide-react";
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
  textPreview,
  Toolbar,
  usePagedAdminResource,
} from "@/components/admin/admin-page-utils";
import { Button } from "@/components/ui/button";
import {
  createBlogRankingOverride,
  deleteBlog,
  getAdminBlog,
  getBlogs,
  updateBlogStatus,
} from "@/lib/api/admin";
import type { Blog, PostStatus } from "@/types/admin";

const postStatuses: PostStatus[] = ["DRAFT", "PUBLISHED", "HIDDEN", "REMOVED"];
const detailStatuses: PostStatus[] = ["PUBLISHED", "HIDDEN", "REMOVED"];

type PendingBlogAction =
  | { type: "status"; blog: Blog; status: PostStatus }
  | { type: "delete"; blog: Blog }
  | { type: "rank"; blog: Blog };

function countLabel(value: number | null | undefined) {
  return (value ?? 0).toLocaleString();
}

function BlogImages({ imageUrls }: { imageUrls: string[] }) {
  const [index, setIndex] = useState(0);
  const imageCount = imageUrls.length;

  if (!imageCount) {
    return (
      <div className="grid min-h-72 place-items-center rounded-md border border-border bg-surface-muted text-sm text-muted">
        No images
      </div>
    );
  }

  const activeImage = imageUrls[Math.min(index, imageCount - 1)];

  return (
    <div className="flex flex-col gap-3">
      <div className="relative grid min-h-[360px] place-items-center rounded-md border border-border bg-black/90">
        <img
          alt={`Blog image ${index + 1}`}
          className="max-h-[68vh] w-full object-contain"
          src={activeImage}
        />
        {imageCount > 1 ? (
          <>
            <Button
              type="button"
              variant="secondary"
              size="icon-sm"
              className="absolute left-3 top-1/2 -translate-y-1/2"
              aria-label="Previous image"
              onClick={() => setIndex((current) => (current === 0 ? imageCount - 1 : current - 1))}
            >
              <ChevronLeftIcon />
            </Button>
            <Button
              type="button"
              variant="secondary"
              size="icon-sm"
              className="absolute right-3 top-1/2 -translate-y-1/2"
              aria-label="Next image"
              onClick={() => setIndex((current) => (current + 1) % imageCount)}
            >
              <ChevronRightIcon />
            </Button>
          </>
        ) : null}
      </div>
      {imageCount > 1 ? (
        <div className="flex justify-center gap-2">
          {imageUrls.map((url, dotIndex) => (
            <button
              aria-label={`Show image ${dotIndex + 1}`}
              className={`size-2 rounded-full ${
                dotIndex === index ? "bg-primary" : "bg-border"
              }`}
              key={url}
              type="button"
              onClick={() => setIndex(dotIndex)}
            />
          ))}
        </div>
      ) : null}
    </div>
  );
}

export function AdminBlogsPage() {
  const [status, setStatus] = useState<PostStatus | "">("");
  const [authorUserId, setAuthorUserId] = useState("");
  const [pageId, setPageId] = useState("");
  const [pendingAction, setPendingAction] = useState<PendingBlogAction | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailBlog, setDetailBlog] = useState<Blog | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState<string | null>(null);

  const resource = usePagedAdminResource(
    (page, signal) =>
      getBlogs({ status, authorUserId, pageId, page, size: PAGE_SIZE }, signal),
    [status, authorUserId, pageId],
  );

  async function openDetail(blog: Blog) {
    setDetailOpen(true);
    setDetailBlog(null);
    setDetailError(null);
    setDetailLoading(true);

    try {
      setDetailBlog(await getAdminBlog(blog.id));
    } catch (requestError) {
      setDetailError(
        requestError instanceof Error ? requestError.message : "Unable to load blog detail.",
      );
    } finally {
      setDetailLoading(false);
    }
  }

  const columns = useMemo<AdminTableColumn<Blog>[]>(
    () => [
      {
        header: "Image",
        className: "w-24",
        cell: (blog) =>
          blog.imageUrls?.[0] ? (
            <img
              alt="Blog thumbnail"
              className="size-16 rounded-md border border-border object-cover"
              src={blog.imageUrls[0]}
            />
          ) : (
            <div className="grid size-16 place-items-center rounded-md border border-border bg-surface-muted text-xs text-muted">
              No image
            </div>
          ),
      },
      {
        header: "Author / page",
        cell: (blog) => (
          <div className="max-w-xs">
            <p className="font-bold text-espresso">
              {blog.displayName || blog.authorUserFullName || blog.authorUserName}
            </p>
            <p className="mt-1 text-xs text-muted">{blog.pageName || "Personal post"}</p>
          </div>
        ),
      },
      {
        header: "Caption",
        cell: (blog) => (
          <p className="max-w-md text-sm leading-6 text-muted">{textPreview(blog.content)}</p>
        ),
      },
      { header: "Status", cell: (blog) => <AdminStatusBadge value={blog.status} /> },
      {
        header: "Signals",
        cell: (blog) => (
          <span className="text-muted">
            {countLabel(blog.likeCount)} likes / {countLabel(blog.commentCount)} comments /{" "}
            {countLabel(blog.shareCount)} shares
          </span>
        ),
      },
      { header: "Created", cell: (blog) => formatDate(blog.createdAt) },
      {
        header: "Actions",
        className: "w-96",
        cell: (blog) => (
          <div className="flex flex-wrap gap-2">
            <Button type="button" variant="outline" size="sm" onClick={() => openDetail(blog)}>
              <EyeIcon data-icon="inline-start" />
              View
            </Button>
            {detailStatuses
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
              <PinIcon data-icon="inline-start" />
              Boost
            </Button>
            <Button
              type="button"
              variant="destructive"
              size="sm"
              onClick={() => setPendingAction({ type: "delete", blog })}
            >
              <Trash2Icon data-icon="inline-start" />
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
        const updatedBlog = await updateBlogStatus(pendingAction.blog.id, pendingAction.status);
        if (detailBlog?.id === updatedBlog.id) {
          setDetailBlog(updatedBlog);
        }
      } else if (pendingAction.type === "delete") {
        await deleteBlog(pendingAction.blog.id);
        if (detailBlog?.id === pendingAction.blog.id) {
          setDetailOpen(false);
          setDetailBlog(null);
        }
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
          label="Status"
          value={status}
          options={postStatuses}
          placeholder="All statuses"
          onChange={setStatus}
        />
        <FilterInput label="Author user ID" value={authorUserId} placeholder="Enter user ID" onChange={setAuthorUserId} />
        <FilterInput label="Cafe page ID" value={pageId} placeholder="Enter page ID" onChange={setPageId} />
      </Toolbar>
      <AdminDataTable
        columns={columns}
        rows={resource.rows}
        getRowKey={(blog) => blog.id}
        isLoading={resource.isLoading}
        error={resource.error}
      />
      <AdminPagination page={resource.data} onPageChange={resource.setPageNumber} />

      <AdminDetailDialog
        open={detailOpen}
        onOpenChange={setDetailOpen}
        title="Blog detail"
        description={detailBlog ? `Post ${detailBlog.id}` : "Latest detail from admin API"}
        isLoading={detailLoading}
        error={detailError}
        footer={
          detailBlog ? (
            <div className="flex flex-wrap justify-end gap-2">
              {detailStatuses.map((nextStatus) => (
                <Button
                  type="button"
                  variant={nextStatus === "REMOVED" ? "destructive" : "outline"}
                  size="sm"
                  disabled={detailBlog.status === nextStatus}
                  key={nextStatus}
                  onClick={() =>
                    setPendingAction({ type: "status", blog: detailBlog, status: nextStatus })
                  }
                >
                  Set {nextStatus}
                </Button>
              ))}
              <Button
                type="button"
                variant="destructive"
                size="sm"
                onClick={() => setPendingAction({ type: "delete", blog: detailBlog })}
              >
                <Trash2Icon data-icon="inline-start" />
                Delete
              </Button>
            </div>
          ) : null
        }
      >
        {detailBlog ? (
          <div className="grid gap-5 xl:grid-cols-[minmax(0,1.25fr)_minmax(360px,0.75fr)]">
            <BlogImages imageUrls={detailBlog.imageUrls ?? []} />
            <div className="flex flex-col gap-4">
              <div>
                <p className="text-xs font-black uppercase tracking-[0.08em] text-muted">
                  Caption
                </p>
                <p className="mt-2 whitespace-pre-wrap text-sm leading-6 text-foreground">
                  {detailBlog.content || "No caption"}
                </p>
              </div>
              <AdminDetailGrid>
                <AdminDetailField label="Author">
                  {detailBlog.displayName ||
                    detailBlog.authorUserFullName ||
                    detailBlog.authorUserName}
                  <br />
                  <span className="text-muted">{detailBlog.authorUserId}</span>
                </AdminDetailField>
                <AdminDetailField label="Cafe page">
                  {detailBlog.pageId ? (
                    <>
                      {detailBlog.pageName || detailBlog.pageId}
                      <br />
                      <span className="text-muted">{detailBlog.pageId}</span>
                    </>
                  ) : (
                    "Personal"
                  )}
                </AdminDetailField>
                <AdminDetailField label="Status">
                  <AdminStatusBadge value={detailBlog.status} />
                </AdminDetailField>
                <AdminDetailField label="Flags">
                  Comments {detailBlog.allowComment ? "on" : "off"} /{" "}
                  {detailBlog.isPinned ? "pinned" : "not pinned"}
                </AdminDetailField>
                <AdminDetailField label="Counts">
                  {countLabel(detailBlog.likeCount)} likes / {countLabel(detailBlog.shareCount)}{" "}
                  shares / {countLabel(detailBlog.commentCount)} comments /{" "}
                  {countLabel(detailBlog.saveCount)} saves
                </AdminDetailField>
                <AdminDetailField label="Rating">
                  {detailBlog.ratingScore ?? "No score"} ({countLabel(detailBlog.ratingCount)}{" "}
                  ratings)
                </AdminDetailField>
                <AdminDetailField label="Created">{formatDate(detailBlog.createdAt)}</AdminDetailField>
                <AdminDetailField label="Updated">{formatDate(detailBlog.updatedAt)}</AdminDetailField>
              </AdminDetailGrid>
            </div>
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

