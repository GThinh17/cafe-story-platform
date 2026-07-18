import { cache, Suspense } from "react";
import { cookies } from "next/headers";
import { CafeFeedSkeleton } from "@/components/feed/cafe-feed-skeleton";
import { HomeAccountPanel } from "@/components/feed/home-account-panel";
import { FeedPostList } from "@/components/feed/feed-post-list";
import { StoryRail } from "@/components/feed/story-rail";
import { TopCafesNearby } from "@/components/feed/top-cafes-nearby";
import { MessageDock } from "@/components/message/message-dock";
import { mapBlogFeedToFeedPosts } from "@/features/blogs/blog-feed-adapter";
import { ApiError } from "@/lib/api/client";
import { getMe } from "@/lib/api/auth";
import { getBlogFeed } from "@/lib/api/blogs";
import { getTopCafePages } from "@/lib/api/cafes";
import { getConversations } from "@/lib/api/chat";
import { getFollowingTargetsByUserId } from "@/lib/api/users";
import { DEFAULT_AVATAR_IMAGE } from "@/lib/avatar";
import { imageWidths, optimizeImageUrl } from "@/lib/image-optimizer";
import { ACCESS_TOKEN_COOKIE } from "@/lib/routes";
import type { FollowTargetResponse } from "@/types/user";
import type { FeedPost, StoryItem, TopCafe } from "@/types/feed";
import type { MessageContact, MessageDockData } from "@/types/message";

export const dynamic = "force-dynamic";

const FEED_PAGE_SIZE = 10;

type HomeFeedState = {
  errorMessage?: string;
  posts: FeedPost[];
};

const getMeCached = cache((cookieHeader: string) =>
  getMe({ headers: { Cookie: cookieHeader } }).catch(() => null),
);

async function loadTopCafes(): Promise<TopCafe[]> {
  try {
    const cafes = await getTopCafePages({ size: 5 });
    return cafes.map((cafe) => ({
      id: cafe.id,
      name: cafe.name,
      rating: typeof cafe.rankingScore === "number" ? cafe.rankingScore.toFixed(1) : "New",
      type: cafe.regionCity ?? "Cafe page",
    }));
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
): Promise<MessageDockData> {
  const base: MessageDockData = {
    title: "Messages",
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
        "Cafe Story user";

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

async function loadHomeFeed(cookieHeader: string): Promise<HomeFeedState> {
  try {
    const feed = await getBlogFeed(
      {
        page: 0,
        size: FEED_PAGE_SIZE,
        windowType: "HOUR_24",
      },
      {
        headers: {
          Cookie: cookieHeader,
        },
      },
    );

    return {
      posts: mapBlogFeedToFeedPosts(feed),
    };
  } catch (error) {
    return {
      errorMessage:
        error instanceof ApiError
          ? error.message
          : "Unable to load your feed right now.",
      posts: [],
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
  if (!hasSession) {
    return (
      <FeedPostList
        errorMessage="Sign in to view your personalized feed."
        initialPage={0}
        pageSize={FEED_PAGE_SIZE}
        posts={[]}
        windowType="HOUR_24"
      />
    );
  }

  const [feedState, storyItems] = await Promise.all([
    loadHomeFeed(cookieHeader),
    loadStoryRail(cookieHeader),
  ]);

  return (
    <>
      <StoryRail stories={storyItems} />
      <FeedPostList
        errorMessage={feedState.errorMessage}
        initialPage={0}
        pageSize={FEED_PAGE_SIZE}
        posts={feedState.posts}
        windowType="HOUR_24"
      />
    </>
  );
}

async function TopCafesSection() {
  const topCafes = await loadTopCafes();

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

  const messageDock = await loadMessageDock(cookieHeader, myId);

  return <MessageDock data={messageDock} />;
}

export default async function Home() {
  const cookieStore = await cookies();
  const cookieHeader = cookieStore.toString();
  const hasSession = cookieStore.has(ACCESS_TOKEN_COOKIE);

  return (
    <div className="min-h-screen w-full max-w-full overflow-x-clip bg-background text-foreground">

      <main className="grid w-full max-w-[1120px] touch-pan-y grid-cols-1 gap-14 overflow-x-clip px-4 py-8 sm:px-8 xl:ml-12 xl:max-w-none xl:grid-cols-[680px_1fr_320px] xl:gap-0 xl:px-0 xl:pr-16 2xl:ml-20 2xl:pr-24">

        <section className="w-full max-w-[630px] space-y-8">
          <Suspense fallback={<CafeFeedSkeleton />}>
            <FeedSection cookieHeader={cookieHeader} hasSession={hasSession} />
          </Suspense>
        </section>

        <aside className="sticky top-8 hidden h-fit w-full xl:col-start-3 xl:block">
          <section className="space-y-7">
            <HomeAccountPanel />

            <Suspense fallback={null}>
              <TopCafesSection />
            </Suspense>

            <p className="text-xs leading-5 text-muted">
              About - Help - Privacy - Terms - Locations
              <br />
              (c) 2026 Cafe Story
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
