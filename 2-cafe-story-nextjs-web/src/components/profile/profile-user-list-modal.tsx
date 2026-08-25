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
  getFollowingTargetsByUserId,
  getUserById,
} from "@/lib/api/users";
import { DEFAULT_AVATAR_IMAGE } from "@/lib/avatar";
import type { FollowTargetResponse, UserResponse } from "@/types/user";
import { useI18n } from "@/components/providers/locale-provider";
import type { TranslationKey } from "@/lib/i18n";

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
    descriptionKey: TranslationKey;
    emptyKey: TranslationKey;
    titleKey: TranslationKey;
  }
> = {
  followers: {
    descriptionKey: "profile.followers.description",
    emptyKey: "profile.followers.empty",
    titleKey: "profile.followers.title",
  },
  following: {
    descriptionKey: "profile.following.description",
    emptyKey: "profile.following.empty",
    titleKey: "profile.following.title",
  },
};

/** Only the fields this list renders, so both sources can feed it. */
type ProfileUserListItem = {
  userId: string;
  userName: string;
  userFullName: string | null;
  avatarUrl: string;
};

function pickAvatar(values: Array<string | null | undefined>) {
  return (
    values
      .map((value) => value?.trim())
      .find(
        (value) =>
          value &&
          value.toLowerCase() !== "null" &&
          value.toLowerCase() !== "undefined",
      ) ?? DEFAULT_AVATAR_IMAGE
  );
}

function userResponseToItem(user: UserResponse): ProfileUserListItem {
  return {
    userId: user.userId,
    userName: user.userName,
    userFullName: user.userFullName,
    avatarUrl: pickAvatar([
      user.userAvatar,
      user.avatar,
      user.profileImage,
      user.imageUrl,
    ]),
  };
}

function followTargetToItem(
  target: FollowTargetResponse,
): ProfileUserListItem | null {
  const userId = target.userId ?? target.targetId;
  const userName = target.username?.trim();
  if (!userId || !userName) {
    return null;
  }

  return {
    userId,
    userName,
    userFullName: target.userFullName,
    avatarUrl: pickAvatar([target.avatar]),
  };
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

function getDisplayName(user: ProfileUserListItem) {
  return user.userFullName?.trim() || user.userName;
}

function uniqueValues(values: string[]) {
  return Array.from(new Set(values.filter(Boolean)));
}

// There is no endpoint that takes a list of user ids, so the followers tab still
// has to fetch one by one. Run them in waves instead of firing every id at once:
// a profile with thousands of followers used to open thousands of parallel
// requests. Failures resolve to null rather than blanking the whole list.
const USER_FETCH_BATCH_SIZE = 10;

async function fetchUsersInBatches(
  userIds: string[],
  isCancelled: () => boolean,
): Promise<ProfileUserListItem[]> {
  const items: ProfileUserListItem[] = [];

  for (let index = 0; index < userIds.length; index += USER_FETCH_BATCH_SIZE) {
    if (isCancelled()) break;

    const batch = userIds.slice(index, index + USER_FETCH_BATCH_SIZE);
    const settled = await Promise.all(
      batch.map((userId) => getUserById(userId).catch(() => null)),
    );

    for (const user of settled) {
      if (user) items.push(userResponseToItem(user));
    }
  }

  return items;
}

export function ProfileUserListModal({
  onOpenChange,
  open,
  profileUserId,
  type,
}: ProfileUserListModalProps) {
  const { t } = useI18n();
  const [users, setUsers] = useState<ProfileUserListItem[]>([]);
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
        if (type === "following") {
          // Follow targets already carry username, full name and avatar, so the
          // whole list is one request.
          const targets = await getFollowingTargetsByUserId(
            profileUserId,
            "USER",
          );
          if (isActive) {
            setUsers(
              targets
                .map(followTargetToItem)
                .filter((item): item is ProfileUserListItem => item !== null),
            );
          }
          return;
        }

        const follows = await getFollowersByUserId(profileUserId);
        const userIds = uniqueValues(
          follows.map((follow) => follow.followerUserId),
        );
        const userDetails = await fetchUsersInBatches(
          userIds,
          () => !isActive,
        );

        if (isActive) {
          setUsers(userDetails);
        }
      } catch {
        if (isActive) {
          setErrorMessage(t("profile.userList.loadError"));
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
          {t(copy.emptyKey)}
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
            const avatarImage = user.avatarUrl;

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
  }, [copy.emptyKey, errorMessage, filteredUsers, isLoading, onOpenChange, t, users]);

  return (
    <Dialog onOpenChange={onOpenChange} open={open}>
      <DialogContent className="grid h-[min(560px,calc(100vh-48px))] w-[min(420px,calc(100vw-32px))] grid-rows-[auto_auto_minmax(0,1fr)] p-0">
        <div className="border-b border-border px-5 py-4">
          <DialogTitle className="text-lg font-black text-foreground">
            {t(copy.titleKey)}
          </DialogTitle>
          <DialogDescription className="sr-only">
            {t(copy.descriptionKey)}
          </DialogDescription>
        </div>

        <div className="px-4 pt-3">
          <Input
            aria-label={t("profile.userList.searchAria", {
              list: t(copy.titleKey).toLowerCase(),
            })}
            className="h-9 focus:border-line-soft focus:ring-0 focus-visible:border-line-soft focus-visible:ring-0"
            onChange={(event) => setSearchQuery(event.target.value)}
            placeholder={t("common.search")}
            value={searchQuery}
          />
        </div>
        <div className="min-h-0">{content}</div>
      </DialogContent>
    </Dialog>
  );
}
