import { AtSign, Award, Pencil, Plus, Store, UserPlus, } from "lucide-react-native";
import { Alert } from "react-native";
import { Pressable, Text } from "react-native";
import * as ImagePicker from "expo-image-picker";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { NativeScrollEvent, NativeSyntheticEvent, RefreshControl, ScrollView, StyleSheet, View } from "react-native";
import { useNavigation } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import {
  Avatar,
  BioEditorModal,
  EditProfileModal,
  EmptyState,
  LocationEditorModal,
  ProfileContentTabs,
  ProfileSkeleton,
  ProfileSuggestions,
  ProfileTopBar,
  PostGridSkeleton,
  Screen,
  UserPostGrid,
} from "../../components";
import { useAuth } from "../../features/auth";
import { formatCurrentCompactNumber } from "../../features/i18n";
import { routes } from "../../navigation";
import type { RootStackParamList } from "../../navigation";
import {
  getBlogsByUser,
  getCafePageById,
  getCafePagesByOwner,
  getSavedBlogsByUser,
  getSharedBlogsByUser,
  getTaggedBlogsByUser,
  blogResponseToPostPreview,
  followCafePage,
  followUser,
  getMyProfile,
  getMixedRecommendations,
  isUserAuthoredBlog,
  updateMyProfile,
  updateMyRegion,
  uploadAvatarToCloudinary,
} from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type {
  BlogResponse,
  AuthUser,
  CafePageResponse,
  ProfileContentTab,
  RecommendationCardResponse,
  UserPostPreview,
  UserRegionUpdateRequest,
  UserResponse,
  UserUpdateRequest,
} from "../../types";
import { t } from "../../features/i18n";

const emptyTabPosts: Record<ProfileContentTab, UserPostPreview[]> = {
  posts: [],
  saved: [],
  shared: [],
  tagged: [],
};

const PROFILE_CONTENT_TABS: ProfileContentTab[] = ["posts", "saved", "shared", "tagged"];
const PROFILE_SCROLL_LOAD_MORE_THRESHOLD = 220;
const PROFILE_TAB_INITIAL_VISIBLE_COUNT = 9;
const PROFILE_TAB_LOAD_MORE_COUNT = 6;

const initialVisiblePostCounts: Record<ProfileContentTab, number> = {
  posts: PROFILE_TAB_INITIAL_VISIBLE_COUNT,
  saved: PROFILE_TAB_INITIAL_VISIBLE_COUNT,
  shared: PROFILE_TAB_INITIAL_VISIBLE_COUNT,
  tagged: PROFILE_TAB_INITIAL_VISIBLE_COUNT,
};

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
  return formatCurrentCompactNumber(value ?? 0);
}

function getEmptyCopy(tab: ProfileContentTab) {
  switch (tab) {
    case "saved":
      return {
        description: t("profile.empty.saved.description"),
        title: t("profile.empty.saved.title"),
      };
    case "shared":
      return {
        description: t("profile.empty.shared.description"),
        title: t("profile.empty.shared.title"),
      };
    case "tagged":
      return {
        description: t("profile.empty.tagged.description"),
        title: t("profile.empty.tagged.title"),
      };
    default:
      return {
        description: t("profile.empty.posts.description"),
        title: t("profile.empty.posts.title"),
      };
  }
}

