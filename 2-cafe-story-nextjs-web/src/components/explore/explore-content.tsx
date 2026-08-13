"use client";

import { Suspense, useCallback, useEffect, useRef, useState } from "react";
import { useSearchParams } from "next/navigation";
import { ExploreSearchHeader } from "@/components/cafe/explore-search-header";
import { ExploreTabs } from "@/components/explore/explore-tabs";
import { ExploreCafeCard } from "@/components/explore/explore-cafe-card";
import { ExploreReviewerCard } from "@/components/explore/explore-reviewer-card";
import { ExploreBlogGrid } from "@/components/explore/explore-blog-grid";
import { PostCommentsModal } from "@/components/feed/post-comments-modal";
import { mapBlogTrendingToFeedPosts } from "@/features/blogs/blog-feed-adapter";
import { getTrendingBlogs } from "@/lib/api/blogs";
import { getAllCafePages, getTopCafePages } from "@/lib/api/cafes";
import {
  getAllActiveReviewers,
  getReviewersByRegion,
  getTopReviewers,
  searchReviewers,
} from "@/lib/api/reviewers";
import { useI18n } from "@/components/providers/locale-provider";
import { useCurrentUser } from "@/hooks/use-current-user";
import type { CafePageRankingResponse, CafePageResponse } from "@/types/cafe";
import type { FeedPost } from "@/types/feed";
import type { ReviewerBadge, ReviewerDiscoveryResponse, ReviewerResponse } from "@/types/reviewer";
import type { AuthUser } from "@/types/auth";
import type { ExploreTab } from "@/components/explore/explore-tabs";

type ExploreCafeItem = {
  id: string;
  name: string;
  avatar: string | null;
  city: string | null;
  isFollowing: boolean;
};

type ExploreReviewerItem = {
  reviewerId: string;
  userId: string;
  username: string | null;
  fullName: string | null;
  avatar: string | null;
  badge: ReviewerBadge | string | null;
  isFollowing: boolean;
};

function cafeRankingToItem(cafe: CafePageRankingResponse): ExploreCafeItem {
  return {
    id: cafe.id,
    name: cafe.name,
    avatar: cafe.avatarUrl ?? null,
    city: cafe.regionCity ?? null,
    isFollowing: cafe.isFollowing ?? false,
  };
}

function cafeResponseToItem(cafe: CafePageResponse): ExploreCafeItem {
  return {
    id: cafe.id,
    name: cafe.name,
    avatar: cafe.avatarUrl ?? null,
    city: cafe.regionCity ?? null,
    isFollowing: cafe.isFollowing ?? false,
  };
}

function discoveryToReviewerItem(r: ReviewerDiscoveryResponse): ExploreReviewerItem {
  return {
    reviewerId: r.reviewerId,
    userId: r.userId,
    username: r.userName ?? null,
    fullName: r.userFullName ?? null,
    avatar: r.avatar ?? null,
    badge: r.badge ?? null,
    isFollowing: r.isFollowing,
  };
}

function reviewerResponseToItem(r: ReviewerResponse): ExploreReviewerItem {
  return {
    reviewerId: r.reviewerId,
    userId: r.userId,
    username: null,
    fullName: r.name ?? null,
    avatar: r.avatar ?? null,
    badge: r.badge ?? null,
    isFollowing: r.isFollowing ?? false,
  };
}

function buildRegionCascade(user: AuthUser | null) {
  const params: Array<{ area?: string; city?: string; province?: string }> = [];
  if (user?.regionArea) params.push({ area: user.regionArea });
  if (user?.regionCity) params.push({ city: user.regionCity });
  if (user?.regionProvince) params.push({ province: user.regionProvince });
  return params;
}

async function loadCafeData(
  searchQuery: string,
  user: AuthUser | null,
): Promise<ExploreCafeItem[]> {
  if (searchQuery.trim()) {
    try {
      const results = await getAllCafePages({ query: searchQuery.trim() });
      return results.map(cafeResponseToItem);
    } catch {
      return [];
    }
  }

  for (const regionParam of buildRegionCascade(user)) {
    try {
      const cafes = await getTopCafePages({ ...regionParam, size: 20 });
      if (cafes.length > 0) return cafes.map(cafeRankingToItem);
    } catch {
      /* try next */
    }
  }

  try {
    const cafes = await getTopCafePages({ size: 20 });
    if (cafes.length > 0) return cafes.map(cafeRankingToItem);
  } catch {
    /* try next */
  }

  try {
    const cafes = await getAllCafePages({ status: "ACTIVE" });
    return cafes.slice(0, 20).map(cafeResponseToItem);
  } catch {
    return [];
  }
}

