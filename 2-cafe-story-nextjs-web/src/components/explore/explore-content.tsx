"use client";

import { Suspense, useCallback, useEffect, useState } from "react";
import { ExploreSearchHeader } from "@/components/cafe/explore-search-header";
import { ExploreTabs } from "@/components/explore/explore-tabs";
import { ExploreRecommendationGrid } from "@/components/explore/explore-recommendation-grid";
import { ExploreBlogGrid } from "@/components/explore/explore-blog-grid";
import { mapBlogResponsesToFeedPosts } from "@/features/blogs/blog-feed-adapter";
import { getBlogById, getTrendingBlogs } from "@/lib/api/blogs";
import { getAllCafePages, getTopCafePages } from "@/lib/api/cafes";
import {
  getCafePageRecommendations,
  getMixedRecommendations,
  getReviewerRecommendations,
} from "@/lib/api/recommendations";
import { getTopReviewers } from "@/lib/api/reviewers";
import type { CafePageRankingResponse, CafePageResponse } from "@/types/cafe";
import type { FeedPost } from "@/types/feed";
import type { RecommendationCardResponse } from "@/types/recommendation";
import type { ReviewerResponse } from "@/types/reviewer";
import type { ExploreTab } from "@/components/explore/explore-tabs";

type RecommendationState = Record<
  Exclude<ExploreTab, "trending">,
  RecommendationCardResponse[]
>;

type ErrorState = Partial<Record<ExploreTab, string>>;

const initialRecommendations: RecommendationState = {
  all: [],
  cafes: [],
  reviewers: [],
};

function isCafeOrReviewer(item: RecommendationCardResponse) {
  return item.targetType === "CAFE_PAGE" || item.targetType === "REVIEWER";
}

function cafeToRecommendation(cafe: CafePageRankingResponse | CafePageResponse): RecommendationCardResponse {
  return {
    targetId: cafe.id,
    targetType: "CAFE_PAGE",
    fullName: cafe.name,
    username: null,
    avatar: cafe.avatarUrl,
    city: cafe.regionCity ?? null,
    reason: cafe.description ?? null,
    userId: null,
  };
}

function reviewerToRecommendation(reviewer: ReviewerResponse): RecommendationCardResponse {
  return {
    targetId: reviewer.reviewerId,
    targetType: "REVIEWER",
    fullName: reviewer.name,
    username: null,
    avatar: reviewer.avatar,
    city: null,
    reason: reviewer.badge ? `${reviewer.badge} reviewer` : null,
    userId: reviewer.userId,
  };
}

