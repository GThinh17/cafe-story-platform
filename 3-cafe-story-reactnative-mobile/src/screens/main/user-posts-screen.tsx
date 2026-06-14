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
import { BlogFeedCard, EmptyState, LoadingState, Screen } from "../../components";
import { getBlogsByUser } from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type { BlogFeedResponse, BlogResponse } from "../../types";
import type { RootStackParamList } from "../../navigation";
import { routes } from "../../navigation";

type UserPostsRouteProp = RouteProp<RootStackParamList, typeof routes.userPosts>;

function toFeedBlog(blog: BlogResponse): BlogFeedResponse {
  return {
    authorUserAvatar: blog.authorUserAvatar ?? null,
    authorUserFullName: blog.authorUserFullName ?? null,
    authorUserId: blog.authorUserId,
    authorUserName: blog.authorUserName ?? null,
    blogId: blog.id,
    commentCount: blog.commentCount ?? 0,
    contentPreview: blog.content,
    createdAt: blog.createdAt,
    displayAuthorType: blog.displayAuthorType ?? null,
    displayAvatarUrl: blog.displayAvatarUrl ?? null,
    displayName: blog.displayName ?? null,
    imageUrls: blog.imageUrls ?? [],
    isFollow: null,
    isLike: blog.isLike ?? false,
    isSave: blog.isSave ?? false,
    likeCount: blog.likeCount ?? 0,
    pageAvatarUrl: blog.pageAvatarUrl ?? null,
    pageId: blog.pageId ?? null,
    pageName: blog.pageName ?? null,
    regionId: blog.regionId ?? null,
    shareCount: blog.shareCount ?? 0,
  };
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
  const { initialBlogId, userId, userName } = route.params;
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
      const nextBlogs = await getBlogsByUser(userId);
      setPosts(nextBlogs.map(toFeedBlog));
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
  }, [userId]);

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
          <Text style={styles.headerTitle}>Posts</Text>
          {userName ? (
            <Text numberOfLines={1} style={styles.headerSubtitle}>
              {userName}
            </Text>
          ) : null}
        </View>
      </View>

      {isLoading ? (
        <LoadingState label="Loading posts..." />
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
              description="Published posts from this profile will appear here."
              title={error ?? "No posts yet"}
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
