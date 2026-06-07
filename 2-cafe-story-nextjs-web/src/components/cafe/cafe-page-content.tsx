"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useParams } from "next/navigation";
import { CafePage } from "@/components/cafe/cafe-page";
import { mapBlogResponsesToFeedPosts } from "@/features/blogs/blog-feed-adapter";
import { useBfcacheRestoreEffect } from "@/hooks/use-bfcache-restore";
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
    .join(", ") || cafe.address;
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
      <div className="h-[240px] animate-pulse rounded-md border border-line-soft bg-surface-muted sm:h-[360px]" />
      <div className="flex gap-5">
        <div className="h-24 w-24 animate-pulse rounded-full bg-surface-muted sm:h-32 sm:w-32" />
        <div className="min-w-0 flex-1 space-y-3 pt-4">
          <div className="h-8 w-2/3 animate-pulse rounded-sm bg-surface-muted" />
          <div className="h-4 w-1/2 animate-pulse rounded-sm bg-surface-muted" />
          <div className="h-4 w-3/4 animate-pulse rounded-sm bg-surface-muted" />
        </div>
      </div>
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

      if (loadedCafe) {
        void loadCafeBlogs(loadedCafe.id);
      }
    })();
  }, [decodedCafePageId, loadCafe, loadCafeBlogs]);

  const handleBfcacheRestore = useCallback(() => {
    void (async () => {
      const loadedCafe = await loadCafe(decodedCafePageId, {
        preserveExisting: true,
      });

      if (loadedCafe) {
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
