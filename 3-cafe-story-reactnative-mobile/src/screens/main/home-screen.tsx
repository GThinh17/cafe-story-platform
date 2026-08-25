import { useNavigation } from "@react-navigation/native";
import { Text } from "react-native";
import type { BottomTabNavigationProp } from "@react-navigation/bottom-tabs";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { Send } from "lucide-react-native";
import { useCallback, useEffect, useRef, useState } from "react";
import {
  RefreshControl, ScrollView, StyleSheet, type NativeScrollEvent, type NativeSyntheticEvent } from "react-native";
import {
  BlogFeedCard,
  EmptyState,
  FeedCardSkeletonList,
  Screen,
  ShareTopBar,
  SponsoredCafeCard,
  StoryRail,
} from "../../components";
import { useAuth } from "../../features/auth";
import {
  getBlogLikesByUser,
  getBlogSavesByUser,
  getFollowingByUserId,
  getFollowingTargetsByUserId,
  getMixedFeed,
  recordFeedImpressions,
} from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import {
  logPerformanceMetric,
  performanceTimestamp,
} from "../../utils/performance";
import { routes } from "../../navigation";
import type { MainTabParamList, RootStackParamList } from "../../navigation";
import type {
  BlogFeedResponse,
  FeedItemResponse,
  FeedResponse,
  BlogLikeResponse,
  BlogSaveResponse,
  FeedImpressionItemRequest,
  FollowTargetResponse,
  StoryItem,
  UserFollowResponse,
} from "../../types";
import { t } from "../../features/i18n";

const INITIAL_FEED_PAGE_SIZE = 10;
const LOAD_MORE_FEED_PAGE_SIZE = 5;
const LOAD_MORE_THRESHOLD = 720;

function applyViewerState(
  blogs: BlogFeedResponse[],
  following: UserFollowResponse[],
  likes: BlogLikeResponse[],
  saves: BlogSaveResponse[],
  currentUserId?: string,
) {
  const followingUserIds = new Set(
    following.map((item) => item.followingUserId),
  );
  const likedBlogIds = new Set(likes.map((item) => item.blogId));
  const savedBlogIds = new Set(saves.map((item) => item.blogId));

  return blogs.map((blog) => ({
    ...blog,
    isFollow:
      blog.authorUserId === currentUserId
        ? false
        : followingUserIds.has(blog.authorUserId),
    isLike: likedBlogIds.has(blog.blogId),
    isSave: savedBlogIds.has(blog.blogId),
  }));
}

function getFeedItemKey(item: FeedItemResponse) {
  if (item.blog?.blogId) {
    return `blog:${item.blog.blogId}`;
  }

  if (item.ad?.campaignId) {
    return `ad:${item.ad.campaignId}:${item.position ?? "unknown"}`;
  }

  return `${item.itemType}:${item.position ?? "unknown"}:${item.trackingToken ?? "item"}`;
}

function mergeUniqueFeedItems(
  currentItems: FeedItemResponse[],
  nextItems: FeedItemResponse[],
) {
  const seenKeys = new Set(currentItems.map(getFeedItemKey));
  const uniqueNextItems = nextItems.filter((item) => {
    const key = getFeedItemKey(item);
    if (seenKeys.has(key)) {
      return false;
    }

    seenKeys.add(key);
    return true;
  });

  return [...currentItems, ...uniqueNextItems];
}