export function ExploreContent() {
  const [activeTab, setActiveTab] = useState<ExploreTab>("all");
  const [recommendations, setRecommendations] =
    useState<RecommendationState>(initialRecommendations);
  const [trendingPosts, setTrendingPosts] = useState<FeedPost[]>([]);
  const [loadedTabs, setLoadedTabs] = useState<
    Partial<Record<ExploreTab, boolean>>
  >({});
  const [errors, setErrors] = useState<ErrorState>({});
  const [isLoading, setIsLoading] = useState(false);

  const loadRecommendations = useCallback(
    async (tab: Exclude<ExploreTab, "trending">) => {
      setIsLoading(true);

      try {
        let data: RecommendationCardResponse[];

        if (tab === "cafes") {
          try {
            data = await getCafePageRecommendations(0, 20);
          } catch {
            data = [];
          }
          if (data.length === 0) {
            try {
              const cafes = await getTopCafePages({ size: 20 });
              data = cafes.map(cafeToRecommendation);
            } catch {
              data = [];
            }
          }
          if (data.length === 0) {
            const cafes = await getAllCafePages();
            data = cafes.slice(0, 20).map(cafeToRecommendation);
          }
        } else if (tab === "reviewers") {
          try {
            data = await getReviewerRecommendations(0, 20);
          } catch {
            data = [];
          }
          if (data.length === 0) {
            const reviewers = await getTopReviewers(20);
            data = reviewers.map(reviewerToRecommendation);
          }
        } else {
          try {
            data = (await getMixedRecommendations(0, 30)).filter(isCafeOrReviewer);
          } catch {
            data = [];
          }
          if (data.length === 0) {
            const [topCafesResult, reviewersResult] = await Promise.allSettled([
              getTopCafePages({ size: 15 }),
              getTopReviewers(15),
            ]);
            const topCafes = topCafesResult.status === "fulfilled" ? topCafesResult.value : [];
            const reviewers = reviewersResult.status === "fulfilled" ? reviewersResult.value : [];
            data = [
              ...topCafes.map(cafeToRecommendation),
              ...reviewers.map(reviewerToRecommendation),
            ];
          }
          if (data.length === 0) {
            const allCafes = await getAllCafePages();
            data = allCafes.slice(0, 20).map(cafeToRecommendation);
          }
        }

        setRecommendations((prev) => ({ ...prev, [tab]: data }));
        setLoadedTabs((prev) => ({ ...prev, [tab]: true }));
        setErrors((prev) => ({ ...prev, [tab]: undefined }));
      } catch (err) {
        setErrors((prev) => ({
          ...prev,
          [tab]:
            err instanceof Error
              ? err.message
              : "Unable to load recommendations.",
        }));
      } finally {
        setIsLoading(false);
      }
    },
    [],
  );

  const loadTrending = useCallback(async () => {
    setIsLoading(true);

    try {
      const trending = await getTrendingBlogs({
        page: 0,
        size: 12,
        windowType: "HOUR_24",
      });

      const blogResults = await Promise.allSettled(
        trending.map((item) => getBlogById(item.blogId)),
      );

      const blogs = blogResults
        .filter(
          (r): r is PromiseFulfilledResult<Awaited<ReturnType<typeof getBlogById>>> =>
            r.status === "fulfilled",
        )
        .map((r) => r.value);

      setTrendingPosts(mapBlogResponsesToFeedPosts(blogs));
      setLoadedTabs((prev) => ({ ...prev, trending: true }));
      setErrors((prev) => ({ ...prev, trending: undefined }));
    } catch (err) {
      setErrors((prev) => ({
        ...prev,
        trending:
          err instanceof Error ? err.message : "Unable to load trending posts.",
      }));
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    if (loadedTabs[activeTab]) {
      return;
    }

    if (activeTab === "trending") {
      void loadTrending();
    } else {
      void loadRecommendations(activeTab);
    }
  }, [activeTab, loadedTabs, loadRecommendations, loadTrending]);

  const tabTitle: Record<ExploreTab, string> = {
    all: "Recommended for you",
    cafes: "Suggested cafes",
    reviewers: "Explore reviewers",
    trending: "Trending posts",
  };

  return (
    <div className="w-full overflow-x-clip px-4 py-12 sm:px-8 xl:px-12">
      <div className="mx-auto max-w-[1140px] space-y-8">
        <Suspense>
          <ExploreSearchHeader />
        </Suspense>

        <ExploreTabs activeTab={activeTab} onChange={setActiveTab} />

        <section>
          <h2 className="mb-5 text-base font-black text-foreground">
            {tabTitle[activeTab]}
          </h2>

          {activeTab === "trending" ? (
            <ExploreBlogGrid
              error={errors.trending}
              isLoading={isLoading && trendingPosts.length === 0}
              posts={trendingPosts}
            />
          ) : (
            <ExploreRecommendationGrid
              emptyDescription="New recommendations will appear as CafeStory learns what you like."
              emptyTitle="No recommendations yet"
              error={errors[activeTab]}
              isLoading={isLoading && recommendations[activeTab].length === 0}
              items={recommendations[activeTab]}
            />
          )}
        </section>
      </div>
    </div>
  );
}
