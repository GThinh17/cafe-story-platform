"use client";

import { useEffect, useMemo, useState } from "react";
import { toast } from "sonner";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Skeleton } from "@/components/ui/skeleton";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { addPageMember } from "@/lib/api/cafes";
import { ApiError } from "@/lib/api/client";
import {
  getFollowersByUserId,
  getFollowingByUserId,
  getUserById,
} from "@/lib/api/users";
import { DEFAULT_AVATAR_IMAGE } from "@/lib/avatar";
import type { UserResponse } from "@/types/user";

type AddCoOwnerDialogProps = {
  cafePageId: string;
  currentUserId: string;
  excludedUserIds: Set<string>;
  onAdded: () => void;
  onOpenChange: (open: boolean) => void;
  open: boolean;
};

function getUserAvatar(user: UserResponse) {
  return (
    [user.userAvatar, user.avatar, user.profileImage, user.imageUrl]
      .map((value) => value?.trim())
      .find(
        (value) =>
          value &&
          value.toLowerCase() !== "null" &&
          value.toLowerCase() !== "undefined",
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

function uniqueIds(ids: string[]) {
  return Array.from(new Set(ids.filter(Boolean)));
}

async function loadFollowedUsers(
  userId: string,
  direction: "following" | "followers",
) {
  const follows =
    direction === "following"
      ? await getFollowingByUserId(userId)
      : await getFollowersByUserId(userId);
  const ids = uniqueIds(
    follows.map((f) =>
      direction === "following" ? f.followingUserId : f.followerUserId,
    ),
  );
  return Promise.all(ids.map((id) => getUserById(id)));
}

type UserListPanelProps = {
  disabled: boolean;
  emptyLabel: string;
  errorMessage: string | null;
  isLoading: boolean;
  onAdd: (user: UserResponse) => void;
  pendingUserId: string | null;
  searchQuery: string;
  users: UserResponse[];
};

function UserListPanel({
  disabled,
  emptyLabel,
  errorMessage,
  isLoading,
  onAdd,
  pendingUserId,
  searchQuery,
  users,
}: UserListPanelProps) {
  const filtered = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    if (!q) return users;
    return users.filter((u) =>
      [u.userName, u.userFullName]
        .filter(Boolean)
        .some((v) => v?.toLowerCase().includes(q)),
    );
  }, [searchQuery, users]);

  if (isLoading) {
    return (
      <div className="flex flex-col gap-3 p-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <div className="flex items-center gap-3" key={i}>
            <Skeleton className="size-10 rounded-full" />
            <div className="flex flex-1 flex-col gap-2">
              <Skeleton className="h-4 w-32" />
              <Skeleton className="h-3 w-24" />
            </div>
            <Skeleton className="h-8 w-16 rounded-md" />
          </div>
        ))}
      </div>
    );
  }

  if (errorMessage) {
    return <p className="p-6 text-center text-sm text-accent">{errorMessage}</p>;
  }

  if (users.length === 0) {
    return <p className="p-6 text-center text-sm font-semibold text-muted">{emptyLabel}</p>;
  }

  if (filtered.length === 0) {
    return (
      <p className="p-6 text-center text-sm font-semibold text-muted">
        No matching users.
      </p>
    );
  }

  return (
    <ScrollArea className="h-full">
      <ul className="flex flex-col gap-1 p-2">
        {filtered.map((user) => {
          const displayName = getDisplayName(user);
          const isPending = pendingUserId === user.userId;
          return (
            <li
              className="flex items-center gap-3 rounded-md px-3 py-2 hover:bg-surface-muted"
              key={user.userId}
            >
              <Avatar className="size-10">
                <AvatarImage alt={`${displayName} avatar`} src={getUserAvatar(user)} />
                <AvatarFallback>{getInitials(displayName)}</AvatarFallback>
              </Avatar>
              <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-semibold text-foreground">{displayName}</p>
                <p className="truncate text-xs text-muted">@{user.userName}</p>
              </div>
              <Button
                disabled={disabled || isPending}
                onClick={() => onAdd(user)}
                size="sm"
                type="button"
                variant="outline"
              >
                {isPending ? "Adding..." : "Add"}
              </Button>
            </li>
          );
        })}
      </ul>
    </ScrollArea>
  );
}

