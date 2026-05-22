import { MessageDock } from "@/components/message/message-dock";
import { PostCard } from "@/components/feed/post-card";
import { StoryRail } from "@/components/feed/story-rail";
import { TopCafesNearby } from "@/components/feed/top-cafes-nearby";
import { mockFeedPosts, mockStories, mockTopCafes } from "@/mocks/feed";
import { mockMessageDock } from "@/mocks/messages";

export default function Home() {
  return (
    <div className="min-h-screen w-full max-w-full overflow-x-clip bg-background text-foreground">
      <main className="grid w-full max-w-[1120px] touch-pan-y grid-cols-1 gap-14 overflow-x-clip px-4 py-8 sm:px-8 xl:ml-12 xl:grid-cols-[630px_320px] xl:px-0 2xl:ml-20">
        <section className="w-full max-w-[630px] space-y-8">
          <StoryRail stories={mockStories} />

          <div className="space-y-6">
            {mockFeedPosts.map((post) => (
              <PostCard key={post.cafe} post={post} />
            ))}
          </div>
        </section>

        <aside className="sticky top-8 hidden h-fit w-full xl:block">
          <section className="space-y-7">
            <div className="flex items-center gap-3">
              <span className="grid h-12 w-12 place-items-center rounded-full bg-surface-muted text-sm font-bold text-primary-strong">
                GT
              </span>
              <span className="min-w-0 flex-1">
                <span className="block truncate text-sm font-bold">
                  gthinh_1704
                </span>
                <span className="block truncate text-sm text-muted">
                  Gia Thinh
                </span>
              </span>
              <a className="text-xs font-bold text-primary" href="#">
                Switch
              </a>
            </div>

            <TopCafesNearby cafes={mockTopCafes} />

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