function getInitials(value: string | null | undefined) {
  return (value || "CS")
    .split(/\s|_/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join("") || "CS";
}

function followingTargetToStory(target: FollowTargetResponse): StoryItem | null {
  const targetId = target.cafePageId ?? target.userId ?? target.targetId;

  if (!targetId) {
    return null;
  }

  const label =
    target.targetType === "CAFE_PAGE"
      ? target.pageName || target.displayName || "Cafe page"
      : target.username || target.userFullName || target.displayName || "CafeStory";

  return {
    avatarUri: target.avatar,
    id: `${target.targetType}:${targetId}`,
    initials: getInitials(label),
    label,
    targetId,
    targetType: target.targetType,
  };
}

export function HomeScreen() {
  const navigation =
    useNavigation<
      BottomTabNavigationProp<MainTabParamList, typeof routes.home> &
        NativeStackNavigationProp<RootStackParamList>
    >();
  const { user } = useAuth();
  const scrollViewRef = useRef<ScrollView | null>(null);
  const isFetchingRef = useRef(false);
  const isPrefetchingRef = useRef(false);
  const prefetchGenerationRef = useRef(0);
  const recordedImpressionBlogIdsRef = useRef<Set<string>>(new Set());
  const feedRenderStartedAtRef = useRef<number | null>(null);
  const feedRenderOperationRef = useRef<"initial" | "refresh" | null>(null);
  const mediaStartedAtRef = useRef<number | null>(null);
  const mediaOperationRef = useRef<"initial" | "refresh" | null>(null);
  const [feedItems, setFeedItems] = useState<FeedItemResponse[]>([]);
  const [error, setError] = useState("");
  const [loadMoreError, setLoadMoreError] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [nextCursor, setNextCursor] = useState<string | null>(null);
  const [hasMore, setHasMore] = useState(true);
  const [isPrefetching, setIsPrefetching] = useState(false);
  const [prefetchedFeedItems, setPrefetchedFeedItems] = useState<FeedItemResponse[]>([]);
  const [prefetchedCursor, setPrefetchedCursor] = useState<string | null>(null);
  const [prefetchedNextCursor, setPrefetchedNextCursor] = useState<string | null>(null);
  const [prefetchFailedCursor, setPrefetchFailedCursor] = useState<string | null>(null);
  const [shouldAppendPrefetch, setShouldAppendPrefetch] = useState(false);
  const [followingTargets, setFollowingTargets] = useState<FollowTargetResponse[]>([]);

  useEffect(() => {
    if (!feedItems.length || feedRenderStartedAtRef.current === null) {
      return;
    }

    const startedAt = feedRenderStartedAtRef.current;
    const operation = feedRenderOperationRef.current ?? "initial";
    feedRenderStartedAtRef.current = null;
    feedRenderOperationRef.current = null;
    const frame = requestAnimationFrame(() => {
      logPerformanceMetric("mobile.home.first_feed_render", startedAt, {
        itemCount: feedItems.length,
        operation,
      });
    });
    return () => cancelAnimationFrame(frame);
  }, [feedItems]);

  useEffect(() => {
    recordedImpressionBlogIdsRef.current.clear();
  }, [user?.userId]);

  const loadFollowingTargets = useCallback(async () => {
    if (!user?.userId) {
      setFollowingTargets([]);
      return;
    }

    try {
      const response = await getFollowingTargetsByUserId(user.userId, "ALL");
      setFollowingTargets(response.slice(0, 14));
    } catch {
      setFollowingTargets([]);
    }
  }, [user?.userId]);

  const enrichBlogsWithViewerState = useCallback(async (
    blogs: BlogFeedResponse[],
  ) => {
    if (!user?.userId) {
      return blogs;
    }

    const [following, likes, saves] = await Promise.all([
      getFollowingByUserId(user.userId).catch(() => []),
      getBlogLikesByUser(user.userId).catch(() => []),
      getBlogSavesByUser(user.userId).catch(() => []),
    ]);

    return applyViewerState(blogs, following, likes, saves, user.userId);
  }, [user?.userId]);

  const enrichFeedResponse = useCallback(async (
    response: FeedResponse,
  ): Promise<FeedResponse> => {
    const items = response.items ?? [];
    const blogItems = items.filter((item) => item.blog);
    const enrichedBlogs = await enrichBlogsWithViewerState(
      blogItems.map((item) => item.blog as BlogFeedResponse),
    );
    const enrichedBlogsById = new Map(
      enrichedBlogs.map((blog) => [blog.blogId, blog]),
    );

    return {
      ...response,
      items: items.map((item) => {
        if (!item.blog) {
          return item;
        }

        return {
          ...item,
          blog: enrichedBlogsById.get(item.blog.blogId) ?? item.blog,
        };
      }),
    };
  }, [enrichBlogsWithViewerState]);

  const fetchFeedPage = useCallback(async (
    cursorToLoad: string | null,
    pageSize: number,
    bypassCache = false,
  ) => {
    const response = await getMixedFeed({
      bypassCache,
      cursor: cursorToLoad,
      size: pageSize,
    });

    return enrichFeedResponse(response);
  }, [enrichFeedResponse]);

  const recordVisibleFeedImpressions = useCallback((
    items: FeedItemResponse[],
  ) => {
    if (!user?.userId) {
      return;
    }

    const impressionItems: FeedImpressionItemRequest[] = [];
    items.forEach((item, index) => {
      const blogId = item.blog?.blogId;
      if (!blogId || recordedImpressionBlogIdsRef.current.has(blogId)) {
        return;
      }

      impressionItems.push({
        blogId,
        position: item.position ?? index,
      });
    });

    if (!impressionItems.length) {
      return;
    }

    void recordFeedImpressions({ items: impressionItems })
      .then(() => {
        impressionItems.forEach((item) => {
          recordedImpressionBlogIdsRef.current.add(item.blogId);
        });
      })
      .catch(() => {
        // Impression tracking should not block feed rendering.
      });
  }, [user?.userId]);

  const prefetchFeedPage = useCallback(async (cursorToPrefetch: string | null) => {
    if (isPrefetchingRef.current || !cursorToPrefetch) {
      return;
    }

    const generation = prefetchGenerationRef.current;
    isPrefetchingRef.current = true;
    setIsPrefetching(true);
    setPrefetchFailedCursor(null);

    try {
      const response = await fetchFeedPage(
        cursorToPrefetch,
        LOAD_MORE_FEED_PAGE_SIZE,
      );

      if (prefetchGenerationRef.current !== generation) {
        return;
      }

      setPrefetchedFeedItems(response.items ?? []);
      setPrefetchedCursor(cursorToPrefetch);
      setPrefetchedNextCursor(response.nextCursor ?? null);
    } catch {
      if (prefetchGenerationRef.current === generation) {
        setPrefetchFailedCursor(cursorToPrefetch);
      }
    } finally {
      isPrefetchingRef.current = false;
      setIsPrefetching(false);
    }
  }, [fetchFeedPage]);

  const loadFeed = useCallback(async ({
    append = false,
    cursorToLoad = null,
    refreshing = false,
  }: {
    append?: boolean;
    cursorToLoad?: string | null;
    refreshing?: boolean;
  } = {}) => {
    if (isFetchingRef.current) {
      return;
    }

    isFetchingRef.current = true;
    const requestGeneration = append
      ? prefetchGenerationRef.current
      : prefetchGenerationRef.current + 1;

    if (!append) {
      const operation = refreshing ? "refresh" : "initial";
      const startedAt = performanceTimestamp();
      feedRenderStartedAtRef.current = startedAt;
      feedRenderOperationRef.current = operation;
      mediaStartedAtRef.current = startedAt;
      mediaOperationRef.current = operation;
      prefetchGenerationRef.current = requestGeneration;
      setPrefetchedFeedItems([]);
      setPrefetchedCursor(null);
      setPrefetchedNextCursor(null);
      setPrefetchFailedCursor(null);
      setShouldAppendPrefetch(false);
    }

    if (refreshing) {
      setIsRefreshing(true);
    } else if (append) {
      setIsLoadingMore(true);
    } else {
      setIsLoading(true);
    }

    if (!append) {
      setError("");
    }
    setLoadMoreError("");

    try {
      const pageSize = append ? LOAD_MORE_FEED_PAGE_SIZE : INITIAL_FEED_PAGE_SIZE;
      const response = await fetchFeedPage(cursorToLoad, pageSize, refreshing);
      const responseItems = response.items ?? [];
      recordVisibleFeedImpressions(responseItems);

      setFeedItems((currentItems) =>
        append
          ? mergeUniqueFeedItems(currentItems, responseItems)
          : responseItems,
      );
      const nextCursorFromResponse = response.nextCursor ?? null;
      const canLoadMore = Boolean(response.hasMore && nextCursorFromResponse);

      setNextCursor(nextCursorFromResponse);
      setHasMore(canLoadMore);

      if (canLoadMore) {
        void prefetchFeedPage(nextCursorFromResponse);
      }
    } catch (requestError) {
      if (append) {
        setLoadMoreError("Unable to load more posts.");
      } else {
        setError(t("Unable to load feed. Pull down to try again."));
      }
    } finally {
      isFetchingRef.current = false;
      setIsLoading(false);
      setIsLoadingMore(false);
      setIsRefreshing(false);
    }
  }, [fetchFeedPage, prefetchFeedPage, recordVisibleFeedImpressions]);

  const appendPrefetchedFeedItems = useCallback(() => {
    if (!nextCursor || prefetchedCursor !== nextCursor) {
      return false;
    }

    const bufferedItems = prefetchedFeedItems;
    const nextCursorFromPrefetch = prefetchedNextCursor;
    const canLoadMore = Boolean(nextCursorFromPrefetch);

    setFeedItems((currentItems) => mergeUniqueFeedItems(currentItems, bufferedItems));
    setNextCursor(nextCursorFromPrefetch);
    setHasMore(canLoadMore);
    setPrefetchedFeedItems([]);
    setPrefetchedCursor(null);
    setPrefetchedNextCursor(null);
    setPrefetchFailedCursor(null);
    setShouldAppendPrefetch(false);
    setIsLoadingMore(false);
    setLoadMoreError("");
    recordVisibleFeedImpressions(bufferedItems);

    if (canLoadMore) {
      void prefetchFeedPage(nextCursorFromPrefetch);
    }

    return true;
  }, [
    nextCursor,
    prefetchedCursor,
    prefetchedFeedItems,
    prefetchedNextCursor,
    prefetchFeedPage,
    recordVisibleFeedImpressions,
  ]);

  useEffect(() => {
    if (!shouldAppendPrefetch) {
      return;
    }

    if (appendPrefetchedFeedItems()) {
      return;
    }

    if (nextCursor && prefetchFailedCursor === nextCursor) {
      setShouldAppendPrefetch(false);
      setIsLoadingMore(false);
      setLoadMoreError("Unable to load more posts.");
    }
  }, [
    appendPrefetchedFeedItems,
    nextCursor,
    prefetchFailedCursor,
    shouldAppendPrefetch,
  ]);

  useEffect(() => {
    void loadFeed();
  }, [loadFeed]);

  useEffect(() => {
    void loadFollowingTargets();
  }, [loadFollowingTargets]);

  useEffect(() => {
    const unsubscribe = navigation.addListener("tabPress", () => {
      scrollViewRef.current?.scrollTo({ animated: true, y: 0 });
    });

    return unsubscribe;
  }, [navigation]);

  const loadNextPage = useCallback(() => {
    if (!hasMore || isLoading || isRefreshing || isLoadingMore) {
      return;
    }

    if (appendPrefetchedFeedItems()) {
      return;
    }

    if (isPrefetching || isPrefetchingRef.current) {
      setIsLoadingMore(true);
      setShouldAppendPrefetch(true);
      return;
    }

    void loadFeed({
      append: true,
      cursorToLoad: nextCursor,
    });
  }, [
    appendPrefetchedFeedItems,
    hasMore,
    isLoading,
    isLoadingMore,
    isPrefetching,
    isRefreshing,
    loadFeed,
    nextCursor,
  ]);

  const handleFeedScroll = useCallback((
    event: NativeSyntheticEvent<NativeScrollEvent>,
  ) => {
    const { contentOffset, contentSize, layoutMeasurement } = event.nativeEvent;
    const distanceFromBottom =
      contentSize.height - (contentOffset.y + layoutMeasurement.height);

    if (distanceFromBottom <= LOAD_MORE_THRESHOLD) {
      loadNextPage();
    }
  }, [loadNextPage]);

  const stories: StoryItem[] = [
    {
      avatarUri: user?.userAvatar,
      id: "story-me",
      initials: user?.userName?.slice(0, 2).toUpperCase() ?? "ME",
      isSelf: true,
      label: t("story.selfLabel"),
      targetType: "CREATE_POST",
    },
    ...followingTargets
      .map(followingTargetToStory)
      .filter((story): story is StoryItem => Boolean(story)),
  ];

  const handleStoryPress = useCallback((story: StoryItem) => {
    if (story.targetType === "CREATE_POST") {
      navigation.navigate(routes.create);
      return;
    }

    if (story.targetType === "CAFE_PAGE" && story.targetId) {
      navigation.navigate(routes.cafeDetail, {
        cafeId: story.targetId,
      });
      return;
    }

    if (story.targetId) {
      navigation.navigate(routes.otherUserProfile, {
        userId: story.targetId,
        userName: story.label,
      });
    }
  }, [navigation]);

  const handleFirstMediaLoad = useCallback(() => {
    if (mediaStartedAtRef.current === null) {
      return;
    }
    const startedAt = mediaStartedAtRef.current;
    const operation = mediaOperationRef.current ?? "initial";
    mediaStartedAtRef.current = null;
    mediaOperationRef.current = null;
    logPerformanceMetric("mobile.home.first_media_load", startedAt, { operation });
  }, []);

  const renderFeedItem = useCallback((item: FeedItemResponse, index: number) => {
    if (item.itemType === "SPONSORED_CAFE" && item.ad) {
      return (
        <SponsoredCafeCard
          ad={item.ad}
          key={getFeedItemKey(item)}
          onOpenCafePage={(cafePageId) =>
            navigation.navigate(routes.cafeDetail, {
              cafeId: cafePageId,
            })
          }
        />
      );
    }

    if (item.blog) {
      return (
        <BlogFeedCard
          blog={item.blog}
          key={getFeedItemKey(item)}
          onFirstMediaLoad={index === 0 ? handleFirstMediaLoad : undefined}
        />
      );
    }

    return null;
  }, [handleFirstMediaLoad, navigation]);

  return (
    <Screen padded={false}>
      <ShareTopBar
        onRightPress={() => navigation.navigate(routes.conversations)}
        rightAccessibilityLabel="Open messages"
        rightIcon={Send}
      />
      <ScrollView
        onScroll={handleFeedScroll}
        ref={scrollViewRef}
        scrollEventThrottle={16}
        contentContainerStyle={styles.feedContent}
        refreshControl={
          <RefreshControl
            onRefresh={() => {
              void loadFeed({ refreshing: true });
              void loadFollowingTargets();
            }}
            refreshing={isRefreshing}
          />
        }
        showsVerticalScrollIndicator={false}
      >
        <StoryRail onStoryPress={handleStoryPress} stories={stories} />
        {isLoading ? (
          <FeedCardSkeletonList />
        ) : error ? (
          <EmptyState description={error} title={t("Feed unavailable")} />
        ) : feedItems.length ? (
          <>
            {feedItems.map(renderFeedItem)}
            {isLoadingMore ? (
              <FeedCardSkeletonList count={2} />
            ) : loadMoreError ? (
              <Text style={styles.loadMoreError}>{loadMoreError}</Text>
            ) : null}
          </>
        ) : (
          <EmptyState
            description={t("New cafe stories will appear here when they are ready.")}
            title={t("No posts yet")}
          />
        )}
      </ScrollView>
    </Screen>
  );
}

const styles = StyleSheet.create({
  feedContent: {
    paddingBottom: 112,
  },
  loadMoreError: {
    color: colors.muted,
    fontSize: typography.label,
    paddingHorizontal: spacing.xl,
    paddingVertical: spacing.lg,
    textAlign: "center",
  },
});
