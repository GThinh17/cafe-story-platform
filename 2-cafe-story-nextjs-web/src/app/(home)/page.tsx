import { cache, Suspense } from "react";
import { cookies } from "next/headers";
import { FeedColumnSkeleton } from "@/components/feed/cafe-feed-skeleton";
import { HomeAccountPanel } from "@/components/feed/home-account-panel";
import { FeedPostList } from "@/components/feed/feed-post-list";
import { StoryRail } from "@/components/feed/story-rail";
import { TopCafesNearby } from "@/components/feed/top-cafes-nearby";
import { MessageDock } from "@/components/message/message-dock";
import { mapMixedFeedToRenderableItems } from "@/features/blogs/blog-feed-adapter";
import { ApiError } from "@/lib/api/client";
import { getMe } from "@/lib/api/auth";
import { getMixedFeed } from "@/lib/api/feed";
import { getTopCafePages } from "@/lib/api/cafes";
import { getConversations } from "@/lib/api/chat";
import { getFollowingTargetsByUserId, getUserById } from "@/lib/api/users";
import { DEFAULT_AVATAR_IMAGE } from "@/lib/avatar";
import { imageWidths, optimizeImageUrl } from "@/lib/image-optimizer";
import { getServerTranslator } from "@/lib/i18n/server";
import { ACCESS_TOKEN_COOKIE } from "@/lib/routes";
import type { Translate } from "@/lib/i18n";
import type { CafePageRankingResponse } from "@/types/cafe";
import type { FeedRenderableItem, StoryItem, TopCafe } from "@/types/feed";
import type { FollowTargetResponse } from "@/types/user";
import type { MessageContact, MessageDockData } from "@/types/message";

export const dynamic = "force-dynamic";

/**
 * Số item mỗi lần nạp feed, dùng cho cả lần render đầu trên server lẫn các lần
 * cuộn tiếp.
 *
 * <p>Backend chèn quảng cáo theo kích thước trang
 * ({@code FeedServiceImpl.maxAdsForSize}) — từ 12 trở lên 2 slot, từ 6 được 1
 * slot, dưới 6 thì KHÔNG có slot nào. Ở mốc 12 này mỗi lượt nạp gồm 10 bài
 * organic + 2 quán tài trợ.
 */
const FEED_PAGE_SIZE = 12;

type HomeFeedState = {
  errorMessage?: string;
  hasMore: boolean;
  nextCursor: string | null;
  items: FeedRenderableItem[];
};

const getMeCached = cache((cookieHeader: string) =>
  getMe({ headers: { Cookie: cookieHeader } }).catch(() => null),
);

type ViewerRegion = {
  area: string | null;
  city: string | null;
  province: string | null;
};

/**
 * `/api/auth/me` only carries identity fields — the region lives on the user
 * profile, so it has to be read separately or every viewer looks region-less.
 */
const getViewerRegionCached = cache(
  async (cookieHeader: string): Promise<ViewerRegion | null> => {
    const me = await getMeCached(cookieHeader);
    const userId = me?.user?.userId;
    if (!userId) return null;

    try {
      const profile = await getUserById(userId, {
        headers: { Cookie: cookieHeader },
      });
      return {
        area: profile.regionArea?.trim() || null,
        city: profile.regionCity?.trim() || null,
        province: profile.regionProvince?.trim() || null,
      };
    } catch {
      return null;
    }
  },
);

/**
 * Narrowest region the viewer belongs to first, widening only when a level has
 * no cafe pages. Mirrors the cascade in `explore-content.tsx` so "near you"
 * means the same thing on both screens. Without this the endpoint ranks
 * nationally and a Da Nang page can surface for a Can Tho viewer.
 */
function buildRegionCascade(region: ViewerRegion | null) {
  const params: Array<{ area?: string; city?: string; province?: string }> = [];
  if (region?.area) params.push({ area: region.area });
  if (region?.city) params.push({ city: region.city });
  if (region?.province) params.push({ province: region.province });
  return params;
}

