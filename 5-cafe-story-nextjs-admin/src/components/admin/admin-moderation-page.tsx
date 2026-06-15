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
  formatDate,
  PAGE_SIZE,
  textPreview,
  Toolbar,
  usePagedAdminResource,
} from "@/components/admin/admin-page-utils";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { getModerationResults, resolveModerationResult } from "@/lib/api/admin";
import type { AdminModerationResult, ModerationResolveAction } from "@/types/admin";

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

  const resource = usePagedAdminResource(
    (page, signal) => getModerationResults(mode, { page, size: PAGE_SIZE }, signal),
    [mode],
  );

  const columns = useMemo<AdminTableColumn<AdminModerationResult>[]>(
    () => [
      {
        header: "Result",
        cell: (result) => (
          <div className="max-w-md">
            <p className="font-bold text-espresso">
              {result.authorUserName || result.authorUserFullName || "Unknown author"}
            </p>
            <p className="mt-1 text-sm leading-6 text-muted">
              {textPreview(
                result.caption || result.explanation || result.captionReason || result.imageReason,
              )}
            </p>
          </div>
        ),
      },
      {
        header: "AI",
        cell: (result) => (
          <div className="flex flex-col gap-2">
            <AdminStatusBadge value={result.decision} />
            <span className="text-xs text-muted">Score {result.score ?? "—"}</span>
          </div>
        ),
      },
      {
        header: "Tags",
        cell: (result) => (
          <div className="flex max-w-56 flex-wrap gap-1">
            {(result.tags || []).slice(0, 4).map((tag) => (
              <Badge variant="secondary" key={tag}>
                {tag}
              </Badge>
            ))}
          </div>
        ),
      },
      { header: "Blog", cell: (result) => <AdminStatusBadge value={result.blogStatus} /> },
      { header: "Resolved", cell: (result) => <AdminStatusBadge value={result.resolved} /> },
      { header: "Created", cell: (result) => formatDate(result.createdAt) },
      {
        header: "Actions",
        className: "w-72",
        cell: (result) => (
          <div className="flex flex-wrap gap-2">
            {resolveActions.map((action) => (
              <Button
                type="button"
                variant={action === "REMOVE" ? "destructive" : "outline"}
                size="sm"
                disabled={Boolean(result.resolved)}
                key={action}
                onClick={() => setPendingAction({ result, action })}
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
      await resolveModerationResult(pendingAction.result.id, pendingAction.action);
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
