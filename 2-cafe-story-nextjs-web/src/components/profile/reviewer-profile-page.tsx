import { CafeRecentReviews } from "@/components/review/cafe-recent-reviews";
import type { CafeReviewPost } from "@/types/review";
import type { ReviewerProfile } from "@/types/user";

type ReviewerProfilePageProps = {
  recentReviews: CafeReviewPost[];
  reviewer: ReviewerProfile;
};

const navItems = ["Home", "Explore", "Bookmarks", "Messages"];

function ReviewerIcon({ name }: { name: string }) {
  const paths: Record<string, string[]> = {
    search: ["m21 21-4.3-4.3", "M11 18a7 7 0 1 0 0-14 7 7 0 0 0 0 14Z"],
    bell: [
      "M18 16v-5a6 6 0 0 0-12 0v5l-2 2h16l-2-2Z",
      "M10 20a2 2 0 0 0 4 0",
    ],
    user: [
      "M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z",
      "M4.5 21a7.5 7.5 0 0 1 15 0",
    ],
    home: ["M3 10.5 12 3l9 7.5", "M5 9.5V21h5v-6h4v6h5V9.5"],
    explore: [
      "M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18Z",
      "m15.5 8.5-2 5-5 2 2-5 5-2Z",
    ],
    bookmarks: ["M6 4h12v17l-6-3-6 3V4Z"],
    messages: ["M4 5.5h16v11H8l-4 4v-15Z", "M8 9h8", "M8 13h5"],
    settings: [
      "M12 15.5a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7Z",
      "M19.4 15a1.7 1.7 0 0 0 .3 1.9l.1.1a2 2 0 0 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.9-.3 1.7 1.7 0 0 0-1 1.6V21a2 2 0 0 1-4 0v-.1a1.7 1.7 0 0 0-1-1.6 1.7 1.7 0 0 0-1.9.3l-.1.1A2 2 0 0 1 4.2 17l.1-.1a1.7 1.7 0 0 0 .3-1.9 1.7 1.7 0 0 0-1.6-1H3a2 2 0 0 1 0-4h.1a1.7 1.7 0 0 0 1.6-1 1.7 1.7 0 0 0-.3-1.9l-.1-.1A2 2 0 0 1 7 4.2l.1.1a1.7 1.7 0 0 0 1.9.3 1.7 1.7 0 0 0 1-1.6V3a2 2 0 0 1 4 0v.1a1.7 1.7 0 0 0 1 1.6 1.7 1.7 0 0 0 1.9-.3l.1-.1A2 2 0 0 1 19.8 7l-.1.1a1.7 1.7 0 0 0-.3 1.9 1.7 1.7 0 0 0 1.6 1h.1a2 2 0 0 1 0 4H21a1.7 1.7 0 0 0-1.6 1Z",
    ],
    location: [
      "M12 21s7-4.5 7-11a7 7 0 1 0-14 0c0 6.5 7 11 7 11Z",
      "M12 10.5a2 2 0 1 0 0-4 2 2 0 0 0 0 4Z",
    ],
  };

  return (
    <svg
      aria-hidden="true"
      className="h-5 w-5"
      fill="none"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth="1.7"
      viewBox="0 0 24 24"
    >
      {paths[name].map((path) => (
        <path d={path} key={path} />
      ))}
    </svg>
  );
}

