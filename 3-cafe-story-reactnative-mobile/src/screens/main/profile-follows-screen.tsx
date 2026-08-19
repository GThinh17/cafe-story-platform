import { RouteProp, useNavigation, useRoute } from "@react-navigation/native";
import { Text } from "react-native";
import { Pressable, TextInput } from "react-native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { ChevronLeft, Search, Store, UserPlus } from "lucide-react-native";
import { useCallback, useEffect, useMemo, useState } from "react";
import {
  FlatList, RefreshControl, StyleSheet, View } from "react-native";
import {
  EmptyState,
  ListRowSkeletonList,
  ProfileFollowUserRow,
  Screen,
  Avatar,
} from "../../components";
import { useAuth } from "../../features/auth";
import { routes } from "../../navigation";
import type { RootStackParamList } from "../../navigation";
import {
  createDirectConversation,
  followUser,
  getFollowersByUserId,
  getFollowingTargetsByUserId,
  getUserProfile,
} from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type { FollowTargetResponse, UserFollowResponse, UserResponse } from "../../types";
import { t } from "../../features/i18n";

type ProfileFollowsRouteProp = RouteProp<
  RootStackParamList,
  typeof routes.profileFollows
>;

type FollowTab = "followers" | "following";
type FollowListItem =
  | {
      key: string;
      kind: "USER";
      user: UserResponse;
    }
  | {
      key: string;
      kind: "CAFE_PAGE";
      target: FollowTargetResponse;
    };

function getFollowUserId(item: UserFollowResponse, tab: FollowTab) {
  return tab === "followers" ? item.followerUserId : item.followingUserId;
}

function matchesSearch(user: UserResponse, query: string) {
  const normalizedQuery = query.trim().toLowerCase();

  if (!normalizedQuery) {
    return true;
  }

  return (
    user.userName.toLowerCase().includes(normalizedQuery) ||
    (user.userFullName ?? "").toLowerCase().includes(normalizedQuery)
  );
}

function matchesFollowItem(item: FollowListItem, query: string) {
  const normalizedQuery = query.trim().toLowerCase();

  if (!normalizedQuery) {
    return true;
  }

  if (item.kind === "USER") {
    return matchesSearch(item.user, query);
  }

  return [
    item.target.displayName,
    item.target.pageName,
    item.target.username,
    item.target.city,
  ].some((value) => (value ?? "").toLowerCase().includes(normalizedQuery));
}

async function hydrateFollowUsers(items: UserFollowResponse[], tab: FollowTab) {
  const uniqueUserIds = Array.from(
    new Set(items.map((item) => getFollowUserId(item, tab))),
  );

  return Promise.all(uniqueUserIds.map((userId) => getUserProfile(userId)));
}

function userToFollowItem(user: UserResponse): FollowListItem {
  return {
    key: `user:${user.userId}`,
    kind: "USER",
    user,
  };
}

function followingTargetToItem(target: FollowTargetResponse): FollowListItem | null {
  if (target.targetType === "CAFE_PAGE") {
    const cafePageId = target.cafePageId ?? target.targetId;

    if (!cafePageId) {
      return null;
    }

    return {
      key: `cafe-page:${cafePageId}`,
      kind: "CAFE_PAGE",
      target: {
        ...target,
        cafePageId,
        targetId: cafePageId,
      },
    };
  }

  const userId = target.userId ?? target.targetId;

  if (!userId) {
    return null;
  }

  return userToFollowItem({
    accountStatus: null,
    followingCount: null,
    isFollowing: true,
    regionArea: null,
    regionCity: target.city,
    regionId: null,
    regionProvince: null,
    regionStreet: null,
    regionWard: null,
    userAvatar: target.avatar,
    userDescription: null,
    userEmail: null,
    userFollower: null,
    userFullName: target.userFullName ?? target.displayName,
    userId,
    userLike: null,
    userName: target.username ?? target.displayName ?? "user",
    userPhone: null,
  });
}

