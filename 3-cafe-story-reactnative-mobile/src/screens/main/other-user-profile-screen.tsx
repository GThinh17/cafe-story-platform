import { RouteProp, useNavigation, useRoute } from "@react-navigation/native";
import { Pressable } from "react-native";
import { Text } from "react-native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import {
  ArrowLeft, Award, MessageCircle, MoreVertical, Store, UserPlus, } from "lucide-react-native";
import { useCallback, useEffect, useMemo, useState } from "react";
import { RefreshControl, ScrollView, StyleSheet, View } from "react-native";
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
import { formatCurrentCompactNumber } from "../../features/i18n";
import { routes } from "../../navigation";
import type { RootStackParamList } from "../../navigation";
import {
  blogResponseToPostPreview,
  createDirectConversation,
  followUser,
  getBlogsByUser,
  getCafePageById,
  getCafePagesByOwner,
  getReviewerByUserId,
  getSharedBlogsByUser,
  getTaggedBlogsByUser,
  getUserProfile,
  isUserAuthoredBlog,
  unfollowUser,
} from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type {
  BlogResponse,
  CafePageResponse,
  ProfileContentTab,
  ReviewerResponse,
  UserPostPreview,
  UserResponse,
} from "../../types";
import { t } from "../../features/i18n";

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
  return formatCurrentCompactNumber(value ?? 0);
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

function isOwnedActiveCafePage(page: CafePageResponse) {
  return page.status === "ACTIVE" && page.pageActive === true;
}

function selectOwnedCafePage(pages: CafePageResponse[]) {
  return pages.find(isOwnedActiveCafePage) ?? pages.find((page) => page.id) ?? null;
}

function linkedCafePageId(profile: UserResponse | null | undefined) {
  return profile?.pageId ?? profile?.cafePageId ?? null;
}