async function loadTopCafes(
  t: Translate,
  cookieHeader: string,
  region: ViewerRegion | null,
): Promise<TopCafe[]> {
  const toTopCafe = (cafe: CafePageRankingResponse): TopCafe => ({
    id: cafe.id,
    name: cafe.name,
    avatarUrl: optimizeImageUrl(cafe.avatarUrl, {
      width: imageWidths.cafeAvatar,
    }),
    type: cafe.regionCity ?? t("home.cafePageLabel"),
  });

  const cascade = buildRegionCascade(region);

  for (const regionParam of cascade) {
    try {
      const cafes = await getTopCafePages(
        { ...regionParam, size: 5 },
        { headers: { Cookie: cookieHeader } },
      );
      if (cafes.length > 0) return cafes.map(toTopCafe);
    } catch {
      /* try the next, wider region */
    }
  }

  // A viewer with a region set must never be shown another province: the
  // national ranking is only a fallback for viewers whose region is unknown.
  if (cascade.length > 0) {
    return [];
  }

  try {
    const cafes = await getTopCafePages(
      { size: 5 },
      { headers: { Cookie: cookieHeader } },
    );
    return cafes.map(toTopCafe);
  } catch {
    return [];
  }
}

function mapFollowTargetToStoryItem(
  target: FollowTargetResponse,
  myId: string,
): StoryItem | null {
  if (target.targetType === "CAFE_PAGE") {
    const cafePageId = target.cafePageId ?? target.targetId;
    if (!cafePageId) {
      return null;
    }

    return {
      id: `cafe-${cafePageId}`,
      kind: "cafe-page",
      label: target.pageName?.trim() || target.displayName?.trim() || "Cafe page",
      avatarUrl:
        optimizeImageUrl(target.avatar, { width: imageWidths.story }) ||
        DEFAULT_AVATAR_IMAGE,
      href: `/cafes/${cafePageId}`,
    };
  }

  const userId = target.userId ?? target.targetId;
  const username = target.username?.trim();
  if (!userId || !username || userId === myId) {
    return null;
  }

  return {
    id: `user-${userId}`,
    kind: "user",
    label: username,
    avatarUrl:
      optimizeImageUrl(target.avatar, { width: imageWidths.story }) ||
      DEFAULT_AVATAR_IMAGE,
    href: `/${username}`,
  };
}

async function loadStoryRail(cookieHeader: string): Promise<StoryItem[]> {
  try {
    const me = await getMeCached(cookieHeader);
    const myId = me?.user?.userId;
    if (!myId) {
      return [];
    }

    const targets = await getFollowingTargetsByUserId(myId, "ALL", {
      headers: { Cookie: cookieHeader },
    });
    const items: StoryItem[] = [];
    const seenIds = new Set<string>();

    for (const target of targets) {
      const item = mapFollowTargetToStoryItem(target, myId);
      if (item && !seenIds.has(item.id)) {
        seenIds.add(item.id);
        items.push(item);
      }
    }

    return items;
  } catch {
    return [];
  }
}

function getContactInitials(source: string) {
  const words = source.trim().split(/\s+/).filter(Boolean);
  if (words.length === 0) return "CS";
  return words
    .slice(0, 2)
    .map((word) => word[0])
    .join("")
    .toUpperCase();
}

async function loadMessageDock(
  cookieHeader: string,
  myId: string,
  t: Translate,
): Promise<MessageDockData> {
  const base: MessageDockData = {
    title: t("nav.messages"),
    unreadCount: 0,
    contacts: [],
  };

  try {
    const conversations = await getConversations({
      headers: { Cookie: cookieHeader },
    });

    const unreadCount = conversations.reduce(
      (total, conv) => total + (conv.unreadCount ?? 0),
      0,
    );

    const sorted = [...conversations].sort((a, b) => {
      const aTime = a.lastMessageAt ? Date.parse(a.lastMessageAt) : 0;
      const bTime = b.lastMessageAt ? Date.parse(b.lastMessageAt) : 0;
      return bTime - aTime;
    });

    const contacts: MessageContact[] = [];
    const seen = new Set<string>();

    for (const conv of sorted) {
      if (contacts.length >= 3) break;
      const other = conv.members?.find((m) => m.userId !== myId);
      if (!other || seen.has(other.userId)) continue;
      seen.add(other.userId);

      const name =
        other.userFullName?.trim() ||
        other.userName?.trim() ||
        conv.chatName?.trim() ||
        t("home.defaultUserName");

      contacts.push({
        id: other.userId,
        name,
        initials: getContactInitials(name),
        avatarImage:
          other.userAvatar?.trim() ||
          conv.chatAvatar?.trim() ||
          undefined,
      });
    }

    return {
      ...base,
      unreadCount,
      contacts,
    };
  } catch {
    return base;
  }
}

