import { RouteProp, useNavigation, useRoute } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import {
  ArrowLeft,
  Award,
  MessageCircle,
  MoreVertical,
  UserPlus,
} from "lucide-react-native";
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
  Avatar,
  EmptyState,
  ProfileContentTabs,
  ProfileSkeleton,
  PostGridSkeleton,
  Screen,
  UserPostGrid,
} from "../../components";
import { useAuth } from "../../features/auth";
import { routes } from "../../navigation";
import type { RootStackParamList } from "../../navigation";
import {
  blogResponseToPostPreview,
  createDirectConversation,
  followUser,
  getBlogsByUser,
  getReviewerByUserId,
  getSharedBlogsByUser,
  getTaggedBlogsByUser,
  getUserProfile,
  unfollowUser,
} from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type {
  BlogResponse,
  ProfileContentTab,
  ReviewerResponse,
  UserPostPreview,
  UserResponse,
} from "../../types";

type OtherUserProfileRouteProp = RouteProp<
  RootStackParamList,
  typeof routes.otherUserProfile
>;

const externalTabs: ProfileContentTab[] = ["posts", "shared", "tagged"];

const emptyTabPosts: Record<ProfileContentTab, UserPostPreview[]> = {
  posts: [],
  saved: [],
  shared: [],
  tagged: [],
};

function initialsFor(name?: string | null) {
  if (!name) {
    return "CS";
  }

  return name
    .split(/\s|_/)
    .filter(Boolean)
    .slice(0, 2)
    .map((word) => word[0])
    .join("")
    .toUpperCase();
}

function formatCount(value?: number | null) {
  const safeValue = value ?? 0;

  if (safeValue >= 1000000) {
    return `${(safeValue / 1000000).toFixed(safeValue >= 10000000 ? 0 : 1)}m`;
  }

  if (safeValue >= 1000) {
    return `${(safeValue / 1000).toFixed(safeValue >= 10000 ? 0 : 1)}k`;
  }

  return String(safeValue);
}

function getEmptyCopy(tab: ProfileContentTab, userName: string) {
  switch (tab) {
    case "shared":
      return {
        description: `Posts shared by ${userName} will appear here.`,
        title: "No shared posts yet",
      };
    case "tagged":
      return {
        description: `Posts that mention ${userName} will appear here.`,
        title: "No tagged posts yet",
      };
    default:
      return {
        description: `Published posts from ${userName} will appear here.`,
        title: "No posts yet",
      };
  }
}

function loadBlogsForProfileTab(tab: ProfileContentTab, userId: string): Promise<BlogResponse[]> {
  switch (tab) {
    case "shared":
      return getSharedBlogsByUser(userId);
    case "tagged":
      return getTaggedBlogsByUser(userId);
    default:
      return getBlogsByUser(userId);
  }
}

function formatRegion(profile: UserResponse | null) {
  if (!profile) {
    return null;
  }

  return [profile.regionCity, profile.regionProvince]
    .filter(Boolean)
    .join(", ");
}

