import { Badge } from "@/components/ui/badge";

type AdminStatusBadgeProps = {
  value: string | boolean | null | undefined;
};

const STATUS_LABELS: Record<string, string> = {
  ACTIVE: "Active",
  PAID: "Paid",
  PUBLISHED: "Published",
  SAFE: "Safe",
  RESOLVED: "Resolved",
  APPROVE: "Approved",
  APPROVED: "Approved",
  APPLIED: "Applied",
  PENDING: "Pending",
  SCHEDULED: "Scheduled",
  OPEN: "Open",
  REVIEWING: "Reviewing",
  NEEDS_REVIEW: "Needs review",
  DRAFT: "Draft",
  PROCESSING: "Processing",
  APPLYING: "Applying",
  HIDE: "Hidden",
  FAILED: "Failed",
  HIDDEN: "Hidden",
  REMOVED: "Removed",
  REMOVE: "Removed",
  VIOLATION: "Violation",
  REJECTED: "Rejected",
  CANCELLED: "Cancelled",
  EXPIRED: "Expired",
  INACTIVE: "Inactive",
  SUSPENDED: "Suspended",
  SKIPPED: "Skipped",
};

function getStatusClassName(normalized: string) {
  if (
    ["ACTIVE", "PAID", "PUBLISHED", "SAFE", "RESOLVED", "APPROVE", "APPROVED", "APPLIED"].includes(normalized)
  ) {
    return "bg-primary/10 text-primary-strong border-transparent";
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
  const raw =
    typeof value === "boolean" ? (value ? "ACTIVE" : "INACTIVE") : value || "UNKNOWN";

  const normalized = String(raw).toUpperCase();
  const label = STATUS_LABELS[normalized] ?? String(raw);

  return <Badge className={getStatusClassName(normalized)}>{label}</Badge>;
}
