"use client";

import { Badge } from "@/components/ui/badge";
import { useEnumLabel } from "@/features/i18n";

type AdminStatusBadgeProps = {
  value: string | boolean | null | undefined;
};

function getStatusClassName(normalized: string) {
  if (
    ["ACTIVE", "PAID", "PUBLISHED", "SAFE", "RESOLVED", "APPROVE", "APPROVED", "APPLIED"].includes(normalized)
  ) {
    return "bg-success/10 text-success border-transparent";
  }

  if (
    ["PENDING", "OPEN", "REVIEWING", "NEEDS_REVIEW", "DRAFT", "PROCESSING", "SCHEDULED", "APPLYING"].includes(normalized)
  ) {
    return "bg-rating/10 text-rating border-transparent";
  }

  if (
    [
      "FAILED",
      "HIDE",
      "HIDDEN",
      "REMOVED",
      "REMOVE",
      "VIOLATION",
      "REJECTED",
      "CANCELLED",
      "EXPIRED",
      "INACTIVE",
      "SUSPENDED",
      "SKIPPED",
    ].includes(normalized)
  ) {
    return "bg-accent/10 text-accent border-transparent";
  }

  return "bg-surface-muted text-coffee-muted border-transparent";
}

export function AdminStatusBadge({ value }: AdminStatusBadgeProps) {
  const enumLabel = useEnumLabel();
  const raw =
    typeof value === "boolean" ? (value ? "ACTIVE" : "INACTIVE") : value || "UNKNOWN";

  const normalized = String(raw).toUpperCase();
  const label = enumLabel(raw);

  return <Badge className={getStatusClassName(normalized)}>{label}</Badge>;
}
