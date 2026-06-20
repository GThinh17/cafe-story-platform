import { RouteProp, useNavigation, useRoute } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { ArrowLeft } from "lucide-react-native";
import { useCallback, useEffect, useMemo, useState } from "react";
import {
  FlatList,
  Pressable,
  RefreshControl,
  StyleSheet,
  Text,
  View,
} from "react-native";
import {
  BlogFeedCard,
  EmptyState,
  FeedCardSkeletonList,
  Screen,
} from "../../components";
import {
  blogResponseToFeedBlog,
  getBlogsByUser,
  getCafePageBlogs,
  getSavedBlogsByUser,
  getSharedBlogsByUser,
  getTaggedBlogsByUser,
  isUserAuthoredBlog,
} from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type { BlogFeedResponse, BlogResponse, ProfileContentTab } from "../../types";
import type { RootStackParamList } from "../../navigation";
import { routes } from "../../navigation";

type UserPostsRouteProp = RouteProp<RootStackParamList, typeof routes.userPosts>;

function getTabTitle(contentTab: ProfileContentTab) {
  switch (contentTab) {
    case "saved":
      return "Saved";
    case "shared":
      return "Shared";
    case "tagged":
      return "Tagged";
    default:
      return "Posts";
  }
}

function getEmptyTitle(contentTab: ProfileContentTab) {
  switch (contentTab) {
    case "saved":
      return "No saved posts yet";
    case "shared":
      return "No shared posts yet";
    case "tagged":
      return "No tagged posts yet";
    default:
      return "No posts yet";
  }
}

function getEmptyDescription(contentTab: ProfileContentTab) {
  switch (contentTab) {
    case "saved":
      return "Posts saved by this profile will appear here.";
    case "shared":
      return "Posts shared by this profile will appear here.";
    case "tagged":
      return "Posts that mention this profile will appear here.";
    default:
      return "Published posts from this profile will appear here.";
  }
}

function loadBlogsForTab(
  contentTab: ProfileContentTab,
  userId: string,
): Promise<BlogResponse[]> {
  switch (contentTab) {
    case "saved":
      return getSavedBlogsByUser(userId);
    case "shared":
      return getSharedBlogsByUser(userId);
    case "tagged":
      return getTaggedBlogsByUser(userId);
    default:
      return getBlogsByUser(userId);
  }
}

function prioritizeInitialPost(
  posts: BlogFeedResponse[],
  initialBlogId?: string,
) {
  if (!initialBlogId) {
    return posts;
  }

  const selectedPost = posts.find((post) => post.blogId === initialBlogId);

  if (!selectedPost) {
    return posts;
  }

  return [
    selectedPost,
    ...posts.filter((post) => post.blogId !== initialBlogId),
  ];
}

export function UserPostsScreen() {
  const navigation =
    useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const route = useRoute<UserPostsRouteProp>();
  const {
    contentTab = "posts",
    initialBlogId,
    pageId,
    pageName,
    userId,
    userName,
  } = route.params;
  const [posts, setPosts] = useState<BlogFeedResponse[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);

  const loadPosts = useCallback(async (refreshing = false) => {
    if (refreshing) {
      setIsRefreshing(true);
    } else {
      setIsLoading(true);
    }

    try {
      if (!pageId && !userId) {
        throw new Error("Missing post source.");
      }

      const nextBlogs = pageId
        ? (await getCafePageBlogs(pageId, { size: 50 })).items ?? []
        : (await loadBlogsForTab(contentTab, userId as string)).filter(isUserAuthoredBlog);
      setPosts(nextBlogs.map(blogResponseToFeedBlog));
      setError(null);
    } catch (nextError) {
      setError(
        nextError instanceof Error
          ? nextError.message
          : "Unable to load posts.",
      );
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  }, [contentTab, pageId, userId]);

  useEffect(() => {
    void loadPosts();
  }, [loadPosts]);

  const orderedPosts = useMemo(
    () => prioritizeInitialPost(posts, initialBlogId),
    [initialBlogId, posts],
  );

  const onRefresh = useCallback(() => {
    void loadPosts(true);
  }, [loadPosts]);

  return (
    <Screen padded={false}>
      <View style={styles.header}>
        <Pressable
          accessibilityLabel="Back to profile"
          accessibilityRole="button"
          hitSlop={10}
          onPress={() => navigation.goBack()}
          style={({ pressed }) => [
            styles.backButton,
            pressed && styles.pressed,
          ]}
        >
          <ArrowLeft color={colors.foreground} size={32} strokeWidth={2.5} />
        </Pressable>
        <View style={styles.headerCopy}>
          <Text style={styles.headerTitle}>
            {pageId ? "Cafe Posts" : getTabTitle(contentTab)}
          </Text>
          {pageName || userName ? (
            <Text numberOfLines={1} style={styles.headerSubtitle}>
              {pageName ?? userName}
            </Text>
          ) : null}
        </View>
      </View>

      {isLoading ? (
        <FeedCardSkeletonList />
      ) : (
        <FlatList
          contentContainerStyle={[
            styles.listContent,
            orderedPosts.length === 0 && styles.emptyContent,
          ]}
          data={orderedPosts}
          keyExtractor={(item) => item.blogId}
          ListEmptyComponent={
            <EmptyState
              description={
                pageId
                  ? "Posts from this cafe page will appear here."
                  : getEmptyDescription(contentTab)
              }
              title={error ?? (pageId ? "No cafe posts yet" : getEmptyTitle(contentTab))}
            />
          }
          refreshControl={
            <RefreshControl refreshing={isRefreshing} onRefresh={onRefresh} />
          }
          renderItem={({ item }) => <BlogFeedCard blog={item} />}
          showsVerticalScrollIndicator={false}
        />
      )}
    </Screen>
  );
}

const styles = StyleSheet.create({
  backButton: {
    alignItems: "center",
    height: 48,
    justifyContent: "center",
    width: 48,
  },
  emptyContent: {
    flexGrow: 1,
  },
  header: {
    alignItems: "center",
    backgroundColor: colors.background,
    borderBottomColor: colors.border,
    borderBottomWidth: 1,
    flexDirection: "row",
    gap: spacing.lg,
    minHeight: 72,
    paddingHorizontal: spacing.lg,
  },
  headerCopy: {
    flex: 1,
  },
  headerSubtitle: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "700",
  },
  headerTitle: {
    color: colors.foreground,
    fontSize: typography.heading,
    fontWeight: "900",
  },
  listContent: {
    paddingBottom: 36,
  },
  pressed: {
    opacity: 0.72,
  },
});
