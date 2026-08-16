"use client";

import { InfoIcon, Settings, StarIcon } from "lucide-react";
import { useState } from "react";
import type { CafeSummary } from "@/types/cafe";
import { useI18n } from "@/components/providers/locale-provider";
import { CafeActionButtons } from "@/components/cafe/cafe-action-buttons";
import { CafeMapModal } from "@/components/cafe/cafe-map-modal";
import { CafePageSettingsModal } from "@/components/cafe/cafe-page-settings-modal";
import { CafeRecentReviews } from "@/components/review/cafe-recent-reviews";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { useCurrentUser } from "@/hooks/use-current-user";
import type { Translate } from "@/lib/i18n";
import type { FeedPost } from "@/types/feed";

type CafePageProps = {
  cafe: CafeSummary;
  cafePosts: FeedPost[];
  hasMorePosts?: boolean;
  isPostsLoading?: boolean;
  onCafeLikeStateChange?: (nextState: {
    isLiked: boolean;
    likeCount: number;
  }) => void;
  onCafeRatingChange?: (next: {
    isRating: boolean;
    myRating: number;
    ratingCount: number | null;
    ratingScore: number | null;
  }) => void;
  onLoadMorePosts?: () => void;
  postsErrorMessage?: string | null;
};

function getDefaultOpeningHours(t: Translate) {
  return [
    { day: t("cafe.hours.monFri"), time: "7:00 - 18:00" },
    { day: t("cafe.hours.saturday"), time: "8:00 - 19:00", highlight: true },
    { day: t("cafe.hours.sunday"), time: "8:00 - 17:00" },
  ];
}

export function CafePage({
  cafe,
  cafePosts,
  hasMorePosts = false,
  isPostsLoading = false,
  onCafeLikeStateChange,
  onCafeRatingChange,
  onLoadMorePosts,
  postsErrorMessage,
}: CafePageProps) {
  const { t } = useI18n();
  const { user: currentUser } = useCurrentUser();
  const [isMapOpen, setIsMapOpen] = useState(false);
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);
  const openingHours = cafe.openingHours ?? getDefaultOpeningHours(t);
  const avatarImage = cafe.avatarImage ?? cafe.image ?? cafe.coverImage;
  const coverImage = cafe.coverImage ?? cafe.gallery[0] ?? avatarImage;
  const coverImageAlt =
    cafe.coverImageAlt ?? t("cafe.coverAlt", { name: cafe.name });
  const avatarImageAlt =
    cafe.avatarImageAlt ?? t("cafe.avatarAlt", { name: cafe.name });
 
  const isCurrentOwner =
    Boolean(cafe.ownerUserId) && cafe.ownerUserId === currentUser?.userId;

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
              <div className="flex min-w-0 items-center gap-2">
                <h1 className="truncate text-4xl font-black leading-tight text-espresso">
                  {cafe.name}
                </h1>
                {isCurrentOwner ? (
                  <Button
                    aria-label={t("cafe.settings.title")}
                    className="shrink-0"
                    onClick={() => setIsSettingsOpen(true)}
                    size="icon-sm"
                    type="button"
                    variant="ghost"
                  >
                    <Settings />
                  </Button>
                ) : null}
              </div>

              <div className="flex flex-wrap items-center gap-x-4 gap-y-2 text-sm text-coffee-muted">
                <span className="flex items-center gap-1 font-black text-espresso">
                  <StarIcon data-icon="inline-start" />
                  {cafe.rating}
                </span>
                <span>{cafe.type}</span>
                <span>
                  {cafe.status ?? t("cafe.status.open")} - {cafe.hours}
                </span>
              </div>

              {cafe.address ? (
                <p className="text-sm leading-6 text-coffee-muted">
                  {cafe.address}
                </p>
              ) : null}
            </div>
          </div>

          <CafeActionButtons
            cafeId={cafe.id}
            isLiked={cafe.isLiked}
            isFollowing={cafe.isFollowing ?? false}
            likeCount={cafe.likeCount}
            myRating={cafe.myRating}
            onLikeStateChange={onCafeLikeStateChange}
            onRatingChange={onCafeRatingChange}
            ratingCount={cafe.ratingCount}
            ratingScore={cafe.ratingScore}
          />
        </div>
        <Separator className="mt-10" />
      </section>

      <section className="grid gap-4 md:grid-cols-[minmax(0,2fr)_minmax(280px,1fr)]">
        <Card className="border-line-soft">
          <CardHeader>
            <CardTitle className="text-xs font-black uppercase tracking-[0.12em] text-muted">
            {t("cafe.vibe.title")}
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
            {t("cafe.hours.title")}
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
            {cafe.peakHours ?? t("cafe.hours.peakFallback")}
            </p>
          </CardContent>
        </Card>
      </section>

      <section className="space-y-6">
        {isPostsLoading && cafePosts.length === 0 ? (
          <div className="rounded-md border border-line-soft bg-surface-muted px-4 py-3 text-sm font-semibold text-muted">
            {t("cafe.posts.loading")}
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
            {t("cafe.posts.loading")}
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
              {t("cafe.posts.loadMore")}
            </Button>
          </div>
        ) : null}
      </section>

      <CafeMapModal
        address={cafe.address}
        cafeName={cafe.name}
        onOpenChange={setIsMapOpen}
        open={isMapOpen}
        regionArea={cafe.regionArea}
        regionCity={cafe.regionCity}
        regionProvince={cafe.regionProvince}
        regionStreet={cafe.regionStreet}
        regionWard={cafe.regionWard}
      />

      <CafePageSettingsModal
        onOpenChange={setIsSettingsOpen}
        open={isSettingsOpen}
      />
    </article>
  );
}
