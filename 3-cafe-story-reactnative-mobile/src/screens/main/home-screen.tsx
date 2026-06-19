import { useNavigation } from "@react-navigation/native";
import type { BottomTabNavigationProp } from "@react-navigation/bottom-tabs";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { Send } from "lucide-react-native";
import { useCallback, useEffect, useRef, useState } from "react";
import {
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  View,
  type NativeScrollEvent,
  type NativeSyntheticEvent,
} from "react-native";
import {
  BlogFeedList,
  EmptyState,
  LoadingState,
  Screen,
  ShareTopBar,
  StoryRail,
} from "../../components";
import { useAuth } from "../../features/auth";
import { mockHomeFeedStories } from "../../mocks";
import {
  getBlogFeed,
  getBlogLikesByUser,
  getBlogSavesByUser,
  getFollowingByUserId,
} from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import { routes } from "../../navigation";
import type { MainTabParamList, RootStackParamList } from "../../navigation";
import type {
  BlogFeedResponse,
  BlogLikeResponse,
  BlogSaveResponse,
  StoryItem,
  UserFollowResponse,
} from "../../types";

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

function mergeUniqueBlogs(
  currentBlogs: BlogFeedResponse[],
  nextBlogs: BlogFeedResponse[],
) {
  const seenBlogIds = new Set(currentBlogs.map((blog) => blog.blogId));
  const uniqueNextBlogs = nextBlogs.filter((blog) => {
    if (seenBlogIds.has(blog.blogId)) {
      return false;
    }

    seenBlogIds.add(blog.blogId);
    return true;
  });

  return [...currentBlogs, ...uniqueNextBlogs];
}

