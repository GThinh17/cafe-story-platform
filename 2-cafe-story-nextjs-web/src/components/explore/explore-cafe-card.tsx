"use client";

import Link from "next/link";
import { AvatarImage } from "@/components/ui/avatar-image";
import { FollowButton } from "@/components/ui/follow-button";

type ExploreCafeCardProps = {
  id: string;
  name: string;
  avatar: string | null;
  city: string | null;
  isFollowing: boolean;
};

export function ExploreCafeCard({
  id,
  name,
  avatar,
  city,
  isFollowing,
}: ExploreCafeCardProps) {
  return (
    <Link
      className="group flex cursor-pointer flex-col gap-3 py-3 no-underline"
      href={`/cafes/${id}`}
    >
      <div className="flex items-center gap-3">
        <div className="size-12 shrink-0 overflow-hidden rounded-full border border-border">
          <AvatarImage alt={name} src={avatar} />
        </div>
        <div className="min-w-0 flex-1">
          <p className="truncate text-sm font-black text-foreground transition-colors group-hover:text-primary">
            {name}
          </p>
          {city ? <p className="truncate text-xs text-muted">{city}</p> : null}
        </div>
        <FollowButton
          className="ml-auto !h-[30px] shrink-0 !px-4 !text-[14px]"
          isFollowing={isFollowing}
          targetId={id}
          targetType="cafe"
        />
      </div>
    </Link>
  );
}
