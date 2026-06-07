"use client";

import type { ProfileHighlight, UserProfile } from "@/types/user";
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from "@/components/ui/avatar";
import { useState } from "react";
import { MoreHorizontal } from "lucide-react";
import { Button } from "@/components/ui/button";
import { ProfileSettingsModal } from "@/components/profile/profile-settings-modal";
import type { AuthUser } from "@/types/auth";
import { DEFAULT_AVATAR_IMAGE } from "@/lib/avatar";

type ProfileHeaderProps = {
  areActionsLoading?: boolean;
  currentUser?: AuthUser | null;
  highlights: ProfileHighlight[];
  isFollowing?: boolean;
  isLoading?: boolean;
  isOwnProfile?: boolean;
  onFollowToggle?: () => void;
  profile: UserProfile;
};

const statLabels: Record<keyof UserProfile["stats"], string> = {
  posts: "posts",
  cafes: "cafes",
  followers: "followers",
};

export function ProfileHeader({
  areActionsLoading = false,
  currentUser,
  highlights,
  isFollowing = false,
  isLoading = false,
  isOwnProfile = true,
  onFollowToggle,
  profile,
}: ProfileHeaderProps) {
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);

  return (
    <>
      <section className="w-full overflow-hidden border-border pb-6">
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
              ) : (
                <div className="flex flex-wrap gap-2">
                  <Button
                    className="h-8 px-5 text-sm font-black"
                    onClick={onFollowToggle}
                    type="button"
                    variant={isFollowing ? "secondary" : "default"}
                  >
                    {isFollowing ? "Following" : "Follow"}
                  </Button>
                  <Button
                    className="h-8 px-5 text-sm font-black"
                    type="button"
                    variant="secondary"
                  >
                    Message
                  </Button>
                </div>
              )}
            </div>

            <div className="flex flex-wrap gap-x-8 gap-y-2 text-sm">
              {Object.entries(profile.stats).map(([label, value]) => (
                <p key={label}>
                  <span className="text-[17px] font-semibold">{value}</span>{" "}
                  <span className="text-[16px] font-normal">
                    {statLabels[label as keyof UserProfile["stats"]]}
                  </span>
                </p>
              ))}
            </div>

            <div className="min-w-0 space-y-1 text-sm leading-6">
              <p className="font-black">{profile.displayName}</p>
              <p className="max-w-full break-words">{profile.bio}</p>
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
                  <AvatarImage
                    alt=""
                    src={highlight.image}
                  />
                  <AvatarFallback>{highlight.label.slice(0, 1)}</AvatarFallback>
                </Avatar>
              </span>
              <span className="block w-20 truncate text-center">{highlight.label}</span>
            </button>
          ))}
        </div>
      </section>

      <ProfileSettingsModal
        currentUser={currentUser}
        onOpenChange={setIsSettingsOpen}
        open={isSettingsOpen}
      />
    </>
  );
}
