import { RouteProp, useNavigation, useRoute } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { ChevronLeft, Search, UserPlus } from "lucide-react-native";
import { useCallback, useEffect, useMemo, useState } from "react";
import {
  FlatList,
  Pressable,
  RefreshControl,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import {
  EmptyState,
  LoadingState,
  ProfileFollowUserRow,
  Screen,
} from "../../components";
import { useAuth } from "../../features/auth";
import { routes } from "../../navigation";
import type { RootStackParamList } from "../../navigation";
import {
  createDirectConversation,
  followUser,
  getFollowersByUserId,
  getFollowingByUserId,
  getUserProfile,
} from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type { UserFollowResponse, UserResponse } from "../../types";

type ProfileFollowsRouteProp = RouteProp<
  RootStackParamList,
  typeof routes.profileFollows
>;

type FollowTab = "followers" | "following";

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

async function hydrateFollowUsers(items: UserFollowResponse[], tab: FollowTab) {
  const uniqueUserIds = Array.from(
    new Set(items.map((item) => getFollowUserId(item, tab))),
  );

  return Promise.all(uniqueUserIds.map((userId) => getUserProfile(userId)));
}

export function ProfileFollowsScreen() {
  const navigation =
    useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const route = useRoute<ProfileFollowsRouteProp>();
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState<FollowTab>(
    route.params.initialTab ?? "followers",
  );
  const [followers, setFollowers] = useState<UserResponse[]>([]);
  const [following, setFollowing] = useState<UserResponse[]>([]);
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
      const [followerLinks, followingLinks] = await Promise.all([
        getFollowersByUserId(profileUserId),
        getFollowingByUserId(profileUserId),
      ]);
      const [nextFollowers, nextFollowing] = await Promise.all([
        hydrateFollowUsers(followerLinks, "followers"),
        hydrateFollowUsers(followingLinks, "following"),
      ]);

      setFollowers(nextFollowers);
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

  const activeUsers = activeTab === "followers" ? followers : following;
  const filteredUsers = useMemo(
    () => activeUsers.filter((item) => matchesSearch(item, query)),
    [activeUsers, query],
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

      setFollowers((currentFollowers) => currentFollowers.map(updateUser));
      setFollowing((currentFollowing) => currentFollowing.map(updateUser));
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
        accessibilityLabel="Show followers"
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
        >
          Followers: {followers.length}
        </Text>
      </Pressable>

      <Pressable
        accessibilityLabel="Show following"
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
        >
          Following: {following.length}
        </Text>
      </Pressable>
    </View>
  );

  return (
    <Screen padded={false}>
      <View style={styles.topBar}>
        <Pressable
          accessibilityLabel="Go back"
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
          accessibilityLabel="Find people"
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
                placeholder="Search"
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
          filteredUsers.length === 0 && styles.emptyContent,
        ]}
        data={filteredUsers}
        keyExtractor={(item) => item.userId}
        refreshControl={
          <RefreshControl
            refreshing={isRefreshing}
            onRefresh={() => void loadFollows(true)}
          />
        }
        renderItem={({ item }) => (
          <ProfileFollowUserRow
            disabled={pendingUserId === item.userId}
            isCurrentUser={item.userId === user?.userId}
            onFollowPress={handleFollow}
            onMessagePress={handleMessage}
            onProfilePress={handleOpenProfile}
            user={item}
          />
        )}
        ListEmptyComponent={
          isLoading ? (
            <LoadingState label="Loading people..." />
          ) : (
            <EmptyState
              description={
                query.trim()
                  ? "Try another name or username."
                  : activeTab === "followers"
                    ? "Followers will appear here."
                    : "People this profile follows will appear here."
              }
              title={
                query.trim()
                  ? "No people found"
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