async function loadReviewerData(
  searchQuery: string,
  user: AuthUser | null,
): Promise<ExploreReviewerItem[]> {
  if (searchQuery.trim()) {
    try {
      const results = await searchReviewers(searchQuery.trim());
      return results.map(reviewerResponseToItem);
    } catch {
      return [];
    }
  }

  for (const regionParam of buildRegionCascade(user)) {
    try {
      const reviewers = await getReviewersByRegion({ ...regionParam, size: 20 });
      if (reviewers.length > 0) return reviewers.map(discoveryToReviewerItem);
    } catch {
      /* try next */
    }
  }

  try {
    const reviewers = await getTopReviewers(0, 20);
    if (reviewers.length > 0) return reviewers.map(discoveryToReviewerItem);
  } catch {
    /* try next */
  }

  try {
    const reviewers = await getAllActiveReviewers();
    return reviewers.slice(0, 20).map(reviewerResponseToItem);
  } catch {
    return [];
  }
}

function SkeletonCard() {
  return (
    <div className="flex items-center gap-3 py-3">
      <div className="size-12 shrink-0 animate-pulse rounded-full bg-surface-muted" />
      <div className="min-w-0 flex-1 space-y-2">
        <div className="h-3.5 w-3/4 animate-pulse rounded bg-surface-muted" />
        <div className="h-3 w-1/2 animate-pulse rounded bg-surface-muted" />
      </div>
      <div className="h-[30px] w-20 shrink-0 animate-pulse rounded-md bg-surface-muted" />
    </div>
  );
}

