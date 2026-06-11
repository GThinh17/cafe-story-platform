"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from "@/components/ui/avatar";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Skeleton } from "@/components/ui/skeleton";
import {
  getFollowersByUserId,
  getFollowingByUserId,
  getUserById,
} from "@/lib/api/users";
import { DEFAULT_AVATAR_IMAGE } from "@/lib/avatar";
import type { UserResponse } from "@/types/user";

type ProfileUserListType = "followers" | "following";

type ProfileUserListModalProps = {
  onOpenChange: (open: boolean) => void;
  open: boolean;
  profileUserId: string;
  type: ProfileUserListType;
};

const modalCopy: Record<
  ProfileUserListType,
  {
    description: string;
    empty: string;
    title: string;
  }
> = {
  followers: {
    description: "People following this profile.",
    empty: "No followers yet.",
    title: "Followers",
  },
  following: {
    description: "People this profile is following.",
    empty: "No following yet.",
    title: "Following",
  },
};

function getUserAvatar(user: UserResponse) {
  return (
    [
      user.userAvatar,
      user.avatar,
      user.profileImage,
      user.imageUrl,
    ]
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
    ? words
        .slice(0, 2)
        .map((word) => word[0])
        .join("")
        .toUpperCase()
    : "CS";
}

function getDisplayName(user: UserResponse) {
  return user.userFullName?.trim() || user.userName;
}

function uniqueValues(values: string[]) {
  return Array.from(new Set(values.filter(Boolean)));
}

export function ProfileUserListModal({
  onOpenChange,
  open,
  profileUserId,
  type,
}: ProfileUserListModalProps) {
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState("");
  const copy = modalCopy[type];

  useEffect(() => {
    let isActive = true;

    async function loadUsers() {
      if (!open || !profileUserId) {
        return;
      }

      setIsLoading(true);
      setErrorMessage(null);
      setUsers([]);
      setSearchQuery("");

      try {
        const follows =
          type === "followers"
            ? await getFollowersByUserId(profileUserId)
            : await getFollowingByUserId(profileUserId);
        const userIds = uniqueValues(
          follows.map((follow) =>
            type === "followers"
              ? follow.followerUserId
              : follow.followingUserId,
          ),
        );
        const userDetails = await Promise.all(
          userIds.map((userId) => getUserById(userId)),
        );

        if (isActive) {
          setUsers(userDetails);
        }
      } catch {
        if (isActive) {
          setErrorMessage("Unable to load users.");
          setUsers([]);
        }
      } finally {
        if (isActive) {
          setIsLoading(false);
        }
      }
    }

    void loadUsers();

    return () => {
      isActive = false;
    };
  }, [open, profileUserId, type]);

  const filteredUsers = useMemo(() => {
    const query = searchQuery.trim().toLowerCase();

    if (!query) {
      return users;
    }

    return users.filter((user) =>
      [user.userName, user.userFullName]
        .filter(Boolean)
        .some((value) => value?.toLowerCase().includes(query)),
    );
  }, [searchQuery, users]);

  const content = useMemo(() => {
    if (isLoading) {
      return (
        <div className="flex flex-col gap-3 p-4">
          {Array.from({ length: 5 }).map((_, index) => (
            <div className="flex items-center gap-3" key={index}>
              <Skeleton className="size-11 rounded-full" />
              <div className="flex min-w-0 flex-1 flex-col gap-2">
                <Skeleton className="h-4 w-32" />
                <Skeleton className="h-3 w-44" />
              </div>
            </div>
          ))}
        </div>
      );
    }

    if (errorMessage) {
      return (
        <div className="p-4">
          <Alert variant="destructive">
            <AlertDescription>{errorMessage}</AlertDescription>
          </Alert>
        </div>
      );
    }

    if (users.length === 0) {
      return (
        <p className="p-6 text-center text-sm font-semibold text-muted">
          {copy.empty}
        </p>
      );
    }

    if (filteredUsers.length === 0) {
      return (
        <p className="p-6 text-center text-sm font-semibold text-muted">
          No users found.
        </p>
      );
    }

    return (
      <ScrollArea className="h-full">
        <div className="flex flex-col p-2">
          {filteredUsers.map((user) => {
            const displayName = getDisplayName(user);
            const avatarImage = getUserAvatar(user);

            return (
              <Link
                className="flex min-w-0 items-center gap-3 rounded-md px-3 py-2.5 text-foreground"
                href={`/${encodeURIComponent(user.userName)}`}
                key={user.userId}
                onClick={() => onOpenChange(false)}
              >
                <Avatar className="size-11">
                  <AvatarImage
                    alt={`${displayName} avatar`}
                    src={avatarImage}
                  />
                  <AvatarFallback>{getInitials(displayName)}</AvatarFallback>
                </Avatar>
                <span className="min-w-0 flex-1">
                  <span className="block truncate text-sm font-black">
                    {user.userName}
                  </span>
                  {user.userFullName?.trim() ? (
                    <span className="block truncate text-sm text-muted">
                      {user.userFullName}
                    </span>
                  ) : null}
                </span>
              </Link>
            );
          })}
        </div>
      </ScrollArea>
    );
  }, [copy.empty, errorMessage, filteredUsers, isLoading, onOpenChange, users]);

  return (
    <Dialog onOpenChange={onOpenChange} open={open}>
      <DialogContent className="grid h-[min(560px,calc(100vh-48px))] w-[min(420px,calc(100vw-32px))] grid-rows-[auto_auto_minmax(0,1fr)] p-0">
        <div className="border-b border-border px-5 py-4">
          <DialogTitle className="text-lg font-black text-foreground">
            {copy.title}
          </DialogTitle>
          <DialogDescription className="sr-only">
            {copy.description}
          </DialogDescription>
        </div>

        <div className="px-4 pt-3">
          <Input
            aria-label={`Search ${copy.title.toLowerCase()}`}
            className="h-9 focus:border-line-soft focus:ring-0 focus-visible:border-line-soft focus-visible:ring-0"
            onChange={(event) => setSearchQuery(event.target.value)}
            placeholder="Search"
            value={searchQuery}
          />
        </div>
        <div className="min-h-0">{content}</div>
      </DialogContent>
    </Dialog>
  );
}
