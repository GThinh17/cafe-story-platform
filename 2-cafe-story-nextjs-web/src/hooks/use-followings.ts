"use client";

import { useEffect, useRef, useState } from "react";
import { getFollowingTargetsByUserId } from "@/lib/api/users";
import {
  ensureUniqueSlug,
  slugifyForMention,
} from "@/lib/mention/parse-mentions";
import type { FollowTargetResponse } from "@/types/user";
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

function mapUserTarget(target: FollowTargetResponse): MentionItem | null {
  const userId = target.userId ?? target.targetId;
  const username = target.username?.trim();
  if (!userId || !username) {
    return null;
  }

  return {
    kind: "user",
    id: userId,
    slug: username,
    displayName: target.userFullName?.trim() || username,
    subtitle: `@${username}`,
    avatarUrl: target.avatar,
  };
}

function mapPageTarget(
  target: FollowTargetResponse,
  taken: Set<string>,
  cafePageLabel: string,
): MentionItem | null {
  const cafePageId = target.cafePageId ?? target.targetId;
  const name = target.pageName?.trim() || target.displayName?.trim();
  if (!cafePageId || !name) {
    return null;
  }

  const slug = ensureUniqueSlug(slugifyForMention(name), taken, cafePageId);
  taken.add(slug);
  return {
    kind: "page",
    id: cafePageId,
    slug,
    displayName: name,
    subtitle: cafePageLabel,
    avatarUrl: target.avatar,
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

    // One request instead of 2 + N: the follow-targets endpoint already returns
    // the avatar, username, full name and page name that a mention row needs, so
    // there is nothing left to look up per followed account. `app/(home)/page.tsx`
    // builds the story rail off the same endpoint.
    getFollowingTargetsByUserId(userId, "ALL")
      .then((targets) => {
        if (!active) return;

        const userItems = targets
          .filter((target) => target.targetType !== "CAFE_PAGE")
          .map(mapUserTarget)
          .filter((item): item is MentionItem => item !== null);

        const taken = new Set(userItems.map((item) => item.slug));
        const pageItems = targets
          .filter((target) => target.targetType === "CAFE_PAGE")
          .map((target) => mapPageTarget(target, taken, t("followings.cafePage")))
          .filter((item): item is MentionItem => item !== null);

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
