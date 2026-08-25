"use client";

import Link from "next/link";
import { AvatarImage } from "@/components/ui/avatar-image";
import { useI18n } from "@/components/providers/locale-provider";
import type { ReviewerRankingItem } from "@/features/reviewer-dashboard/reviewer-dashboard.types";
import { cn } from "@/lib/utils";

type ReviewerRankingNameProps = {
  className?: string;
  item: ReviewerRankingItem;
};

/**
 * Tên reviewer trong bảng xếp hạng, bấm vào mở trang cá nhân.
 *
 * <p>Trang cá nhân định tuyến theo userName (app/(main)/[username]), không theo
 * userId — chưa có route /users/{id} nào. Nên reviewer thiếu userName sẽ hiển
 * thị chữ thường thay vì một link chết.
 */
export function ReviewerRankingName({ className, item }: ReviewerRankingNameProps) {
  const { t } = useI18n();
  const label = item.userName ?? t("reviewer.leaderboard.unknownUser");

  const body = (
    <>
      <span className="size-6 shrink-0 overflow-hidden rounded-full">
        <AvatarImage alt={label} loading="lazy" src={item.userAvatar} />
      </span>
      <span className="truncate">{label}</span>
    </>
  );

  const baseClassName = cn(
    "flex min-w-0 items-center gap-2 font-black text-espresso",
    className,
  );

  if (!item.userName) {
    return <span className={baseClassName}>{body}</span>;
  }

  return (
    <Link
      className={cn(baseClassName, "hover:text-primary hover:underline")}
      href={`/${item.userName}`}
    >
      {body}
    </Link>
  );
}
