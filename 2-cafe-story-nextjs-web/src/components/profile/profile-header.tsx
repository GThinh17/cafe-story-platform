"use client";

import type { ProfileHighlight, UserProfile } from "@/types/user";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { useState } from "react";
import { MoreHorizontal } from "lucide-react";
import { Button } from "@/components/ui/button";
import { FollowButton } from "@/components/ui/follow-button";
import { ReviewerBadgeChip } from "@/components/ui/reviewer-badge-chip";
import { ProfileSettingsModal } from "@/components/profile/profile-settings-modal";
import type { AuthUser } from "@/types/auth";
import { DEFAULT_AVATAR_IMAGE } from "@/lib/avatar";

type ProfileHeaderProps = {
  areActionsLoading?: boolean;
  cafePageHref?: string;
  currentUser?: AuthUser | null;
  highlights: ProfileHighlight[];
  isFollowing?: boolean;
  isLoading?: boolean;
  isMessageLoading?: boolean;
  isOwnProfile?: boolean;
  profileUserId?: string;
  onFollowToggle?: (nextIsFollowing: boolean) => void;
  onFollowersClick?: () => void;
  onFollowingClick?: () => void;
  onMessageClick?: () => void;
  profile: UserProfile;
};

const statLabels: Record<keyof UserProfile["stats"], string> = {
  posts: "posts",
  following: "following",
  followers: "followers",
};

export function ProfileHeader({
  areActionsLoading = false,
  cafePageHref,
  currentUser,
  highlights,
  isFollowing = false,
  isLoading = false,
  isMessageLoading = false,
  isOwnProfile = true,
  profileUserId,
  onFollowToggle,
  onFollowersClick,
  onFollowingClick,
  onMessageClick,
  profile,
}: ProfileHeaderProps) {
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);

  return (
    <>
      <section className="w-full overflow-hidden border-border">
        <div className="flex items-start gap-7 sm:gap-12">
          <div className="size-24 shrink-0 overflow-hidden rounded-full bg-gradient-to-tr from-rating via-accent to-primary p-[3px] sm:size-36">
            <div className="size-full overflow-hidden rounded-full border-4 border-background bg-surface-muted">
              <img
                alt={`${profile.displayName} avatar`}
                className="size-full rounded-full object-cover object-center"
                decoding="async"
                onError={(event) => {
                  event.currentTarget.src = DEFAULT_AVATAR_IMAGE;
                }}
                src={profile.avatarImage}
              />
            </div>
          </div>

          <div className="min-w-0 flex-1 space-y-2">
            <div className="flex flex-wrap items-center gap-3">
              <h1 className="truncate text-3xl font-bold text-foreground">
                {isLoading ? "Loading..." : profile.username}
              </h1>
              <ReviewerBadgeChip badge={profile.badge} className="px-3 py-1 text-xs" />
              {areActionsLoading ? null : isOwnProfile ? (
                <Button
                  aria-label="Profile settings"
                  className="bg-transparent text-lg font-black hover:cursor-pointer hover:bg-transparent focus:ring-0"
                  onClick={() => setIsSettingsOpen(true)}
                  size="icon-sm"
                  type="button"
                  variant="ghost"
                >
                  <MoreHorizontal />
                </Button>
              ) : null}
            </div>

            {areActionsLoading ? null : !isOwnProfile ? (
              <div className="flex flex-wrap gap-2 w-full">
                {profileUserId ? (
                  <FollowButton
                    className="flex-1"
                    isFollowing={isFollowing}
                    targetId={profileUserId}
                    targetType="user"
                    onToggle={onFollowToggle}
                  />
                ) : null}
                <Button
                  className="h-8 flex-1 text-sm font-black"
                  disabled={isMessageLoading}
                  onClick={onMessageClick}
                  type="button"
                  variant="secondary"
                >
                  {isMessageLoading ? "Opening" : "Message"}
                </Button>
              </div>
            ) : null}

            <div className="flex flex-wrap items-center gap-x-8 gap-y-2 text-sm">
              {Object.entries(profile.stats).map(([label, value]) => {
                const statKey = label as keyof UserProfile["stats"];

                const clickHandler =
                  statKey === "followers"
                    ? onFollowersClick
                    : statKey === "following"
                      ? onFollowingClick
                      : undefined;

                const content = (
                  <span className="inline-flex items-baseline gap-1 no-underline hover:">
                    <span className="text-[17px] font-semibold leading-none">
                      {value}
                    </span>
                    <span className="text-[16px] font-normal leading-none">
                      {statLabels[statKey]}
                    </span>
                  </span>
                );

                return clickHandler ? (
                  <button
                    className="m-0 inline-flex border-0 bg-transparent p-0 text-left text-foreground hover:cursor-pointer"
                    key={label}
                    onClick={clickHandler}
                    type="button"
                  >
                    {content}
                  </button>
                ) : (
                  <p key={label}>{content}</p>
                );
              })}
            </div>

            <div className="min-w-0 space-y-1 text-sm leading-6">
              <p className="font-black">{profile.displayName}</p>
              {profile.bio ? (
                <p className="max-w-full break-words">{profile.bio}</p>
              ) : null}
              <p className="text-muted">{profile.location}</p>
            </div>
          </div>
        </div>

        <div className="mt-6 grid grid-cols-4 justify-items-center gap-4 sm:flex sm:justify-start sm:gap-8">
          {highlights.map((highlight) => (
            <button
              className="grid w-20 justify-items-center gap-2 text-xs font-black"
              key={highlight.id}
              type="button"
            >
              <span className="size-16 overflow-hidden rounded-full border border-border bg-surface p-1 sm:size-20">
                <Avatar className="size-full">
                  <AvatarImage alt="" src={highlight.image} />
                  <AvatarFallback>{highlight.label.slice(0, 1)}</AvatarFallback>
                </Avatar>
              </span>
              <span className="block w-20 truncate text-center">
                {highlight.label}
              </span>
            </button>
          ))}
        </div>
      </section>

      <ProfileSettingsModal
        cafePageHref={cafePageHref}
        currentUser={currentUser}
        onOpenChange={setIsSettingsOpen}
        open={isSettingsOpen}
      />
    </>
  );
}
