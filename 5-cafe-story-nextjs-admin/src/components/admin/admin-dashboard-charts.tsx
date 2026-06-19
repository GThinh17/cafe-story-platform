"use client";

import {
  AlertTriangleIcon,
  CheckCircle2Icon,
  CircleIcon,
  ClockIcon,
  ShieldAlertIcon,
} from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { cn } from "@/lib/utils";
import type { AdminDashboardSummary } from "@/types/admin";

type ChartSegment = {
  label: string;
  value: number;
  className: string;
};

function safePercent(value: number, total: number) {
  if (total <= 0) {
    return 0;
  }

  return Math.round((value / total) * 100);
}

function RatioChart({
  title,
  value,
  total,
  primaryLabel,
  totalLabel,
  className,
}: {
  title: string;
  value: number;
  total: number;
  primaryLabel: string;
  totalLabel: string;
  className: string;
}) {
  const percent = safePercent(value, total);

  return (
    <Card>
      <CardHeader className="p-4 pb-2">
        <CardTitle className="text-sm">{title}</CardTitle>
      </CardHeader>
      <CardContent className="flex flex-col gap-4 p-4 pt-0">
        <div className="flex items-end justify-between gap-3">
          <div>
            <p className="text-3xl font-black text-espresso">{percent}%</p>
            <p className="text-xs font-semibold text-muted">{primaryLabel}</p>
          </div>
          <p className="text-sm text-muted">
            {value.toLocaleString()} / {total.toLocaleString()} {totalLabel}
          </p>
        </div>
        <div className="h-3 overflow-hidden rounded-full bg-surface-muted">
          <div className={cn("h-full rounded-full", className)} style={{ width: `${percent}%` }} />
        </div>
      </CardContent>
    </Card>
  );
}

function StackedStatusChart({
  title,
  total,
  segments,
}: {
  title: string;
  total: number;
  segments: ChartSegment[];
}) {
  return (
    <Card>
      <CardHeader className="p-4 pb-2">
        <CardTitle className="text-sm">{title}</CardTitle>
      </CardHeader>
      <CardContent className="flex flex-col gap-4 p-4 pt-0">
        <div className="flex items-end justify-between gap-3">
          <p className="text-3xl font-black text-espresso">{total.toLocaleString()}</p>
          <p className="text-sm text-muted">total</p>
        </div>
        <div className="flex h-3 overflow-hidden rounded-full bg-surface-muted">
          {segments.map((segment) => (
            <div
              className={segment.className}
              key={segment.label}
              style={{ width: `${safePercent(segment.value, total)}%` }}
            />
          ))}
        </div>
        <div className="grid gap-2">
          {segments.map((segment) => (
            <div className="flex items-center justify-between gap-2 text-sm" key={segment.label}>
              <span className="flex items-center gap-2 text-muted">
                <span className={cn("size-2 rounded-full", segment.className)} />
                {segment.label}
              </span>
              <span className="font-bold text-foreground">{segment.value.toLocaleString()}</span>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
}

function ModerationQueueChart({ pending }: { pending: number }) {
  const pressure = Math.min(pending, 100);

  return (
    <Card>
      <CardHeader className="p-4 pb-2">
        <CardTitle className="text-sm">Moderation queue</CardTitle>
      </CardHeader>
      <CardContent className="flex flex-col gap-4 p-4 pt-0">
        <div className="flex items-center justify-between gap-4">
          <div>
            <p className="text-3xl font-black text-espresso">{pending.toLocaleString()}</p>
            <p className="text-xs font-semibold text-muted">pending items</p>
          </div>
          <ShieldAlertIcon className="size-9 text-rating" />
        </div>
        <div className="grid h-20 grid-cols-10 items-end gap-1">
          {Array.from({ length: 10 }).map((_, index) => {
            const threshold = (index + 1) * 10;
            const isActive = pressure >= threshold - 9;

            return (
              <div
                className={cn(
                  "rounded-sm",
                  isActive ? "bg-rating" : "bg-surface-muted",
                )}
                key={threshold}
                style={{ height: `${18 + index * 7}%` }}
              />
            );
          })}
        </div>
      </CardContent>
    </Card>
  );
}

export function AdminDashboardCharts({ summary }: { summary: AdminDashboardSummary }) {
  return (
    <div className="grid gap-4 lg:grid-cols-2 xl:grid-cols-5">
      <RatioChart
        title="User activity"
        value={summary.activeUsers}
        total={summary.totalUsers}
        primaryLabel="active users"
        totalLabel="users"
        className="bg-primary"
      />
      <StackedStatusChart
        title="Blog status"
        total={summary.totalBlogs}
        segments={[
          { label: "Published", value: summary.publishedBlogs, className: "bg-primary" },
          { label: "Hidden", value: summary.hiddenBlogs, className: "bg-accent" },
          {
            label: "Other",
            value: Math.max(summary.totalBlogs - summary.publishedBlogs - summary.hiddenBlogs, 0),
            className: "bg-coffee-muted",
          },
        ]}
      />
      <StackedStatusChart
        title="Payment status"
        total={summary.totalPayments}
        segments={[
          { label: "Paid", value: summary.paidPayments, className: "bg-primary" },
          { label: "Pending", value: summary.pendingPayments, className: "bg-rating" },
          { label: "Failed", value: summary.failedPayments, className: "bg-accent" },
        ]}
      />
      <RatioChart
        title="Cafe pages"
        value={summary.activeCafePages}
        total={summary.totalCafePages}
        primaryLabel="active cafe pages"
        totalLabel="pages"
        className="bg-coffee-muted"
      />
      <ModerationQueueChart pending={summary.pendingModerationItems} />
    </div>
  );
}

export const dashboardChartLegend = [
  { label: "Active / paid / published", icon: CheckCircle2Icon, className: "text-primary" },
  { label: "Pending / review", icon: ClockIcon, className: "text-rating" },
  { label: "Failed / hidden", icon: AlertTriangleIcon, className: "text-accent" },
  { label: "Total / other", icon: CircleIcon, className: "text-coffee-muted" },
] as const;
