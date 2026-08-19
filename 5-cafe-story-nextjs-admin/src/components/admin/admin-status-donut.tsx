"use client";

import { useMemo } from "react";
import { Cell, Pie, PieChart } from "recharts";
import {
  ChartContainer,
  ChartTooltip,
  type ChartConfig,
} from "@/components/ui/chart";
import { formatNumber, useI18n, useUiText } from "@/features/i18n";

export type DonutSlice = {
  key: string;
  label: string;
  value: number;
  color: string;
};

type TooltipPayloadItem = {
  payload?: DonutSlice;
};

function DonutTooltip({
  active,
  payload,
  total,
}: {
  active?: boolean;
  payload?: TooltipPayloadItem[];
  total: number;
}) {
  const { localeTag } = useI18n();
  const ui = useUiText();
  const slice = payload?.[0]?.payload;
  if (!active || !slice) {
    return null;
  }
  const percent = total > 0 ? Math.round((slice.value / total) * 100) : 0;

  return (
    <div className="rounded-md border border-border bg-surface px-2.5 py-1.5 text-xs shadow-md">
      <div className="flex items-center gap-2">
        <span
          className="size-2 rounded-[2px]"
          style={{ backgroundColor: slice.color }}
        />
        <span className="text-muted">{ui(slice.label)}</span>
        <span className="ml-2 font-medium text-foreground">
          {formatNumber(slice.value, localeTag)} · {percent}%
        </span>
      </div>
    </div>
  );
}

export function AdminStatusDonut({
  slices,
  centerLabel,
  className,
}: {
  slices: DonutSlice[];
  centerLabel?: string;
  className?: string;
}) {
  const { localeTag } = useI18n();
  const ui = useUiText();
  const total = useMemo(
    () => slices.reduce((sum, slice) => sum + slice.value, 0),
    [slices],
  );

  const config: ChartConfig = useMemo(
    () =>
      Object.fromEntries(
        slices.map((slice) => [
          slice.key,
          { label: ui(slice.label), color: slice.color },
        ]),
      ),
    [slices, ui],
  );

  return (
    <div className={className}>
      <ChartContainer config={config} className="mx-auto aspect-square max-h-44">
        <PieChart margin={{ top: 0, right: 0, bottom: 0, left: 0 }}>
          <ChartTooltip content={<DonutTooltip total={total} />} />
          <Pie
            data={slices}
            dataKey="value"
            nameKey="label"
            innerRadius="62%"
            outerRadius="90%"
            paddingAngle={2}
            strokeWidth={0}
          >
            {slices.map((slice) => (
              <Cell key={slice.key} fill={slice.color} />
            ))}
          </Pie>
          <text
            x="50%"
            y="47%"
            textAnchor="middle"
            dominantBaseline="middle"
            className="fill-espresso text-xl font-semibold"
          >
            {formatNumber(total, localeTag)}
          </text>
          {centerLabel ? (
            <text
              x="50%"
              y="58%"
              textAnchor="middle"
              dominantBaseline="middle"
              className="fill-muted text-[10px]"
            >
              {ui(centerLabel)}
            </text>
          ) : null}
        </PieChart>
      </ChartContainer>
      <div className="mt-1 flex flex-wrap justify-center gap-x-3 gap-y-1 text-xs text-muted">
        {slices.map((slice) => (
          <span className="flex items-center gap-1.5" key={slice.key}>
            <span
              className="size-2 rounded-[2px]"
              style={{ backgroundColor: slice.color }}
            />
            {ui(slice.label)}
          </span>
        ))}
      </div>
    </div>
  );
}
