"use client";

import { useId, useState } from "react";
import { Card } from "@/components/ui/card";
import type { ReviewerPerformancePoint } from "@/features/reviewer-dashboard/reviewer-dashboard.types";
import { useI18n } from "@/components/providers/locale-provider";

type ReviewerPerformanceChartProps = {
  data: ReviewerPerformancePoint[];
};

/**
 * Khung vẽ theo đơn vị viewBox, tỉ lệ ~3.5:1 để SVG co giãn theo chiều rộng mà
 * không cần preserveAspectRatio="none" — bản cũ dùng cờ đó nên nét vẽ và chữ bị
 * kéo ngang méo trên màn hình rộng.
 */
const VIEW_W = 900;
const VIEW_H = 340;
const PAD_LEFT = 46;
// Nhãn tháng canh giữa điểm; điểm cuối nằm sát mép phải nên padding phải đủ
// rộng cho nửa nhãn, không thì "Tháng 8" bị viewBox cắt cụt thành "Tháng".
const PAD_RIGHT = 46;
const PLOT_TOP = 20;
const PLOT_BOTTOM = 296;
const PLOT_W = VIEW_W - PAD_LEFT - PAD_RIGHT;
const PLOT_H = PLOT_BOTTOM - PLOT_TOP;

const GRID_RATIOS = [0, 0.25, 0.5, 0.75, 1];

const compactFormatter = new Intl.NumberFormat("en", {
  notation: "compact",
  maximumFractionDigits: 1,
});

/** Trần trục Y làm tròn lên mốc đẹp để đường lưới không ra số lẻ. */
function niceCeil(value: number) {
  if (value <= 0) return 1;
  const magnitude = 10 ** Math.floor(Math.log10(value));
  const normalized = value / magnitude;
  const step = normalized <= 1 ? 1 : normalized <= 2 ? 2 : normalized <= 5 ? 5 : 10;
  return step * magnitude;
}

