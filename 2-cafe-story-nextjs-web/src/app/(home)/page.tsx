import { cookies } from "next/headers";
import { HomeAccountPanel } from "@/components/feed/home-account-panel";
import { FeedPostList } from "@/components/feed/feed-post-list";
import { TopCafesNearby } from "@/components/feed/top-cafes-nearby";
import { MessageDock } from "@/components/message/message-dock";
import { mapBlogFeedToFeedPosts } from "@/features/blogs/blog-feed-adapter";
import { ApiError } from "@/lib/api/client";
import { getBlogFeed } from "@/lib/api/blogs";
import { getTopCafePages } from "@/lib/api/cafes";
import { ACCESS_TOKEN_COOKIE } from "@/lib/routes";
import { mockMessageDock } from "@/mocks/messages";
import type { FeedPost, TopCafe } from "@/types/feed";

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
  const [feedState, topCafes] = await Promise.all([loadHomeFeed(), loadTopCafes()]);

  return (
    <div className="min-h-screen w-full max-w-full overflow-x-clip bg-background text-foreground">
      <main className="grid w-full max-w-[1120px] touch-pan-y grid-cols-1 gap-14 overflow-x-clip px-4 py-8 sm:px-8 xl:ml-12 xl:grid-cols-[630px_320px] xl:px-0 2xl:ml-20">
        <section className="w-full max-w-[630px] space-y-8">
          <FeedPostList
            errorMessage={feedState.errorMessage}
            initialPage={0}
            pageSize={20}
            posts={feedState.posts}
            windowType="HOUR_24"
          />
        </section>

        <aside className="sticky top-8 hidden h-fit w-full xl:block">
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
        <MessageDock data={mockMessageDock} />
      </div>
    </div>
  );
}