export function OtherUserProfileScreen() {
  const navigation =
    useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const route = useRoute<OtherUserProfileRouteProp>();
  const { user } = useAuth();
  const [profile, setProfile] = useState<UserResponse | null>(null);
  const [reviewer, setReviewer] = useState<ReviewerResponse | null>(null);
  const [ownedCafePage, setOwnedCafePage] = useState<CafePageResponse | null>(null);
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
        [tab]: blogs.filter(isUserAuthoredBlog).map(blogResponseToPostPreview),
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

      const [nextProfile, nextReviewer, userBlogs, ownerCafePages] = await Promise.all([
        getUserProfile(targetUserId),
        getReviewerByUserId(targetUserId).catch(() => null),
        getBlogsByUser(targetUserId),
        getCafePagesByOwner(targetUserId).catch(() => []),
      ]);
      const selectedCafePage = selectOwnedCafePage(ownerCafePages);
      const fallbackCafePageId = linkedCafePageId(nextProfile);
      const nextOwnedCafePage =
        selectedCafePage ??
        (fallbackCafePageId
          ? await getCafePageById(fallbackCafePageId).catch(() => null)
          : null);

      setProfile(nextProfile);
      setReviewer(nextReviewer);
      setOwnedCafePage(nextOwnedCafePage);
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
      setOwnedCafePage(null);
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
  const ownedCafePageId = ownedCafePage?.id ?? linkedCafePageId(profile);
  const visiblePosts = tabPosts[activeTab];
  const emptyCopy = getEmptyCopy(activeTab, userName);

  const stats = useMemo(
    () => [
      { key: "posts", label: t("profile.stat.posts"), displayLabel: t("profile.stat.posts"), value: formatCount(tabPosts.posts.length) },
      { key: "followers", label: t("profile.stat.followers"), displayLabel: t("profile.stat.followersCompact"), value: formatCount(profile?.userFollower) },
      { key: "following", label: t("profile.stat.following"), displayLabel: t("profile.stat.followingCompact"), value: formatCount(profile?.followingCount) },
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

  const openOwnedCafePage = useCallback(() => {
    if (!ownedCafePageId) {
      return;
    }

    navigation.navigate(routes.cafeDetail, {
      cafeId: ownedCafePageId,
    });
  }, [navigation, ownedCafePageId]);

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
            accessibilityLabel={t("Go back")}
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
          accessibilityLabel={t("Go back")}
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
            accessibilityLabel={t("Message user")}
            accessibilityRole="button"
            disabled={isOwnProfile || isMessagePending}
            onPress={handleMessagePress}
            style={({ pressed }) => [styles.iconButton, pressed && styles.pressed]}
          >
            <MessageCircle color={colors.foreground} size={25} strokeWidth={2.4} />
          </Pressable>
          <Pressable
            accessibilityLabel={t("Open profile options")}
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
              size={88}
              uri={profile?.userAvatar}
            />
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
                  accessibilityLabel={t("common.a11y.openNamed", { name: stat.label })}
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
                  <Text
                    adjustsFontSizeToFit
                    minimumFontScale={0.82}
                    numberOfLines={1}
                    style={styles.statLabel}
                  >
                    {stat.displayLabel}
                  </Text>
                </Pressable>
              ))}
            </View>
          </View>
        </View>

        {regionLabel ? (
          <Text numberOfLines={2} style={styles.location}>
            {regionLabel}
          </Text>
        ) : null}

        {userDescription ? (
          <Text style={styles.description}>{userDescription}</Text>
        ) : null}

        {ownedCafePageId ? (
          <Pressable
            accessibilityLabel={t("Open cafe page")}
            accessibilityRole="button"
            onPress={openOwnedCafePage}
            style={({ pressed }) => [
              styles.cafePageTag,
              pressed && styles.pressed,
            ]}
          >
            <Store color={colors.primary} size={16} strokeWidth={2.5} />
            <Text numberOfLines={1} style={styles.cafePageTagName}>
              {ownedCafePage?.name || t("profile.cafe.view")}
            </Text>
          </Pressable>
        ) : null}

        <View style={styles.actions}>
          <Pressable
            accessibilityLabel={isFollowing ? t("Unfollow user") : t("Follow user")}
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
              adjustsFontSizeToFit
              minimumFontScale={0.84}
              numberOfLines={1}
              style={[
                styles.primaryActionText,
                (isFollowing || isOwnProfile) && styles.followingActionText,
              ]}
            >
              {isOwnProfile
                ? t("Your Profile")
                : isFollowing
                  ? t("Following")
                  : t("Follow")}
            </Text>
          </Pressable>

          <Pressable
            accessibilityLabel={t("Message user")}
            accessibilityRole="button"
            disabled={isOwnProfile || isMessagePending}
            onPress={handleMessagePress}
            style={({ pressed }) => [
              styles.secondaryAction,
              pressed && !isMessagePending && styles.pressed,
            ]}
          >
            <Text
              adjustsFontSizeToFit
              minimumFontScale={0.84}
              numberOfLines={1}
              style={styles.secondaryActionText}
            >
              {t("Message")}
            </Text>
          </Pressable>

          <Pressable
            accessibilityLabel={t("Add profile suggestion")}
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
                description={t("Pull down to refresh and try again.")}
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
    alignItems: "flex-start",
    width: 88,
  },
  content: {
    gap: spacing.md,
    paddingBottom: 112,
  },
  cafePageTag: {
    alignItems: "center",
    alignSelf: "flex-start",
    backgroundColor: colors.primarySoft,
    borderColor: colors.primary,
    borderRadius: 18,
    borderWidth: 1,
    flexDirection: "row",
    gap: spacing.sm,
    marginHorizontal: spacing.lg,
    maxWidth: "82%",
    minHeight: 36,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.xs,
  },
  cafePageTagName: {
    color: colors.primary,
    flexShrink: 1,
    fontSize: typography.label,
    fontWeight: "800",
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
    height: 44,
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
    alignItems: "flex-start",
    flexDirection: "row",
    gap: spacing.xl,
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.md,
  },
  identityContent: {
    flex: 1,
    gap: spacing.md,
    minWidth: 0,
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
    height: 44,
    justifyContent: "center",
    minWidth: 0,
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
  secondaryAction: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 10,
    borderWidth: 1,
    flex: 1,
    height: 44,
    justifyContent: "center",
    minWidth: 0,
  },
  secondaryActionText: {
    color: colors.foreground,
    fontSize: typography.label,
    fontWeight: "900",
  },
  statItem: {
    alignItems: "center",
    flex: 1,
    flexBasis: 0,
    gap: 2,
    minWidth: 0,
    paddingHorizontal: 2,
  },
  statLabel: {
    color: colors.muted,
    fontSize: 11,
    flexShrink: 1,
    lineHeight: 14,
    textAlign: "center",
    width: "100%",
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
    width: "100%",
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