function ExploreContentInner() {
  const { t } = useI18n();
  const { user } = useCurrentUser();
  const searchParams = useSearchParams();
  const searchQuery = searchParams.get("query") ?? "";

  const [activeTab, setActiveTab] = useState<ExploreTab>("cafes");
  const [cafes, setCafes] = useState<ExploreCafeItem[]>([]);
  const [reviewers, setReviewers] = useState<ExploreReviewerItem[]>([]);
  const [trendingPosts, setTrendingPosts] = useState<FeedPost[]>([]);
  const [errors, setErrors] = useState<Partial<Record<ExploreTab, string>>>({});
  const [isLoading, setIsLoading] = useState(false);
  const [selectedPost, setSelectedPost] = useState<FeedPost | null>(null);

  // What a tab's results actually depend on. Switching tabs is not one of those
  // things, so each tab records the key it was last loaded for and skips the
  // refetch on the way back. Previously only `trending` was guarded, and the
  // cafe/reviewer region cascade re-ran in full on every tab switch.
  const loadKey = `${searchQuery}|${user?.userId ?? "anon"}`;
  const loadedKeysRef = useRef<Partial<Record<ExploreTab, string>>>({});

  const loadCafes = useCallback(async () => {
    setIsLoading(true);
    try {
      const items = await loadCafeData(searchQuery, user ?? null);
      setCafes(items);
      setErrors((prev) => ({ ...prev, cafes: undefined }));
    } catch (err) {
      setErrors((prev) => ({
        ...prev,
        cafes: err instanceof Error ? err.message : t("explore.cafes.loadError"),
      }));
    } finally {
      setIsLoading(false);
    }
  }, [searchQuery, t, user]);

  const loadReviewers = useCallback(async () => {
    setIsLoading(true);
    try {
      const items = await loadReviewerData(searchQuery, user ?? null);
      setReviewers(items);
      setErrors((prev) => ({ ...prev, reviewers: undefined }));
    } catch (err) {
      setErrors((prev) => ({
        ...prev,
        reviewers:
          err instanceof Error ? err.message : t("explore.reviewers.loadError"),
      }));
    } finally {
      setIsLoading(false);
    }
  }, [searchQuery, t, user]);

  const loadTrending = useCallback(async () => {
    setIsLoading(true);
    try {
      const trending = await getTrendingBlogs({ page: 0, size: 12, windowType: "HOUR_24" });
      setTrendingPosts(mapBlogTrendingToFeedPosts(trending, t));
      setErrors((prev) => ({ ...prev, trending: undefined }));
    } catch (err) {
      setErrors((prev) => ({
        ...prev,
        trending:
          err instanceof Error ? err.message : t("explore.trending.loadError"),
      }));
    } finally {
      setIsLoading(false);
    }
  }, [t]);

  useEffect(() => {
    if (loadedKeysRef.current[activeTab] === loadKey) {
      return;
    }
    loadedKeysRef.current[activeTab] = loadKey;

    const load =
      activeTab === "trending"
        ? loadTrending
        : activeTab === "cafes"
          ? loadCafes
          : loadReviewers;

    // Clear the marker on failure so coming back to the tab retries instead of
    // leaving the user stuck on an error until they reload the page.
    void load().catch(() => {
      loadedKeysRef.current[activeTab] = undefined;
    });
  }, [activeTab, loadKey, loadCafes, loadReviewers, loadTrending]);

  function handleTabChange(tab: ExploreTab) {
    setActiveTab(tab);
  }

  function handlePostLikeClick(post: FeedPost) {
    setTrendingPosts((prev) =>
      prev.map((p) =>
        p.id === post.id
          ? { ...p, isLiked: !p.isLiked, likeCount: (p.likeCount ?? 0) + (p.isLiked ? -1 : 1) }
          : p,
      ),
    );
  }

  function handleCommentCountChange(postId: string, patch: Pick<FeedPost, "commentCount" | "comments">) {
    setTrendingPosts((prev) =>
      prev.map((p) => (p.id === postId ? { ...p, ...patch } : p)),
    );
  }

  return (
    <div className="w-full overflow-x-clip px-4 py-12 sm:px-8 xl:px-12">
      <div className="mx-auto max-w-[1026px] space-y-8">
        <Suspense>
          <ExploreSearchHeader />
        </Suspense>

        <ExploreTabs activeTab={activeTab} onChange={handleTabChange} />

        <section>
          {activeTab === "trending" ? (
            <ExploreBlogGrid
              error={errors.trending}
              isLoading={isLoading && trendingPosts.length === 0}
              onPostClick={(post) => setSelectedPost(post)}
              posts={trendingPosts}
            />
          ) : activeTab === "cafes" ? (
            isLoading && cafes.length === 0 ? (
              <div className="grid grid-cols-2 gap-x-6">
                {Array.from({ length: 6 }).map((_, i) => <SkeletonCard key={i} />)}
              </div>
            ) : errors.cafes ? (
              <p className="py-16 text-center text-sm text-muted">{errors.cafes}</p>
            ) : cafes.length === 0 ? (
              <p className="py-16 text-center text-sm text-muted">
                {t("explore.cafes.empty")}
              </p>
            ) : (
              <div className="grid grid-cols-2 gap-x-6">
                {cafes.map((cafe) => (
                  <ExploreCafeCard
                    key={cafe.id}
                    avatar={cafe.avatar}
                    city={cafe.city}
                    id={cafe.id}
                    isFollowing={cafe.isFollowing}
                    name={cafe.name}
                  />
                ))}
              </div>
            )
          ) : (
            isLoading && reviewers.length === 0 ? (
              <div className="grid grid-cols-2 gap-x-6">
                {Array.from({ length: 6 }).map((_, i) => <SkeletonCard key={i} />)}
              </div>
            ) : errors.reviewers ? (
              <p className="py-16 text-center text-sm text-muted">{errors.reviewers}</p>
            ) : reviewers.length === 0 ? (
              <p className="py-16 text-center text-sm text-muted">
                {t("explore.reviewers.empty")}
              </p>
            ) : (
              <div className="grid grid-cols-2 gap-x-6">
                {reviewers.map((reviewer) => (
                  <ExploreReviewerCard
                    key={reviewer.reviewerId}
                    avatar={reviewer.avatar}
                    badge={reviewer.badge}
                    fullName={reviewer.fullName}
                    isFollowing={reviewer.isFollowing}
                    reviewerId={reviewer.reviewerId}
                    userId={reviewer.userId}
                    username={reviewer.username}
                  />
                ))}
              </div>
            )
          )}
        </section>
      </div>

      <PostCommentsModal
        currentUser={user ?? null}
        onCommentCountChange={handleCommentCountChange}
        onOpenChange={(open) => { if (!open) setSelectedPost(null); }}
        onPostLikeClick={handlePostLikeClick}
        post={selectedPost}
      />
    </div>
  );
}

export function ExploreContent() {
  return (
    <Suspense>
      <ExploreContentInner />
    </Suspense>
  );
}
