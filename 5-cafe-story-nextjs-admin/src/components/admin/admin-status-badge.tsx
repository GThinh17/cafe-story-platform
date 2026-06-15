import { Badge } from "@/components/ui/badge";

type AdminStatusBadgeProps = {
  value: string | boolean | null | undefined;
};

export function AdminStatusBadge({ value }: AdminStatusBadgeProps) {
  const label =
    typeof value === "boolean"
      ? value
        ? "ACTIVE"
        : "INACTIVE"
      : value || "UNKNOWN";

  const normalized = String(label).toUpperCase();
  const positive = ["ACTIVE", "PAID", "PUBLISHED", "SAFE", "RESOLVED"].includes(
    normalized,
  );
  const warning = [
    "PENDING",
    "OPEN",
    "REVIEWING",
    "NEEDS_REVIEW",
    "DRAFT",
  ].includes(normalized);

  return (
    <Badge variant={positive ? "default" : warning ? "rating" : "outline"}>
      {label}
    </Badge>
  );
}