export function ReviewerProfilePage({
  recentReviews,
  reviewer,
}: ReviewerProfilePageProps) {
  return (
    <section className="min-h-screen w-full bg-background text-espresso">
      <header className="sticky top-0 z-30 flex h-16 items-center justify-between border-b border-line-soft bg-background/90 px-6 backdrop-blur-md">
        <h1 className="font-serif text-2xl italic text-espresso">
          EspressoEdit
        </h1>

        <div className="flex items-center gap-5">
          <label className="hidden h-10 items-center gap-2 rounded-md bg-surface-muted px-4 md:flex">
            <ReviewerIcon name="search" />
            <input
              className="w-44 border-0 bg-transparent p-0 text-sm font-semibold outline-none placeholder:text-espresso focus:ring-0"
              placeholder="Search cafes..."
              type="search"
            />
          </label>
          <button aria-label="Notifications" type="button">
            <ReviewerIcon name="bell" />
          </button>
          <button aria-label="Profile" type="button">
            <ReviewerIcon name="user" />
          </button>
        </div>
      </header>

      <div className="mx-auto flex min-h-[calc(100vh-64px)] max-w-[1440px]">
        <aside className="sticky top-16 hidden h-[calc(100vh-64px)] w-64 shrink-0 flex-col border-r border-line-soft px-6 py-10 lg:flex">
          <div>
            <h2 className="font-serif text-2xl font-medium">Welcome back</h2>
            <p className="text-sm font-semibold text-coffee-muted">
              {reviewer.title}
            </p>
          </div>

          <nav className="mt-16 grid gap-5">
            {navItems.map((item) => (
              <a
                className="flex items-center gap-4 text-sm font-semibold text-coffee-muted no-underline transition hover:text-espresso"
                href="#"
                key={item}
              >
                <ReviewerIcon name={item.toLowerCase()} />
                {item}
              </a>
            ))}
          </nav>

          <div className="mt-auto space-y-6">
            <button
              className="h-12 w-full rounded-sm bg-espresso text-sm font-black text-white transition hover:bg-primary-strong"
              type="button"
            >
              New Review
            </button>
            <div className="border-t border-line-soft pt-6">
              <a
                className="flex items-center gap-4 text-sm font-semibold text-coffee-muted no-underline"
                href="#"
              >
                <ReviewerIcon name="settings" />
                Settings
              </a>
            </div>
          </div>
        </aside>

        <main className="min-w-0 flex-1 px-6 py-10 md:px-10">
          <section className="flex flex-col gap-10 border-b border-line-soft pb-16 md:flex-row md:items-start">
            <div className="relative h-40 w-40 shrink-0 overflow-visible rounded-md">
              <img
                alt={`${reviewer.displayName} portrait`}
                className="h-40 w-40 rounded-md border border-line-soft object-cover shadow-sm"
                decoding="async"
                src={reviewer.avatarImage}
              />
              <span className="absolute -bottom-3 left-1/2 w-28 -translate-x-1/2 rounded-md bg-espresso px-3 py-1.5 text-center text-[11px] font-black uppercase leading-3 text-white shadow-md">
                {reviewer.badge}
              </span>
            </div>

            <div className="max-w-2xl space-y-6">
              <div>
                <h2 className="font-serif text-4xl font-medium">
                  {reviewer.displayName}
                </h2>
                <p className="mt-1 flex items-center gap-2 text-sm text-coffee-muted">
                  <ReviewerIcon name="location" />
                  {reviewer.location}
                </p>
              </div>

              <p className="max-w-xl text-base italic leading-7 text-coffee-muted">
                "{reviewer.bio}"
              </p>

              <div className="flex flex-wrap gap-14">
                {Object.entries(reviewer.stats).map(([label, value]) => (
                  <div key={label}>
                    <span className="block font-serif text-3xl font-medium">
                      {value}
                    </span>
                    <span className="text-xs font-semibold uppercase tracking-[0.08em] text-coffee-muted">
                      {label}
                    </span>
                  </div>
                ))}
              </div>

              <div className="flex gap-4 pt-2">
                <button
                  className="h-11 rounded-md bg-espresso px-12 text-sm font-black text-white transition hover:bg-primary-strong"
                  type="button"
                >
                  Follow
                </button>
                <button
                  className="h-11 rounded-md border border-espresso px-12 text-sm font-medium text-espresso transition hover:bg-surface-muted"
                  type="button"
                >
                  Message
                </button>
              </div>
            </div>
          </section>

          <div className="mt-16">
            <CafeRecentReviews reviews={recentReviews} />
          </div>

          <section className="mt-16 space-y-8">
            <h2 className="font-serif text-3xl font-medium text-espresso">
              Visual Diary
            </h2>
            <div className="grid grid-cols-2 gap-[2px] md:grid-cols-3">
              {reviewer.visualDiary.map((photo) => (
                <img
                  alt={photo.alt}
                  className="aspect-square w-full object-cover"
                  decoding="async"
                  key={photo.image}
                  loading="lazy"
                  src={photo.image}
                />
              ))}
            </div>
          </section>
        </main>
      </div>
    </section>
  );
}
