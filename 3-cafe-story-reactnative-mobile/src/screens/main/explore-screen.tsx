import { useFocusEffect, useNavigation } from "@react-navigation/native";
import type { BottomTabNavigationProp } from "@react-navigation/bottom-tabs";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { Search, Send } from "lucide-react-native";
import { useCallback, useEffect, useMemo, useState } from "react";
import {
  ActivityIndicator,
  Pressable,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";

import {
  Avatar,
  BlogFeedCard,
  EmptyState,
  ExploreRecommendationList,
  ExploreTabs,
  FeedCardSkeletonList,
  ListRowSkeletonList,
  Screen,
  ShareTopBar,
} from "../../components";
import { routes } from "../../navigation";
import type { MainTabParamList, RootStackParamList } from "../../navigation";
import {
  getCafePageRecommendations,
  getMixedRecommendations,
  getReviewerRecommendations,
  getTrendingBlogs,
  blogTrendingToFeedBlog,
  searchExplore,
} from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type {
  BlogResponse,
  BlogTrendingResponse,
  CafePageResponse,
  ExploreSearchResults,
  RecommendationCardResponse,
  UserResponse,
} from "../../types";
import type { ExploreTab } from "../../components";

type ExploreRecommendationState = Record<Exclude<ExploreTab, "trending">, RecommendationCardResponse[]>;

type ExploreErrorState = Partial<Record<ExploreTab, string>>;

const initialRecommendations: ExploreRecommendationState = {
  all: [],
  cafes: [],
  reviewers: [],
};

function isCafeOrReviewer(item: RecommendationCardResponse) {
  return item.targetType === "CAFE_PAGE" || item.targetType === "REVIEWER";
}

function hasSearchResults(results: ExploreSearchResults) {
  return results.users.length > 0 || results.cafePages.length > 0 || results.blogs.length > 0;
}

function blogSearchTitle(blog: BlogResponse) {
  return blog.content?.trim() || blog.displayName || blog.authorUserName || "Blog post";
}

function blogSearchSubtitle(blog: BlogResponse) {
  return blog.displayName || blog.pageName || blog.authorUserName || "CafeStory post";
}

type SearchResultRowProps = {
  avatar?: string | null;
  label: string;
  onPress: () => void;
  subtitle: string;
  title: string;
};

function SearchResultRow({
  avatar,
  label,
  onPress,
  subtitle,
  title,
}: SearchResultRowProps) {
  return (
    <Pressable
      accessibilityLabel={`Open ${title}`}
      accessibilityRole="button"
      onPress={onPress}
      style={({ pressed }) => [
        styles.searchResultRow,
        pressed && styles.pressed,
      ]}
    >
      <Avatar size={48} uri={avatar} />
      <View style={styles.searchResultCopy}>
        <View style={styles.searchResultTitleRow}>
          <Text numberOfLines={1} style={styles.searchResultTitle}>
            {title}
          </Text>
          <Text style={styles.searchResultLabel}>{label}</Text>
        </View>
        <Text numberOfLines={1} style={styles.searchResultSubtitle}>
          {subtitle}
        </Text>
      </View>
    </Pressable>
  );
}

export function ExploreScreen() {
  const navigation =
    useNavigation<
      BottomTabNavigationProp<MainTabParamList, typeof routes.explore> &
        NativeStackNavigationProp<RootStackParamList>
    >();
  const [activeTab, setActiveTab] = useState<ExploreTab>("all");
  const [recommendations, setRecommendations] =
    useState<ExploreRecommendationState>(initialRecommendations);
  const [trendingBlogs, setTrendingBlogs] = useState<BlogTrendingResponse[]>([]);
  const [loadedTabs, setLoadedTabs] = useState<Partial<Record<ExploreTab, boolean>>>({});
  const [errors, setErrors] = useState<ExploreErrorState>({});
  const [isLoading, setIsLoading] = useState(false);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState<ExploreSearchResults>({
    blogs: [],
    cafePages: [],
    users: [],
  });
  const [isSearching, setIsSearching] = useState(false);
  const [searchError, setSearchError] = useState<string | null>(null);

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
      setTrendingBlogs(trending);
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
  }, []);

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

  const handleUserSearchPress = useCallback((user: UserResponse) => {
    navigation.navigate(routes.otherUserProfile, {
      userId: user.userId,
      userName: user.userName,
    });
  }, [navigation]);

  const handleCafeSearchPress = useCallback((page: CafePageResponse) => {
    navigation.navigate(routes.cafeDetail, {
      cafeId: page.id,
    });
  }, [navigation]);

  const handleBlogSearchPress = useCallback((blog: BlogResponse) => {
    navigation.navigate(routes.blogDetail, {
      blogId: blog.id,
    });
  }, [navigation]);

  useEffect(() => {
    const normalizedQuery = searchQuery.trim();

    if (normalizedQuery.length < 2) {
      setSearchResults({
        blogs: [],
        cafePages: [],
        users: [],
      });
      setSearchError(null);
      setIsSearching(false);
      return;
    }

    let isActive = true;
    setIsSearching(true);
    setSearchError(null);

    const timeoutId = setTimeout(() => {
      void searchExplore(normalizedQuery)
        .then((results) => {
          if (isActive) {
            setSearchResults(results);
          }
        })
        .catch((requestError) => {
          if (isActive) {
            setSearchError(
              requestError instanceof Error
                ? requestError.message
                : "Unable to search right now.",
            );
          }
        })
        .finally(() => {
          if (isActive) {
            setIsSearching(false);
          }
        });
    }, 320);

    return () => {
      isActive = false;
      clearTimeout(timeoutId);
    };
  }, [searchQuery]);

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

  const normalizedSearchQuery = searchQuery.trim();
  const isSearchMode = normalizedSearchQuery.length > 0;

  const searchContent = normalizedSearchQuery.length < 2 ? (
    <EmptyState
      description="Type at least 2 characters to search users, cafe pages, and posts."
      title="Keep typing"
    />
  ) : isSearching && !hasSearchResults(searchResults) ? (
    <View style={styles.searchSection}>
      <View style={styles.searchingTitleRow}>
        <Text style={styles.title}>Searching</Text>
        <ActivityIndicator color={colors.primary} />
      </View>
      <ListRowSkeletonList padded={false} />
    </View>
  ) : searchError ? (
    <EmptyState description="Pull down or edit the query to try again." title={searchError} />
  ) : hasSearchResults(searchResults) ? (
    <View style={styles.searchSection}>
      <Text style={styles.title}>Search results</Text>
      {searchResults.users.length > 0 ? (
        <View style={styles.searchGroup}>
          <Text style={styles.searchGroupTitle}>Users and reviewers</Text>
          {searchResults.users.map((user) => (
            <SearchResultRow
              avatar={user.userAvatar}
              key={user.userId}
              label="User"
              onPress={() => handleUserSearchPress(user)}
              subtitle={user.userName ? `@${user.userName}` : user.regionCity ?? "CafeStory user"}
              title={user.userFullName || user.userName || "CafeStory user"}
            />
          ))}
        </View>
      ) : null}
      {searchResults.cafePages.length > 0 ? (
        <View style={styles.searchGroup}>
          <Text style={styles.searchGroupTitle}>Cafe pages</Text>
          {searchResults.cafePages.map((page) => (
            <SearchResultRow
              avatar={page.avatarUrl}
              key={page.id}
              label="Cafe"
              onPress={() => handleCafeSearchPress(page)}
              subtitle={page.regionCity || page.address || "Cafe page"}
              title={page.name || "Cafe page"}
            />
          ))}
        </View>
      ) : null}
      {searchResults.blogs.length > 0 ? (
        <View style={styles.searchGroup}>
          <Text style={styles.searchGroupTitle}>Posts</Text>
          {searchResults.blogs.map((blog) => (
            <SearchResultRow
              avatar={blog.displayAvatarUrl || blog.pageAvatarUrl || blog.authorUserAvatar}
              key={blog.id}
              label="Post"
              onPress={() => handleBlogSearchPress(blog)}
              subtitle={blogSearchSubtitle(blog)}
              title={blogSearchTitle(blog)}
            />
          ))}
        </View>
      ) : null}
    </View>
  ) : (
    <EmptyState
      description="Try another cafe name, reviewer, location, or post keyword."
      title="No results found"
    />
  );

  const content = isSearchMode ? searchContent : activeTab === "trending" ? (
    isLoading && !trendingBlogs.length ? (
      <FeedCardSkeletonList />
    ) : errors.trending ? (
      <EmptyState description="Pull down to try again." title={errors.trending} />
    ) : trendingBlogs.length ? (
      <View style={styles.trendingList}>
        {trendingBlogs.map((item) => (
          <BlogFeedCard
            blog={blogTrendingToFeedBlog(item)}
            key={item.blogId}
          />
        ))}
      </View>
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
            autoCorrect={false}
            onChangeText={setSearchQuery}
            placeholder="Search cafes, reviewers, or posts"
            placeholderTextColor={colors.muted}
            returnKeyType="search"
            style={styles.searchInput}
            value={searchQuery}
          />
        </View>

        {isSearchMode ? null : <ExploreTabs activeTab={activeTab} onChange={setActiveTab} />}

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
  pressed: {
    opacity: 0.72,
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
  searchGroup: {
    gap: spacing.sm,
  },
  searchGroupTitle: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "900",
    textTransform: "uppercase",
  },
  searchResultCopy: {
    flex: 1,
    gap: 3,
  },
  searchResultLabel: {
    color: colors.primary,
    fontSize: typography.caption,
    fontWeight: "900",
  },
  searchResultRow: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 14,
    borderWidth: 1,
    flexDirection: "row",
    gap: spacing.md,
    padding: spacing.md,
  },
  searchResultSubtitle: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "700",
  },
  searchResultTitle: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.label,
    fontWeight: "900",
  },
  searchResultTitleRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.sm,
  },
  searchingTitleRow: {
    alignItems: "center",
    flexDirection: "row",
    justifyContent: "space-between",
  },
  searchSection: {
    gap: spacing.md,
    paddingHorizontal: spacing.lg,
  },
  title: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },
  trendingList: {
    gap: spacing.md,
  },
});