export function ProfileFollowsScreen() {
  const navigation =
    useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const route = useRoute<ProfileFollowsRouteProp>();
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState<FollowTab>(
    route.params.initialTab ?? "followers",
  );
  const [followers, setFollowers] = useState<FollowListItem[]>([]);
  const [following, setFollowing] = useState<FollowListItem[]>([]);
  const [query, setQuery] = useState("");
  const [error, setError] = useState("");
  const [pendingUserId, setPendingUserId] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);

  const profileUserId = route.params.userId;
  const title = route.params.userName || "Profile";

  const loadFollows = useCallback(async (refreshing = false) => {
    if (refreshing) {
      setIsRefreshing(true);
    } else {
      setIsLoading(true);
    }

    setError("");

    try {
      const [followerLinks, followingTargets] = await Promise.all([
        getFollowersByUserId(profileUserId),
        getFollowingTargetsByUserId(profileUserId),
      ]);
      const nextFollowers = await hydrateFollowUsers(followerLinks, "followers");
      const nextFollowing = followingTargets
        .map(followingTargetToItem)
        .filter((item): item is FollowListItem => Boolean(item));

      setFollowers(nextFollowers.map(userToFollowItem));
      setFollowing(nextFollowing);
    } catch (nextError) {
      setError(
        nextError instanceof Error
          ? nextError.message
          : "Unable to load follows.",
      );
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  }, [profileUserId]);

  useEffect(() => {
    void loadFollows();
  }, [loadFollows]);

  const activeItems = activeTab === "followers" ? followers : following;
  const filteredItems = useMemo(
    () => activeItems.filter((item) => matchesFollowItem(item, query)),
    [activeItems, query],
  );

  const handleOpenProfile = useCallback((selectedUser: UserResponse) => {
    if (selectedUser.userId === user?.userId) {
      navigation.navigate(routes.main, {
        screen: routes.profile,
      });
      return;
    }

    navigation.navigate(routes.otherUserProfile, {
      userId: selectedUser.userId,
      userName: selectedUser.userName,
    });
  }, [navigation, user?.userId]);

  const handleMessage = useCallback(async (selectedUser: UserResponse) => {
    if (pendingUserId || selectedUser.userId === user?.userId) {
      return;
    }

    setPendingUserId(selectedUser.userId);
    setError("");

    try {
      const conversation = await createDirectConversation(selectedUser.userId);

      navigation.navigate(routes.chatDetail, {
        chatAvatar: conversation.chatAvatar || selectedUser.userAvatar,
        chatName:
          conversation.chatName ||
          selectedUser.userFullName ||
          selectedUser.userName,
        conversationId: conversation.id,
        targetUserId: selectedUser.userId,
        userName: conversation.userName || selectedUser.userName,
      });
    } catch (nextError) {
      setError(
        nextError instanceof Error
          ? nextError.message
          : "Unable to start this conversation.",
      );
    } finally {
      setPendingUserId(null);
    }
  }, [navigation, pendingUserId, user?.userId]);

  const handleFollow = useCallback(async (selectedUser: UserResponse) => {
    if (pendingUserId || selectedUser.userId === user?.userId) {
      return;
    }

    setPendingUserId(selectedUser.userId);
    setError("");

    try {
      await followUser(selectedUser.userId);

      const updateUser = (item: UserResponse) =>
        item.userId === selectedUser.userId
          ? {
              ...item,
              isFollowing: true,
              userFollower: (item.userFollower ?? 0) + 1,
            }
          : item;

      const updateItem = (item: FollowListItem): FollowListItem =>
        item.kind === "USER"
          ? {
              ...item,
              user: updateUser(item.user),
            }
          : item;

      setFollowers((currentFollowers) => currentFollowers.map(updateItem));
      setFollowing((currentFollowing) => currentFollowing.map(updateItem));
    } catch (nextError) {
      setError(
        nextError instanceof Error
          ? nextError.message
          : "Unable to follow this user.",
      );
    } finally {
      setPendingUserId(null);
    }
  }, [pendingUserId, user?.userId]);

  const renderTabs = (
    <View style={styles.tabs}>
      <Pressable
        accessibilityLabel={t("Show followers")}
        accessibilityRole="tab"
        accessibilityState={{ selected: activeTab === "followers" }}
        onPress={() => setActiveTab("followers")}
        style={({ pressed }) => [
          styles.tab,
          activeTab === "followers" && styles.activeTab,
          pressed && styles.pressed,
        ]}
      >
        <Text
          numberOfLines={1}
          style={[
            styles.tabText,
            activeTab === "followers" && styles.activeTabText,
          ]}
        >{t("Followers:")}{followers.length}
        </Text>
      </Pressable>

      <Pressable
        accessibilityLabel={t("Show following")}
        accessibilityRole="tab"
        accessibilityState={{ selected: activeTab === "following" }}
        onPress={() => setActiveTab("following")}
        style={({ pressed }) => [
          styles.tab,
          activeTab === "following" && styles.activeTab,
          pressed && styles.pressed,
        ]}
      >
        <Text
          numberOfLines={1}
          style={[
            styles.tabText,
            activeTab === "following" && styles.activeTabText,
          ]}
        >{t("Following:")}{following.length}
        </Text>
      </Pressable>
    </View>
  );

  return (
    <Screen padded={false}>
      <View style={styles.topBar}>
        <Pressable
          accessibilityLabel={t("Go back")}
          accessibilityRole="button"
          onPress={() => navigation.goBack()}
          style={({ pressed }) => [styles.iconButton, pressed && styles.pressed]}
        >
          <ChevronLeft color={colors.foreground} size={32} strokeWidth={2.4} />
        </Pressable>

        <Text numberOfLines={1} style={styles.title}>
          {title}
        </Text>

        <Pressable
          accessibilityLabel={t("Find people")}
          accessibilityRole="button"
          style={({ pressed }) => [styles.iconButton, pressed && styles.pressed]}
        >
          <UserPlus color={colors.foreground} size={25} strokeWidth={2.4} />
        </Pressable>
      </View>

      {renderTabs}

      <FlatList
        ListHeaderComponent={
          <View style={styles.headerContent}>
            <View style={styles.searchBox}>
              <Search color={colors.muted} size={20} strokeWidth={2.2} />
              <TextInput
                autoCapitalize="none"
                onChangeText={setQuery}
                placeholder={t("Search")}
                placeholderTextColor={colors.muted}
                style={styles.searchInput}
                value={query}
              />
            </View>
            {error ? <Text style={styles.errorText}>{error}</Text> : null}
          </View>
        }
        contentContainerStyle={[
          styles.content,
          filteredItems.length === 0 && styles.emptyContent,
        ]}
        data={filteredItems}
        keyExtractor={(item) => item.key}
        refreshControl={
          <RefreshControl
            refreshing={isRefreshing}
            onRefresh={() => void loadFollows(true)}
          />
        }
        renderItem={({ item }) =>
          item.kind === "USER" ? (
            <ProfileFollowUserRow
              disabled={pendingUserId === item.user.userId}
              isCurrentUser={item.user.userId === user?.userId}
              onFollowPress={handleFollow}
              onMessagePress={handleMessage}
              onProfilePress={handleOpenProfile}
              user={item.user}
            />
          ) : (
            <FollowingCafePageRow
              onPress={(cafePageId) => navigation.navigate(routes.cafeDetail, { cafeId: cafePageId })}
              target={item.target}
            />
          )
        }
        ListEmptyComponent={
          isLoading ? (
            <ListRowSkeletonList />
          ) : (
            <EmptyState
              description={
                query.trim()
                  ? t("Try another name or username.")
                  : activeTab === "followers"
                    ? "Followers will appear here."
                    : "People and cafe pages this profile follows will appear here."
              }
              title={
                query.trim()
                  ? t("No people found")
                  : activeTab === "followers"
                    ? "No followers yet"
                    : "No following yet"
              }
            />
          )
        }
        showsVerticalScrollIndicator={false}
      />
    </Screen>
  );
}

