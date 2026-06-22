"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useParams } from "next/navigation";
import { CafePage } from "@/components/cafe/cafe-page";
import { CafePageExpiredModal } from "@/components/cafe/cafe-page-expired-modal";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { mapBlogResponsesToFeedPosts } from "@/features/blogs/blog-feed-adapter";
import { useBfcacheRestoreEffect } from "@/hooks/use-bfcache-restore";
import { useCurrentUser } from "@/hooks/use-current-user";
import { ApiError } from "@/lib/api/client";
import { getBlogsByCafePageId, getCafePageById } from "@/lib/api/cafes";
import { mockCafeMenu } from "@/mocks/cafes";
import type { CafePageResponse, CafeSummary } from "@/types/cafe";
import type { FeedPost } from "@/types/feed";

const DEFAULT_CAFE_IMAGE = "/images/default-avatar.svg";
const CAFE_BLOG_PAGE_SIZE = 20;

type CafePageContentProps = {
  cafePageId: string;
};

type LoadOptions = {
  preserveExisting?: boolean;
};

function getParamValue(value: string | string[] | undefined) {
  return Array.isArray(value) ? value[0] : value;
}

function firstNonEmpty(values: Array<string | null | undefined>) {
  return values
    .map((value) => value?.trim())
    .find((value) => Boolean(value));
}

function isNonEmptyString(value: string | null | undefined): value is string {
  return Boolean(value?.trim());
}

function formatCount(value: number | null | undefined) {
  return new Intl.NumberFormat("en", {
    notation: "compact",
    maximumFractionDigits: 1,
  }).format(value ?? 0);
}

