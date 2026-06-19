import { useFocusEffect, useNavigation } from "@react-navigation/native";
import type { BottomTabNavigationProp } from "@react-navigation/bottom-tabs";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { Search, Send } from "lucide-react-native";
import { useCallback, useMemo, useState } from "react";
import {
  RefreshControl,
  ScrollView,
  StyleSheet,
  TextInput,
  View,
} from "react-native";

import {
  BlogFeedList,
  EmptyState,
  ExploreRecommendationList,
  ExploreTabs,
  FeedCardSkeletonList,
  Screen,
  ShareTopBar,
} from "../../components";
import { useAuth } from "../../features/auth";
import { routes } from "../../navigation";
import type { MainTabParamList, RootStackParamList } from "../../navigation";
import {
  getBlogById,
  getBlogLikesByUser,
  getBlogSavesByUser,
  getCafePageRecommendations,
  getFollowingByUserId,
  getMixedRecommendations,
  getReviewerRecommendations,
  getTrendingBlogs,
} from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type {
  BlogFeedResponse,
  BlogResponse,
  RecommendationCardResponse,
  UserFollowResponse,
} from "../../types";
import type { ExploreTab } from "../../components";

type ExploreRecommendationState = Record<Exclude<ExploreTab, "trending">, RecommendationCardResponse[]>;

type ExploreErrorState = Partial<Record<ExploreTab, string>>;

const initialRecommendations: ExploreRecommendationState = {
  all: [],
  cafes: [],
  reviewers: [],
};

function blogResponseToFeedResponse(blog: BlogResponse): BlogFeedResponse {
  return {
    authorUserAvatar: blog.authorUserAvatar,
    authorUserFullName: blog.authorUserFullName,
    authorUserId: blog.authorUserId,
    authorUserName: blog.authorUserName,
    blogId: blog.id,
    commentCount: blog.commentCount ?? 0,
    contentPreview: blog.content,
    createdAt: blog.createdAt,
    displayAuthorType: blog.displayAuthorType,
    displayAvatarUrl: blog.displayAvatarUrl,
    displayName: blog.displayName,
    imageUrls: blog.imageUrls,
    isLike: blog.isLike,
    isSave: blog.isSave,
    likeCount: blog.likeCount,
    pageAvatarUrl: blog.pageAvatarUrl,
    pageId: blog.pageId,
    pageName: blog.pageName,
    regionId: blog.regionId,
    shareCount: blog.shareCount,
  };
}

function applyViewerState(
  blogs: BlogFeedResponse[],
  following: UserFollowResponse[],
  likedBlogIds: Set<string>,
  savedBlogIds: Set<string>,
  currentUserId?: string,
) {
  const followingUserIds = new Set(
    following.map((item) => item.followingUserId),
  );

  return blogs.map((blog) => ({
    ...blog,
    isFollow:
      blog.authorUserId === currentUserId
        ? false
        : followingUserIds.has(blog.authorUserId),
    isLike: likedBlogIds.has(blog.blogId) || Boolean(blog.isLike),
    isSave: savedBlogIds.has(blog.blogId) || Boolean(blog.isSave),
  }));
}

function isCafeOrReviewer(item: RecommendationCardResponse) {
  return item.targetType === "CAFE_PAGE" || item.targetType === "REVIEWER";
}