function loadBlogsForProfileTab(tab: ProfileContentTab, userId: string): Promise<BlogResponse[]> {
  switch (tab) {
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

function timestampForPost(post: UserPostPreview) {
  const timestamp = post.createdAt ? Date.parse(post.createdAt) : 0;

  return Number.isNaN(timestamp) ? 0 : timestamp;
}

function sortProfileTabPosts(tab: ProfileContentTab, posts: UserPostPreview[]) {
  if (tab !== "posts") {
    return posts;
  }

  return [...posts].sort((leftPost, rightPost) => {
    const pinnedDelta = Number(Boolean(rightPost.isPinned)) - Number(Boolean(leftPost.isPinned));

    if (pinnedDelta !== 0) {
      return pinnedDelta;
    }

    return timestampForPost(rightPost) - timestampForPost(leftPost);
  });
}

function isOwnedActiveCafePage(page: CafePageResponse) {
  return page.status === "ACTIVE" && page.pageActive === true;
}

function selectOwnedCafePage(pages: CafePageResponse[]) {
  return pages.find(isOwnedActiveCafePage) ?? pages.find((page) => page.id) ?? null;
}

function linkedCafePageId(
  profile: UserResponse | null | undefined,
  user: AuthUser | null | undefined,
) {
  return profile?.pageId ?? profile?.cafePageId ?? user?.pageId ?? user?.cafePageId ?? null;
}

function hasCafePageRole(user: AuthUser | null | undefined) {
  return Boolean(
    user?.roles?.some((role) => {
      const normalizedRole = normalizeRole(role);

      return normalizedRole === "CAFE_PAGE" || normalizedRole === "CAFE";
    }),
  );
}

function hasReviewerRole(user: AuthUser | null | undefined) {
  return Boolean(
    user?.roles?.some((role) => normalizeRole(role) === "REVIEWER"),
  );
}

function normalizeRole(role: string) {
  return role.replace(/^ROLE_/, "").toUpperCase();
}

export function ProfileScreen() {
  const navigation =
    useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const { user } = useAuth();
  const isProfileLoadInFlightRef = useRef(false);
  const loadMoreMarkerRef = useRef<string | null>(null);
  const preloadingTabsRef = useRef<Set<ProfileContentTab>>(new Set());
  const [profile, setProfile] = useState<UserResponse | null>(null);
  const [ownedCafePage, setOwnedCafePage] = useState<CafePageResponse | null>(null);
  const [tabPosts, setTabPosts] =
    useState<Record<ProfileContentTab, UserPostPreview[]>>(emptyTabPosts);
  const [visiblePostCounts, setVisiblePostCounts] =
    useState<Record<ProfileContentTab, number>>(initialVisiblePostCounts);
  const [loadedTabs, setLoadedTabs] =
    useState<Partial<Record<ProfileContentTab, boolean>>>({});
  const [error, setError] = useState<string | null>(null);
  const [contentError, setContentError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isContentLoading, setIsContentLoading] = useState(false);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [isBioModalVisible, setIsBioModalVisible] = useState(false);
  const [bioError, setBioError] = useState<string | null>(null);
  const [isSavingBio, setIsSavingBio] = useState(false);
  const [isEditProfileVisible, setIsEditProfileVisible] = useState(false);
  const [editProfileError, setEditProfileError] = useState<string | null>(null);
  const [isSavingProfile, setIsSavingProfile] = useState(false);
  const [isUploadingAvatar, setIsUploadingAvatar] = useState(false);
  const [isLocationModalVisible, setIsLocationModalVisible] = useState(false);
  const [locationError, setLocationError] = useState<string | null>(null);
  const [isSavingLocation, setIsSavingLocation] = useState(false);
  const [isSuggestionsVisible, setIsSuggestionsVisible] = useState(false);
  const [suggestions, setSuggestions] = useState<RecommendationCardResponse[]>([]);
  const [dismissedSuggestionIds, setDismissedSuggestionIds] = useState<string[]>([]);
  const [followedSuggestionIds, setFollowedSuggestionIds] = useState<string[]>([]);
  const [suggestionError, setSuggestionError] = useState<string | null>(null);
  const [isLoadingSuggestions, setIsLoadingSuggestions] = useState(false);
  const [pendingSuggestionId, setPendingSuggestionId] = useState<string | null>(null);
  const [activeContentTab, setActiveContentTab] =
    useState<ProfileContentTab>("posts");

  const loadOwnedCafePage = useCallback(async (nextProfile: UserResponse) => {
    const selectedPage = selectOwnedCafePage(
      await getCafePagesByOwner(nextProfile.userId),
    );

    if (selectedPage) {
      return selectedPage;
    }

    const cafePageId = linkedCafePageId(nextProfile, user);

    if (!cafePageId) {
      return null;
    }

    return getCafePageById(cafePageId);
  }, [user?.cafePageId, user?.pageId]);

  const loadProfile = useCallback(async (refreshing = false) => {
    if (isProfileLoadInFlightRef.current) {
      return;
    }

    isProfileLoadInFlightRef.current = true;

    if (refreshing) {
      setIsRefreshing(true);
    } else {
      setIsLoading(true);
    }

    try {
      const nextProfile = await getMyProfile();

      setProfile(nextProfile);
      setError(null);

      try {
        setOwnedCafePage(await loadOwnedCafePage(nextProfile));
      } catch {
        setOwnedCafePage(null);
      }
    } catch (nextError) {
      setOwnedCafePage(null);
      setError(
        nextError instanceof Error
          ? nextError.message
          : "Unable to load your profile.",
      );
    } finally {
      isProfileLoadInFlightRef.current = false;
      setIsLoading(false);
      setIsRefreshing(false);
    }
  }, [loadOwnedCafePage]);

  const loadContentTab = useCallback(async (
    tab: ProfileContentTab,
    userId: string,
    options: { refreshing?: boolean; silent?: boolean } = {},
  ) => {
    const { refreshing = false, silent = false } = options;

    if (refreshing) {
      setIsRefreshing(true);
    } else if (!silent) {
      setIsContentLoading(true);
    }

    try {
      const blogs = await loadBlogsForProfileTab(tab, userId);
      const userBlogs = blogs.filter(isUserAuthoredBlog);
      const nextPosts = sortProfileTabPosts(
        tab,
        userBlogs.map(blogResponseToPostPreview),
      );

      setTabPosts((currentPosts) => ({
        ...currentPosts,
        [tab]: nextPosts,
      }));
      setVisiblePostCounts((currentCounts) => ({
        ...currentCounts,
        [tab]: PROFILE_TAB_INITIAL_VISIBLE_COUNT,
      }));
      setLoadedTabs((currentTabs) => ({
        ...currentTabs,
        [tab]: true,
      }));
      if (!silent) {
        setContentError(null);
      }
    } catch (nextError) {
      if (!silent) {
        setContentError(
          nextError instanceof Error
            ? nextError.message
            : "Unable to load this profile section.",
        );
      }
    } finally {
      if (!silent) {
        setIsContentLoading(false);
      }
      if (refreshing) {
        setIsRefreshing(false);
      }
    }
  }, []);

  useEffect(() => {
    void loadProfile();
  }, [loadProfile]);

  const onRefresh = useCallback(() => {
    const currentUserId = profile?.userId ?? user?.userId;

    void loadProfile(true);

    if (!currentUserId) {
      return;
    }

    void loadContentTab(activeContentTab, currentUserId, { refreshing: true });
  }, [activeContentTab, loadContentTab, loadProfile, profile?.userId, user?.userId]);

  const loadSuggestions = useCallback(async () => {
    setIsLoadingSuggestions(true);
    setSuggestionError(null);

    try {
      const nextSuggestions = await getMixedRecommendations(0, 12);

      setSuggestions(nextSuggestions);
    } catch (nextError) {
      setSuggestionError(
        nextError instanceof Error
          ? nextError.message
          : "Unable to load suggestions.",
      );
    } finally {
      setIsLoadingSuggestions(false);
    }
  }, []);

  const toggleSuggestions = useCallback(() => {
    setIsSuggestionsVisible((currentValue) => {
      const nextValue = !currentValue;

      if (nextValue && suggestions.length === 0 && !isLoadingSuggestions) {
        void loadSuggestions();
      }

      return nextValue;
    });
  }, [isLoadingSuggestions, loadSuggestions, suggestions.length]);

  const dismissSuggestion = useCallback((suggestion: RecommendationCardResponse) => {
    setDismissedSuggestionIds((currentIds) =>
      currentIds.includes(suggestion.targetId)
        ? currentIds
        : [...currentIds, suggestion.targetId],
    );
  }, []);

  const followSuggestion = useCallback(async (suggestion: RecommendationCardResponse) => {
    if (pendingSuggestionId || !["USER", "REVIEWER", "CAFE_PAGE"].includes(suggestion.targetType)) {
      return;
    }

    setPendingSuggestionId(suggestion.targetId);

    try {
      if (suggestion.targetType === "CAFE_PAGE") {
        await followCafePage(suggestion.targetId);
      } else {
        await followUser(suggestion.userId ?? suggestion.targetId);
      }
      setFollowedSuggestionIds((currentIds) =>
        currentIds.includes(suggestion.targetId)
          ? currentIds
          : [...currentIds, suggestion.targetId],
      );
      setDismissedSuggestionIds((currentIds) =>
        currentIds.includes(suggestion.targetId)
          ? currentIds
          : [...currentIds, suggestion.targetId],
      );
    } catch (nextError) {
      setSuggestionError(
        nextError instanceof Error
          ? nextError.message
          : "Unable to follow this user.",
      );
    } finally {
      setPendingSuggestionId(null);
    }
  }, [pendingSuggestionId]);

  const openSuggestionProfile = useCallback((suggestion: RecommendationCardResponse) => {
    if (suggestion.targetType === "CAFE_PAGE") {
      navigation.navigate(routes.cafeDetail, {
        cafeId: suggestion.targetId,
      });
      return;
    }

    if (suggestion.targetType !== "USER" && suggestion.targetType !== "REVIEWER") {
      return;
    }

    navigation.navigate(routes.otherUserProfile, {
      userId: suggestion.userId ?? suggestion.targetId,
      userName: suggestion.username,
    });
  }, [navigation]);

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

  const openLocationModal = useCallback(() => {
    setLocationError(null);
    setIsLocationModalVisible(true);
  }, []);

  const closeLocationModal = useCallback(() => {
    if (!isSavingLocation) {
      setIsLocationModalVisible(false);
      setLocationError(null);
    }
  }, [isSavingLocation]);

  const saveLocation = useCallback(async (request: UserRegionUpdateRequest) => {
    setIsSavingLocation(true);
    setLocationError(null);

    try {
      const nextProfile = await updateMyRegion(request);

      setProfile(nextProfile);
      setIsLocationModalVisible(false);
    } catch (nextError) {
      setLocationError(
        nextError instanceof Error
          ? nextError.message
          : "Unable to update your location.",
      );
    } finally {
      setIsSavingLocation(false);
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
      const uploadedAvatarUrl = await uploadAvatarToCloudinary({
        name: asset.fileName ?? `avatar-${Date.now()}.jpg`,
        type: asset.mimeType ?? "image/jpeg",
        uri: asset.uri,
      });
      const nextProfile = await updateMyProfile({
        userAvatar: uploadedAvatarUrl,
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
      cafePageId: user.cafePageId ?? null,
      followingCount: null,
      isFollowing: false,
      pageId: user.pageId ?? null,
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

  const displayName = activeProfile?.userFullName || activeProfile?.userName || t("profile.fallback.user");
  const userName = activeProfile?.userName || "cafestory";
  const avatarUri = activeProfile?.userAvatar;
  const userDescription = activeProfile?.userDescription?.trim();
  const stats = useMemo(
    () => [
      { key: "posts", label: t("profile.stat.posts"), displayLabel: t("profile.stat.posts"), value: formatCount(tabPosts.posts.length) },
      { key: "followers", label: t("profile.stat.followers"), displayLabel: t("profile.stat.followersCompact"), value: formatCount(activeProfile?.userFollower) },
      { key: "following", label: t("profile.stat.following"), displayLabel: t("profile.stat.followingCompact"), value: formatCount(activeProfile?.followingCount) },
    ],
    [activeProfile?.followingCount, activeProfile?.userFollower, tabPosts.posts.length],
  );
  const regionLabel = useMemo(
    () =>
      [
        activeProfile?.regionProvince,
        activeProfile?.regionWard,
      ]
        .filter(Boolean)
        .join(", "),
    [
      activeProfile?.regionProvince,
      activeProfile?.regionWard,
    ],
  );
  const visibleSuggestions = useMemo(
    () =>
      suggestions.filter(
        (suggestion) =>
          (suggestion.targetType === "USER" || suggestion.targetType === "CAFE_PAGE") &&
          suggestion.targetId !== activeProfile?.userId &&
          !dismissedSuggestionIds.includes(suggestion.targetId),
      ),
    [activeProfile?.userId, dismissedSuggestionIds, suggestions],
  );
  const activeTabPosts = tabPosts[activeContentTab];
  const activeVisiblePostCount =
    visiblePostCounts[activeContentTab] ?? PROFILE_TAB_INITIAL_VISIBLE_COUNT;
  const visiblePosts = activeTabPosts.slice(0, activeVisiblePostCount);
  const visibleEmptyCopy = getEmptyCopy(activeContentTab);
  const ownedCafePageId = ownedCafePage?.id ?? linkedCafePageId(activeProfile, user);
  const shouldShowCafePageAction = hasCafePageRole(user) || Boolean(ownedCafePageId);
  const shouldShowAdsManagerAction = Boolean(
    ownedCafePage && isOwnedActiveCafePage(ownedCafePage),
  );
  const shouldShowReviewerDashboardAction = hasReviewerRole(user);

  const openOwnedCafePage = useCallback(() => {
    if (!ownedCafePageId) {
      Alert.alert(
        t("Cafe page"),
        t("Cafe page is not available yet."),
      );
      return;
    }

    navigation.navigate(routes.cafeDetail, {
      cafeId: ownedCafePageId,
    });
  }, [navigation, ownedCafePageId]);

  const openPayment = useCallback(() => {
    navigation.navigate(routes.paymentOptions, {
      initialTab: "reviewer",
    });
  }, [navigation]);

  const openAdsManager = useCallback(() => {
    navigation.navigate(routes.adsManager);
  }, [navigation]);

  const openReviewerDashboard = useCallback(() => {
    navigation.navigate(routes.reviewerDashboard);
  }, [navigation]);

  const openUserPosts = useCallback((post?: UserPostPreview) => {
    if (!activeProfile?.userId) {
      return;
    }

    navigation.navigate(routes.userPosts, {
      contentTab: activeContentTab,
      initialBlogId: post?.id,
      userId: activeProfile.userId,
      userName,
    });
  }, [activeContentTab, activeProfile?.userId, navigation, userName]);

  const openProfileFollows = useCallback((initialTab: "followers" | "following") => {
    if (!activeProfile?.userId) {
      return;
    }

    navigation.navigate(routes.profileFollows, {
      initialTab,
      userId: activeProfile.userId,
      userName,
    });
  }, [activeProfile?.userId, navigation, userName]);

  const loadMoreVisiblePosts = useCallback(() => {
    const currentCount =
      visiblePostCounts[activeContentTab] ?? PROFILE_TAB_INITIAL_VISIBLE_COUNT;
    const totalCount = tabPosts[activeContentTab].length;

    if (currentCount >= totalCount) {
      return;
    }

    const marker = `${activeContentTab}:${currentCount}:${totalCount}`;

    if (loadMoreMarkerRef.current === marker) {
      return;
    }

    loadMoreMarkerRef.current = marker;
    setVisiblePostCounts((currentCounts) => {
      const nextCurrentCount =
        currentCounts[activeContentTab] ?? PROFILE_TAB_INITIAL_VISIBLE_COUNT;

      return {
        ...currentCounts,
        [activeContentTab]: Math.min(
          nextCurrentCount + PROFILE_TAB_LOAD_MORE_COUNT,
          tabPosts[activeContentTab].length,
        ),
      };
    });
    setTimeout(() => {
      if (loadMoreMarkerRef.current === marker) {
        loadMoreMarkerRef.current = null;
      }
    }, 250);
  }, [activeContentTab, tabPosts, visiblePostCounts]);

  const handleProfileScroll = useCallback((
    event: NativeSyntheticEvent<NativeScrollEvent>,
  ) => {
    const { contentOffset, contentSize, layoutMeasurement } = event.nativeEvent;
    const distanceFromBottom =
      contentSize.height - (contentOffset.y + layoutMeasurement.height);

    if (distanceFromBottom <= PROFILE_SCROLL_LOAD_MORE_THRESHOLD) {
      loadMoreVisiblePosts();
    }
  }, [loadMoreVisiblePosts]);

  useEffect(() => {
    const currentUserId = profile?.userId;

    if (!currentUserId || loadedTabs[activeContentTab]) {
      return;
    }

    void loadContentTab(activeContentTab, currentUserId);
  }, [activeContentTab, loadContentTab, loadedTabs, profile?.userId]);

  useEffect(() => {
    const currentUserId = profile?.userId;

    if (!currentUserId) {
      return;
    }

    for (const tab of PROFILE_CONTENT_TABS) {
      if (
        tab === activeContentTab ||
        loadedTabs[tab] ||
        preloadingTabsRef.current.has(tab)
      ) {
        continue;
      }

      preloadingTabsRef.current.add(tab);
      void loadContentTab(tab, currentUserId, { silent: true }).finally(() => {
        preloadingTabsRef.current.delete(tab);
      });
    }
  }, [activeContentTab, loadContentTab, loadedTabs, profile?.userId]);

  if (isLoading && !profile) {
    return (
      <Screen padded={false}>
        <ProfileTopBar
          onAdsManagerPress={openAdsManager}
          onCafePagePress={openOwnedCafePage}
          onMessagePress={() => navigation.navigate(routes.conversations)}
          onPaymentPress={openPayment}
          onReviewerDashboardPress={openReviewerDashboard}
          onSettingsPress={() => navigation.navigate(routes.settings)}
          showAdsManagerAction={shouldShowAdsManagerAction}
          showCafePageAction={shouldShowCafePageAction}
          showReviewerDashboardAction={shouldShowReviewerDashboardAction}
          userName={userName}
        />
        <ProfileSkeleton />
      </Screen>
    );
  }

  return (
    <Screen padded={false}>
      <ProfileTopBar
        onAdsManagerPress={openAdsManager}
        onCafePagePress={openOwnedCafePage}
        onMessagePress={() => navigation.navigate(routes.conversations)}
        onPaymentPress={openPayment}
        onReviewerDashboardPress={openReviewerDashboard}
        onSettingsPress={() => navigation.navigate(routes.settings)}
        showAdsManagerAction={shouldShowAdsManagerAction}
        showCafePageAction={shouldShowCafePageAction}
        showReviewerDashboardAction={shouldShowReviewerDashboardAction}
        userName={userName}
      />

      <ScrollView
        contentContainerStyle={styles.content}
        onScroll={handleProfileScroll}
        refreshControl={
          <RefreshControl refreshing={isRefreshing} onRefresh={onRefresh} />
        }
        scrollEventThrottle={16}
        showsVerticalScrollIndicator={false}
      >
        {error ? (
          <View style={styles.errorBanner}>
            <Text style={styles.errorText}>{error}</Text>
          </View>
        ) : null}

        <View style={styles.identity}>
          <View style={styles.avatarColumn}>
            <Avatar initials={initialsFor(displayName)} size={88} uri={avatarUri} />
            {regionLabel ? (
              <Text numberOfLines={2} style={styles.regionText}>
                {regionLabel}
              </Text>
            ) : null}
          </View>

          <View style={styles.identityContent}>
            <Text numberOfLines={1} style={styles.title}>
              {displayName}
            </Text>

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
                    pressed && stat.key !== "posts" && styles.actionPressed,
                  ]}
                >
                  <Text style={styles.statValue}>{stat.value}</Text>
                  <Text numberOfLines={2} style={styles.statLabel}>{stat.displayLabel}</Text>
                </Pressable>
              ))}
            </View>
          </View>
        </View>

        {userDescription ? (
          <Pressable
            accessibilityLabel={t("Edit profile bio")}
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
            accessibilityLabel={t("Add profile description")}
            accessibilityRole="button"
            onPress={openBioModal}
            style={({ pressed }) => [
              styles.descriptionPrompt,
              pressed && styles.actionPressed,
            ]}
          >
            <Pencil color={colors.muted} size={16} strokeWidth={2.3} />
            <Text style={styles.descriptionPromptText}>{t("Add a short description about you here")}</Text>
          </Pressable>
        )}

        {shouldShowCafePageAction ? (
          <Pressable
            accessibilityLabel={t("Open owned cafe page")}
            accessibilityRole="button"
            onPress={openOwnedCafePage}
            style={({ pressed }) => [
              styles.cafePageTag,
              pressed && styles.actionPressed,
            ]}
          >
            <Store color={colors.primary} size={16} strokeWidth={2.5} />
            <Text numberOfLines={1} style={styles.cafePageTagName}>
              {ownedCafePage?.name || t("profile.cafe.view")}
            </Text>
          </Pressable>
        ) : null}

        <View style={styles.profileChips}>
          <View style={styles.profileChip}>
            <AtSign color={colors.foreground} size={16} strokeWidth={2.4} />
            <Text numberOfLines={1} style={styles.profileChipText}>
              {userName}
            </Text>
          </View>

          {shouldShowReviewerDashboardAction ? (
            <Pressable
              accessibilityLabel={t("Open reviewer dashboard")}
              accessibilityRole="button"
              onPress={openReviewerDashboard}
              style={({ pressed }) => [
                styles.profileChip,
                styles.reviewerChip,
                pressed && styles.actionPressed,
              ]}
            >
              <Award color={colors.tertiaryStrong} size={16} strokeWidth={2.5} />
              <Text numberOfLines={1} style={styles.reviewerChipText}>{t("Reviewer")}</Text>
            </Pressable>
          ) : null}

          <Pressable
            accessibilityLabel={t("Open profile suggestions")}
            accessibilityRole="button"
            onPress={toggleSuggestions}
            style={({ pressed }) => [
              styles.profileChip,
              pressed && styles.actionPressed,
            ]}
          >
            <Plus color={colors.muted} size={18} strokeWidth={2.4} />
            <Text style={styles.profileChipMuted}>{t("Add")}</Text>
          </Pressable>
        </View>

        <View style={styles.actions}>
          <Pressable
            accessibilityLabel={t("Edit profile")}
            accessibilityRole="button"
            onPress={openEditProfile}
            style={({ pressed }) => [
              styles.profileActionButton,
              pressed && styles.actionPressed,
            ]}
          >
            <Text style={styles.profileActionText}>{t("Edit Profile")}</Text>
          </Pressable>

          <Pressable
            accessibilityLabel={t("Share profile")}
            accessibilityRole="button"
            style={({ pressed }) => [
              styles.profileActionButton,
              pressed && styles.actionPressed,
            ]}
          >
            <Text style={styles.profileActionText}>{t("Share Profile")}</Text>
          </Pressable>

          <Pressable
            accessibilityLabel={t("Open profile suggestions")}
            accessibilityRole="button"
            onPress={toggleSuggestions}
            style={({ pressed }) => [
              styles.addFriendButton,
              pressed && styles.actionPressed,
            ]}
          >
            <UserPlus color={colors.foreground} size={19} strokeWidth={2.5} />
          </Pressable>
        </View>

        {isSuggestionsVisible ? (
          <ProfileSuggestions
            disabledUserIds={[
              ...followedSuggestionIds,
              ...(pendingSuggestionId ? [pendingSuggestionId] : []),
            ]}
            error={suggestionError}
            isLoading={isLoadingSuggestions}
            onDismiss={dismissSuggestion}
            onFollow={followSuggestion}
            onProfilePress={openSuggestionProfile}
            suggestions={visibleSuggestions}
          />
        ) : null}

        <ProfileContentTabs
          activeTab={activeContentTab}
          onChange={setActiveContentTab}
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
          ) : visiblePosts.length > 0 ? (
            <UserPostGrid
              onPostPress={openUserPosts}
              posts={visiblePosts}
              showPinBadges={activeContentTab === "posts"}
            />
          ) : (
            <View style={styles.emptyPosts}>
              <EmptyState
                description={visibleEmptyCopy.description}
                title={visibleEmptyCopy.title}
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
        onLocationPress={openLocationModal}
        onSave={saveProfile}
        profile={activeProfile}
        visible={isEditProfileVisible}
      />

      <LocationEditorModal
        error={locationError}
        isSaving={isSavingLocation}
        onClose={closeLocationModal}
        onSave={saveLocation}
        profile={activeProfile}
        visible={isLocationModalVisible}
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

  avatarColumn: {
    alignItems: "center",
    gap: spacing.xs,
    width: 104,
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

  regionText: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "700",
    lineHeight: 16,
    textAlign: "center",
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
    alignItems: "center",
    flex: 1,
    flexBasis: 0,
    gap: 2,
    minWidth: 0,
    paddingHorizontal: 2,
  },

  statValue: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },

  statLabel: {
    color: colors.muted,
    fontSize: 11,
    flexShrink: 1,
    lineHeight: 14,
    maxWidth: 64,
    textAlign: "center",
    width: "100%",
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

  reviewerChip: {
    backgroundColor: colors.tertiarySoft,
    borderColor: colors.tertiary,
  },

  reviewerChipText: {
    color: colors.tertiaryStrong,
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