async function loadHomeFeed(
  cookieHeader: string,
  t: Translate,
): Promise<HomeFeedState> {
  try {
    const feed = await getMixedFeed(
      {
        size: FEED_PAGE_SIZE,
      },
      {
        headers: {
          Cookie: cookieHeader,
        },
      },
    );

    return {
      hasMore: Boolean(feed.hasMore && feed.nextCursor),
      nextCursor: feed.nextCursor,
      items: mapMixedFeedToRenderableItems(feed, t),
    };
  } catch (error) {
    return {
      errorMessage:
        error instanceof ApiError
          ? error.message
          : t("home.feedLoadError"),
      hasMore: false,
      nextCursor: null,
      items: [],
    };
  }
}

async function FeedSection({
  cookieHeader,
  hasSession,
}: {
  cookieHeader: string;
  hasSession: boolean;
}) {
  const t = await getServerTranslator();

  if (!hasSession) {
    return (
      <FeedPostList
        errorMessage={t("home.signInPrompt")}
        initialHasMore={false}
        initialNextCursor={null}
        initialPage={0}
        pageSize={FEED_PAGE_SIZE}
        items={[]}
      />
    );
  }

  const [feedState, storyItems] = await Promise.all([
    loadHomeFeed(cookieHeader, t),
    loadStoryRail(cookieHeader),
  ]);

  return (
    <>
      <StoryRail stories={storyItems} />
      <FeedPostList
        errorMessage={feedState.errorMessage}
        initialHasMore={feedState.hasMore}
        initialNextCursor={feedState.nextCursor}
        initialPage={0}
        pageSize={FEED_PAGE_SIZE}
        items={feedState.items}
      />
    </>
  );
}

async function TopCafesSection({
  cookieHeader,
  hasSession,
}: {
  cookieHeader: string;
  hasSession: boolean;
}) {
  const t = await getServerTranslator();
  const region = hasSession ? await getViewerRegionCached(cookieHeader) : null;
  const topCafes = await loadTopCafes(t, cookieHeader, region);

  if (topCafes.length === 0) {
    return null;
  }

  return <TopCafesNearby cafes={topCafes} />;
}

async function MessageDockSection({
  cookieHeader,
  hasSession,
}: {
  cookieHeader: string;
  hasSession: boolean;
}) {
  const me = hasSession ? await getMeCached(cookieHeader) : null;
  const myId = me?.user?.userId;

  if (!myId) {
    return null;
  }

  const t = await getServerTranslator();
  const messageDock = await loadMessageDock(cookieHeader, myId, t);

  return <MessageDock data={messageDock} />;
}

export default async function Home() {
  const t = await getServerTranslator();
  const cookieStore = await cookies();
  const cookieHeader = cookieStore.toString();
  const hasSession = cookieStore.has(ACCESS_TOKEN_COOKIE);

  return (
    <div className="min-h-screen w-full max-w-full overflow-x-clip bg-background text-foreground">

      <main className="grid w-full max-w-[1120px] touch-pan-y grid-cols-1 gap-14 overflow-x-clip px-4 py-8 sm:px-8 xl:ml-12 xl:max-w-none xl:grid-cols-[680px_1fr_320px] xl:gap-0 xl:px-0 xl:pr-16 2xl:ml-20 2xl:pr-24">

        <section className="w-full max-w-[630px] space-y-8">
          <Suspense fallback={<FeedColumnSkeleton />}>
            <FeedSection cookieHeader={cookieHeader} hasSession={hasSession} />
          </Suspense>
        </section>

        <aside className="sticky top-8 hidden h-fit w-full xl:col-start-3 xl:block">
          <section className="space-y-7">
            <HomeAccountPanel />

            <Suspense fallback={null}>
              <TopCafesSection
                cookieHeader={cookieHeader}
                hasSession={hasSession}
              />
            </Suspense>

            <p className="text-xs leading-5 text-muted">
              {[
                t("footer.about"),
                t("footer.help"),
                t("footer.privacy"),
                t("footer.terms"),
                t("footer.locations"),
              ].join(" - ")}
              <br />
              {t("footer.copyright")}
            </p>
          </section>
        </aside>
      </main>

      <div className="fixed bottom-8 right-6 z-40 hidden xl:block 2xl:right-10">
        <Suspense fallback={null}>
          <MessageDockSection
            cookieHeader={cookieHeader}
            hasSession={hasSession}
          />
        </Suspense>
      </div>
    </div>
  );
}
