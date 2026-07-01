import { cookies } from "next/headers";
import { HomeAccountPanel } from "@/components/feed/home-account-panel";
import { FeedPostList } from "@/components/feed/feed-post-list";
import { StoryRail } from "@/components/feed/story-rail";
import { TopCafesNearby } from "@/components/feed/top-cafes-nearby";
import { MessageDock } from "@/components/message/message-dock";
import { mapBlogFeedToFeedPosts } from "@/features/blogs/blog-feed-adapter";
import { ApiError } from "@/lib/api/client";
import { getMe } from "@/lib/api/auth";
import { getBlogFeed } from "@/lib/api/blogs";
import {
  getCafePageById,
  getFollowedCafePagesByUserId,
  getTopCafePages,
} from "@/lib/api/cafes";
import { getConversations } from "@/lib/api/chat";
import { getFollowingByUserId, getUserById } from "@/lib/api/users";
import { DEFAULT_AVATAR_IMAGE } from "@/lib/avatar";
import { ACCESS_TOKEN_COOKIE } from "@/lib/routes";
import type { FeedPost, StoryItem, TopCafe } from "@/types/feed";
import type { MessageContact, MessageDockData } from "@/types/message";

export const dynamic = "force-dynamic";

type HomeFeedState = {
  errorMessage?: string;
  posts: FeedPost[];
};

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

async function loadStoryRail(cookieHeader: string): Promise<StoryItem[]> {
  try {
    const headers = { Cookie: cookieHeader };
    const me = await getMe({ headers }).catch(() => null);
    const myId = me?.user?.userId;
    if (!myId) {
      return [];
    }

    const [followingUsers, followedCafePages] = await Promise.all([
      getFollowingByUserId(myId, { headers }).catch(() => []),
      getFollowedCafePagesByUserId(myId, { headers }).catch(() => []),
    ]);

    const followingUserIds = Array.from(
      new Set(
        followingUsers
          .map((entry) => entry.followingUserId)
          .filter((id): id is string => Boolean(id) && id !== myId),
      ),
    );
    const followedCafePageIds = Array.from(
      new Set(
        followedCafePages
          .map((entry) => entry.cafePageId)
          .filter((id): id is string => Boolean(id)),
      ),
    );

    const userItems = await Promise.all(
      followingUserIds.map(async (userId) => {
        try {
          const user = await getUserById(userId, { headers });
          const avatarUrl =
            user.userAvatar?.trim() ||
            user.avatar?.trim() ||
            user.profileImage?.trim() ||
            user.imageUrl?.trim() ||
            DEFAULT_AVATAR_IMAGE;

          const item: StoryItem = {
            id: `user-${user.userId}`,
            kind: "user",
            label: user.userName,
            avatarUrl,
            href: `/${user.userName}`,
          };
          return item;
        } catch {
          return null;
        }
      }),
    );

    const cafeItems = await Promise.all(
      followedCafePageIds.map(async (cafePageId) => {
        try {
          const cafe = await getCafePageById(cafePageId, { headers });
          const item: StoryItem = {
            id: `cafe-${cafe.id}`,
            kind: "cafe-page",
            label: cafe.name,
            avatarUrl: cafe.avatarUrl?.trim() || DEFAULT_AVATAR_IMAGE,
            href: `/cafes/${cafe.id}`,
          };
          return item;
        } catch {
          return null;
        }
      }),
    );

    return [...userItems, ...cafeItems].filter(
      (item): item is StoryItem => item !== null,
    );
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

async function loadHomeFeed(): Promise<HomeFeedState> {
  const cookieStore = await cookies();

  if (!cookieStore.has(ACCESS_TOKEN_COOKIE)) {
    return {
      errorMessage: "Sign in to view your personalized feed.",
      posts: [],
    };
  }

  try {
    const feed = await getBlogFeed(
      {
        page: 0,
        size: 20,
        windowType: "HOUR_24",
      },
      {
        headers: {
          Cookie: cookieStore.toString(),
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

export default async function Home() {
  const cookieStore = await cookies();
  const cookieHeader = cookieStore.toString();
  const hasSession = cookieStore.has(ACCESS_TOKEN_COOKIE);

  const me = hasSession
    ? await getMe({ headers: { Cookie: cookieHeader } }).catch(() => null)
    : null;
  const myId = me?.user?.userId ?? null;

  const fallbackDock: MessageDockData = {
    title: "Messages",
    unreadCount: 0,
    contacts: [],
  };

  const [feedState, topCafes, storyItems, messageDock] = await Promise.all([
    loadHomeFeed(),
    loadTopCafes(),
    hasSession ? loadStoryRail(cookieHeader) : Promise.resolve<StoryItem[]>([]),
    myId
      ? loadMessageDock(cookieHeader, myId)
      : Promise.resolve(fallbackDock),
  ]);

  return (
    <div className="min-h-screen w-full max-w-full overflow-x-clip bg-background text-foreground">
      
      <main className="grid w-full max-w-[1120px] touch-pan-y grid-cols-1 gap-14 overflow-x-clip px-4 py-8 sm:px-8 xl:ml-12 xl:max-w-none xl:grid-cols-[680px_1fr_320px] xl:gap-0 xl:px-0 xl:pr-16 2xl:ml-20 2xl:pr-24">

        <section className="w-full max-w-[630px] space-y-8">
          {hasSession ? <StoryRail stories={storyItems} /> : null}
          <FeedPostList
            errorMessage={feedState.errorMessage}
            initialPage={0}
            pageSize={20}
            posts={feedState.posts}
            windowType="HOUR_24"
          />
        </section>

        <aside className="sticky top-8 hidden h-fit w-full xl:col-start-3 xl:block">
          <section className="space-y-7">
            <HomeAccountPanel />

            {topCafes.length > 0 ? (
              <TopCafesNearby cafes={topCafes} />
            ) : null}

            <p className="text-xs leading-5 text-muted">
              About - Help - Privacy - Terms - Locations
              <br />
              (c) 2026 Cafe Story
            </p>
          </section>
        </aside>
      </main>

      <div className="fixed bottom-8 right-6 z-40 hidden xl:block 2xl:right-10">
        <MessageDock data={messageDock} />
      </div>
    </div>
  );
}