export function AddCoOwnerDialog({
  cafePageId,
  currentUserId,
  excludedUserIds,
  onAdded,
  onOpenChange,
  open,
}: AddCoOwnerDialogProps) {
  const [following, setFollowing] = useState<UserResponse[]>([]);
  const [followers, setFollowers] = useState<UserResponse[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [pendingUserId, setPendingUserId] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<"following" | "followers">("following");

  useEffect(() => {
    let cancelled = false;
    if (!open || !currentUserId) return;

    setSearchQuery("");
    setActiveTab("following");
    setIsLoading(true);
    setLoadError(null);

    (async () => {
      try {
        const [followingList, followersList] = await Promise.all([
          loadFollowedUsers(currentUserId, "following"),
          loadFollowedUsers(currentUserId, "followers"),
        ]);
        if (cancelled) return;
        setFollowing(followingList);
        setFollowers(followersList);
      } catch {
        if (!cancelled) setLoadError("Không tải được danh sách người dùng.");
      } finally {
        if (!cancelled) setIsLoading(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [currentUserId, open]);

  const filterPool = (pool: UserResponse[]) =>
    pool.filter((u) => u.userId !== currentUserId && !excludedUserIds.has(u.userId));

  const followingPool = useMemo(() => filterPool(following), [following, currentUserId, excludedUserIds]);
  const followersPool = useMemo(() => filterPool(followers), [followers, currentUserId, excludedUserIds]);

  async function handleAdd(user: UserResponse) {
    setPendingUserId(user.userId);
    try {
      await addPageMember(cafePageId, { userId: user.userId, roleName: "CO_OWNER" });
      toast.success(`Added ${getDisplayName(user)} as co-owner.`);
      onAdded();
      onOpenChange(false);
    } catch (err) {
      const msg = err instanceof ApiError || err instanceof Error ? err.message : "Unable to add co-owner.";
      toast.error(msg);
    } finally {
      setPendingUserId(null);
    }
  }

  return (
    <Dialog onOpenChange={onOpenChange} open={open}>
      <DialogContent className="flex h-[min(560px,calc(100vh-48px))] w-[min(460px,calc(100vw-32px))] flex-col p-0">
        <div className="border-b border-border px-5 py-4">
          <DialogTitle className="text-lg font-black text-foreground">Add co-owner</DialogTitle>
          <DialogDescription className="mt-1 text-sm text-muted">
            Choose someone you follow or who follows you.
          </DialogDescription>
        </div>

        <div className="px-4 pt-3">
          <Input
            aria-label="Search users"
            className="h-9"
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search by name or username"
            value={searchQuery}
          />
        </div>

        <Tabs
          className="flex min-h-0 flex-1 flex-col px-4 pb-4"
          onValueChange={(value) => setActiveTab(value as "following" | "followers")}
          value={activeTab}
        >
          <TabsList className="w-full" variant="line">
            <TabsTrigger value="following">Following</TabsTrigger>
            <TabsTrigger value="followers">Followers</TabsTrigger>
          </TabsList>

          <TabsContent className="mt-0 min-h-0 flex-1" value="following">
            <UserListPanel
              disabled={pendingUserId !== null}
              emptyLabel="You are not following anyone yet."
              errorMessage={loadError}
              isLoading={isLoading}
              onAdd={handleAdd}
              pendingUserId={pendingUserId}
              searchQuery={searchQuery}
              users={followingPool}
            />
          </TabsContent>

          <TabsContent className="mt-0 min-h-0 flex-1" value="followers">
            <UserListPanel
              disabled={pendingUserId !== null}
              emptyLabel="No followers yet."
              errorMessage={loadError}
              isLoading={isLoading}
              onAdd={handleAdd}
              pendingUserId={pendingUserId}
              searchQuery={searchQuery}
              users={followersPool}
            />
          </TabsContent>
        </Tabs>
      </DialogContent>
    </Dialog>
  );
}
