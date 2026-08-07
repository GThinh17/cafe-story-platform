"use client";

import { useEffect, useRef, useState } from "react";
import {
  getCafePageById,
  getFollowedCafePagesByUserId,
} from "@/lib/api/cafes";
import { getFollowingByUserId, getUserById } from "@/lib/api/users";
import {
  ensureUniqueSlug,
  slugifyForMention,
} from "@/lib/mention/parse-mentions";
import type { CafePageResponse } from "@/types/cafe";
import type { UserResponse } from "@/types/user";
import { useI18n } from "@/components/providers/locale-provider";

export type MentionItem = {
  kind: "user" | "page";
  id: string;
  slug: string;
  displayName: string;
  subtitle: string | null;
  avatarUrl: string | null;
};

type State = {
  items: MentionItem[];
  isLoading: boolean;
  error: string | null;
};

function pickUserAvatar(user: UserResponse): string | null {
  return user.userAvatar ?? user.avatar ?? user.profileImage ?? user.imageUrl ?? null;
}

function mapUser(user: UserResponse): MentionItem {
  return {
    kind: "user",
    id: user.userId,
    slug: user.userName,
    displayName: user.userFullName ?? user.userName,
    subtitle: `@${user.userName}`,
    avatarUrl: pickUserAvatar(user),
  };
}

function mapPage(
  page: CafePageResponse,
  taken: Set<string>,
  cafePageLabel: string,
): MentionItem {
  const base = slugifyForMention(page.name);
  const slug = ensureUniqueSlug(base, taken, page.id);
  taken.add(slug);
  return {
    kind: "page",
    id: page.id,
    slug,
    displayName: page.name,
    subtitle: cafePageLabel,
    avatarUrl: page.avatarUrl,
  };
}

export function useFollowings(userId: string | undefined, enabled: boolean) {
  const { t } = useI18n();
  const [state, setState] = useState<State>({
    items: [],
    isLoading: false,
    error: null,
  });
  const fetchedRef = useRef(false);

  useEffect(() => {
    if (!enabled || !userId || fetchedRef.current) return;
    fetchedRef.current = true;
    let active = true;

    setState((s) => ({ ...s, isLoading: true, error: null }));

    Promise.all([
      getFollowingByUserId(userId).catch(() => []),
      getFollowedCafePagesByUserId(userId).catch(() => []),
    ])
      .then(async ([userFollows, pageFollows]) => {
        const [users, pages] = await Promise.all([
          Promise.all(
            userFollows.map((f) =>
              getUserById(f.followingUserId).catch(() => null),
            ),
          ),
          Promise.all(
            pageFollows.map((f) =>
              getCafePageById(f.cafePageId).catch(() => null),
            ),
          ),
        ]);

        if (!active) return;

        const userItems = users
          .filter((u): u is UserResponse => u !== null)
          .map(mapUser);
        const taken = new Set(userItems.map((u) => u.slug));
        const pageItems = pages
          .filter((p): p is CafePageResponse => p !== null)
          .map((p) => mapPage(p, taken, t("followings.cafePage")));

        setState({
          items: [...userItems, ...pageItems],
          isLoading: false,
          error: null,
        });
      })
      .catch((err: unknown) => {
        if (!active) return;
        setState({
          items: [],
          isLoading: false,
          error: err instanceof Error ? err.message : t("followings.loadError"),
        });
      });

    return () => {
      active = false;
    };
  }, [enabled, userId]);

  return state;
}
