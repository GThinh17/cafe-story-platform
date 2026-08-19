import * as ImagePicker from "expo-image-picker";
import { Pressable } from "react-native";
import { Text } from "react-native";
import { RouteProp, useNavigation, useRoute } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import {
  ArrowLeft, Grid3X3, MessageCircle, MessageSquareText, SquarePlus, UserPlus, Users, } from "lucide-react-native";
import { useCallback, useEffect, useMemo, useState } from "react";
import { RefreshControl, ScrollView, StyleSheet, View } from "react-native";

import {
  CafePageHeader,
  EditCafePageModal,
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
  createCafePageConversation,
  followCafePage,
  getCafePageBlogs,
  getCafePageById,
  likeCafePage,
  unfollowCafePage,
  unlikeCafePage,
  updateCafePage,
  uploadCafeImageToCloudinary,
} from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type { CafePageResponse, CafePageUpdateRequest, UserPostPreview } from "../../types";
import { t } from "../../features/i18n";

type CafePageRouteProp = RouteProp<RootStackParamList, typeof routes.cafeDetail>;
type CafePageContentTab = "posts" | "reviews" | "members" | "requests";

function adjustCount(value: number | null | undefined, delta: number) {
  return Math.max(0, (value ?? 0) + delta);
}

const contentTabs: Array<{
  icon: typeof Grid3X3;
  key: CafePageContentTab;
  label: string;
}> = [
  { icon: Grid3X3, key: "posts", label: "Posts" },
  { icon: MessageSquareText, key: "reviews", label: "Reviews" },
  { icon: Users, key: "members", label: "Members" },
  { icon: UserPlus, key: "requests", label: "Requests" },
];

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
  const [isMessagePending, setIsMessagePending] = useState(false);
  const [isEditModalVisible, setIsEditModalVisible] = useState(false);
  const [isEditSaving, setIsEditSaving] = useState(false);
  const [isUploadingAvatar, setIsUploadingAvatar] = useState(false);
  const [isUploadingCover, setIsUploadingCover] = useState(false);
  const [editError, setEditError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<CafePageContentTab>("posts");

  const isOwner = useMemo(
    () => Boolean(cafePage?.ownerUserId && cafePage.ownerUserId === user?.userId),
    [cafePage?.ownerUserId, user?.userId],
  );
  const canManageCafePage = Boolean(isOwner || cafePage?.canManage);

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

  const showComingSoon = useCallback((feature: string) => {
    setError(`${feature} is coming soon.`);
  }, []);

  const cafeLocationName = useMemo(() => {
    if (!cafePage) {
      return null;
    }

    return [
      cafePage.regionWard,
      cafePage.regionCity,
      cafePage.regionProvince,
    ]
      .filter(Boolean)
      .join(", ") || cafePage.address || null;
  }, [cafePage]);

  const openEditModal = useCallback(() => {
    if (!isOwner) {
      return;
    }

    setEditError(null);
    setIsEditModalVisible(true);
  }, [isOwner]);

  const closeEditModal = useCallback(() => {
    if (isEditSaving || isUploadingAvatar || isUploadingCover) {
      return;
    }

    setIsEditModalVisible(false);
    setEditError(null);
  }, [isEditSaving, isUploadingAvatar, isUploadingCover]);

  const pickCafeImage = useCallback(async (imageType: "avatar" | "cover") => {
    if (!cafePage) {
      return;
    }

    const permission = await ImagePicker.requestMediaLibraryPermissionsAsync();

    if (!permission.granted) {
      setEditError(
        imageType === "avatar"
          ? "Photo access is required to update the cafe avatar."
          : "Photo access is required to update the cafe cover.",
      );
      return;
    }

    const result = await ImagePicker.launchImageLibraryAsync({
      allowsEditing: true,
      aspect: imageType === "avatar" ? [1, 1] : [16, 9],
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      quality: 0.85,
    });

    if (result.canceled || !result.assets[0]?.uri) {
      return;
    }

    const asset = result.assets[0];

    try {
      setEditError(null);
      if (imageType === "avatar") {
        setIsUploadingAvatar(true);
      } else {
        setIsUploadingCover(true);
      }

      const uploadedUrl = await uploadCafeImageToCloudinary(
        {
          name: asset.fileName ?? `cafe-${imageType}-${Date.now()}.jpg`,
          type: asset.mimeType ?? "image/jpeg",
          uri: asset.uri,
        },
        imageType,
      );

      setCafePage((currentPage) =>
        currentPage
          ? {
              ...currentPage,
              avatarUrl: imageType === "avatar" ? uploadedUrl : currentPage.avatarUrl,
              coverUrl: imageType === "cover" ? uploadedUrl : currentPage.coverUrl,
            }
          : currentPage,
      );
    } catch (nextError) {
      setEditError(
        nextError instanceof Error
          ? nextError.message
          : `Unable to upload cafe ${imageType}.`,
      );
    } finally {
      if (imageType === "avatar") {
        setIsUploadingAvatar(false);
      } else {
        setIsUploadingCover(false);
      }
    }
  }, [cafePage]);

  const saveCafePage = useCallback(async (request: CafePageUpdateRequest) => {
    if (!cafePage || isEditSaving) {
      return;
    }

    setIsEditSaving(true);
    setEditError(null);

    try {
      const updatedPage = await updateCafePage(cafePage.id, request);
      setCafePage(updatedPage);
      setIsEditModalVisible(false);
      setError(null);
    } catch (nextError) {
      setEditError(
        nextError instanceof Error
          ? nextError.message
          : "Unable to update this cafe page.",
      );
    } finally {
      setIsEditSaving(false);
    }
  }, [cafePage, isEditSaving]);

  const openCafePageMessages = useCallback(async () => {
    if (!cafePage || isMessagePending) {
      return;
    }

    if (canManageCafePage) {
      navigation.navigate(routes.conversations, {
        cafePageId: cafePage.id,
        cafePageName: cafePage.name,
      });
      return;
    }

    setIsMessagePending(true);
    setError(null);

    try {
      const conversation = await createCafePageConversation(cafePage.id);

      navigation.navigate(routes.chatDetail, {
        chatAvatar: conversation.chatAvatar || cafePage.avatarUrl,
        chatName: conversation.chatName || cafePage.name || "Cafe Page",
        conversationId: conversation.id,
        targetCafePageId: conversation.targetCafePageId ?? cafePage.id,
        targetType: conversation.targetType ?? "CAFE_PAGE",
        targetUserId: null,
        userName: conversation.userName || cafePage.name || "",
      });
    } catch (nextError) {
      setError(
        nextError instanceof Error
          ? nextError.message
          : "Unable to open cafe page messages.",
      );
    } finally {
      setIsMessagePending(false);
    }
  }, [cafePage, canManageCafePage, isMessagePending, navigation]);

  const openCafePagePostComposer = useCallback(() => {
    if (!cafePage || !canManageCafePage) {
      return;
    }

    navigation.navigate(routes.main, {
      params: {
        cafeAvatarUrl: cafePage.avatarUrl,
        cafePageId: cafePage.id,
        cafePageName: cafePage.name,
        locationName: cafeLocationName,
        regionId: cafePage.regionId,
      },
      screen: routes.create,
    });
  }, [cafeLocationName, cafePage, canManageCafePage, navigation]);

  const renderTabContent = () => {
    if (activeTab === "posts") {
      return posts.length > 0 ? (
        <View style={styles.tabContent}>
          <View style={styles.grid}>
            <UserPostGrid onPostPress={openPost} posts={posts} />
          </View>
        </View>
      ) : (
        <View style={[styles.tabContent, styles.emptyPosts]}>
          <EmptyState
            description={t("Posts from this cafe page will appear here.")}
            title={t("No cafe posts yet")}
          />
        </View>
      );
    }

    if (activeTab === "reviews") {
      return (
        <View style={[styles.tabContent, styles.emptyPosts]}>
          <EmptyState
            description={t("Cafe reviews will appear here when reviewers publish them.")}
            title={t("No reviews yet")}
          />
        </View>
      );
    }

    if (activeTab === "members") {
      return (
        <View style={[styles.tabContent, styles.emptyPosts]}>
          <EmptyState
            description={t("Page members will appear here when member APIs are connected.")}
            title={t("No members to show")}
          />
        </View>
      );
    }

    return (
      <View style={[styles.tabContent, styles.emptyPosts]}>
        <EmptyState
          description={t("Join requests will appear here for page owners.")}
          title={t("No requests right now")}
        />
      </View>
    );
  };

  return (
    <Screen padded={false}>
      <View style={styles.topBar}>
        <Pressable
          accessibilityLabel={t("Back")}
          accessibilityRole="button"
          hitSlop={10}
          onPress={() => navigation.goBack()}
          style={({ pressed }) => [styles.iconButton, pressed && styles.pressed]}
        >
          <ArrowLeft color={colors.foreground} size={30} strokeWidth={2.5} />
        </Pressable>

        <Text numberOfLines={1} style={styles.topBarTitle}>{t("Cafe Page")}</Text>

        {canManageCafePage ? (
          <View style={styles.topBarActions}>
            <Pressable
              accessibilityLabel={t("Create cafe page post")}
              accessibilityRole="button"
              hitSlop={10}
              onPress={openCafePagePostComposer}
              style={({ pressed }) => [styles.iconButton, pressed && styles.pressed]}
            >
              <SquarePlus color={colors.foreground} size={27} strokeWidth={2.5} />
            </Pressable>

            <Pressable
              accessibilityLabel={t("Open cafe page messages")}
              accessibilityRole="button"
              hitSlop={10}
              onPress={() => {
                void openCafePageMessages();
              }}
              style={({ pressed }) => [styles.iconButton, pressed && styles.pressed]}
            >
              <MessageCircle color={colors.foreground} size={28} strokeWidth={2.5} />
            </Pressable>
          </View>
        ) : (
          <View style={styles.iconButton} />
        )}
      </View>

      {isLoading && !cafePage ? (
        <LoadingState label={t("Loading cafe page...")} />
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
              canManage={canManageCafePage}
              isFollowPending={isFollowPending}
              isLikePending={isLikePending}
              isOwner={isOwner}
              onEditPress={openEditModal}
              onFollowPress={toggleFollow}
              onLikePress={toggleLike}
              onMessagePress={() => {
                void openCafePageMessages();
              }}
              onSharePress={() => showComingSoon("Cafe page sharing")}
              onSuggestPress={() => showComingSoon("Cafe page suggestions")}
            />
          ) : (
            <View style={styles.emptyPage}>
              <EmptyState
                description={t("Pull down to refresh and try again.")}
                title={t("Cafe page unavailable")}
              />
            </View>
          )}

          <View style={styles.tabBar}>
            {contentTabs.map((tab) => {
              const Icon = tab.icon;
              const isActive = activeTab === tab.key;

              return (
                <Pressable
                  accessibilityLabel={`Open cafe page ${tab.label.toLowerCase()}`}
                  accessibilityRole="tab"
                  key={tab.key}
                  onPress={() => setActiveTab(tab.key)}
                  style={({ pressed }) => [
                    styles.tabItem,
                    isActive && styles.tabItemActive,
                    pressed && styles.pressed,
                  ]}
                >
                  <Icon
                    color={isActive ? colors.foreground : colors.muted}
                    size={23}
                    strokeWidth={2.4}
                  />
                  <Text
                    numberOfLines={1}
                    style={[
                      styles.tabLabel,
                      isActive && styles.tabLabelActive,
                    ]}
                  >
                    {tab.label}
                  </Text>
                </Pressable>
              );
            })}
          </View>

          {renderTabContent()}
        </ScrollView>
      )}

      <EditCafePageModal
        cafePage={cafePage}
        error={editError}
        isSaving={isEditSaving}
        isUploadingAvatar={isUploadingAvatar}
        isUploadingCover={isUploadingCover}
        onAvatarPress={() => {
          void pickCafeImage("avatar");
        }}
        onClose={closeEditModal}
        onCoverPress={() => {
          void pickCafeImage("cover");
        }}
        onSave={(request) => {
          void saveCafePage(request);
        }}
        visible={isEditModalVisible}
      />
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
  tabBar: {
    borderBottomColor: colors.border,
    borderBottomWidth: 1,
    borderTopColor: colors.border,
    borderTopWidth: 1,
    flexDirection: "row",
  },
  tabContent: {
    paddingTop: spacing.sm,
  },
  tabItem: {
    alignItems: "center",
    borderBottomColor: "transparent",
    borderBottomWidth: 2,
    flex: 1,
    gap: 3,
    height: 58,
    justifyContent: "center",
  },
  tabItemActive: {
    borderBottomColor: colors.foreground,
  },
  tabLabel: {
    color: colors.muted,
    fontSize: 10,
    fontWeight: "800",
  },
  tabLabelActive: {
    color: colors.foreground,
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
  topBarActions: {
    flexDirection: "row",
  },
});
