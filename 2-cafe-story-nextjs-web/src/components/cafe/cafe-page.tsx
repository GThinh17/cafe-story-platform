import { InfoIcon, StarIcon } from "lucide-react";
import type { CafeMenu, CafeSummary } from "@/types/cafe";
import { CafeActionButtons } from "@/features/cafes/components/cafe-action-buttons";
import { CafeRecentReviews } from "@/components/review/cafe-recent-reviews";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import type { CafeReviewPost } from "@/types/review";

type CafePageProps = {
  cafe: CafeSummary;
  menu: CafeMenu;
  recentReviews?: CafeReviewPost[];
};

const defaultOpeningHours = [
  { day: "Mon - Fri", time: "7:00 AM - 6:00 PM" },
  { day: "Saturday", time: "8:00 AM - 7:00 PM", highlight: true },
  { day: "Sunday", time: "8:00 AM - 5:00 PM" },
];

export function CafePage({ cafe, menu, recentReviews = [] }: CafePageProps) {
  const openingHours = cafe.openingHours ?? defaultOpeningHours;
  const coverImage = cafe.coverImage ?? cafe.gallery[0] ?? cafe.image;
  const coverImageAlt = cafe.coverImageAlt ?? `${cafe.name} cover image`;
  const communityPhotos =
    cafe.communityPhotos ??
    [cafe.image, ...cafe.gallery].map((image) => ({
      image,
      alt: `${cafe.name} community photo`,
    }));

  return (
    <article className="w-full space-y-10 bg-background text-espresso">
      <section className="space-y-8 pb-10 pt-2">
        <div className="group relative min-h-[320px] overflow-hidden rounded-md border border-line-soft bg-surface-muted shadow-sm sm:min-h-[420px]">
          <img
            alt={coverImageAlt}
            className="absolute inset-0 h-full w-full object-cover transition duration-700 group-hover:scale-105"
            decoding="async"
            fetchPriority="high"
            src={coverImage}
          />
          <div className="absolute inset-0 bg-gradient-to-t from-espresso/75 via-espresso/20 to-transparent" />
          <div className="absolute bottom-0 left-0 right-0 p-6 text-white sm:p-10">
            <p className="text-xs font-black uppercase tracking-[0.18em] text-white/75">
              CafeStory Cover
            </p>
            <h2 className="mt-2 max-w-2xl font-serif text-4xl font-medium italic leading-tight sm:text-6xl">
              {cafe.name}
            </h2>
          </div>
        </div>

        <div className="flex flex-col gap-6 md:flex-row md:items-end md:justify-between">
          <div className="min-w-0 space-y-3">
            <h1 className="font-serif text-4xl font-medium leading-tight text-espresso">
              {cafe.name}
            </h1>

            <div className="flex flex-wrap items-center gap-x-4 gap-y-2 text-sm text-coffee-muted">
              <span className="flex items-center gap-1 font-black text-espresso">
                <StarIcon data-icon="inline-start" />
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

          <CafeActionButtons cafeName={cafe.name} menu={menu} />
        </div>
        <Separator className="mt-10" />
      </section>

      <section className="grid gap-4 md:grid-cols-[minmax(0,2fr)_minmax(280px,1fr)]">
        <Card className="border-line-soft">
          <CardHeader>
            <CardTitle className="text-xs font-black uppercase tracking-[0.12em] text-muted">
            Vibe & Features
            </CardTitle>
          </CardHeader>

          <CardContent className="flex flex-col gap-6">
            <div className="flex flex-wrap gap-3">
            {cafe.amenities.map((amenity) => (
              <Badge className="rounded-full border-line-soft px-4 py-2 text-xs font-black text-espresso" key={amenity} variant="secondary">
                {amenity}
              </Badge>
            ))}
            </div>

            <p className="max-w-[720px] text-sm leading-7 text-coffee-muted">
            {cafe.featureSummary ?? cafe.description}
            </p>
          </CardContent>
        </Card>

        <Card className="border-line-soft bg-surface-muted shadow-none">
          <CardHeader>
            <CardTitle className="text-xs font-black uppercase tracking-[0.12em] text-muted">
            Opening Hours
            </CardTitle>
          </CardHeader>

          <CardContent className="flex flex-col gap-6">
            <ul className="flex flex-col gap-4 text-sm">
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

            <p className="flex items-center gap-2 border-t border-line-soft pt-6 text-sm text-coffee-muted">
            <InfoIcon data-icon="inline-start" />
            {cafe.peakHours ?? "Peak hours usually 10 AM - 1 PM"}
            </p>
          </CardContent>
        </Card>
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