export function OtherUserProfileScreen() {
  const navigation =
    useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const route = useRoute<OtherUserProfileRouteProp>();
  const { user } = useAuth();
  const [profile, setProfile] = useState<UserResponse | null>(null);
  const [reviewer, setReviewer] = useState<ReviewerResponse | null>(null);
  const [tabPosts, setTabPosts] =
    useState<Record<ProfileContentTab, UserPostPreview[]>>(emptyTabPosts);
  const [loadedTabs, setLoadedTabs] =
    useState<Partial<Record<ProfileContentTab, boolean>>>({});
  const [activeTab, setActiveTab] = useState<ProfileContentTab>("posts");
  const [error, setError] = useState("");
  const [contentError, setContentError] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isContentLoading, setIsContentLoading] = useState(false);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [isFollowPending, setIsFollowPending] = useState(false);
  const [isMessagePending, setIsMessagePending] = useState(false);

  const targetUserId = route.params.userId;

  const loadContentTab = useCallback(async (
    tab: ProfileContentTab,
    refreshing = false,
  ) => {
    if (refreshing) {
      setIsRefreshing(true);
    } else {
      setIsContentLoading(true);
    }

    try {
      const blogs = await loadBlogsForProfileTab(tab, targetUserId);

      setTabPosts((currentPosts) => ({
        ...currentPosts,
        [tab]: blogs.map(blogResponseToPostPreview),
      }));
      setLoadedTabs((currentTabs) => ({
        ...currentTabs,
        [tab]: true,
      }));
      setContentError("");
    } catch (nextError) {
      setContentError(
        nextError instanceof Error
          ? nextError.message
          : "Unable to load this profile section.",
      );
    } finally {
      setIsContentLoading(false);
      setIsRefreshing(false);
    }
  }, [targetUserId]);

  const loadProfile = useCallback(async (refreshing = false) => {
    if (refreshing) {
      setIsRefreshing(true);
    } else {
      setIsLoading(true);
    }

    try {
      setTabPosts(emptyTabPosts);
      setLoadedTabs({});

      const [nextProfile, nextReviewer, userBlogs] = await Promise.all([
        getUserProfile(targetUserId),
        getReviewerByUserId(targetUserId).catch(() => null),
        getBlogsByUser(targetUserId),
      ]);

      setProfile(nextProfile);
      setReviewer(nextReviewer);
      setTabPosts((currentPosts) => ({
        ...currentPosts,
        posts: userBlogs.map(blogResponseToPostPreview),
      }));
      setLoadedTabs((currentTabs) => ({
        ...currentTabs,
        posts: true,
      }));
      setError("");
      setContentError("");
    } catch (nextError) {
      setError(
        nextError instanceof Error
          ? nextError.message
          : "Unable to load this profile.",
      );
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  }, [targetUserId]);

  useEffect(() => {
    void loadProfile();
  }, [loadProfile]);

  useEffect(() => {
    if (loadedTabs[activeTab]) {
      return;
    }

    void loadContentTab(activeTab);
  }, [activeTab, loadContentTab, loadedTabs]);

  const displayName = profile?.userFullName || profile?.userName || "CafeStory user";
  const userName = profile?.userName || route.params.userName || "cafestory";
  const userDescription = profile?.userDescription?.trim();
  const regionLabel = formatRegion(profile);
  const isOwnProfile = user?.userId === targetUserId;
  const isFollowing = Boolean(profile?.isFollowing);
  const visiblePosts = tabPosts[activeTab];
  const emptyCopy = getEmptyCopy(activeTab, userName);

  const stats = useMemo(
    () => [
      { key: "posts", label: "posts", value: formatCount(tabPosts.posts.length) },
      { key: "followers", label: "followers", value: formatCount(profile?.userFollower) },
      { key: "following", label: "following", value: formatCount(profile?.followingCount) },
    ],
    [profile?.followingCount, profile?.userFollower, tabPosts.posts.length],
  );

  const handleRefresh = useCallback(() => {
    if (activeTab === "posts") {
      void loadProfile(true);
      return;
    }

    void loadContentTab(activeTab, true);
  }, [activeTab, loadContentTab, loadProfile]);

  const handleToggleFollow = useCallback(async () => {
    if (!profile || isOwnProfile || isFollowPending) {
      return;
    }

    const nextIsFollowing = !isFollowing;
    setIsFollowPending(true);
    setProfile((currentProfile) =>
      currentProfile
        ? {
            ...currentProfile,
            isFollowing: nextIsFollowing,
            userFollower: Math.max(
              0,
              (currentProfile.userFollower ?? 0) + (nextIsFollowing ? 1 : -1),
            ),
          }
        : currentProfile,
    );

    try {
      if (nextIsFollowing) {
        await followUser(targetUserId);
      } else {
        await unfollowUser(targetUserId);
      }
    } catch {
      setProfile((currentProfile) =>
        currentProfile
          ? {
              ...currentProfile,
              isFollowing,
              userFollower: Math.max(
                0,
                (currentProfile.userFollower ?? 0) + (nextIsFollowing ? -1 : 1),
              ),
            }
          : currentProfile,
      );
    } finally {
      setIsFollowPending(false);
    }
  }, [isFollowPending, isFollowing, isOwnProfile, profile, targetUserId]);

  const handleMessagePress = useCallback(async () => {
    if (!profile || isOwnProfile || isMessagePending) {
      return;
    }

    setIsMessagePending(true);

    try {
      const conversation = await createDirectConversation(targetUserId);

      navigation.navigate(routes.chatDetail, {
        chatAvatar: conversation.chatAvatar || profile.userAvatar,
        chatName: conversation.chatName || displayName,
        conversationId: conversation.id,
        targetUserId,
        userName: conversation.userName || profile.userName,
      });
      setError("");
    } catch (nextError) {
      setError(
        nextError instanceof Error
          ? nextError.message
          : "Unable to start this conversation.",
      );
    } finally {
      setIsMessagePending(false);
    }
  }, [
    displayName,
    isMessagePending,
    isOwnProfile,
    navigation,
    profile,
    targetUserId,
  ]);

  const openUserPosts = useCallback((post?: UserPostPreview) => {
    navigation.navigate(routes.userPosts, {
      contentTab: activeTab,
      initialBlogId: post?.id,
      userId: targetUserId,
      userName,
    });
  }, [activeTab, navigation, targetUserId, userName]);

  const openProfileFollows = useCallback((initialTab: "followers" | "following") => {
    navigation.navigate(routes.profileFollows, {
      initialTab,
      userId: targetUserId,
      userName,
    });
  }, [navigation, targetUserId, userName]);

  if (isLoading && !profile) {
    return (
      <Screen padded={false}>
        <View style={styles.topBar}>
          <Pressable
            accessibilityLabel="Go back"
            accessibilityRole="button"
            onPress={() => navigation.goBack()}
            style={({ pressed }) => [styles.iconButton, pressed && styles.pressed]}
          >
            <ArrowLeft color={colors.foreground} size={32} strokeWidth={2.5} />
          </Pressable>
          <Text numberOfLines={1} style={styles.topTitle}>
            {route.params.userName || "Profile"}
          </Text>
          <View style={styles.iconButton} />
        </View>
        <ProfileSkeleton />
      </Screen>
    );
  }

  return (
    <Screen padded={false}>
      <View style={styles.topBar}>
        <Pressable
          accessibilityLabel="Go back"
          accessibilityRole="button"
          onPress={() => navigation.goBack()}
          style={({ pressed }) => [styles.iconButton, pressed && styles.pressed]}
        >
          <ArrowLeft color={colors.foreground} size={32} strokeWidth={2.5} />
        </Pressable>
        <Text numberOfLines={1} style={styles.topTitle}>
          {userName}
        </Text>
        <View style={styles.topActions}>
          <Pressable
            accessibilityLabel="Message user"
            accessibilityRole="button"
            disabled={isOwnProfile || isMessagePending}
            onPress={handleMessagePress}
            style={({ pressed }) => [styles.iconButton, pressed && styles.pressed]}
          >
            <MessageCircle color={colors.foreground} size={25} strokeWidth={2.4} />
          </Pressable>
          <Pressable
            accessibilityLabel="Open profile options"
            accessibilityRole="button"
            style={({ pressed }) => [styles.iconButton, pressed && styles.pressed]}
          >
            <MoreVertical color={colors.foreground} size={24} strokeWidth={2.4} />
          </Pressable>
        </View>
      </View>

      <ScrollView
        contentContainerStyle={styles.content}
        refreshControl={
          <RefreshControl refreshing={isRefreshing} onRefresh={handleRefresh} />
        }
        showsVerticalScrollIndicator={false}
      >
        {error ? (
          <View style={styles.errorBanner}>
            <Text style={styles.errorText}>{error}</Text>
          </View>
        ) : null}

        <View style={styles.identity}>
          <View style={styles.avatarColumn}>
            <Avatar
              initials={initialsFor(displayName)}
              size={96}
              uri={profile?.userAvatar}
            />
            {reviewer && regionLabel ? (
              <Text numberOfLines={2} style={styles.regionUnderBadge}>
                {regionLabel}
              </Text>
            ) : null}
          </View>

          <View style={styles.identityContent}>
            <Text numberOfLines={1} style={styles.name}>
              {displayName}
            </Text>
            {reviewer?.badge ? (
              <View style={styles.reviewerBadge}>
                <Award color={colors.rating} size={14} strokeWidth={2.6} />
                <Text numberOfLines={1} style={styles.reviewerBadgeText}>
                  {reviewer.badge.toLowerCase()}
                </Text>
              </View>
            ) : null}
            <View style={styles.stats}>
              {stats.map((stat) => (
                <Pressable
                  accessibilityLabel={`Open ${stat.label}`}
                  accessibilityRole="button"
                  disabled={stat.key === "posts"}
                  key={stat.label}
                  onPress={() => {
                    if (stat.key === "followers" || stat.key === "following") {
                      openProfileFollows(stat.key);
                    }
                  }}
                  style={({ pressed }) => [
                    styles.statItem,
                    pressed && stat.key !== "posts" && styles.pressed,
                  ]}
                >
                  <Text style={styles.statValue}>{stat.value}</Text>
                  <Text style={styles.statLabel}>{stat.label}</Text>
                </Pressable>
              ))}
            </View>
          </View>
        </View>

        {userDescription ? (
          <Text style={styles.description}>{userDescription}</Text>
        ) : null}

        {!reviewer && regionLabel ? (
          <Text style={styles.location}>{regionLabel}</Text>
        ) : null}

        <View style={styles.actions}>
          <Pressable
            accessibilityLabel={isFollowing ? "Unfollow user" : "Follow user"}
            accessibilityRole="button"
            disabled={isOwnProfile || isFollowPending}
            onPress={handleToggleFollow}
            style={({ pressed }) => [
              styles.primaryAction,
              (isFollowing || isOwnProfile) && styles.followingAction,
              pressed && !isFollowPending && styles.pressed,
            ]}
          >
            <Text
              style={[
                styles.primaryActionText,
                (isFollowing || isOwnProfile) && styles.followingActionText,
              ]}
            >
              {isOwnProfile ? "Your Profile" : isFollowing ? "Following" : "Follow"}
            </Text>
          </Pressable>

          <Pressable
            accessibilityLabel="Message user"
            accessibilityRole="button"
            disabled={isOwnProfile || isMessagePending}
            onPress={handleMessagePress}
            style={({ pressed }) => [
              styles.secondaryAction,
              pressed && !isMessagePending && styles.pressed,
            ]}
          >
            <Text style={styles.secondaryActionText}>Message</Text>
          </Pressable>

          <Pressable
            accessibilityLabel="Add profile suggestion"
            accessibilityRole="button"
            style={({ pressed }) => [styles.iconAction, pressed && styles.pressed]}
          >
            <UserPlus color={colors.foreground} size={19} strokeWidth={2.5} />
          </Pressable>
        </View>

        <ProfileContentTabs
          activeTab={activeTab}
          onChange={setActiveTab}
          visibleTabs={externalTabs}
        />

        <View style={styles.grid}>
          {isContentLoading ? (
            <PostGridSkeleton />
          ) : contentError ? (
            <View style={styles.emptyPosts}>
              <EmptyState
                description="Pull down to refresh and try again."
                title={contentError}
              />
            </View>
          ) : visiblePosts.length ? (
            <UserPostGrid onPostPress={openUserPosts} posts={visiblePosts} />
          ) : (
            <View style={styles.emptyPosts}>
              <EmptyState
                description={emptyCopy.description}
                title={emptyCopy.title}
              />
            </View>
          )}
        </View>
      </ScrollView>
    </Screen>
  );
}

