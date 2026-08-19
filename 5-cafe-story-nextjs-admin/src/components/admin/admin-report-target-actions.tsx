"use client";

import {
  AlertTriangleIcon,
  EyeOffIcon,
  Globe2Icon,
  LoaderCircleIcon,
  PaperclipIcon,
  RefreshCwIcon,
  Trash2Icon,
} from "lucide-react";
import { AdminStatusBadge } from "@/components/admin/admin-status-badge";
import { AdminTranslatableContent } from "@/components/admin/admin-translatable-content";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { useUiText } from "@/features/i18n";
import type { Blog, Comment, ContentReport, PostStatus } from "@/types/admin";

type ReportTarget = Blog | Comment;

const targetStatuses: Array<Exclude<PostStatus, "DRAFT">> = [
  "PUBLISHED",
  "HIDDEN",
  "REMOVED",
];

const statusActions: Record<
  Exclude<PostStatus, "DRAFT">,
  { label: string; icon: typeof Globe2Icon; variant: "outline" | "destructive" }
> = {
  PUBLISHED: { label: "Publish content", icon: Globe2Icon, variant: "outline" },
  HIDDEN: { label: "Hide content", icon: EyeOffIcon, variant: "outline" },
  REMOVED: { label: "Remove content", icon: Trash2Icon, variant: "destructive" },
};

type AdminReportTargetActionsProps = {
  report: ContentReport;
  target: ReportTarget | null;
  isLoading: boolean;
  error: string | null;
  isMutating: boolean;
  suggestedStatus: PostStatus | null;
  recommendationStale: boolean;
  successMessage: string | null;
  onRetry: () => void;
  onRequestStatus: (status: PostStatus) => void;
};

export function AdminReportTargetActions({
  report,
  target,
  isLoading,
  error,
  isMutating,
  suggestedStatus,
  recommendationStale,
  successMessage,
  onRetry,
  onRequestStatus,
}: AdminReportTargetActionsProps) {
  const ui = useUiText();
  const targetLabel = report.targetType === "BLOG" ? ui("Blog") : ui("Comment");

  return (
    <section className="rounded-md border border-border bg-background p-4">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <p className="text-xs font-black uppercase tracking-[0.08em] text-muted">
            {ui("Reported target content")}
          </p>
          <p className="mt-1 text-sm text-muted">
            {ui("Current content loaded from the admin API. It may differ from the AI snapshot.")}
          </p>
        </div>
        <Badge variant="outline">{targetLabel}</Badge>
      </div>

      {isLoading ? (
        <div className="mt-4 flex flex-col gap-3" aria-label={ui("Loading target content...")}>
          <Skeleton className="h-20 w-full" />
          <Skeleton className="h-8 w-44" />
        </div>
      ) : error ? (
        <div className="mt-4 rounded-md border border-accent/30 bg-accent/10 p-3">
          <div className="flex items-start gap-2 text-sm text-accent">
            <AlertTriangleIcon className="mt-0.5 size-4 shrink-0" />
            <p>{error}</p>
          </div>
          <Button type="button" variant="outline" size="sm" className="mt-3" onClick={onRetry}>
            <RefreshCwIcon data-icon="inline-start" />
            {ui("Retry loading content")}
          </Button>
        </div>
      ) : target ? (
        <div className="mt-4 flex flex-col gap-4">
          <div className="rounded-md border border-border bg-surface p-3">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <p className="font-mono text-xs text-muted">{target.id}</p>
              <AdminStatusBadge value={target.status} />
            </div>
            <AdminTranslatableContent
              className="mt-3"
              contentKind={report.targetType === "BLOG" ? "BLOG_CONTENT" : "COMMENT_CONTENT"}
              text={target.content || ui("No text content")}
              textClassName="text-sm leading-6 text-foreground"
            />
            {target.imageUrls.length ? (
              <div className="mt-3 flex flex-col gap-2 border-t border-border pt-3">
                <p className="flex items-center gap-2 text-xs font-bold uppercase text-muted">
                  <PaperclipIcon className="size-4" />
                  {ui("Attachments")}
                </p>
                {target.imageUrls.map((url, index) => (
                  <a
                    className="break-all text-xs text-primary underline-offset-4 hover:underline"
                    href={url}
                    key={`${url}-${index}`}
                    rel="noreferrer"
                    target="_blank"
                  >
                    {ui("Attachment {index}", { index: index + 1 })}: {url}
                  </a>
                ))}
              </div>
            ) : null}
          </div>

          <div className="border-t border-border pt-4">
            <div className="flex flex-wrap items-start justify-between gap-2">
              <div>
                <p className="text-sm font-bold text-espresso">{ui("Moderate content")}</p>
                <p className="mt-1 text-xs text-muted">
                  {ui("This action changes the target status but does not close the report.")}
                </p>
              </div>
              {recommendationStale ? (
                <Badge className="border-rating/30 bg-rating/10 text-rating" variant="outline">
                  {ui("AI recommendation may be stale")}
                </Badge>
              ) : null}
            </div>
            <div className="mt-3 grid gap-2 sm:grid-cols-3">
              {targetStatuses
                .filter((status) => status !== target.status)
                .map((status) => {
                  const action = statusActions[status];
                  const Icon = action.icon;
                  const suggested = suggestedStatus === status && !recommendationStale;
                  return (
                    <Button
                      type="button"
                      variant={action.variant}
                      className={suggested ? "ring-2 ring-primary/40 ring-offset-2" : undefined}
                      disabled={isMutating}
                      key={status}
                      onClick={() => onRequestStatus(status)}
                    >
                      {isMutating ? (
                        <LoaderCircleIcon className="animate-spin" data-icon="inline-start" />
                      ) : (
                        <Icon data-icon="inline-start" />
                      )}
                      {ui(action.label)}
                      {suggested ? <Badge className="ml-1">{ui("AI suggested")}</Badge> : null}
                    </Button>
                  );
                })}
            </div>
            {recommendationStale ? (
              <p className="mt-3 text-xs text-rating">
                {ui("The target changed after the latest AI analysis. Run AI again before relying on it.")}
              </p>
            ) : null}
            {successMessage ? (
              <p className="mt-3 rounded-md border border-success/30 bg-success/10 p-3 text-sm text-success">
                {successMessage}
              </p>
            ) : null}
          </div>
        </div>
      ) : (
        <div className="mt-4 rounded-md border border-dashed border-border p-4 text-sm text-muted">
          {ui("The target is unavailable or was deleted.")}
        </div>
      )}
    </section>
  );
}
