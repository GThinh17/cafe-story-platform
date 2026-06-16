import { RouteProp, useNavigation, useRoute } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { ArrowLeft } from "lucide-react-native";
import { useCallback, useEffect, useMemo, useState } from "react";
import {
  Pressable,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";

import {
  CafePageHeader,
  EmptyState,
  LoadingState,
  Screen,
  UserPostGrid,
} from "../../components";
import { useAuth } from "../../features/auth";
import { routes } from "../../navigation";
import type { RootStackParamList } from "../../navigation";
import {
  blogResponseToPostPreview,
  followCafePage,
  getCafePageBlogs,
  getCafePageById,
  likeCafePage,
  unfollowCafePage,
  unlikeCafePage,
} from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type { CafePageResponse, UserPostPreview } from "../../types";

type CafePageRouteProp = RouteProp<RootStackParamList, typeof routes.cafeDetail>;

function adjustCount(value: number | null | undefined, delta: number) {
  return Math.max(0, (value ?? 0) + delta);
}

export function CafePageScreen() {
  const navigation =
    useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const route = useRoute<CafePageRouteProp>();
  const { user } = useAuth();
  const { cafeId } = route.params;

  const [cafePage, setCafePage] = useState<CafePageResponse | null>(null);
  const [posts, setPosts] = useState<UserPostPreview[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [isFollowPending, setIsFollowPending] = useState(false);
  const [isLikePending, setIsLikePending] = useState(false);

  const isOwner = useMemo(
    () => Boolean(cafePage?.ownerUserId && cafePage.ownerUserId === user?.userId),
    [cafePage?.ownerUserId, user?.userId],
  );

  const loadCafePage = useCallback(async (refreshing = false) => {
    if (refreshing) {
      setIsRefreshing(true);
    } else {
      setIsLoading(true);
    }

    try {
      const [nextCafePage, blogPage] = await Promise.all([
        getCafePageById(cafeId),
        getCafePageBlogs(cafeId, { size: 30 }),
      ]);

      setCafePage(nextCafePage);
      setPosts((blogPage.items ?? []).map(blogResponseToPostPreview));
      setError(null);
    } catch (nextError) {
      setError(
        nextError instanceof Error
          ? nextError.message
          : "Unable to load this cafe page.",
      );
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  }, [cafeId]);

  useEffect(() => {
    void loadCafePage();
  }, [loadCafePage]);

  const toggleFollow = useCallback(async () => {
    if (!cafePage || isOwner || isFollowPending) {
      return;
    }

    const wasFollowing = Boolean(cafePage.isFollowing);
    const delta = wasFollowing ? -1 : 1;

    setIsFollowPending(true);
    setCafePage((currentPage) =>
      currentPage
        ? {
            ...currentPage,
            followerCount: adjustCount(currentPage.followerCount, delta),
            isFollowing: !wasFollowing,
          }
        : currentPage,
    );

    try {
      if (wasFollowing) {
        await unfollowCafePage(cafePage.id);
      } else {
        await followCafePage(cafePage.id);
      }
      setError(null);
    } catch (nextError) {
      setCafePage((currentPage) =>
        currentPage
          ? {
              ...currentPage,
              followerCount: adjustCount(currentPage.followerCount, -delta),
              isFollowing: wasFollowing,
            }
          : currentPage,
      );
      setError(
        nextError instanceof Error
          ? nextError.message
          : "Unable to update follow state.",
      );
    } finally {
      setIsFollowPending(false);
    }
  }, [cafePage, isFollowPending, isOwner]);

  const toggleLike = useCallback(async () => {
    if (!cafePage || isOwner || isLikePending) {
      return;
    }

    const wasLiked = Boolean(cafePage.isLiked);
    const delta = wasLiked ? -1 : 1;

    setIsLikePending(true);
    setCafePage((currentPage) =>
      currentPage
        ? {
            ...currentPage,
            isLiked: !wasLiked,
            likeCount: adjustCount(currentPage.likeCount, delta),
          }
        : currentPage,
    );

    try {
      if (wasLiked) {
        await unlikeCafePage(cafePage.id);
      } else {
        await likeCafePage(cafePage.id);
      }
      setError(null);
    } catch (nextError) {
      setCafePage((currentPage) =>
        currentPage
          ? {
              ...currentPage,
              isLiked: wasLiked,
              likeCount: adjustCount(currentPage.likeCount, -delta),
            }
          : currentPage,
      );
      setError(
        nextError instanceof Error
          ? nextError.message
          : "Unable to update like state.",
      );
    } finally {
      setIsLikePending(false);
    }
  }, [cafePage, isLikePending, isOwner]);

  const openPost = useCallback((post: UserPostPreview) => {
    navigation.navigate(routes.userPosts, {
      initialBlogId: post.id,
      pageId: cafeId,
      pageName: cafePage?.name,
      userId: cafePage?.ownerUserId ?? undefined,
      userName: cafePage?.name,
    });
  }, [cafeId, cafePage?.name, cafePage?.ownerUserId, navigation]);

  const onRefresh = useCallback(() => {
    void loadCafePage(true);
  }, [loadCafePage]);

  return (
    <Screen padded={false}>
      <View style={styles.topBar}>
        <Pressable
          accessibilityLabel="Back"
          accessibilityRole="button"
          hitSlop={10}
          onPress={() => navigation.goBack()}
          style={({ pressed }) => [styles.iconButton, pressed && styles.pressed]}
        >
          <ArrowLeft color={colors.foreground} size={30} strokeWidth={2.5} />
        </Pressable>

        <Text numberOfLines={1} style={styles.topBarTitle}>
          Cafe Page
        </Text>

        <View style={styles.iconButton} />
      </View>

      {isLoading && !cafePage ? (
        <LoadingState label="Loading cafe page..." />
      ) : (
        <ScrollView
          contentContainerStyle={styles.content}
          refreshControl={
            <RefreshControl refreshing={isRefreshing} onRefresh={onRefresh} />
          }
          showsVerticalScrollIndicator={false}
        >
          {error ? (
            <View style={styles.errorBanner}>
              <Text style={styles.errorText}>{error}</Text>
            </View>
          ) : null}

          {cafePage ? (
            <CafePageHeader
              cafePage={cafePage}
              isFollowPending={isFollowPending}
              isLikePending={isLikePending}
              isOwner={isOwner}
              onEditPress={() => setError("Cafe page editing is coming soon.")}
              onFollowPress={toggleFollow}
              onLikePress={toggleLike}
            />
          ) : (
            <View style={styles.emptyPage}>
              <EmptyState
                description="Pull down to refresh and try again."
                title="Cafe page unavailable"
              />
            </View>
          )}

          <View style={styles.sectionHeader}>
            <Text style={styles.sectionTitle}>Posts</Text>
          </View>

          {posts.length > 0 ? (
            <View style={styles.grid}>
              <UserPostGrid onPostPress={openPost} posts={posts} />
            </View>
          ) : (
            <View style={styles.emptyPosts}>
              <EmptyState
                description="Posts from this cafe page will appear here."
                title="No cafe posts yet"
              />
            </View>
          )}
        </ScrollView>
      )}
    </Screen>
  );
}

const styles = StyleSheet.create({
  content: {
    paddingBottom: 112,
  },
  emptyPage: {
    minHeight: 260,
  },
  emptyPosts: {
    minHeight: 220,
  },
  errorBanner: {
    backgroundColor: colors.secondarySoft,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    marginHorizontal: spacing.lg,
    marginTop: spacing.sm,
    padding: spacing.md,
  },
  errorText: {
    color: colors.primaryStrong,
    fontSize: typography.caption,
    fontWeight: "700",
    lineHeight: 18,
  },
  grid: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: 2,
  },
  iconButton: {
    alignItems: "center",
    height: 48,
    justifyContent: "center",
    width: 48,
  },
  pressed: {
    opacity: 0.72,
  },
  sectionHeader: {
    borderBottomColor: colors.border,
    borderBottomWidth: 1,
    borderTopColor: colors.border,
    borderTopWidth: 1,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.md,
  },
  sectionTitle: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },
  topBar: {
    alignItems: "center",
    backgroundColor: colors.background,
    borderBottomColor: colors.border,
    borderBottomWidth: 1,
    flexDirection: "row",
    minHeight: 64,
    paddingHorizontal: spacing.sm,
  },
  topBarTitle: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.title,
    fontWeight: "900",
    textAlign: "center",
  },
});
