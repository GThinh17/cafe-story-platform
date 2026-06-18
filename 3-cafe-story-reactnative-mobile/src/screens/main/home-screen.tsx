import { useNavigation } from "@react-navigation/native";
import type { BottomTabNavigationProp } from "@react-navigation/bottom-tabs";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { Send } from "lucide-react-native";
import { useCallback, useEffect, useRef, useState } from "react";
import {
  ActivityIndicator,
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

const FEED_PAGE_SIZE = 20;
const LOAD_MORE_THRESHOLD = 360;

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

export function HomeScreen() {
  const navigation =
    useNavigation<
      BottomTabNavigationProp<MainTabParamList, typeof routes.home> &
        NativeStackNavigationProp<RootStackParamList>
    >();
  const { user } = useAuth();
  const scrollViewRef = useRef<ScrollView | null>(null);
  const isFetchingRef = useRef(false);
  const [blogs, setBlogs] = useState<BlogFeedResponse[]>([]);
  const [error, setError] = useState("");
  const [loadMoreError, setLoadMoreError] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);

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
      const response = await getBlogFeed({
        page: pageToLoad,
        size: FEED_PAGE_SIZE,
      });
      const enrichedResponse = await enrichFeedWithViewerState(response);

      setBlogs((currentBlogs) =>
        append
          ? mergeUniqueBlogs(currentBlogs, enrichedResponse)
          : enrichedResponse,
      );
      setPage(pageToLoad);
      setHasMore(response.length === FEED_PAGE_SIZE);
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
  }, [enrichFeedWithViewerState]);

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

    void loadFeed({
      append: true,
      pageToLoad: page + 1,
    });
  }, [hasMore, isLoading, isLoadingMore, isRefreshing, loadFeed, page]);

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
              <View style={styles.loadingMore}>
                <ActivityIndicator color={colors.primary} />
              </View>
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
  loadingMore: {
    alignItems: "center",
    paddingVertical: spacing.lg,
  },
  loadMoreError: {
    color: colors.muted,
    fontSize: typography.label,
    paddingHorizontal: spacing.xl,
    paddingVertical: spacing.lg,
    textAlign: "center",
  },
});
