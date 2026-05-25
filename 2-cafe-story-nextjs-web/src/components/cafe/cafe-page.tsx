import type { CafeSummary } from "@/types/cafe";
import { CafeRecentReviews } from "@/components/review/cafe-recent-reviews";
import type { CafeReviewPost } from "@/types/review";

type CafePageProps = {
  cafe: CafeSummary;
  recentReviews?: CafeReviewPost[];
};

const defaultOpeningHours = [
  { day: "Mon - Fri", time: "7:00 AM - 6:00 PM" },
  { day: "Saturday", time: "8:00 AM - 7:00 PM", highlight: true },
  { day: "Sunday", time: "8:00 AM - 5:00 PM" },
];

function CafeIcon({ name }: { name: string }) {
  const paths: Record<string, string[]> = {
    menu: [
      "M5 5.5h8a3 3 0 0 1 3 3V19H8a3 3 0 0 0-3-3V5.5Z",
      "M19 5.5h-3a3 3 0 0 0-3 3V19h3a3 3 0 0 1 3-3V5.5Z",
    ],
    directions: [
      "M12 3 3 12l9 9 9-9-9-9Z",
      "M9 12h6",
      "m12 9 3 3-3 3",
    ],
    star: [
      "m12 3 2.8 5.7 6.2.9-4.5 4.4 1.1 6.2L12 17.9 6.4 21.2 7.5 15 3 10.6l6.2-.9L12 3Z",
    ],
    info: [
      "M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18Z",
      "M12 10v6",
      "M12 7h.01",
    ],
  };

  return (
    <svg
      aria-hidden="true"
      className="h-4 w-4"
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

export function CafePage({ cafe, recentReviews = [] }: CafePageProps) {
  const openingHours = cafe.openingHours ?? defaultOpeningHours;
  const communityPhotos =
    cafe.communityPhotos ??
    [cafe.image, ...cafe.gallery].map((image) => ({
      image,
      alt: `${cafe.name} community photo`,
    }));

  return (
    <article className="w-full space-y-10 bg-background text-espresso">
      <section className="border-b border-line-soft pb-10 pt-2">
        <div className="flex flex-col gap-6 md:flex-row md:items-end md:justify-between">
          <div className="min-w-0 space-y-3">
            <h1 className="font-serif text-4xl font-medium leading-tight text-espresso">
              {cafe.name}
            </h1>

            <div className="flex flex-wrap items-center gap-x-4 gap-y-2 text-sm text-coffee-muted">
              <span className="flex items-center gap-1 font-black text-espresso">
                <CafeIcon name="star" />
                {cafe.rating}
              </span>
              <span>{cafe.type}</span>
              <span>
                {cafe.status ?? "Open"} - {cafe.hours}
              </span>
            </div>

            <p className="text-sm leading-6 text-coffee-muted">
              {cafe.address}
            </p>
          </div>

          <div className="flex flex-col gap-3 sm:flex-row">
            <button
              className="flex h-12 items-center justify-center gap-2 rounded-sm bg-espresso px-8 text-sm font-black text-white transition hover:bg-primary-strong"
              type="button"
            >
              <CafeIcon name="menu" />
              View Menu
            </button>
            <button
              className="flex h-12 items-center justify-center gap-2 rounded-sm border border-espresso bg-transparent px-8 text-sm font-black text-espresso transition hover:bg-surface-muted"
              type="button"
            >
              <CafeIcon name="directions" />
              Get Directions
            </button>
          </div>
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-[minmax(0,2fr)_minmax(280px,1fr)]">
        <div className="space-y-6 rounded-md border border-line-soft bg-surface p-6">
          <h2 className="text-xs font-black uppercase tracking-[0.12em] text-muted">
            Vibe & Features
          </h2>

          <div className="flex flex-wrap gap-3">
            {cafe.amenities.map((amenity) => (
              <span
                className="rounded-full border border-line-soft bg-surface-muted px-4 py-2 text-xs font-black text-espresso"
                key={amenity}
              >
                {amenity}
              </span>
            ))}
          </div>

          <p className="max-w-[720px] text-sm leading-7 text-coffee-muted">
            {cafe.featureSummary ?? cafe.description}
          </p>
        </div>

        <aside className="space-y-6 rounded-md border border-line-soft bg-surface-muted p-6">
          <h2 className="text-xs font-black uppercase tracking-[0.12em] text-muted">
            Opening Hours
          </h2>

          <ul className="space-y-4 text-sm">
            {openingHours.map((item) => (
              <li
                className={`flex items-center justify-between gap-4 ${
                  item.highlight ? "font-black text-espresso" : "text-coffee-muted"
                }`}
                key={item.day}
              >
                <span>{item.day}</span>
                <span>{item.time}</span>
              </li>
            ))}
          </ul>

          <p className="flex items-center gap-2 pt-6 text-sm text-coffee-muted">
            <CafeIcon name="info" />
            {cafe.peakHours ?? "Peak hours usually 10 AM - 1 PM"}
          </p>
        </aside>
      </section>

      <CafeRecentReviews reviews={recentReviews} />

      <section className="space-y-6">
        <div className="flex items-center justify-between gap-4">
          <h2 className="font-serif text-3xl font-medium text-espresso">
            Community Photos
          </h2>
          <span className="text-sm font-black text-coffee-muted">
            {cafe.photoCount ?? cafe.reviewCount} photos
          </span>
        </div>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {communityPhotos.map((photo) => (
            <article
              className="aspect-square overflow-hidden bg-surface-muted"
              key={photo.image}
            >
              <img
                alt={photo.alt}
                className="h-full w-full object-cover transition duration-500 hover:scale-105"
                decoding="async"
                loading="lazy"
                src={photo.image}
              />
            </article>
          ))}
        </div>
      </section>
    </article>
  );
}