export function ExploreScreen() {
  const navigation =
    useNavigation<
      BottomTabNavigationProp<MainTabParamList, typeof routes.explore> &
        NativeStackNavigationProp<RootStackParamList>
    >();
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState<ExploreTab>("all");
  const [recommendations, setRecommendations] =
    useState<ExploreRecommendationState>(initialRecommendations);
  const [trendingBlogs, setTrendingBlogs] = useState<BlogFeedResponse[]>([]);
  const [loadedTabs, setLoadedTabs] = useState<Partial<Record<ExploreTab, boolean>>>({});
  const [errors, setErrors] = useState<ExploreErrorState>({});
  const [isLoading, setIsLoading] = useState(false);
  const [isRefreshing, setIsRefreshing] = useState(false);

  const loadRecommendations = useCallback(async (
    tab: Exclude<ExploreTab, "trending">,
    refreshing = false,
  ) => {
    if (refreshing) {
      setIsRefreshing(true);
    } else {
      setIsLoading(true);
    }

    try {
      const response =
        tab === "cafes"
          ? await getCafePageRecommendations(0, 20)
          : tab === "reviewers"
            ? await getReviewerRecommendations(0, 20)
            : (await getMixedRecommendations(0, 30)).filter(isCafeOrReviewer);

      setRecommendations((currentRecommendations) => ({
        ...currentRecommendations,
        [tab]: response,
      }));
      setLoadedTabs((currentTabs) => ({ ...currentTabs, [tab]: true }));
      setErrors((currentErrors) => ({ ...currentErrors, [tab]: undefined }));
    } catch (requestError) {
      setErrors((currentErrors) => ({
        ...currentErrors,
        [tab]:
          requestError instanceof Error
            ? requestError.message
            : "Unable to load recommendations.",
      }));
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  }, []);

  const loadTrending = useCallback(async (refreshing = false) => {
    if (refreshing) {
      setIsRefreshing(true);
    } else {
      setIsLoading(true);
    }

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
        .filter((result): result is PromiseFulfilledResult<BlogResponse> =>
          result.status === "fulfilled")
        .map((result) => blogResponseToFeedResponse(result.value));

      if (!user?.userId) {
        setTrendingBlogs(blogs);
      } else {
        const [following, likes, saves] = await Promise.all([
          getFollowingByUserId(user.userId).catch(() => []),
          getBlogLikesByUser(user.userId).catch(() => []),
          getBlogSavesByUser(user.userId).catch(() => []),
        ]);

        setTrendingBlogs(
          applyViewerState(
            blogs,
            following,
            new Set(likes.map((item) => item.blogId)),
            new Set(saves.map((item) => item.blogId)),
            user.userId,
          ),
        );
      }

      setLoadedTabs((currentTabs) => ({ ...currentTabs, trending: true }));
      setErrors((currentErrors) => ({ ...currentErrors, trending: undefined }));
    } catch (requestError) {
      setErrors((currentErrors) => ({
        ...currentErrors,
        trending:
          requestError instanceof Error
            ? requestError.message
            : "Unable to load trending posts.",
      }));
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  }, [user?.userId]);

  const loadActiveTab = useCallback((refreshing = false) => {
    if (activeTab === "trending") {
      void loadTrending(refreshing);
      return;
    }

    void loadRecommendations(activeTab, refreshing);
  }, [activeTab, loadRecommendations, loadTrending]);

  const handleRecommendationPress = useCallback((item: RecommendationCardResponse) => {
    if (item.targetType === "CAFE_PAGE") {
      navigation.navigate(routes.cafeDetail, {
        cafeId: item.targetId,
      });
      return;
    }

    if (item.targetType !== "USER" && item.targetType !== "REVIEWER") {
      return;
    }

    navigation.navigate(routes.otherUserProfile, {
      userId: item.userId ?? item.targetId,
      userName: item.username,
    });
  }, [navigation]);

  useFocusEffect(
    useCallback(() => {
      if (loadedTabs[activeTab]) {
        return;
      }

      loadActiveTab();
    }, [activeTab, loadActiveTab, loadedTabs]),
  );

  const title = useMemo(() => {
    switch (activeTab) {
      case "cafes":
        return "Suggested cafes";
      case "reviewers":
        return "Explore reviewers";
      case "trending":
        return "Trending posts";
      default:
        return "Recommended for you";
    }
  }, [activeTab]);

  const content = activeTab === "trending" ? (
    isLoading && !trendingBlogs.length ? (
      <FeedCardSkeletonList />
    ) : errors.trending ? (
      <EmptyState description="Pull down to try again." title={errors.trending} />
    ) : trendingBlogs.length ? (
      <BlogFeedList blogs={trendingBlogs} />
    ) : (
      <EmptyState
        description="Trending cafe stories will appear here soon."
        title="No trending posts yet"
      />
    )
  ) : (
    <ExploreRecommendationList
      emptyDescription="New recommendations will appear as CafeStory learns what you like."
      emptyTitle="No recommendations yet"
      error={errors[activeTab]}
      isLoading={isLoading && !recommendations[activeTab].length}
      items={recommendations[activeTab]}
      onItemPress={handleRecommendationPress}
      title={title}
    />
  );

  return (
    <Screen padded={false}>
      <ShareTopBar
        onRightPress={() => navigation.navigate(routes.conversations)}
        rightAccessibilityLabel="Open messages"
        rightIcon={Send}
      />

      <ScrollView
        contentContainerStyle={styles.content}
        refreshControl={
          <RefreshControl
            onRefresh={() => loadActiveTab(true)}
            refreshing={isRefreshing}
          />
        }
        showsVerticalScrollIndicator={false}
      >
        <View style={styles.searchBox}>
          <Search color={colors.muted} size={20} strokeWidth={2.3} />
          <TextInput
            editable={false}
            placeholder="Search cafes, reviewers, or posts"
            placeholderTextColor={colors.muted}
            style={styles.searchInput}
          />
        </View>

        <ExploreTabs activeTab={activeTab} onChange={setActiveTab} />

        {content}
      </ScrollView>
    </Screen>
  );
}

const styles = StyleSheet.create({
  content: {
    gap: spacing.md,
    paddingBottom: 112,
  },
  searchBox: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 18,
    borderWidth: 1,
    flexDirection: "row",
    gap: spacing.sm,
    marginHorizontal: spacing.lg,
    marginTop: spacing.sm,
    minHeight: 46,
    paddingHorizontal: spacing.md,
  },
  searchInput: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.label,
    fontWeight: "700",
    paddingVertical: spacing.sm,
  },
});
