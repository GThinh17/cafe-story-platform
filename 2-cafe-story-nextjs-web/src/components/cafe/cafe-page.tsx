"use client";

import { InfoIcon, StarIcon } from "lucide-react";
import { useState } from "react";
import type { CafeMenu, CafeSummary } from "@/types/cafe";
import { CafeActionButtons } from "@/components/cafe/cafe-action-buttons";
import { CafeMapModal } from "@/components/cafe/cafe-map-modal";
import { CafeRecentReviews } from "@/components/review/cafe-recent-reviews";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import type { FeedPost } from "@/types/feed";

type CafePageProps = {
  cafe: CafeSummary;
  cafePosts: FeedPost[];
  menu: CafeMenu;
  hasMorePosts?: boolean;
  isPostsLoading?: boolean;
  onCafeLikeStateChange?: (nextState: {
    isLiked: boolean;
    likeCount: number;
  }) => void;
  onLoadMorePosts?: () => void;
  postsErrorMessage?: string | null;
};

const defaultOpeningHours = [
  { day: "Mon - Fri", time: "7:00 AM - 6:00 PM" },
  { day: "Saturday", time: "8:00 AM - 7:00 PM", highlight: true },
  { day: "Sunday", time: "8:00 AM - 5:00 PM" },
];

export function CafePage({
  cafe,
  cafePosts,
  hasMorePosts = false,
  isPostsLoading = false,
  menu,
  onCafeLikeStateChange,
  onLoadMorePosts,
  postsErrorMessage,
}: CafePageProps) {
  const [isMapOpen, setIsMapOpen] = useState(false);
  const openingHours = cafe.openingHours ?? defaultOpeningHours;
  const avatarImage = cafe.avatarImage ?? cafe.image ?? cafe.coverImage;
  const coverImage = cafe.coverImage ?? cafe.gallery[0] ?? avatarImage;
  const coverImageAlt = cafe.coverImageAlt ?? `${cafe.name} cover image`;
  const avatarImageAlt = cafe.avatarImageAlt ?? `${cafe.name} avatar`;
  const communityPhotos =
    cafe.communityPhotos ??
    Array.from(new Set([coverImage, avatarImage, ...cafe.gallery]))
      .filter((image): image is string => Boolean(image))
      .map((image) => ({
        image,
        alt: `${cafe.name} community photo`,
      }));

  return (
    <article className="w-full space-y-8 bg-background text-espresso">
      <section className="space-y-8 pb-8 -mt-8 pt-0">
        <div className="relative">
          <div className="relative min-h-[240px] overflow-hidden rounded-md border border-line-soft bg-surface-muted shadow-sm sm:min-h-[360px]">
            {coverImage ? (
              <img
                alt={coverImageAlt}
                className="absolute inset-0 h-full w-full object-cover"
                decoding="async"
                fetchPriority="high"
                src={coverImage}
              />
            ) : null}
          </div>
        </div>

        <div className="flex flex-col gap-6 md:flex-row md:items-end md:justify-between">
          <div className="flex min-w-0 flex-col gap-5 sm:flex-row sm:items-center">
            <img
              alt={avatarImageAlt}
              className="size-36 shrink-0 rounded-full border-4 border-background bg-surface object-cover shadow-sm ring-1 ring-line-soft sm:size-48"
              decoding="async"
              src={avatarImage}
            />
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
          </div>

          <CafeActionButtons
            cafeId={cafe.id}
            cafeName={cafe.name}
            isLiked={cafe.isLiked}
            likeCount={cafe.likeCount}
            menu={menu}
            onLikeStateChange={onCafeLikeStateChange}
          />
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

      <section className="space-y-6">
        {isPostsLoading && cafePosts.length === 0 ? (
          <div className="rounded-md border border-line-soft bg-surface-muted px-4 py-3 text-sm font-semibold text-muted">
            Loading cafe posts...
          </div>
        ) : (
          <CafeRecentReviews
            errorMessage={
              cafePosts.length === 0 ? postsErrorMessage ?? undefined : undefined
            }
            onMapViewClick={() => setIsMapOpen(true)}
            posts={cafePosts}
          />
        )}

        {postsErrorMessage && cafePosts.length > 0 ? (
          <div className="rounded-md border border-line-soft bg-surface-muted px-4 py-3 text-sm font-semibold text-muted">
            {postsErrorMessage}
          </div>
        ) : null}

        {isPostsLoading && cafePosts.length > 0 ? (
          <p className="py-2 text-center text-sm font-semibold text-muted">
            Loading cafe posts...
          </p>
        ) : null}

        {hasMorePosts ? (
          <div className="flex justify-center">
            <Button
              className="cursor-pointer rounded-sm border-line-soft px-6 text-sm font-black"
              disabled={isPostsLoading}
              onClick={onLoadMorePosts}
              type="button"
              variant="outline"
            >
              Load more posts
            </Button>
          </div>
        ) : null}
      </section>

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

      <CafeMapModal
        cafeName={cafe.name}
        onOpenChange={setIsMapOpen}
        open={isMapOpen}
        regionArea={cafe.regionArea}
        regionCity={cafe.regionCity}
        regionProvince={cafe.regionProvince}
        regionStreet={cafe.regionStreet}
        regionWard={cafe.regionWard}
      />
    </article>
  );
}
