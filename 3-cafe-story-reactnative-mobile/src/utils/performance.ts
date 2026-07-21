import { env } from "../config/env";

type PerformanceMetadata = Record<string, boolean | number | string | null>;

export function performanceTimestamp() {
  return globalThis.performance?.now?.() ?? Date.now();
}

export function logPerformanceMetric(
  metric: string,
  startedAt: number,
  metadata: PerformanceMetadata = {},
) {
  if (!env.performanceLoggingEnabled) {
    return;
  }

  const durationMs = Math.max(0, performanceTimestamp() - startedAt);
  console.info(`[PERF] ${metric}`, JSON.stringify({
    durationMs: Number(durationMs.toFixed(2)),
    ...metadata,
  }));
}