type FollowingCafePageRowProps = {
  onPress: (cafePageId: string) => void;
  target: FollowTargetResponse;
};

function FollowingCafePageRow({ onPress, target }: FollowingCafePageRowProps) {
  const cafePageId = target.cafePageId ?? target.targetId;
  const displayName = target.displayName ?? target.pageName ?? "Cafe page";

  return (
    <View style={styles.container}>
      <Pressable
        accessibilityLabel={`Open ${displayName}`}
        accessibilityRole="button"
        disabled={!cafePageId}
        onPress={() => cafePageId && onPress(cafePageId)}
        style={({ pressed }) => [
          styles.identityAction,
          pressed && styles.pressed,
        ]}
      >
        <Avatar size={58} uri={target.avatar} />
        <View style={styles.identity}>
          <Text numberOfLines={1} style={styles.username}>
            {displayName}
          </Text>
          <Text numberOfLines={1} style={styles.name}>
            {target.city ?? "Cafe page"}
          </Text>
        </View>
      </Pressable>

      <View style={styles.pageBadge}>
        <Store color={colors.primaryStrong} size={16} strokeWidth={2.4} />
        <Text style={styles.pageBadgeText}>{t("Page")}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  activeTab: {
    borderBottomColor: colors.foreground,
  },
  activeTabText: {
    color: colors.foreground,
  },
  content: {
    paddingBottom: 96,
  },
  emptyContent: {
    flexGrow: 1,
  },
  errorText: {
    color: colors.danger,
    fontSize: typography.label,
    fontWeight: "700",
    paddingHorizontal: spacing.lg,
  },
  headerContent: {
    gap: spacing.md,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.md,
  },
  iconButton: {
    alignItems: "center",
    borderRadius: 24,
    height: 48,
    justifyContent: "center",
    width: 48,
  },
  container: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.sm,
    minHeight: 78,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.sm,
  },
  identity: {
    flex: 1,
    justifyContent: "center",
  },
  identityAction: {
    alignItems: "center",
    flex: 1,
    flexDirection: "row",
    gap: spacing.md,
  },
  name: {
    color: colors.muted,
    fontSize: typography.label,
    fontWeight: "600",
    marginTop: 2,
  },
  pageBadge: {
    alignItems: "center",
    backgroundColor: colors.primarySoft,
    borderRadius: 12,
    flexDirection: "row",
    gap: spacing.xs,
    minHeight: 38,
    minWidth: 86,
    justifyContent: "center",
    paddingHorizontal: spacing.md,
  },
  pageBadgeText: {
    color: colors.primaryStrong,
    fontSize: typography.label,
    fontWeight: "900",
  },
  pressed: {
    opacity: 0.72,
  },
  searchBox: {
    alignItems: "center",
    backgroundColor: colors.surfaceMuted,
    borderRadius: 18,
    flexDirection: "row",
    gap: spacing.sm,
    minHeight: 46,
    paddingHorizontal: spacing.md,
  },
  searchInput: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.body,
    paddingVertical: spacing.sm,
  },
  tab: {
    alignItems: "center",
    borderBottomColor: "transparent",
    borderBottomWidth: 3,
    flex: 1,
    minHeight: 52,
    justifyContent: "center",
    paddingHorizontal: spacing.md,
  },
  tabText: {
    color: colors.muted,
    fontSize: typography.body,
    fontWeight: "900",
  },
  tabs: {
    backgroundColor: colors.background,
    borderBottomColor: colors.border,
    borderBottomWidth: 1,
    flexDirection: "row",
  },
  title: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.title,
    fontWeight: "800",
  },
  username: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },
  topBar: {
    alignItems: "center",
    backgroundColor: colors.background,
    flexDirection: "row",
    gap: spacing.md,
    minHeight: 64,
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.sm,
  },
});
