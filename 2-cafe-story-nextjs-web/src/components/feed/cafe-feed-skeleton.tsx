function SkeletonBlock({ className = "" }: { className?: string }) {
  return (
    <span
      aria-hidden="true"
      className={`block rounded-md bg-surface-muted ${className}`}
    />
  );
}

function StoryRailSkeleton() {
  return (
    <div className="w-full overflow-x-clip">
      <div className="grid grid-cols-[repeat(auto-fit,64px)] gap-x-4 gap-y-4">
        {Array.from({ length: 6 }).map((_, index) => (
          <div
            className="grid w-16 justify-items-center text-center"
            key={index}
          >
            <span className="grid h-[64px] w-[64px] place-items-center rounded-full bg-[linear-gradient(135deg,var(--primary),var(--accent))] p-[4px] opacity-45">
              <SkeletonBlock className="h-[56px] w-[56px] rounded-full bg-background/80" />
            </span>
            <SkeletonBlock className="mt-2 h-3 w-12" />
          </div>
        ))}
      </div>
    </div>
  );
}

function PostCardSkeleton() {
  return (
    <article className="overflow-hidden rounded-md border border-border bg-surface shadow-sm">
      <div className="flex items-center justify-between px-4 py-4">
        <div className="flex min-w-0 items-center gap-3">
          <SkeletonBlock className="h-11 w-11 shrink-0 rounded-full" />
          <div className="min-w-0 space-y-2">
            <SkeletonBlock className="h-4 w-36" />
            <SkeletonBlock className="h-3 w-44" />
          </div>
        </div>
        <SkeletonBlock className="h-8 w-12" />
      </div>

      <SkeletonBlock className="aspect-[4/5] w-full rounded-none sm:aspect-[5/4]" />

      <div className="space-y-4 px-4 py-4">
        <div className="flex items-center justify-between">
          <div className="flex gap-2">
            <SkeletonBlock className="h-9 w-14" />
            <SkeletonBlock className="h-9 w-20" />
            <SkeletonBlock className="h-9 w-16" />
          </div>
          <SkeletonBlock className="h-9 w-14" />
        </div>

        <SkeletonBlock className="h-4 w-40" />
        <div className="space-y-2">
          <SkeletonBlock className="h-4 w-full" />
          <SkeletonBlock className="h-4 w-4/5" />
        </div>
        <div className="flex flex-wrap gap-2">
          <SkeletonBlock className="h-6 w-24" />
          <SkeletonBlock className="h-6 w-28" />
          <SkeletonBlock className="h-6 w-20" />
        </div>
      </div>
    </article>
  );
}

function RightRailSkeleton() {
  return (
    <section className="space-y-7">
      <div className="flex items-center gap-3">
        <SkeletonBlock className="h-12 w-12 rounded-full" />
        <div className="min-w-0 flex-1 space-y-2">
          <SkeletonBlock className="h-4 w-28" />
          <SkeletonBlock className="h-4 w-20" />
        </div>
        <SkeletonBlock className="h-3 w-10" />
      </div>

      <div>
        <div className="mb-4 flex items-center justify-between">
          <SkeletonBlock className="h-5 w-36" />
          <SkeletonBlock className="h-3 w-10" />
        </div>
        <div className="space-y-4">
          {Array.from({ length: 4 }).map((_, index) => (
            <div className="flex items-center gap-3" key={index}>
              <SkeletonBlock className="h-11 w-11 rounded-full" />
              <div className="min-w-0 flex-1 space-y-2">
                <SkeletonBlock className="h-4 w-28" />
                <SkeletonBlock className="h-3 w-24" />
              </div>
              <SkeletonBlock className="h-3 w-8" />
            </div>
          ))}
        </div>
      </div>

      <div className="space-y-2">
        <SkeletonBlock className="h-3 w-60" />
        <SkeletonBlock className="h-3 w-44" />
      </div>
    </section>
  );
}

function MessageDockSkeleton() {
  return (
    <div className="flex h-14 w-fit items-center gap-3 rounded-full border border-border bg-surface px-4 shadow-xl">
      <SkeletonBlock className="h-8 w-8 rounded-full" />
      <SkeletonBlock className="h-5 w-20" />
      <span className="flex -space-x-2 pl-3">
        {Array.from({ length: 3 }).map((_, index) => (
          <SkeletonBlock
            className="h-7 w-7 rounded-full border-2 border-surface"
            key={index}
          />
        ))}
      </span>
    </div>
  );
}

export function CafeFeedSkeleton() {
  return (
    <div className="min-h-screen w-full max-w-full overflow-x-clip bg-background text-foreground">
      <main
        aria-busy="true"
        aria-label="Loading cafe feed"
        className="grid w-full max-w-[1120px] touch-pan-y grid-cols-1 gap-14 overflow-x-clip px-4 py-8 sm:px-8 xl:ml-12 xl:grid-cols-[630px_320px] xl:px-0 2xl:ml-20"
      >
        <section className="w-full max-w-[630px] space-y-8">
          <div className="animate-pulse space-y-8">
            <StoryRailSkeleton />

            <div className="space-y-6">
              <PostCardSkeleton />
              <PostCardSkeleton />
            </div>
          </div>
        </section>

        <aside className="sticky top-8 hidden h-fit w-full animate-pulse xl:block">
          <RightRailSkeleton />
        </aside>
      </main>

      <div className="fixed bottom-8 right-6 z-40 hidden animate-pulse xl:block 2xl:right-10">
        <MessageDockSkeleton />
      </div>
    </div>
  );
}
