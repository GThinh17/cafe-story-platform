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
  BooleanFilterSelect,
  FilterSelect,
  formatDate,
  PAGE_SIZE,
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
import {
  localizeApiError,
  useEnumLabel,
  useI18n,
  useUiText,
} from "@/features/i18n";
import type {
  AdminModerationResult,
  AiStatus,
  Blog,
  ModerationDecision,
  ModerationResolveAction,
} from "@/types/admin";

const resolveActions: ModerationResolveAction[] = ["APPROVE", "REMOVE"];
const aiStatusOptions: AiStatus[] = ["SEND_ADMIN", "APPROVE", "DENY"];
const decisionOptions: ModerationDecision[] = ["SAFE", "NEEDS_REVIEW", "VIOLATION"];

type PendingModerationAction = {
  result: AdminModerationResult;
  action: ModerationResolveAction;
};

export function AdminModerationPage() {
  const { locale, localeTag, t } = useI18n();
  const ui = useUiText();
  const enumLabel = useEnumLabel();
  const [filterAiStatus, setFilterAiStatus] = useState<AiStatus | "">("");
  const [filterDecision, setFilterDecision] = useState<ModerationDecision | "">("");
  const [filterResolved, setFilterResolved] = useState<boolean | null>(null);

  const [pendingAction, setPendingAction] = useState<PendingModerationAction | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailResult, setDetailResult] = useState<AdminModerationResult | null>(null);
  const [detailBlog, setDetailBlog] = useState<Blog | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState<string | null>(null);

  const resource = usePagedAdminResource(
    (page, signal) =>
      getModerationResults(
        "results",
        {
          page,
          size: PAGE_SIZE,
          aiStatus: filterAiStatus || undefined,
          decision: filterDecision || undefined,
          resolved: filterResolved,
        },
        signal,
      ),
    [filterAiStatus, filterDecision, filterResolved],
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
      setDetailError(localizeApiError(moderationRes.reason, locale, t));
    }

    if (blogRes.status === "fulfilled") {
      setDetailBlog(blogRes.value);
    }

    setDetailLoading(false);
  }

  const columns = useMemo<AdminTableColumn<AdminModerationResult>[]>(
    () => [
      { header: "Blog", cell: (result) => result.blogId?.slice(0, 8) || "-" },
      {
        header: "Author",
        cell: (result) => (
          <UserCell
            name={result.authorUserName || result.authorUserFullName}
            avatar={result.authorUserAvatar}
          />
        ),
      },
      {
        header: "Caption",
        lines: 2,
        maxWidth: 420,
        cell: (result) => (
          <p className="text-sm leading-6 text-muted">
            {result.caption || result.explanation || "—"}
          </p>
        ),
      },
      { header: "Caption score", cell: (result) => result.captionScore ?? "-" },
      { header: "Image score", cell: (result) => result.imageScore ?? "-" },
      { header: "AI status", cell: (result) => <AdminStatusBadge value={result.aiStatus} /> },
      { header: "Decision", cell: (result) => <AdminStatusBadge value={result.decision} /> },
      { header: "Resolved", cell: (result) => <AdminStatusBadge value={result.resolved} /> },
      { header: "Created", cell: (result) => formatDate(result.createdAt, localeTag) },
      {
        header: "",
        className: "w-12 text-right",
        cell: (result) => (
          <AdminRowActions
            actions={[
              {
                label: "View detail",
                icon: EyeIcon,
                onSelect: () => openDetail(result),
              },
              ...resolveActions.map((action) => ({
                label: enumLabel(action),
                destructive: action === "REMOVE",
                onSelect: () => setPendingAction({ result, action }),
              })),
            ]}
          />
        ),
      },
    ],
    [enumLabel, localeTag],
  );

  async function handleConfirm() {
    if (!pendingAction) {
      return;
    }

    const { result: targetResult, action } = pendingAction;
    const optimisticPatch: Partial<AdminModerationResult> = {
      resolved: true,
      resolvedAction: action,
      resolvedAt: new Date().toISOString(),
      blogStatus: action === "APPROVE" ? "PUBLISHED" : action === "HIDE" ? "HIDDEN" : "REMOVED",
    };

    setIsSubmitting(true);
    setActionError(null);

    resource.updateRow(
      (item) => item.id === targetResult.id,
      (item) => ({ ...item, ...optimisticPatch }),
    );

    if (detailResult?.id === targetResult.id) {
      setDetailResult((prev) => (prev ? { ...prev, ...optimisticPatch } : prev));
    }

    setPendingAction(null);

    try {
      const updatedResult = await resolveModerationResult(targetResult.id, action);

      resource.updateRow(
        (item) => item.id === updatedResult.id,
        () => updatedResult,
      );

      if (detailResult?.id === updatedResult.id) {
        setDetailResult(updatedResult);
      }
    } catch (requestError) {
      setActionError(localizeApiError(requestError, locale, t, "common.error.action"));
      resource.refetch();
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <AdminPageHeader
        title="AI Moderation"
        description="Review AI moderation results and resolve flagged content."
      />
      <Toolbar onRefresh={resource.refetch}>
        <FilterSelect<AiStatus>
          label="AI status"
          value={filterAiStatus}
          options={aiStatusOptions}
          placeholder="All AI statuses"
          onChange={setFilterAiStatus}
        />
        <FilterSelect<ModerationDecision>
          label="Decision"
          value={filterDecision}
          options={decisionOptions}
          placeholder="All decisions"
          onChange={setFilterDecision}
        />
        <BooleanFilterSelect
          label="Resolved"
          value={filterResolved}
          onChange={setFilterResolved}
          trueLabel="Resolved"
          falseLabel="Unresolved"
        />
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
        description={
          detailResult
            ? ui("Result {id}", { id: detailResult.id })
            : "Latest detail from admin API"
        }
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
                  key={action}
                  onClick={() => setPendingAction({ result: detailResult, action })}
                >
                  {enumLabel(action)}
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
                  {ui("Blog Images")}
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
                  <p className="mt-2 text-sm text-muted">{ui("No images")}</p>
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
              {ui("AI Evaluation")}
            </p>
            <div>
              <p className="text-xs font-black uppercase tracking-[0.08em] text-muted">
                {ui("Caption")}
              </p>
              <p className="mt-2 whitespace-pre-wrap text-sm leading-6 text-foreground">
                {detailResult.caption || ui("No caption")}
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
                {detailResult.resolvedAction
                  ? enumLabel(detailResult.resolvedAction)
                  : "—"}
              </AdminDetailField>
              <AdminDetailField label="Resolved at">
                {formatDate(detailResult.resolvedAt, localeTag)}
              </AdminDetailField>
              <AdminDetailField label="Created">{formatDate(detailResult.createdAt, localeTag)}</AdminDetailField>
              <AdminDetailField label="Updated">{formatDate(detailResult.updatedAt, localeTag)}</AdminDetailField>
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
        title={
          pendingAction
            ? ui("{action} content", { action: enumLabel(pendingAction.action) })
            : "Confirm action"
        }
        description={
          pendingAction
            ? pendingAction.result.authorUserName
              ? ui("Apply {action} to flagged content by {name}. This action cannot be undone.", {
                  action: enumLabel(pendingAction.action),
                  name: pendingAction.result.authorUserName,
                })
              : ui("Apply {action} to flagged content. This action cannot be undone.", {
                  action: enumLabel(pendingAction.action),
                })
            : "Confirm the selected moderation action."
        }
        confirmLabel={pendingAction ? enumLabel(pendingAction.action) : "Confirm"}
        isSubmitting={isSubmitting}
        onConfirm={handleConfirm}
      >
        {actionError ? <p className="text-sm text-accent">{actionError}</p> : null}
      </AdminConfirmDialog>
    </div>
  );
}
