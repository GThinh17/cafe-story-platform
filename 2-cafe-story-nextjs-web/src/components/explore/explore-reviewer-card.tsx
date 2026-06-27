"use client";

import Link from "next/link";
import { AvatarImage } from "@/components/ui/avatar-image";
import { FollowButton } from "@/components/ui/follow-button";
import { ReviewerBadgeChip } from "@/components/ui/reviewer-badge-chip";

type ExploreReviewerCardProps = {
  reviewerId: string;
  userId: string;
  username: string | null;
  fullName: string | null;
  avatar: string | null;
  badge: string | null;
  isFollowing: boolean;
};

export function ExploreReviewerCard({
  userId,
  username,
  fullName,
  avatar,
  badge,
  isFollowing,
}: ExploreReviewerCardProps) {
  const href = username ? `/${username}` : `/users/${userId}`;
  const title = username ? `@${username}` : (fullName ?? "Reviewer");
  const subtitle = username ? (fullName ?? null) : null;

  return (
    <Link
      className="group flex cursor-pointer flex-col gap-3 py-3 no-underline"
      href={href}
    >
      <div className="flex items-center gap-3">
        <div className="size-12 shrink-0 overflow-hidden rounded-full border border-border">
          <AvatarImage alt={title} src={avatar} />
        </div>
        <div className="min-w-0 flex-1">
          <p className="truncate text-sm font-black text-foreground transition-colors group-hover:text-primary">
            {title} <ReviewerBadgeChip badge={badge} className="px-1 py-0.5 text-[8px] tracking-normal" />
          </p>
          {subtitle ? (
            <p className="truncate text-xs text-muted">{subtitle}</p>
          ) : null}
        </div>
        <FollowButton
          className="ml-auto !h-[30px] shrink-0 !px-4 !text-[14px]"
          isFollowing={isFollowing}
          targetId={userId}
          targetType="user"
        />
      </div>
    </Link>
  );
}