export function ReviewerPerformanceChart({
  data,
}: ReviewerPerformanceChartProps) {
  const { t } = useI18n();
  const gradientId = useId();
  const [activeIndex, setActiveIndex] = useState<number | null>(null);

  const header = (
    <div>
      <p className="text-sm font-black text-muted">
        {t("reviewer.performance.title")}
      </p>
      <h2 className="mt-1 text-xl font-black text-espresso">
        {t("reviewer.performance.subtitle")}
      </h2>
    </div>
  );

  if (data.length === 0) {
    return (
      <Card className="p-5">
        {header}
        <div className="mt-6 flex h-56 items-center justify-center rounded-md bg-surface-muted/55 text-sm font-semibold text-muted">
          {t("reviewer.performance.noChartData")}
        </div>
      </Card>
    );
  }

  const yMax = niceCeil(Math.max(...data.map((item) => item.score)));
  const stepX = data.length > 1 ? PLOT_W / (data.length - 1) : 0;
  const xAt = (index: number) =>
    data.length > 1 ? PAD_LEFT + index * stepX : PAD_LEFT + PLOT_W / 2;
  const yAt = (score: number) => PLOT_BOTTOM - (score / yMax) * PLOT_H;

  const linePoints = data.map((item, i) => `${xAt(i)},${yAt(item.score)}`);
  const areaPath = [
    `M ${xAt(0)},${PLOT_BOTTOM}`,
    ...data.map((item, i) => `L ${xAt(i)},${yAt(item.score)}`),
    `L ${xAt(data.length - 1)},${PLOT_BOTTOM}`,
    "Z",
  ].join(" ");

  const active = activeIndex === null ? null : data[activeIndex];

  // Tooltip mặc định nổi phía trên điểm. Điểm nằm cao thì phần trên bị mép khung
  // cắt mất (container có overflow nên không tràn ra ngoài được) — lật xuống dưới.
  const flipTooltipBelow =
    active !== null && yAt(active.score) < PLOT_TOP + PLOT_H * 0.45;

  return (
    <Card className="p-5">
      {header}

      <div className="relative mt-6 overflow-x-auto rounded-md bg-surface-muted/55 p-4">
        <svg
          aria-label={t("reviewer.performance.chartAria")}
          className="w-full min-w-[560px] text-muted"
          role="img"
          viewBox={`0 0 ${VIEW_W} ${VIEW_H}`}
        >
          <defs>
            <linearGradient id={gradientId} x1="0" x2="0" y1="0" y2="1">
              <stop offset="0%" stopColor="var(--primary)" stopOpacity="0.22" />
              <stop offset="100%" stopColor="var(--primary)" stopOpacity="0" />
            </linearGradient>
          </defs>

          {/* Lưới ngang + nhãn trục Y */}
          {GRID_RATIOS.map((ratio) => {
            const y = PLOT_BOTTOM - ratio * PLOT_H;
            return (
              <g key={ratio}>
                <line
                  stroke="currentColor"
                  strokeOpacity="0.18"
                  strokeWidth="1"
                  x1={PAD_LEFT}
                  x2={VIEW_W - PAD_RIGHT}
                  y1={y}
                  y2={y}
                />
                <text
                  fill="currentColor"
                  fontSize="11"
                  fontWeight="500"
                  textAnchor="end"
                  x={PAD_LEFT - 10}
                  y={y + 4}
                >
                  {compactFormatter.format(Math.round(ratio * yMax))}
                </text>
              </g>
            );
          })}

          <path d={areaPath} fill={`url(#${gradientId})`} />

          {/* Đường nối chỉ vẽ khi có từ 2 điểm — 1 điểm thì polyline rỗng */}
          {data.length > 1 ? (
            <polyline
              fill="none"
              points={linePoints.join(" ")}
              stroke="var(--primary)"
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="3"
            />
          ) : null}

          {/* Đường dóng dọc tại điểm đang hover */}
          {activeIndex !== null ? (
            <line
              stroke="var(--primary)"
              strokeDasharray="4 4"
              strokeOpacity="0.45"
              strokeWidth="1.5"
              x1={xAt(activeIndex)}
              x2={xAt(activeIndex)}
              y1={PLOT_TOP}
              y2={PLOT_BOTTOM}
            />
          ) : null}

          {data.map((item, index) => {
            const x = xAt(index);
            const y = yAt(item.score);
            const isActive = activeIndex === index;

            return (
              <g key={item.label}>
                <circle
                  cx={x}
                  cy={y}
                  fill="var(--surface)"
                  r={isActive ? 8 : 6}
                  stroke="var(--primary)"
                  strokeWidth="2"
                />
                <circle
                  cx={x}
                  cy={y}
                  fill="var(--primary)"
                  r={isActive ? 4 : 3}
                />
                <text
                  fill="currentColor"
                  fontSize="12"
                  fontWeight={isActive ? 800 : 600}
                  textAnchor="middle"
                  x={x}
                  y={VIEW_H - 14}
                >
                  {item.label}
                </text>
              </g>
            );
          })}

          {/* Vùng bắt hover rộng cả cột: rê vào đâu trong cột cũng trúng, không
              phải nhắm đúng chấm 6px. */}
          {data.map((item, index) => {
            const width = data.length > 1 ? stepX : PLOT_W;
            const x = xAt(index) - width / 2;

            return (
              <rect
                aria-label={`${item.label}: ${item.score}`}
                className="cursor-pointer focus:outline-none"
                fill="transparent"
                height={PLOT_H}
                key={`hit-${item.label}`}
                onBlur={() => setActiveIndex(null)}
                onFocus={() => setActiveIndex(index)}
                onMouseEnter={() => setActiveIndex(index)}
                onMouseLeave={() => setActiveIndex(null)}
                tabIndex={0}
                width={width}
                x={Math.max(x, 0)}
                y={PLOT_TOP}
              />
            );
          })}
        </svg>

        {active && activeIndex !== null ? (
          <div
            className="pointer-events-none absolute z-10 min-w-40 rounded-md border border-border bg-surface p-3 shadow-lg"
            style={{
              left: `${(xAt(activeIndex) / VIEW_W) * 100}%`,
              top: `${(yAt(active.score) / VIEW_H) * 100}%`,
              transform: `translate(${
                activeIndex === 0
                  ? "-10%"
                  : activeIndex === data.length - 1
                    ? "-90%"
                    : "-50%"
              }, ${flipTooltipBelow ? "16px" : "calc(-100% - 16px)"})`,
            }}
          >
            <p className="text-xs font-black text-espresso">{active.label}</p>
            <p className="mt-1 text-lg font-black leading-none text-primary">
              {active.score}
              <span className="ml-1 text-xs font-semibold text-muted">
                {t("reviewer.table.score")}
              </span>
            </p>
            <dl className="mt-2 flex flex-col gap-1 text-xs">
              <div className="flex justify-between gap-4">
                <dt className="font-semibold text-muted">
                  {t("reviewer.table.likes")}
                </dt>
                <dd className="font-black text-espresso">{active.likes}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="font-semibold text-muted">
                  {t("reviewer.table.shares")}
                </dt>
                <dd className="font-black text-espresso">{active.shares}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="font-semibold text-muted">
                  {t("reviewer.table.comments")}
                </dt>
                <dd className="font-black text-espresso">{active.comments}</dd>
              </div>
            </dl>
          </div>
        ) : null}
      </div>
    </Card>
  );
}
