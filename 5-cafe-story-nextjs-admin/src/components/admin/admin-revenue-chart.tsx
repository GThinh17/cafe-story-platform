"use client";

import { useMemo } from "react";
import { Bar, BarChart, CartesianGrid, XAxis, YAxis } from "recharts";
import {
  ChartContainer,
  ChartTooltip,
  type ChartConfig,
} from "@/components/ui/chart";
import type { AdminRevenueAnalytics, AdminRevenuePoint } from "@/types/admin";
import {
  formatCompactNumber,
  formatCurrency,
  formatDate,
  useI18n,
  useUiText,
  type LocaleTag,
} from "@/features/i18n";

export function formatVnd(value: number, localeTag: LocaleTag) {
  return formatCurrency(value, "VND", localeTag);
}

export function formatVndCompact(value: number, localeTag: LocaleTag) {
  return formatCompactNumber(value, localeTag);
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
  const { localeTag } = useI18n();
  const ui = useUiText();
  const point = payload?.[0]?.payload;
  if (!active || !point) {
    return null;
  }

  const total = point.totalAmount;

  return (
    <div className="min-w-48 rounded-md border border-border bg-surface px-3 py-2 text-xs shadow-md">
      <div className="flex items-center justify-between gap-4">
        <span className="font-semibold text-espresso">
          {formatDate(point.date, localeTag)}
        </span>
        <span className="font-semibold text-espresso">{formatVnd(total, localeTag)}</span>
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
              <span className="text-muted">{ui(series.label)}</span>
              <span className="ml-auto font-medium text-foreground">
                {formatVnd(value, localeTag)} · {percent}%
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
  const { localeTag } = useI18n();
  const ui = useUiText();
  const chartConfig: ChartConfig = useMemo(
    () =>
      Object.fromEntries(
        REVENUE_SERIES.map((series) => [
          series.key,
          { label: ui(series.label), color: series.color },
        ]),
      ),
    [ui],
  );
  const data = useMemo(
    () =>
      analytics.daily.map((point) => ({
        ...point,
        label: formatDate(point.date, localeTag),
      })),
    [analytics.daily, localeTag],
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
          tickFormatter={(value: number) => formatVndCompact(value, localeTag)}
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
  const ui = useUiText();
  return (
    <div className="flex flex-wrap items-center gap-3 text-xs text-muted">
      {REVENUE_SERIES.map((series) => (
        <span className="flex items-center gap-1.5" key={series.key}>
          <span
            className="size-2 rounded-[2px]"
            style={{ backgroundColor: series.color }}
          />
          {ui(series.label)}
        </span>
      ))}
    </div>
  );
}
