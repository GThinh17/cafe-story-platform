"use client";

import { useMemo, useState } from "react";
import {
  VIETNAM_MAP_VIEWBOX,
  VIETNAM_PROVINCES_34,
} from "@/lib/geo/vietnam-provinces-34";
import { cn } from "@/lib/utils";

export type VietnamMapDatum = {
  provinceCode: string;
  provinceName: string;
  value: number;
};

type VietnamMapProps = {
  data: VietnamMapDatum[];
  /** Label shown in the tooltip after the value, e.g. "users". */
  valueLabel?: string;
  selectedProvince?: string | null;
  onProvinceClick?: (provinceCode: string) => void;
  className?: string;
};

const FILL_STEPS = [12, 28, 46, 66, 88] as const;

function fillForValue(value: number, max: number) {
  if (value <= 0 || max <= 0) {
    return "var(--surface-muted)";
  }
  const ratio = value / max;
  const step =
    FILL_STEPS[
      Math.min(FILL_STEPS.length - 1, Math.floor(ratio * FILL_STEPS.length))
    ];
  return `color-mix(in srgb, var(--chart-1) ${step}%, var(--surface))`;
}

export function VietnamMap({
  data,
  valueLabel = "users",
  selectedProvince,
  onProvinceClick,
  className,
}: VietnamMapProps) {
  const [hovered, setHovered] = useState<string | null>(null);

  const byCode = useMemo(() => {
    const map = new Map<string, VietnamMapDatum>();
    data.forEach((item) => map.set(item.provinceCode, item));
    return map;
  }, [data]);

  const maxValue = useMemo(
    () => Math.max(0, ...data.map((item) => item.value)),
    [data],
  );

  const hoveredDatum = hovered ? byCode.get(hovered) : null;
  const hoveredProvince = hovered
    ? VIETNAM_PROVINCES_34.find((province) => province.code === hovered)
    : null;

  return (
    <div className={cn("relative", className)}>
      <svg
        viewBox={VIETNAM_MAP_VIEWBOX}
        className="h-full w-full"
        role="img"
        aria-label="Vietnam province map"
      >
        {VIETNAM_PROVINCES_34.map((province) => {
          const datum = byCode.get(province.code);
          const isHovered = hovered === province.code;
          const isSelected = selectedProvince === province.code;
          return (
            <path
              key={province.code}
              d={province.d}
              fill={fillForValue(datum?.value ?? 0, maxValue)}
              stroke={
                isHovered || isSelected ? "var(--primary)" : "var(--border)"
              }
              strokeWidth={isHovered || isSelected ? 1.6 : 0.6}
              className={cn(
                "transition-[fill,stroke]",
                onProvinceClick && "cursor-pointer",
              )}
              onMouseEnter={() => setHovered(province.code)}
              onMouseLeave={() =>
                setHovered((current) =>
                  current === province.code ? null : current,
                )
              }
              onClick={
                onProvinceClick
                  ? (event) => {
                      event.stopPropagation();
                      onProvinceClick(province.code);
                    }
                  : undefined
              }
            >
              <title>
                {`${province.name}: ${(datum?.value ?? 0).toLocaleString("vi-VN")} ${valueLabel}`}
              </title>
            </path>
          );
        })}
      </svg>
      {hoveredProvince ? (
        <div className="pointer-events-none absolute left-2 top-2 rounded-md border border-border bg-surface px-2.5 py-1.5 text-xs shadow-sm">
          <p className="font-semibold text-espresso">{hoveredProvince.name}</p>
          <p className="text-muted">
            {(hoveredDatum?.value ?? 0).toLocaleString("vi-VN")} {valueLabel}
          </p>
        </div>
      ) : null}
    </div>
  );
}
