"use client";

import { useMemo } from "react";
import { Bar, BarChart, CartesianGrid, XAxis, YAxis } from "recharts";
import {
  ChartContainer,
  ChartTooltip,
  type ChartConfig,
} from "@/components/ui/chart";
import type { AdminRevenueAnalytics, AdminRevenuePoint } from "@/types/admin";

const vndFormatter = new Intl.NumberFormat("vi-VN", {
  style: "currency",
  currency: "VND",
  maximumFractionDigits: 0,
});

export function formatVnd(value: number) {
  return vndFormatter.format(value);
}

export function formatVndCompact(value: number) {
  if (Math.abs(value) >= 1_000_000_000) {
    return `${(value / 1_000_000_000).toLocaleString("vi-VN", { maximumFractionDigits: 1 })} tỷ`;
  }
  if (Math.abs(value) >= 1_000_000) {
    return `${(value / 1_000_000).toLocaleString("vi-VN", { maximumFractionDigits: 1 })} tr`;
  }
  if (Math.abs(value) >= 1_000) {
    return `${(value / 1_000).toLocaleString("vi-VN", { maximumFractionDigits: 0 })} k`;
  }
  return value.toLocaleString("vi-VN");
}

const REVENUE_SERIES = [
  {
    key: "reviewerRegistrationAmount",
    label: "Reviewer",
    color: "var(--chart-2)",
  },
  {
    key: "cafePageOpeningAmount",
    label: "Cafe page",
    color: "var(--chart-3)",
  },
  {
    key: "advertiseAmount",
    label: "Advertise",
    color: "var(--chart-4)",
  },
] as const;

const chartConfig: ChartConfig = Object.fromEntries(
  REVENUE_SERIES.map((series) => [
    series.key,
    { label: series.label, color: series.color },
  ]),
);

const dateLabel = new Intl.DateTimeFormat("vi-VN", {
  day: "2-digit",
  month: "2-digit",
});

type TooltipPayloadItem = {
  dataKey?: string | number;
  payload?: AdminRevenuePoint;
};

function RevenueTooltip({
  active,
  payload,
}: {
  active?: boolean;
  payload?: TooltipPayloadItem[];
}) {
  const point = payload?.[0]?.payload;
  if (!active || !point) {
    return null;
  }

  const total = point.totalAmount;

  return (
    <div className="min-w-48 rounded-md border border-border bg-surface px-3 py-2 text-xs shadow-md">
      <div className="flex items-center justify-between gap-4">
        <span className="font-semibold text-espresso">
          {dateLabel.format(new Date(point.date))}
        </span>
        <span className="font-semibold text-espresso">{formatVnd(total)}</span>
      </div>
      <div className="mt-2 flex flex-col gap-1">
        {REVENUE_SERIES.map((series) => {
          const value = point[series.key];
          const percent = total > 0 ? Math.round((value / total) * 100) : 0;
          return (
            <div className="flex items-center gap-2" key={series.key}>
              <span
                className="size-2 shrink-0 rounded-[2px]"
                style={{ backgroundColor: series.color }}
              />
              <span className="text-muted">{series.label}</span>
              <span className="ml-auto font-medium text-foreground">
                {formatVnd(value)} · {percent}%
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export function AdminRevenueChart({
  analytics,
  className,
}: {
  analytics: AdminRevenueAnalytics;
  className?: string;
}) {
  const data = useMemo(
    () =>
      analytics.daily.map((point) => ({
        ...point,
        label: dateLabel.format(new Date(point.date)),
      })),
    [analytics.daily],
  );

  return (
    <ChartContainer config={chartConfig} className={className}>
      <BarChart data={data} margin={{ top: 8, right: 8, bottom: 0, left: 8 }}>
        <CartesianGrid vertical={false} stroke="var(--line-soft)" />
        <XAxis
          dataKey="label"
          tickLine={false}
          axisLine={false}
          tickMargin={6}
          minTickGap={24}
          tick={{ fontSize: 11 }}
        />
        <YAxis
          tickLine={false}
          axisLine={false}
          width={44}
          tick={{ fontSize: 11 }}
          tickFormatter={(value: number) => formatVndCompact(value)}
        />
        <ChartTooltip cursor={{ fill: "var(--surface-muted)" }} content={<RevenueTooltip />} />
        {REVENUE_SERIES.map((series, index) => (
          <Bar
            key={series.key}
            dataKey={series.key}
            stackId="revenue"
            fill={series.color}
            radius={
              index === REVENUE_SERIES.length - 1 ? [3, 3, 0, 0] : [0, 0, 0, 0]
            }
            maxBarSize={26}
          />
        ))}
      </BarChart>
    </ChartContainer>
  );
}

export function RevenueLegend() {
  return (
    <div className="flex flex-wrap items-center gap-3 text-xs text-muted">
      {REVENUE_SERIES.map((series) => (
        <span className="flex items-center gap-1.5" key={series.key}>
          <span
            className="size-2 rounded-[2px]"
            style={{ backgroundColor: series.color }}
          />
          {series.label}
        </span>
      ))}
    </div>
  );
}