const styles = StyleSheet.create({
  actions: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.sm,
    paddingHorizontal: spacing.lg,
  },
  avatarColumn: {
    alignItems: "center",
    gap: spacing.sm,
  },
  content: {
    gap: spacing.md,
    paddingBottom: 112,
  },
  description: {
    color: colors.foreground,
    fontSize: typography.label,
    lineHeight: 20,
    paddingHorizontal: spacing.lg,
  },
  emptyPosts: {
    minHeight: 220,
    width: "100%",
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
  followingAction: {
    backgroundColor: colors.surfaceMuted,
  },
  followingActionText: {
    color: colors.foreground,
  },
  grid: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: 2,
  },
  iconAction: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 10,
    borderWidth: 1,
    height: 38,
    justifyContent: "center",
    width: 44,
  },
  iconButton: {
    alignItems: "center",
    height: 48,
    justifyContent: "center",
    width: 48,
  },
  identity: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.xl,
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.md,
  },
  identityContent: {
    flex: 1,
    gap: spacing.md,
  },
  location: {
    color: colors.muted,
    fontSize: typography.label,
    fontWeight: "700",
    paddingHorizontal: spacing.lg,
  },
  name: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },
  pressed: {
    opacity: 0.72,
  },
  primaryAction: {
    alignItems: "center",
    backgroundColor: colors.primary,
    borderRadius: 10,
    flex: 1,
    height: 38,
    justifyContent: "center",
  },
  primaryActionText: {
    color: colors.white,
    fontSize: typography.label,
    fontWeight: "900",
  },
  reviewerBadge: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 14,
    borderWidth: 1,
    flexDirection: "row",
    gap: spacing.xs,
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs,
    alignSelf: "flex-start",
  },
  reviewerBadgeText: {
    color: colors.rating,
    fontSize: typography.caption,
    fontWeight: "900",
    textTransform: "uppercase",
  },
  regionUnderBadge: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "700",
    lineHeight: 16,
    maxWidth: 112,
    textAlign: "center",
  },
  secondaryAction: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 10,
    borderWidth: 1,
    flex: 1,
    height: 38,
    justifyContent: "center",
  },
  secondaryActionText: {
    color: colors.foreground,
    fontSize: typography.label,
    fontWeight: "900",
  },
  statItem: {
    alignItems: "flex-start",
    flex: 1,
    gap: 2,
  },
  statLabel: {
    color: colors.muted,
    fontSize: typography.caption,
  },
  statValue: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },
  stats: {
    alignItems: "flex-start",
    flexDirection: "row",
    justifyContent: "space-between",
  },
  topActions: {
    alignItems: "center",
    flexDirection: "row",
  },
  topBar: {
    alignItems: "center",
    backgroundColor: colors.background,
    flexDirection: "row",
    minHeight: 64,
    paddingHorizontal: spacing.sm,
  },
  topTitle: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.title,
    fontWeight: "800",
  },
});
