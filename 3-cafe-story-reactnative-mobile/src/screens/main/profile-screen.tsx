import {
  AtSign,
  Pencil,
  Plus,
  UserPlus,
} from "lucide-react-native";
import * as ImagePicker from "expo-image-picker";
import { useCallback, useEffect, useMemo, useState } from "react";
import {
  Pressable,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { useNavigation } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import {
  Avatar,
  BioEditorModal,
  EditProfileModal,
  EmptyState,
  ProfileContentTabs,
  ProfileSkeleton,
  ProfileTopBar,
  Screen,
  UserPostGrid,
} from "../../components";
import type { ProfileContentTab } from "../../components";
import { useAuth } from "../../features/auth";
import { routes } from "../../navigation";
import type { RootStackParamList } from "../../navigation";
import {
  getBlogsByUser,
  getMyProfile,
  updateMyProfile,
  uploadMyAvatar,
} from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type {
  BlogResponse,
  UserPostPreview,
  UserResponse,
  UserUpdateRequest,
} from "../../types";

function initialsFor(name?: string | null) {
  if (!name) {
    return "CS";
  }

  return name
    .split(/\s+/)
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

function toPostPreview(blog: BlogResponse): UserPostPreview {
  return {
    caption: blog.content,
    commentCount: blog.commentCount ?? 0,
    id: blog.id,
    imageUri: blog.imageUrls?.[0] ?? null,
    likeCount: blog.likeCount ?? 0,
  };
}

export function ProfileScreen() {
  const navigation =
    useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const { user } = useAuth();
  const [profile, setProfile] = useState<UserResponse | null>(null);
  const [posts, setPosts] = useState<UserPostPreview[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [isBioModalVisible, setIsBioModalVisible] = useState(false);
  const [bioError, setBioError] = useState<string | null>(null);
  const [isSavingBio, setIsSavingBio] = useState(false);
  const [isEditProfileVisible, setIsEditProfileVisible] = useState(false);
  const [editProfileError, setEditProfileError] = useState<string | null>(null);
  const [isSavingProfile, setIsSavingProfile] = useState(false);
  const [isUploadingAvatar, setIsUploadingAvatar] = useState(false);
  const [activeContentTab, setActiveContentTab] =
    useState<ProfileContentTab>("posts");

  const loadProfile = useCallback(async (refreshing = false) => {
    if (refreshing) {
      setIsRefreshing(true);
    } else {
      setIsLoading(true);
    }

    try {
      const profilePromise = getMyProfile();
      const blogsPromise = user?.userId
        ? getBlogsByUser(user.userId)
        : profilePromise.then((nextProfile) => getBlogsByUser(nextProfile.userId));
      const [nextProfile, userBlogs] = await Promise.all([
        profilePromise,
        blogsPromise,
      ]);

      setProfile(nextProfile);
      setPosts(userBlogs.map(toPostPreview));
      setError(null);
    } catch (nextError) {
      setError(
        nextError instanceof Error
          ? nextError.message
          : "Unable to load your profile.",
      );
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  }, [user?.userId]);

  useEffect(() => {
    void loadProfile();
  }, [loadProfile]);

  const onRefresh = useCallback(() => {
    void loadProfile(true);
  }, [loadProfile]);

  const openBioModal = useCallback(() => {
    setBioError(null);
    setIsBioModalVisible(true);
  }, []);

  const closeBioModal = useCallback(() => {
    if (!isSavingBio) {
      setIsBioModalVisible(false);
      setBioError(null);
    }
  }, [isSavingBio]);

  const saveBio = useCallback(async (bio: string) => {
    const nextDescription = bio.trim();

    setIsSavingBio(true);
    setBioError(null);

    try {
      const nextProfile = await updateMyProfile({
        userDescription: nextDescription,
      });

      setProfile(nextProfile);
      setIsBioModalVisible(false);
    } catch (nextError) {
      setBioError(
        nextError instanceof Error
          ? nextError.message
          : "Unable to update your bio.",
      );
    } finally {
      setIsSavingBio(false);
    }
  }, []);

  const openEditProfile = useCallback(() => {
    setEditProfileError(null);
    setIsEditProfileVisible(true);
  }, []);

  const closeEditProfile = useCallback(() => {
    if (!isSavingProfile) {
      setIsEditProfileVisible(false);
      setEditProfileError(null);
    }
  }, [isSavingProfile]);

  const saveProfile = useCallback(async (request: UserUpdateRequest) => {
    setIsSavingProfile(true);
    setEditProfileError(null);

    try {
      const nextProfile = await updateMyProfile(request);

      setProfile(nextProfile);
      setIsEditProfileVisible(false);
    } catch (nextError) {
      setEditProfileError(
        nextError instanceof Error
          ? nextError.message
          : "Unable to update your profile.",
      );
    } finally {
      setIsSavingProfile(false);
    }
  }, []);

  const pickAndUploadAvatar = useCallback(async () => {
    const permission = await ImagePicker.requestMediaLibraryPermissionsAsync();

    if (!permission.granted) {
      setEditProfileError("Photo access is required to update your avatar.");
      return;
    }

    const result = await ImagePicker.launchImageLibraryAsync({
      allowsEditing: true,
      aspect: [1, 1],
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      quality: 0.86,
    });

    if (result.canceled || !result.assets[0]?.uri) {
      return;
    }

    const asset = result.assets[0];

    setIsUploadingAvatar(true);
    setEditProfileError(null);

    try {
      const nextProfile = await uploadMyAvatar({
        name: asset.fileName ?? `avatar-${Date.now()}.jpg`,
        type: asset.mimeType ?? "image/jpeg",
        uri: asset.uri,
      });

      setProfile(nextProfile);
    } catch (nextError) {
      setEditProfileError(
        nextError instanceof Error
          ? nextError.message
          : "Unable to upload your avatar.",
      );
    } finally {
      setIsUploadingAvatar(false);
    }
  }, []);

  const activeProfile = profile ?? (user
    ? {
      accountStatus: user.accountStatus,
      followingCount: null,
      isFollowing: false,
      regionArea: null,
      regionCity: null,
      regionId: null,
      regionProvince: null,
      regionStreet: null,
      regionWard: null,
      userAvatar: user.userAvatar,
      userDescription: user.userDescription ?? null,
      userEmail: user.userEmail,
      userFollower: null,
      userFullName: user.userFullName,
      userId: user.userId,
      userLike: null,
      userName: user.userName,
      userPhone: user.userPhone,
    }
    : null);

  const displayName = activeProfile?.userFullName || activeProfile?.userName || "Cafe Story user";
  const userName = activeProfile?.userName || "cafestory";
  const avatarUri = activeProfile?.userAvatar;
  const userDescription = activeProfile?.userDescription?.trim();
  const stats = useMemo(
    () => [
      { label: "posts", value: formatCount(posts.length) },
      { label: "followers", value: formatCount(activeProfile?.userFollower) },
      { label: "following", value: formatCount(activeProfile?.followingCount) },
    ],
    [activeProfile?.followingCount, activeProfile?.userFollower, posts.length],
  );

  const openUserPosts = useCallback((post?: UserPostPreview) => {
    if (!activeProfile?.userId) {
      return;
    }

    navigation.navigate(routes.userPosts, {
      initialBlogId: post?.id,
      userId: activeProfile.userId,
      userName,
    });
  }, [activeProfile?.userId, navigation, userName]);

  if (isLoading && !profile) {
    return (
      <Screen padded={false}>
        <ProfileTopBar
          onMessagePress={() => navigation.navigate(routes.conversations)}
          onSettingsPress={() => navigation.navigate(routes.settings)}
          userName={userName}
        />
        <ProfileSkeleton />
      </Screen>
    );
  }

  return (
    <Screen padded={false}>
      <ProfileTopBar
        onMessagePress={() => navigation.navigate(routes.conversations)}
        onSettingsPress={() => navigation.navigate(routes.settings)}
        userName={userName}
      />

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

        <View style={styles.identity}>
          <Avatar initials={initialsFor(displayName)} size={88} uri={avatarUri} />

          <View style={styles.identityContent}>
            <Text numberOfLines={1} style={styles.title}>
              {displayName}
            </Text>

            <View style={styles.stats}>
              {stats.map((stat) => (
                <View key={stat.label} style={styles.statItem}>
                  <Text style={styles.statValue}>{stat.value}</Text>
                  <Text style={styles.statLabel}>{stat.label}</Text>
                </View>
              ))}
            </View>
          </View>
        </View>

        {userDescription ? (
          <Pressable
            accessibilityLabel="Edit profile bio"
            accessibilityRole="button"
            onPress={openBioModal}
            style={({ pressed }) => [
              styles.bioInlineRow,
              pressed && styles.actionPressed,
            ]}
          >
            {/* <Text style={styles.bioInlineLabel}>Bio</Text> */}
            <Text numberOfLines={2} style={styles.bioInlineText}>
              {userDescription}
            </Text>
            <Pencil color={colors.muted} size={14} strokeWidth={2.4} />
          </Pressable>
        ) : (
          <Pressable
            accessibilityLabel="Add profile description"
            accessibilityRole="button"
            onPress={openBioModal}
            style={({ pressed }) => [
              styles.descriptionPrompt,
              pressed && styles.actionPressed,
            ]}
          >
            <Pencil color={colors.muted} size={16} strokeWidth={2.3} />
            <Text style={styles.descriptionPromptText}>
              Add a short description about you here
            </Text>
          </Pressable>
        )}

        <View style={styles.profileChips}>
          <View style={styles.profileChip}>
            <AtSign color={colors.foreground} size={16} strokeWidth={2.4} />
            <Text numberOfLines={1} style={styles.profileChipText}>
              {userName}
            </Text>
          </View>

          <View style={styles.profileChip}>
            <Plus color={colors.muted} size={18} strokeWidth={2.4} />
            <Text style={styles.profileChipMuted}>Add</Text>
          </View>
        </View>

        <View style={styles.actions}>
          <Pressable
            accessibilityLabel="Edit profile"
            accessibilityRole="button"
            onPress={openEditProfile}
            style={({ pressed }) => [
              styles.profileActionButton,
              pressed && styles.actionPressed,
            ]}
          >
            <Text style={styles.profileActionText}>Edit Profile</Text>
          </Pressable>

          <Pressable
            accessibilityLabel="Share profile"
            accessibilityRole="button"
            style={({ pressed }) => [
              styles.profileActionButton,
              pressed && styles.actionPressed,
            ]}
          >
            <Text style={styles.profileActionText}>Share Profile</Text>
          </Pressable>

          <Pressable
            accessibilityLabel="Open profile suggestions"
            accessibilityRole="button"
            style={({ pressed }) => [
              styles.addFriendButton,
              pressed && styles.actionPressed,
            ]}
          >
            <UserPlus color={colors.foreground} size={19} strokeWidth={2.5} />
          </Pressable>
        </View>

        <ProfileContentTabs
          activeTab={activeContentTab}
          onChange={setActiveContentTab}
        />

        <View style={styles.grid}>
          {activeContentTab === "posts" && posts.length > 0 ? (
            <UserPostGrid onPostPress={openUserPosts} posts={posts} />
          ) : activeContentTab === "posts" ? (
            <View style={styles.emptyPosts}>
              <EmptyState
                description="Your cafe stories will appear here after you publish them."
                title="No blogs yet"
              />
            </View>
          ) : activeContentTab === "videos" ? (
            <View style={styles.emptyPosts}>
              <EmptyState
                description="Short cafe videos you share will appear here."
                title="No videos yet"
              />
            </View>
          ) : activeContentTab === "reposts" ? (
            <View style={styles.emptyPosts}>
              <EmptyState
                description="Stories you repost will appear here."
                title="No reposts yet"
              />
            </View>
          ) : (
            <View style={styles.emptyPosts}>
              <EmptyState
                description="Posts that tag you will appear here."
                title="No tagged posts yet"
              />
            </View>
          )}
        </View>
      </ScrollView>

      <BioEditorModal
        error={bioError}
        initialBio={activeProfile?.userDescription}
        isSaving={isSavingBio}
        onClose={closeBioModal}
        onSave={saveBio}
        visible={isBioModalVisible}
      />

      <EditProfileModal
        error={editProfileError}
        isUploadingAvatar={isUploadingAvatar}
        isSaving={isSavingProfile}
        onAvatarPress={pickAndUploadAvatar}
        onClose={closeEditProfile}
        onSave={saveProfile}
        profile={activeProfile}
        visible={isEditProfileVisible}
      />
    </Screen>
  );
}

const styles = StyleSheet.create({
  content: {
    gap: spacing.md,
    paddingBottom: 112,
  },

  bioInlineLabel: {
    color: colors.foreground,
    fontSize: typography.caption,
    fontWeight: "900",
  },

  bioInlineRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.sm,
    paddingHorizontal: spacing.lg,
  },

  bioInlineText: {
    color: colors.foreground,

    flex: 1,
    fontSize: typography.label,
    lineHeight: 18,
  },

  descriptionPrompt: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.sm,
    paddingHorizontal: spacing.lg,
  },

  descriptionPromptText: {
    color: colors.muted,
    flex: 1,
    fontSize: typography.label,
    fontWeight: "700",
    lineHeight: 20,
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

  identity: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.lg,
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.md,
  },

  identityContent: {
    flex: 1,
    gap: spacing.md,
  },

  title: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },

  stats: {
    alignItems: "flex-start",
    flex: 1,
    flexDirection: "row",
    justifyContent: "space-between",
  },

  statItem: {
    alignItems: "flex-start",
    flex: 1,
    gap: 2,
  },

  statValue: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },

  statLabel: {
    color: colors.muted,
    fontSize: typography.caption,
  },

  description: {
    color: colors.foreground,
    fontSize: typography.label,
    lineHeight: 20,
    paddingHorizontal: spacing.lg,
  },

  actions: {
    alignItems: "center",
    flexDirection: "row",
    gap: 8,
    paddingHorizontal: spacing.lg,
  },

  profileActionButton: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 10,
    borderWidth: 1,
    flex: 1,
    height: 36,
    justifyContent: "center",
  },

  profileActionText: {
    color: colors.foreground,
    fontSize: typography.caption,
    fontWeight: "800",
  },

  addFriendButton: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 10,
    borderWidth: 1,
    height: 36,
    justifyContent: "center",
    width: 42,
  },

  actionPressed: {
    opacity: 0.72,
  },

  profileChips: {
    flexDirection: "row",
    gap: spacing.sm,
    paddingHorizontal: spacing.lg,
  },

  profileChip: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 18,
    borderWidth: 1,
    flexDirection: "row",
    gap: spacing.xs,
    minHeight: 36,
    paddingHorizontal: spacing.md,
  },

  profileChipMuted: {
    color: colors.muted,
    fontSize: typography.label,
    fontWeight: "800",
  },

  profileChipText: {
    color: colors.foreground,
    fontSize: typography.label,
    fontWeight: "900",
  },

  profileTabs: {
    alignItems: "center",
    flexDirection: "row",
    justifyContent: "space-around",
    paddingTop: spacing.sm,
  },

  profileTab: {
    alignItems: "center",
    borderBottomColor: "transparent",
    borderBottomWidth: 2,
    flex: 1,
    height: 48,
    justifyContent: "center",
  },

  profileTabActive: {
    borderBottomColor: colors.foreground,
  },

  grid: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: 2,
  },
});
