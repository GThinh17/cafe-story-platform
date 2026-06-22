import { cn } from "@/lib/utils";

type ReviewerBadgeChipProps = {
  badge: string | null | undefined;
  className?: string;
};

const BADGE_GRADIENT: Record<string, string> = {
  IRON: "var(--badge-iron)",
  BRONZE: "var(--badge-bronze)",
  SILVER: "var(--badge-silver)",
  GOLD: "var(--badge-gold)",
  DIAMOND: "var(--badge-diamond)",
};

export function ReviewerBadgeChip({ badge, className }: ReviewerBadgeChipProps) {
  if (!badge) return null;
  const key = badge.toUpperCase();
  const gradient = BADGE_GRADIENT[key];
  if (!gradient) return null;

  const label = key.charAt(0) + badge.slice(1).toLowerCase();

  return (
    <span
      className={cn(
        "inline-flex shrink-0 items-center rounded-full px-2 py-0.5 text-[10px] font-black uppercase tracking-wide text-white",
        className,
      )}
      style={{ background: gradient }}
    >
      {label}
    </span>
  );
}
