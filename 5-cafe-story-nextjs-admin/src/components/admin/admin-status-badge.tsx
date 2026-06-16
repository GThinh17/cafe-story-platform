import { Badge } from "@/components/ui/badge";

type AdminStatusBadgeProps = {
  value: string | boolean | null | undefined;
};

function getStatusClassName(normalized: string) {
  if (
    ["ACTIVE", "PAID", "PUBLISHED", "SAFE", "RESOLVED", "APPROVE", "APPROVED"].includes(
      normalized,
    )
  ) {
    return "bg-emerald-100 text-emerald-800";
  }

  if (
    [
      "PENDING",
      "OPEN",
      "REVIEWING",
      "NEEDS_REVIEW",
      "DRAFT",
      "HIDE",
    ].includes(normalized)
  ) {
    return "bg-amber-100 text-amber-800";
  }

  if (
    [
      "FAILED",
      "HIDDEN",
      "REMOVED",
      "REMOVE",
      "VIOLATION",
      "REJECTED",
      "CANCELLED",
      "EXPIRED",
      "INACTIVE",
      "SUSPENDED",
    ].includes(normalized)
  ) {
    return "bg-red-100 text-red-800";
  }

  return "bg-blue-100 text-blue-800";
}

export function AdminStatusBadge({ value }: AdminStatusBadgeProps) {
  const label =
    typeof value === "boolean"
      ? value
        ? "ACTIVE"
        : "INACTIVE"
      : value || "UNKNOWN";

  const normalized = String(label).toUpperCase();

  return <Badge className={getStatusClassName(normalized)}>{label}</Badge>;
}
