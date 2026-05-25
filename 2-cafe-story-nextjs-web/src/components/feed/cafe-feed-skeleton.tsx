function SkeletonBlock({ className = "" }: { className?: string }) {
  return (
    <span
      aria-hidden="true"
      className={`block animate-pulse rounded-md bg-surface-muted ${className}`}
    />
  );
}

function SkeletonIcon({ className = "" }: { className?: string }) {
  return (
    <span
      aria-hidden="true"
      className={`grid h-5 w-5 place-items-center rounded-sm border border-espresso/80 ${className}`}
    />
  );
}

function TopBarSkeleton() {
  return (
    <header className="sticky top-0 z-30 flex h-16 items-center justify-between border-b border-border bg-surface/90 px-5 backdrop-blur">
      <div className="flex min-w-0 items-center gap-5">
        <p className="font-serif text-2xl font-medium text-espresso">
          Cafe Story
        </p>
        <label className="relative hidden h-10 w-64 items-center rounded-md border border-border bg-background px-4 md:flex">
          <span className="sr-only">Loading search</span>
          <SkeletonIcon className="h-4 w-4 border-0" />
          <SkeletonBlock className="ml-3 h-3 w-28" />
        </label>
      </div>

      <nav className="hidden items-center gap-12 text-xs font-black tracking-[0.08em] text-espresso md:flex">
        <span className="border-b-2 border-espresso pb-2">Overview</span>
        <span>Users</span>
        <span>Moderation</span>
        <span>Settings</span>
      </nav>

      <div className="flex items-center gap-5">
        <SkeletonIcon />
        <SkeletonIcon />
        <SkeletonBlock className="h-8 w-8 rounded-full" />
      </div>
    </header>
  );
}

function LeftRailSkeleton() {
  const railItems = ["Overview", "Users", "Moderation", "Settings"];

  return (
    <aside className="sticky top-16 hidden h-[calc(100vh-64px)] w-64 shrink-0 border-r border-border bg-surface/60 px-6 py-8 lg:block">
      <div className="mb-16 space-y-3">
        <SkeletonBlock className="h-12 w-12 rounded-md" />
        <SkeletonBlock className="h-4 w-32" />
        <SkeletonBlock className="h-3 w-24 opacity-70" />
      </div>

      <nav className="space-y-3">
        {railItems.map((item, index) => (
          <div
            className={`flex h-12 items-center gap-4 rounded-md px-4 text-sm font-semibold tracking-[0.08em] ${
              index === 0 ? "bg-[#eadbd4] text-espresso" : "text-espresso"
            }`}
            key={item}
          >
            <SkeletonIcon className="h-4 w-4 border-espresso" />
            <span>{item}</span>
          </div>
        ))}
      </nav>

      <div className="absolute bottom-10 left-10 right-10">
        <div className="flex h-16 items-center justify-center rounded-md bg-espresso">
          <SkeletonBlock className="h-4 w-24 bg-white/80" />
        </div>
      </div>
    </aside>
  );
}

function StorySkeleton() {
  return (
    <div className="flex flex-col items-center gap-2">
      <span className="grid h-16 w-16 place-items-center rounded-full bg-surface-muted p-1">
        <SkeletonBlock className="h-full w-full rounded-full bg-background" />
      </span>
      <SkeletonBlock className="h-3 w-12" />
    </div>
  );
}

function StoriesSkeleton() {
  return (
    <section className="border-b border-border/70 pb-14">
      <div className="flex justify-center gap-8 overflow-hidden">
        {Array.from({ length: 6 }).map((_, index) => (
          <div
            className={index === 0 ? "rounded-xl ring-2 ring-espresso" : ""}
            key={index}
          >
            <StorySkeleton />
          </div>
        ))}
      </div>
    </section>
  );
}

function PostCardSkeleton() {
  return (
    <article className="overflow-hidden rounded-md border border-line-soft bg-surface">
      <div className="flex items-center gap-4 px-6 py-5">
        <SkeletonBlock className="h-10 w-10 rounded-full" />
        <div className="min-w-0 flex-1 space-y-2">
          <SkeletonBlock className="h-4 w-36" />
          <SkeletonBlock className="h-3 w-24 opacity-70" />
        </div>
        <span className="text-xl font-black text-espresso">...</span>
      </div>

      <SkeletonBlock className="h-[520px] w-full rounded-none bg-[linear-gradient(90deg,#eeeeee_0%,#f7f4ef_50%,#eeeeee_100%)]" />

      <div className="space-y-5 px-6 py-5">
        <div className="flex items-center justify-between text-espresso">
          <div className="flex gap-5">
            <SkeletonIcon className="h-5 w-5" />
            <SkeletonIcon className="h-5 w-5" />
            <SkeletonIcon className="h-5 w-5" />
          </div>
          <SkeletonIcon className="h-5 w-5" />
        </div>

        <div className="space-y-3">
          <SkeletonBlock className="h-4 w-[78%]" />
          <SkeletonBlock className="h-4 w-[52%]" />
          <SkeletonBlock className="h-4 w-24" />
        </div>
      </div>
    </article>
  );
}

function FeedColumnSkeleton() {
  return (
    <main className="flex min-w-0 flex-1 justify-center px-4 py-10 sm:px-8">
      <div className="w-full max-w-[600px] space-y-16">
        <StoriesSkeleton />
        <div className="space-y-16">
          <PostCardSkeleton />
          <PostCardSkeleton />
        </div>
      </div>
    </main>
  );
}

export function CafeFeedSkeleton() {
  return (
    <div
      aria-busy="true"
      aria-label="Loading cafe feed"
      className="-ml-8 min-h-screen w-[calc(100vw-64px)] max-w-none touch-pan-y overflow-x-clip bg-background text-foreground sm:-ml-14 sm:w-[calc(100vw-72px)] xl:-ml-[248px]"
    >
      <TopBarSkeleton />
      <div className="flex min-h-[calc(100vh-64px)]">
        <LeftRailSkeleton />
        <FeedColumnSkeleton />
      </div>
    </div>
  );
}
