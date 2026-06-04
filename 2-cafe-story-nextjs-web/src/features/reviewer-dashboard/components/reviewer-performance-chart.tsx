import { Card } from "@/components/ui/card";
import type { ReviewerPerformancePoint } from "@/features/reviewer-dashboard/reviewer-dashboard.types";

type ReviewerPerformanceChartProps = {
  data: ReviewerPerformancePoint[];
};

export function ReviewerPerformanceChart({
  data,
}: ReviewerPerformanceChartProps) {
  const maxScore = Math.max(...data.map((item) => item.score), 1);
  const points = data
    .map((item, index) => {
      const x = 32 + index * (336 / Math.max(data.length - 1, 1));
      const y = 178 - (item.score / maxScore) * 136;

      return `${x},${y}`;
    })
    .join(" ");

  return (
    <Card className="p-5">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-sm font-black text-muted">Performance</p>
          <h2 className="mt-1 text-xl font-black text-espresso">
            Score trend by month
          </h2>
        </div>
        <p className="text-xs font-semibold text-muted">
          CSS/SVG mock chart, no chart dependency
        </p>
      </div>

      <div className="mt-6 overflow-hidden rounded-md bg-surface-muted/55 p-4">
        <svg
          aria-label="Reviewer performance chart"
          className="h-64 w-full"
          preserveAspectRatio="none"
          viewBox="0 0 400 220"
        >
          {[48, 92, 136, 180].map((y) => (
            <line
              key={y}
              stroke="currentColor"
              strokeOpacity="0.15"
              strokeWidth="1"
              x1="24"
              x2="376"
              y1={y}
              y2={y}
            />
          ))}
          <polyline
            fill="none"
            points={points}
            stroke="var(--primary)"
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="4"
          />
          {data.map((item, index) => {
            const x = 32 + index * (336 / Math.max(data.length - 1, 1));
            const y = 178 - (item.score / maxScore) * 136;

            return (
              <g key={item.label}>
                <circle cx={x} cy={y} fill="var(--surface)" r="6" />
                <circle cx={x} cy={y} fill="var(--primary)" r="3" />
                <text
                  fill="currentColor"
                  fontSize="10"
                  fontWeight="700"
                  textAnchor="middle"
                  x={x}
                  y="205"
                >
                  {item.label}
                </text>
              </g>
            );
          })}
        </svg>
      </div>
    </Card>
  );
}
