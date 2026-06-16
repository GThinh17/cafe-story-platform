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
  formatDate,
  PAGE_SIZE,
  textPreview,
  Toolbar,
  usePagedAdminResource,
} from "@/components/admin/admin-page-utils";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import {
  getAdminBlog,
  getAdminModerationResult,
  getModerationResults,
  resolveModerationResult,
} from "@/lib/api/admin";
import type { AdminModerationResult, Blog, ModerationResolveAction } from "@/types/admin";

const resolveActions: ModerationResolveAction[] = ["APPROVE", "HIDE", "REMOVE"];

type PendingModerationAction = {
  result: AdminModerationResult;
  action: ModerationResolveAction;
};

export function AdminModerationPage() {
  const [mode, setMode] = useState<"queue" | "results">("queue");
  const [pendingAction, setPendingAction] = useState<PendingModerationAction | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailResult, setDetailResult] = useState<AdminModerationResult | null>(null);
  const [detailBlog, setDetailBlog] = useState<Blog | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState<string | null>(null);

  const resource = usePagedAdminResource(
    (page, signal) => getModerationResults(mode, { page, size: PAGE_SIZE }, signal),
    [mode],
  );

  async function openDetail(result: AdminModerationResult) {
    setDetailOpen(true);
    setDetailResult(null);
    setDetailBlog(null);
    setDetailError(null);
    setDetailLoading(true);

    const [moderationRes, blogRes] = await Promise.allSettled([
      getAdminModerationResult(result.id),
      result.blogId ? getAdminBlog(result.blogId) : Promise.resolve(null),
    ]);

    if (moderationRes.status === "fulfilled") {
      setDetailResult(moderationRes.value);
    } else {
      setDetailError(
        moderationRes.reason instanceof Error
          ? moderationRes.reason.message
          : "Unable to load moderation detail.",
      );
    }

    if (blogRes.status === "fulfilled") {
      setDetailBlog(blogRes.value);
    }
    // blog fetch failure is non-critical, no error set

    setDetailLoading(false);
  }

  const columns = useMemo<AdminTableColumn<AdminModerationResult>[]>(
    () => [
      { header: "Blog", cell: (result) => result.blogId?.slice(0, 8) || "-" },
      {
        header: "Author",
        cell: (result) =>
          result.authorUserName || result.authorUserFullName || result.authorUserId?.slice(0, 8) || "-",
      },
      {
        header: "Caption",
        cell: (result) => (
          <p className="max-w-md text-sm leading-6 text-muted">
            {textPreview(result.caption || result.explanation)}
          </p>
        ),
      },
      { header: "Caption score", cell: (result) => result.captionScore ?? "-" },
      { header: "Image score", cell: (result) => result.imageScore ?? "-" },
      { header: "AI status", cell: (result) => <AdminStatusBadge value={result.aiStatus} /> },
      { header: "Decision", cell: (result) => <AdminStatusBadge value={result.decision} /> },
      { header: "Resolved", cell: (result) => <AdminStatusBadge value={result.resolved} /> },
      { header: "Created", cell: (result) => formatDate(result.createdAt) },
      {
        header: "Actions",
        className: "w-80",
        cell: (result) => (
          <div className="flex flex-wrap gap-2">
            <Button type="button" variant="outline" size="sm" onClick={() => openDetail(result)}>
              <EyeIcon data-icon="inline-start" />
              View
            </Button>
            {resolveActions.map((action) => (
              <Button
                type="button"
                variant={action === "REMOVE" ? "destructive" : "outline"}
                size="sm"
                disabled={Boolean(result.resolved)}
                key={action}
                onClick={(e) => {
                  e.stopPropagation();
                  setPendingAction({ result, action });
                }}
              >
                {action}
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
      const updatedResult = await resolveModerationResult(
        pendingAction.result.id,
        pendingAction.action,
      );
      if (detailResult?.id === updatedResult.id) {
        setDetailResult(updatedResult);
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
        title="AI Moderation"
        description="Review AI moderation queue and resolved results."
      />
      <Toolbar onRefresh={resource.refetch}>
        <div className="flex rounded-md border border-border bg-background p-1">
          {(["queue", "results"] as const).map((nextMode) => (
            <Button
              type="button"
              variant={mode === nextMode ? "secondary" : "ghost"}
              size="sm"
              key={nextMode}
              onClick={() => setMode(nextMode)}
            >
              {nextMode === "queue" ? "Queue" : "Results"}
            </Button>
          ))}
        </div>
      </Toolbar>
      <AdminDataTable
        columns={columns}
        rows={resource.rows}
        getRowKey={(result) => result.id}
        isLoading={resource.isLoading}
        error={resource.error}
        onRowClick={(result) => openDetail(result)}
      />
      <AdminPagination page={resource.data} onPageChange={resource.setPageNumber} />

      <AdminDetailDialog
        open={detailOpen}
        onOpenChange={setDetailOpen}
        title="Moderation detail"
        description={detailResult ? `Result ${detailResult.id}` : "Latest detail from admin API"}
        isLoading={detailLoading}
        error={detailError}
        footer={
          detailResult ? (
            <div className="flex flex-wrap justify-end gap-2">
              {resolveActions.map((action) => (
                <Button
                  type="button"
                  variant={action === "REMOVE" ? "destructive" : "outline"}
                  size="sm"
                  disabled={Boolean(detailResult.resolved)}
                  key={action}
                  onClick={() => setPendingAction({ result: detailResult, action })}
                >
                  {action}
                </Button>
              ))}
            </div>
          ) : null
        }
      >
        {detailResult ? (
          <div className="flex flex-col gap-5">
            {detailBlog && (
              <div>
                <p className="text-xs font-black uppercase tracking-[0.08em] text-muted">
                  Blog Images
                </p>
                {detailBlog.imageUrls.length > 0 ? (
                  <div className="mt-2 grid grid-cols-2 gap-2 sm:grid-cols-4">
                    {detailBlog.imageUrls.slice(0, 4).map((url) => (
                      <img
                        key={url}
                        src={url}
                        alt=""
                        className="aspect-[4/3] w-full rounded-md object-cover"
                      />
                    ))}
                  </div>
                ) : (
                  <p className="mt-2 text-sm text-muted">No images</p>
                )}
                {detailBlog.content && (
                  <p className="mt-3 max-h-40 overflow-y-auto whitespace-pre-wrap text-sm leading-6 text-foreground">
                    {detailBlog.content}
                  </p>
                )}
              </div>
            )}
            <Separator />
            <p className="text-xs font-black uppercase tracking-[0.08em] text-muted">
              AI Evaluation
            </p>
            <div>
              <p className="text-xs font-black uppercase tracking-[0.08em] text-muted">
                Caption
              </p>
              <p className="mt-2 whitespace-pre-wrap text-sm leading-6 text-foreground">
                {detailResult.caption || "No caption"}
              </p>
            </div>
            <AdminDetailGrid>
              <AdminDetailField label="Caption reason" className="sm:col-span-2">
                {detailResult.captionReason || "-"}
              </AdminDetailField>
              <AdminDetailField label="Image reason" className="sm:col-span-2">
                {detailResult.imageReason || "-"}
              </AdminDetailField>
              <AdminDetailField label="Tags" className="sm:col-span-2">
                <div className="flex flex-wrap gap-1">
                  {(detailResult.tags || []).length ? (
                    detailResult.tags.map((tag) => (
                      <Badge variant="secondary" key={tag}>
                        {tag}
                      </Badge>
                    ))
                  ) : (
                    "-"
                  )}
                </div>
              </AdminDetailField>
              <AdminDetailField label="Model">{detailResult.modelName || "-"}</AdminDetailField>
              <AdminDetailField label="Score">{detailResult.score ?? "-"}</AdminDetailField>
              <AdminDetailField label="Caption score">
                {detailResult.captionScore ?? "-"}
              </AdminDetailField>
              <AdminDetailField label="Image score">
                {detailResult.imageScore ?? "-"}
              </AdminDetailField>
              <AdminDetailField label="Decision">
                <AdminStatusBadge value={detailResult.decision} />
              </AdminDetailField>
              <AdminDetailField label="AI status">
                <AdminStatusBadge value={detailResult.aiStatus} />
              </AdminDetailField>
              <AdminDetailField label="Blog status">
                <AdminStatusBadge value={detailResult.blogStatus} />
              </AdminDetailField>
              <AdminDetailField label="Resolved">
                <AdminStatusBadge value={detailResult.resolved} />
              </AdminDetailField>
              <AdminDetailField label="Resolved action">
                {detailResult.resolvedAction || "-"}
              </AdminDetailField>
              <AdminDetailField label="Resolved at">
                {formatDate(detailResult.resolvedAt)}
              </AdminDetailField>
              <AdminDetailField label="Created">{formatDate(detailResult.createdAt)}</AdminDetailField>
              <AdminDetailField label="Updated">{formatDate(detailResult.updatedAt)}</AdminDetailField>
            </AdminDetailGrid>
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
        title="Resolve moderation result"
        description="This applies the selected moderation resolution."
        confirmLabel={pendingAction?.action || "Resolve"}
        isSubmitting={isSubmitting}
        onConfirm={handleConfirm}
      >
        {actionError ? <p className="text-sm text-accent">{actionError}</p> : null}
      </AdminConfirmDialog>
    </div>
  );
}
