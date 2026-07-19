"use client";

import { useMemo, useState } from "react";
import { UserPlus, X } from "lucide-react";
import { toast } from "sonner";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { AddCoOwnerDialog } from "@/components/cafe/add-co-owner-dialog";
import { useCafePageMembers } from "@/hooks/use-cafe-page-members";
import { updatePageMemberStatus } from "@/lib/api/cafes";
import { ApiError } from "@/lib/api/client";
import { DEFAULT_AVATAR_IMAGE } from "@/lib/avatar";
import { cn } from "@/lib/utils";
import type { UserResponse } from "@/types/user";

type CoOwnerSectionProps = {
  cafePageId: string;
  currentUserId: string;
  maxMembers: number | null;
};

const DEFAULT_MAX_MEMBERS = 2;

function getUserAvatar(user: UserResponse) {
  return (
    [user.userAvatar, user.avatar, user.profileImage, user.imageUrl]
      .map((v) => v?.trim())
      .find(
        (v) =>
          v && v.toLowerCase() !== "null" && v.toLowerCase() !== "undefined",
      ) ?? DEFAULT_AVATAR_IMAGE
  );
}

function getInitials(source: string) {
  const words = source.split(/\s+/).filter(Boolean);
  return words.length > 0
    ? words.slice(0, 2).map((w) => w[0]).join("").toUpperCase()
    : "CS";
}

function getDisplayName(user: UserResponse) {
  return user.userFullName?.trim() || user.userName;
}

export function CoOwnerSection({ cafePageId, currentUserId, maxMembers }: CoOwnerSectionProps) {
  const { coOwners, isLoading, error, refresh } = useCafePageMembers(cafePageId);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [confirmingUserId, setConfirmingUserId] = useState<string | null>(null);
  const [removingUserId, setRemovingUserId] = useState<string | null>(null);

  const totalCap = maxMembers ?? DEFAULT_MAX_MEMBERS;
  const coOwnerCap = Math.max(totalCap - 1, 0);
  const isFull = coOwners.length >= coOwnerCap;

  const excludedUserIds = useMemo(
    () => new Set(coOwners.map((c) => c.user.userId)),
    [coOwners],
  );

  async function handleRemove(userId: string, displayName: string) {
    setRemovingUserId(userId);
    try {
      await updatePageMemberStatus(cafePageId, userId, "REJECTED");
      toast.success(`Removed ${displayName} from co-owners.`);
      setConfirmingUserId(null);
      await refresh();
    } catch (err) {
      const msg = err instanceof ApiError || err instanceof Error ? err.message : "Unable to remove co-owner.";
      toast.error(msg);
    } finally {
      setRemovingUserId(null);
    }
  }

  return (
    <>
      <div className="overflow-hidden rounded-md border border-border bg-surface">
        <div className="flex items-start justify-between gap-3 border-b border-border p-4 sm:p-5">
          <div className="min-w-0">
            <p className="text-xs font-semibold uppercase tracking-wider text-muted">Co-owners</p>
            <p className="mt-1 text-sm text-muted">
              People you trust to help manage this cafe page.
            </p>
          </div>
          <span className="shrink-0 rounded-md bg-surface-muted px-2 py-1 text-xs font-semibold text-primary-strong">
            {coOwners.length}/{coOwnerCap}
          </span>
        </div>

        <div className="space-y-4 p-4 sm:p-5">
          {error ? (
            <p className="rounded-md border border-accent/40 bg-accent/5 px-3 py-2 text-sm text-accent">
              {error}
            </p>
          ) : null}

          {isLoading ? (
            <p className="text-sm text-muted">Loading...</p>
          ) : coOwners.length === 0 ? (
            <p className="text-sm text-muted">
              No co-owners yet. Add someone from your followers or following to help manage this page.
            </p>
          ) : (
            <ul className="flex flex-col divide-y divide-border">
              {coOwners.map(({ user }) => {
                const displayName = getDisplayName(user);
                const isConfirming = confirmingUserId === user.userId;
                const isRemoving = removingUserId === user.userId;
                return (
                  <li className="flex items-center gap-3 py-3" key={user.userId}>
                    <Avatar className="size-10">
                      <AvatarImage alt={`${displayName} avatar`} src={getUserAvatar(user)} />
                      <AvatarFallback>{getInitials(displayName)}</AvatarFallback>
                    </Avatar>
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-semibold text-foreground">{displayName}</p>
                      <p className="truncate text-xs text-muted">@{user.userName}</p>
                    </div>
                    {isConfirming ? (
                      <div className="flex items-center gap-2">
                        <span className="text-xs text-muted">Remove?</span>
                        <Button
                          disabled={isRemoving}
                          onClick={() => void handleRemove(user.userId, displayName)}
                          size="sm"
                          type="button"
                          variant="destructive"
                        >
                          {isRemoving ? "Removing..." : "Confirm"}
                        </Button>
                        <Button
                          disabled={isRemoving}
                          onClick={() => setConfirmingUserId(null)}
                          size="sm"
                          type="button"
                          variant="ghost"
                        >
                          Cancel
                        </Button>
                      </div>
                    ) : (
                      <Button
                        aria-label={`Remove ${displayName}`}
                        className={cn("shrink-0")}
                        onClick={() => setConfirmingUserId(user.userId)}
                        size="sm"
                        type="button"
                        variant="ghost"
                      >
                        <X className="size-4" />
                      </Button>
                    )}
                  </li>
                );
              })}
            </ul>
          )}

          <Button
            disabled={isFull || isLoading}
            onClick={() => setIsDialogOpen(true)}
            type="button"
            variant="outline"
          >
            <UserPlus data-icon="inline-start" />
            {isFull ? "Co-owner limit reached" : "Add co-owner"}
          </Button>
        </div>
      </div>

      <AddCoOwnerDialog
        cafePageId={cafePageId}
        currentUserId={currentUserId}
        excludedUserIds={excludedUserIds}
        onAdded={() => void refresh()}
        onOpenChange={setIsDialogOpen}
        open={isDialogOpen}
      />
    </>
  );
}