function formatStatus(cafe: CafePageResponse) {
  if (cafe.pageActive === false) {
    return "Inactive";
  }

  if (!cafe.status) {
    return cafe.pageActive ? "Active" : "Cafe Story";
  }

  return cafe.status
    .toLowerCase()
    .replace(/_/g, " ")
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function formatLocation(cafe: CafePageResponse) {
  return [
    cafe.regionStreet,
    cafe.regionWard,
    cafe.regionArea,
    cafe.regionCity,
    cafe.regionProvince,
  ]
    .filter(Boolean)
    .join(", ");
}

function buildCafeTags(cafe: CafePageResponse) {
  return [
    cafe.regionCity,
    cafe.regionArea,
    `${formatCount(cafe.followerCount)} followers`,
    `${formatCount(cafe.likeCount)} likes`,
  ].filter(Boolean) as string[];
}

function mapCafePageResponseToCafeSummary(cafe: CafePageResponse): CafeSummary {
  const avatarImage = firstNonEmpty([
    cafe.avatarUrl,
    cafe.coverUrl,
    DEFAULT_CAFE_IMAGE,
  ]) ?? DEFAULT_CAFE_IMAGE;
  const coverImage = firstNonEmpty([cafe.coverUrl, cafe.avatarUrl]);
  const rating =
    typeof cafe.ratingScore === "number" ? cafe.ratingScore.toFixed(1) : "New";
  const tags = buildCafeTags(cafe);
  const description =
    cafe.description?.trim() || `${cafe.name} is now on CafeStory.`;

  return {
    id: cafe.id,
    ownerUserId: cafe.ownerUserId,
    name: cafe.name,
    location: formatLocation(cafe),
    address: cafe.address,
    type: cafe.regionCity ?? "Cafe page",
    rating,
    reviewCount: `${formatCount(cafe.ratingCount)} ratings`,
    distance: "",
    priceLevel: "",
    hours: cafe.pageActive ? "Active page" : "Cafe Story",
    status: formatStatus(cafe),
    photoCount: `${formatCount(cafe.likeCount)} likes`,
    likeCount: cafe.likeCount ?? 0,
    isLiked: cafe.isLiked ?? false,
    isFollowing: cafe.isFollowing ?? false,
    pageActive: cafe.pageActive,
    image: avatarImage,
    avatarImage,
    avatarImageAlt: `${cafe.name} avatar`,
    coverImage,
    coverImageAlt: `${cafe.name} cover image`,
    gallery: [coverImage, avatarImage].filter(Boolean) as string[],
    tags,
    amenities: tags.length > 0 ? tags : ["Cafe Story page"],
    popularDrinks: ["View menu", "Follow", "Stories"],
    description,
    regionArea: cafe.regionArea,
    regionCity: cafe.regionCity,
    regionProvince: cafe.regionProvince,
    regionStreet: cafe.regionStreet,
    regionWard: cafe.regionWard,
    featureSummary: description,
    peakHours: `${formatCount(cafe.followerCount)} followers`,
    communityPhotos: [coverImage, avatarImage]
      .filter(isNonEmptyString)
      .map((image) => ({
        image,
        alt: `${cafe.name} photo`,
      })),
  };
}

function getCafePageErrorMessage(error: unknown) {
  if (error instanceof ApiError) {
    return error.statusCode === 404 ? "Cafe not found." : error.message;
  }

  return "Unable to load cafe page.";
}

function CafePageLoadingState() {
  return (
    <div className="w-full space-y-8" aria-busy="true">
      <section className="space-y-8 pb-8 -mt-8 pt-0">
        <Skeleton className="h-[240px] rounded-md border border-line-soft sm:h-[360px]" />

        <div className="flex flex-col gap-6 md:flex-row md:items-end md:justify-between">
          <div className="flex min-w-0 flex-col gap-5 sm:flex-row sm:items-center">
            <Skeleton className="size-36 shrink-0 rounded-full border-4 border-background shadow-sm ring-1 ring-line-soft sm:size-48" />
            <div className="min-w-0 space-y-3">
              <div className="flex items-center gap-2">
                <Skeleton className="h-10 w-56 max-w-full" />
                <Skeleton className="size-8 rounded-full" />
              </div>
              <div className="flex flex-wrap items-center gap-x-4 gap-y-2">
                <Skeleton className="h-4 w-16" />
                <Skeleton className="h-4 w-24" />
                <Skeleton className="h-4 w-32" />
              </div>
              <Skeleton className="h-4 w-[min(420px,100%)]" />
            </div>
          </div>

          <div className="flex flex-wrap gap-2 sm:justify-end">
            <Skeleton className="h-10 w-24 rounded-sm" />
            <Skeleton className="h-10 w-24 rounded-sm" />
            <Skeleton className="h-10 w-28 rounded-sm" />
          </div>
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-[minmax(0,2fr)_minmax(280px,1fr)]">
        <Card className="border-line-soft">
          <CardHeader>
            <Skeleton className="h-3 w-32" />
          </CardHeader>
          <CardContent className="flex flex-col gap-6">
            <div className="flex flex-wrap gap-3">
              <Skeleton className="h-8 w-24 rounded-full" />
              <Skeleton className="h-8 w-28 rounded-full" />
              <Skeleton className="h-8 w-20 rounded-full" />
            </div>
            <div className="space-y-2">
              <Skeleton className="h-4 w-full" />
              <Skeleton className="h-4 w-4/5" />
            </div>
          </CardContent>
        </Card>

        <Card className="border-line-soft shadow-none">
          <CardHeader>
            <Skeleton className="h-3 w-28" />
          </CardHeader>
          <CardContent className="flex flex-col gap-6">
            {Array.from({ length: 3 }).map((_, index) => (
              <div className="flex items-center justify-between gap-4" key={index}>
                <Skeleton className="h-4 w-20" />
                <Skeleton className="h-4 w-28" />
              </div>
            ))}
            <Skeleton className="h-4 w-3/4" />
          </CardContent>
        </Card>
      </section>
    </div>
  );
}

function CafePageErrorState({ message }: { message: string }) {
  return (
    <div className="rounded-md border border-line-soft bg-surface-muted px-5 py-6 text-sm font-semibold text-muted">
      {message}
    </div>
  );
}

export function CafePageContent({ cafePageId }: CafePageContentProps) {
  const params = useParams<{ id?: string | string[] }>();
  const { user: currentUser, isLoading: isAuthLoading } = useCurrentUser();
  const cafeRequestIdRef = useRef(0);
  const blogsRequestIdRef = useRef(0);
  const routeCafePageId = getParamValue(params.id) || cafePageId;
  const decodedCafePageId = useMemo(
    () => decodeURIComponent(routeCafePageId ?? "").trim(),
    [routeCafePageId],
  );
  const [cafe, setCafe] = useState<CafeSummary | null>(null);
  const [cafePosts, setCafePosts] = useState<FeedPost[]>([]);
  const [isCafeLoading, setIsCafeLoading] = useState(false);
  const [cafeError, setCafeError] = useState<string | null>(null);
  const [isBlogsLoading, setIsBlogsLoading] = useState(false);
  const [blogsError, setBlogsError] = useState<string | null>(null);
  const [cursor, setCursor] = useState<string | null>(null);
  const [hasMore, setHasMore] = useState(false);

  const loadCafe = useCallback(async (
    idOverride?: string,
    options: LoadOptions = {},
  ) => {
    const idToFetch = idOverride || decodedCafePageId;
    const requestId = cafeRequestIdRef.current + 1;
    cafeRequestIdRef.current = requestId;
    const preserveExisting = options.preserveExisting ?? false;

    setCafeError(null);
    if (!preserveExisting) {
      setCafe(null);
      blogsRequestIdRef.current += 1;
      setCafePosts([]);
      setBlogsError(null);
      setCursor(null);
      setHasMore(false);
      setIsBlogsLoading(false);
    }
    setIsCafeLoading(Boolean(idToFetch));

    if (!idToFetch) {
      setIsCafeLoading(false);
      setCafeError("Cafe page id is missing.");
      return null;
    }

    try {
      const response = await getCafePageById(idToFetch);

      if (cafeRequestIdRef.current !== requestId) {
        return null;
      }

      const nextCafe = mapCafePageResponseToCafeSummary(response);

      setCafe(nextCafe);

      return nextCafe;
    } catch (requestError) {
      if (cafeRequestIdRef.current !== requestId) {
        return null;
      }

      setCafe(null);
      setCafeError(getCafePageErrorMessage(requestError));

      return null;
    } finally {
      if (cafeRequestIdRef.current === requestId) {
        setIsCafeLoading(false);
      }
    }
  }, [decodedCafePageId]);

  const loadCafeBlogs = useCallback(
    async (
      idOverride?: string,
      cursorOverride?: string | null,
      options: LoadOptions = {},
    ) => {
      const idToFetch = idOverride || decodedCafePageId;
      const requestId = blogsRequestIdRef.current + 1;
      blogsRequestIdRef.current = requestId;
      const isFirstPage = !cursorOverride;
      const preserveExisting = options.preserveExisting ?? false;

      if (!idToFetch) {
        setCafePosts([]);
        setIsBlogsLoading(false);
        setBlogsError(null);
        setCursor(null);
        setHasMore(false);
        return;
      }

      setIsBlogsLoading(true);
      setBlogsError(null);

      if (isFirstPage && !preserveExisting) {
        setCafePosts([]);
        setCursor(null);
        setHasMore(false);
      }

      try {
        const response = await getBlogsByCafePageId(idToFetch, {
          cursor: cursorOverride,
          size: CAFE_BLOG_PAGE_SIZE,
        });

        if (blogsRequestIdRef.current !== requestId) {
          return;
        }

        const nextPosts = mapBlogResponsesToFeedPosts(response.items);

        setCafePosts((currentPosts) =>
          isFirstPage ? nextPosts : [...currentPosts, ...nextPosts],
        );
        setCursor(response.nextCursor);
        setHasMore(Boolean(response.hasMore));
      } catch (requestError) {
        if (blogsRequestIdRef.current !== requestId) {
          return;
        }

        if (isFirstPage) {
          setCafePosts([]);
        }

        setBlogsError(
          requestError instanceof ApiError
            ? requestError.message
            : "Unable to load cafe posts.",
        );
        setCursor(null);
        setHasMore(false);
      } finally {
        if (blogsRequestIdRef.current === requestId) {
          setIsBlogsLoading(false);
        }
      }
    },
    [decodedCafePageId],
  );

  useEffect(() => {
    void (async () => {
      const loadedCafe = await loadCafe(decodedCafePageId);

      if (loadedCafe && loadedCafe.pageActive !== false) {
        void loadCafeBlogs(loadedCafe.id);
      }
    })();
  }, [decodedCafePageId, loadCafe, loadCafeBlogs]);

  const handleBfcacheRestore = useCallback(() => {
    void (async () => {
      const loadedCafe = await loadCafe(decodedCafePageId, {
        preserveExisting: true,
      });

      if (loadedCafe && loadedCafe.pageActive !== false) {
        void loadCafeBlogs(loadedCafe.id, null, {
          preserveExisting: true,
        });
      }
    })();
  }, [decodedCafePageId, loadCafe, loadCafeBlogs]);

  useBfcacheRestoreEffect(handleBfcacheRestore);

  const handleCafeLikeStateChange = useCallback(
    (nextState: { isLiked: boolean; likeCount: number }) => {
      setCafe((currentCafe) =>
        currentCafe
          ? {
              ...currentCafe,
              isLiked: nextState.isLiked,
              likeCount: nextState.likeCount,
              photoCount: `${formatCount(nextState.likeCount)} likes`,
            }
          : currentCafe,
      );
    },
    [],
  );

  if (isCafeLoading && !cafe) {
    return <CafePageLoadingState />;
  }

  if (cafeError || !cafe) {
    return <CafePageErrorState message={cafeError ?? "Cafe not found."} />;
  }

  if (cafe.pageActive === false) {
    if (isAuthLoading) {
      return <CafePageLoadingState />;
    }

    return (
      <CafePageExpiredModal
        isOwner={Boolean(cafe.ownerUserId && cafe.ownerUserId === currentUser?.userId)}
      />
    );
  }

  return (
    <div className="space-y-6">
      <CafePage
        cafe={cafe}
        cafePosts={cafePosts}
        hasMorePosts={hasMore}
        isPostsLoading={isBlogsLoading}
        menu={mockCafeMenu}
        onCafeLikeStateChange={handleCafeLikeStateChange}
        onLoadMorePosts={() => {
          void loadCafeBlogs(decodedCafePageId, cursor);
        }}
        postsErrorMessage={blogsError}
      />
    </div>
  );
}