function FeedSkeletonFooter() {
  return (
    <View style={styles.skeletonList}>
      {[0, 1].map((item) => (
        <View key={item} style={styles.skeletonCard}>
          <View style={styles.skeletonHeader}>
            <View style={styles.skeletonAvatar} />
            <View style={styles.skeletonHeaderText}>
              <View style={styles.skeletonLineStrong} />
              <View style={styles.skeletonLineShort} />
            </View>
          </View>
          <View style={styles.skeletonLine} />
          <View style={styles.skeletonLineWide} />
        </View>
      ))}
    </View>
  );
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
  const [blogs, setBlogs] = useState<BlogFeedResponse[]>([]);
  const [error, setError] = useState("");
  const [loadMoreError, setLoadMoreError] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [nextLoadPage, setNextLoadPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [isPrefetching, setIsPrefetching] = useState(false);
  const [prefetchedBlogs, setPrefetchedBlogs] = useState<BlogFeedResponse[]>([]);
  const [prefetchedPage, setPrefetchedPage] = useState<number | null>(null);
  const [prefetchFailedPage, setPrefetchFailedPage] = useState<number | null>(null);
  const [shouldAppendPrefetch, setShouldAppendPrefetch] = useState(false);

  const enrichFeedWithViewerState = useCallback(async (
    response: BlogFeedResponse[],
  ) => {
    if (!user?.userId) {
      return response;
    }

    const [following, likes, saves] = await Promise.all([
      getFollowingByUserId(user.userId).catch(() => []),
      getBlogLikesByUser(user.userId).catch(() => []),
      getBlogSavesByUser(user.userId).catch(() => []),
    ]);

    return applyViewerState(response, following, likes, saves, user.userId);
  }, [user?.userId]);

  const fetchFeedPage = useCallback(async (pageToLoad: number, pageSize: number) => {
    const response = await getBlogFeed({
      page: pageToLoad,
      size: pageSize,
    });

    return enrichFeedWithViewerState(response);
  }, [enrichFeedWithViewerState]);

  const prefetchFeedPage = useCallback(async (pageToPrefetch: number) => {
    if (isPrefetchingRef.current) {
      return;
    }

    const generation = prefetchGenerationRef.current;
    isPrefetchingRef.current = true;
    setIsPrefetching(true);
    setPrefetchFailedPage(null);

    try {
      const response = await fetchFeedPage(
        pageToPrefetch,
        LOAD_MORE_FEED_PAGE_SIZE,
      );

      if (prefetchGenerationRef.current !== generation) {
        return;
      }

      setPrefetchedBlogs(response);
      setPrefetchedPage(pageToPrefetch);
    } catch {
      if (prefetchGenerationRef.current === generation) {
        setPrefetchFailedPage(pageToPrefetch);
      }
    } finally {
      isPrefetchingRef.current = false;
      setIsPrefetching(false);
    }
  }, [fetchFeedPage]);

  const loadFeed = useCallback(async ({
    append = false,
    pageToLoad = 0,
    refreshing = false,
  }: {
    append?: boolean;
    pageToLoad?: number;
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
      prefetchGenerationRef.current = requestGeneration;
      setPrefetchedBlogs([]);
      setPrefetchedPage(null);
      setPrefetchFailedPage(null);
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
      const response = await fetchFeedPage(pageToLoad, pageSize);

      setBlogs((currentBlogs) =>
        append
          ? mergeUniqueBlogs(currentBlogs, response)
          : response,
      );
      const nextPage = append
        ? pageToLoad + 1
        : Math.ceil(response.length / LOAD_MORE_FEED_PAGE_SIZE);
      const canLoadMore = response.length === pageSize;

      setNextLoadPage(
        nextPage,
      );
      setHasMore(canLoadMore);

      if (canLoadMore) {
        void prefetchFeedPage(nextPage);
      }
    } catch (requestError) {
      if (append) {
        setLoadMoreError("Unable to load more posts.");
      } else {
        setError(
          requestError instanceof Error
            ? requestError.message
            : "Unable to load feed.",
        );
      }
    } finally {
      isFetchingRef.current = false;
      setIsLoading(false);
      setIsLoadingMore(false);
      setIsRefreshing(false);
    }
  }, [fetchFeedPage, prefetchFeedPage]);

  const appendPrefetchedBlogs = useCallback(() => {
    if (prefetchedPage !== nextLoadPage) {
      return false;
    }

    const bufferedBlogs = prefetchedBlogs;
    const nextPage = nextLoadPage + 1;
    const canLoadMore = bufferedBlogs.length === LOAD_MORE_FEED_PAGE_SIZE;

    setBlogs((currentBlogs) => mergeUniqueBlogs(currentBlogs, bufferedBlogs));
    setNextLoadPage(nextPage);
    setHasMore(canLoadMore);
    setPrefetchedBlogs([]);
    setPrefetchedPage(null);
    setPrefetchFailedPage(null);
    setShouldAppendPrefetch(false);
    setIsLoadingMore(false);
    setLoadMoreError("");

    if (canLoadMore) {
      void prefetchFeedPage(nextPage);
    }

    return true;
  }, [nextLoadPage, prefetchedBlogs, prefetchedPage, prefetchFeedPage]);

  useEffect(() => {
    if (!shouldAppendPrefetch) {
      return;
    }

    if (appendPrefetchedBlogs()) {
      return;
    }

    if (prefetchFailedPage === nextLoadPage) {
      setShouldAppendPrefetch(false);
      setIsLoadingMore(false);
      setLoadMoreError("Unable to load more posts.");
    }
  }, [
    appendPrefetchedBlogs,
    nextLoadPage,
    prefetchFailedPage,
    shouldAppendPrefetch,
  ]);

  useEffect(() => {
    void loadFeed();
  }, [loadFeed]);

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

    if (appendPrefetchedBlogs()) {
      return;
    }

    if (isPrefetching || isPrefetchingRef.current) {
      setIsLoadingMore(true);
      setShouldAppendPrefetch(true);
      return;
    }

    void loadFeed({
      append: true,
      pageToLoad: nextLoadPage,
    });
  }, [
    appendPrefetchedBlogs,
    hasMore,
    isLoading,
    isLoadingMore,
    isPrefetching,
    isRefreshing,
    loadFeed,
    nextLoadPage,
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
      label: "Your story",
    },
    ...mockHomeFeedStories,
  ];

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
            onRefresh={() => void loadFeed({ refreshing: true })}
            refreshing={isRefreshing}
          />
        }
        showsVerticalScrollIndicator={false}
      >
        <StoryRail stories={stories} />
        {isLoading ? (
          <LoadingState label="Loading feed..." />
        ) : error ? (
          <EmptyState description={error} title="Feed unavailable" />
        ) : blogs.length ? (
          <>
            <BlogFeedList blogs={blogs} />
            {isLoadingMore ? (
              <FeedSkeletonFooter />
            ) : loadMoreError ? (
              <Text style={styles.loadMoreError}>{loadMoreError}</Text>
            ) : null}
          </>
        ) : (
          <EmptyState
            description="New cafe stories will appear here when they are ready."
            title="No posts yet"
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
  skeletonAvatar: {
    backgroundColor: colors.surfaceMuted,
    borderRadius: 18,
    height: 36,
    width: 36,
  },
  skeletonCard: {
    backgroundColor: colors.background,
    gap: spacing.md,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.lg,
  },
  skeletonHeader: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.md,
  },
  skeletonHeaderText: {
    flex: 1,
    gap: spacing.xs,
  },
  skeletonLine: {
    backgroundColor: colors.surfaceMuted,
    borderRadius: 8,
    height: 12,
    width: "72%",
  },
  skeletonLineShort: {
    backgroundColor: colors.surfaceMuted,
    borderRadius: 8,
    height: 10,
    width: "34%",
  },
  skeletonLineStrong: {
    backgroundColor: colors.border,
    borderRadius: 8,
    height: 12,
    width: "48%",
  },
  skeletonLineWide: {
    backgroundColor: colors.surfaceMuted,
    borderRadius: 8,
    height: 12,
    width: "92%",
  },
  skeletonList: {
    gap: 2,
    paddingTop: 2,
  },
});
