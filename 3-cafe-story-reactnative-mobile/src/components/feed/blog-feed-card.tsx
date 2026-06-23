import {
  Bookmark,
  Heart,
  MessageSquare,
  MoreHorizontal,
  Send,
} from "lucide-react-native";
import { useNavigation } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { useEffect, useState } from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";

import { Avatar } from "../ui/avatar";
import { CommentModal } from "./comment-modal";
import { MobilePostCarousel } from "./mobile-post-carousel";
import { PostOptionsModal } from "./post-options-modal";
import { ReportPostModal } from "./report-post-modal";
import { useAuth } from "../../features/auth";
import { routes } from "../../navigation";
import type { RootStackParamList } from "../../navigation";
import {
  followCafePage,
  followUser,
  likeBlog,
  saveBlog,
  shareBlog,
  unlikeBlog,
  unsaveBlog,
} from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type { BlogFeedResponse } from "../../types";

type BlogFeedCardProps = {
  blog: BlogFeedResponse;
};

function compactCount(value: number | null) {
  const count = value ?? 0;

  if (count >= 1000) {
    return `${(count / 1000).toFixed(count >= 10000 ? 0 : 1)}k`;
  }

  return String(count);
}

function firstNonBlank(...values: Array<string | null | undefined>) {
  return values.find((value) => value?.trim())?.trim() ?? null;
}

function getDisplayName(blog: BlogFeedResponse) {
  if (blog.displayAuthorType === "CAFE_PAGE") {
    return firstNonBlank(blog.pageName, blog.displayName) || "CafeStory";
  }

  return (
    firstNonBlank(
      blog.authorUserName,
      blog.displayName,
      blog.authorUserFullName,
    ) || "CafeStory"
  );
}

function getDisplayAvatar(blog: BlogFeedResponse) {
  return firstNonBlank(
    blog.displayAvatarUrl,
    blog.pageAvatarUrl,
    blog.authorUserAvatar,
    blog.authorAvatar,
  );
}

function getLocationLabel(blog: BlogFeedResponse) {
  const region = [blog.regionArea, blog.regionCity || blog.regionProvince]
    .filter(Boolean)
    .join(", ");

  return region || blog.pageAddress || "CafeStory";
}

function getInitials(name: string) {
  return name
    .split(/\s|_/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join("");
}

function formatTimeAgo(createdAt: string | null) {
  if (!createdAt) {
    return "JUST NOW";
  }

  const created = new Date(createdAt).getTime();

  if (Number.isNaN(created)) {
    return "JUST NOW";
  }

  const diffMinutes = Math.max(1, Math.floor((Date.now() - created) / 60000));

  if (diffMinutes < 60) {
    return `${diffMinutes} MINUTES AGO`;
  }

  const diffHours = Math.floor(diffMinutes / 60);

  if (diffHours < 24) {
    return `${diffHours} HOURS AGO`;
  }

  return `${Math.floor(diffHours / 24)} DAYS AGO`;
}

export function BlogFeedCard({ blog }: BlogFeedCardProps) {
  const navigation =
    useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const { user } = useAuth();
  const [isCommentModalVisible, setIsCommentModalVisible] = useState(false);
  const [isOptionsModalVisible, setIsOptionsModalVisible] = useState(false);
  const [isReportModalVisible, setIsReportModalVisible] = useState(false);
  const [isFollowPending, setIsFollowPending] = useState(false);
  const [isLikePending, setIsLikePending] = useState(false);
  const [isSavePending, setIsSavePending] = useState(false);
  const [isSharePending, setIsSharePending] = useState(false);
  const [isFollowed, setIsFollowed] = useState(Boolean(blog.isFollow));
  const [isLiked, setIsLiked] = useState(Boolean(blog.isLike));
  const [isSaved, setIsSaved] = useState(Boolean(blog.isSave));
  const [likeCount, setLikeCount] = useState(blog.likeCount ?? 0);
  const [commentCount, setCommentCount] = useState(blog.commentCount ?? 0);
  const [shareCount, setShareCount] = useState(blog.shareCount ?? 0);
  const displayName = getDisplayName(blog);
  const images = (
    blog.imageUrls?.length ? blog.imageUrls : [blog.pageCoverUrl]
  ).filter((uri): uri is string => Boolean(uri));
  const isOwnPost = user?.userId === blog.authorUserId;
  const canFollowAuthor = Boolean(blog.authorUserId) && !isOwnPost;
  const shouldShowFollowButton = canFollowAuthor && !isFollowed;
  const isFollowDisabled = !shouldShowFollowButton || isFollowPending;
  const canOpenAuthorProfile =
    blog.displayAuthorType === "CAFE_PAGE"
      ? Boolean(blog.pageId)
      : Boolean(blog.authorUserId) && !isOwnPost;

  useEffect(() => {
    setIsFollowed(Boolean(blog.isFollow));
  }, [blog.blogId, blog.isFollow]);

  useEffect(() => {
    setIsLiked(Boolean(blog.isLike));
  }, [blog.blogId, blog.isLike]);

  useEffect(() => {
    setIsSaved(Boolean(blog.isSave));
  }, [blog.blogId, blog.isSave]);

  useEffect(() => {
    setLikeCount(blog.likeCount ?? 0);
  }, [blog.blogId, blog.likeCount]);

  useEffect(() => {
    setCommentCount(blog.commentCount ?? 0);
  }, [blog.blogId, blog.commentCount]);

  useEffect(() => {
    setShareCount(blog.shareCount ?? 0);
  }, [blog.blogId, blog.shareCount]);

  async function handleToggleLike() {
    if (isLikePending) {
      return;
    }

    const nextIsLiked = !isLiked;
    setIsLikePending(true);
    setIsLiked(nextIsLiked);
    setLikeCount((currentCount) =>
      Math.max(0, currentCount + (nextIsLiked ? 1 : -1)),
    );

    try {
      if (nextIsLiked) {
        await likeBlog(blog.blogId);
      } else {
        await unlikeBlog(blog.blogId);
      }
    } catch {
      setIsLiked(!nextIsLiked);
      setLikeCount((currentCount) =>
        Math.max(0, currentCount + (nextIsLiked ? -1 : 1)),
      );
    } finally {
      setIsLikePending(false);
    }
  }

  async function handleToggleSave() {
    if (isSavePending) {
      return;
    }

    const nextIsSaved = !isSaved;
    setIsSavePending(true);
    setIsSaved(nextIsSaved);

    try {
      if (nextIsSaved) {
        await saveBlog(blog.blogId);
      } else {
        await unsaveBlog(blog.blogId);
      }
    } catch {
      setIsSaved(!nextIsSaved);
    } finally {
      setIsSavePending(false);
    }
  }

  async function handleShare() {
    if (isSharePending) {
      return;
    }

    setIsSharePending(true);
    setShareCount((currentCount) => currentCount + 1);

    try {
      await shareBlog(blog.blogId, { shareType: "PUBLIC" });
    } catch {
      setShareCount((currentCount) => Math.max(0, currentCount - 1));
    } finally {
      setIsSharePending(false);
    }
  }

  async function handleFollowAuthor() {
    if (isFollowDisabled) {
      return;
    }

    setIsFollowPending(true);
    const nextIsFollowed = true;
    setIsFollowed(nextIsFollowed);

    try {
      if (blog.displayAuthorType === "CAFE_PAGE" && blog.pageId) {
        await followCafePage(blog.pageId);
      } else {
        await followUser(blog.authorUserId);
      }
    } catch {
      setIsFollowed(!nextIsFollowed);
    } finally {
      setIsFollowPending(false);
    }
  }

  function handleOpenReport() {
    setIsOptionsModalVisible(false);
    setTimeout(() => {
      setIsReportModalVisible(true);
    }, 120);
  }

  function handleOpenAuthorProfile() {
    if (!canOpenAuthorProfile) {
      return;
    }

    if (blog.displayAuthorType === "CAFE_PAGE" && blog.pageId) {
      navigation.navigate(routes.cafeDetail, {
        cafeId: blog.pageId,
      });
      return;
    }

    navigation.navigate(routes.otherUserProfile, {
      userId: blog.authorUserId,
      userName: blog.authorUserName,
    });
  }

  return (
    <View style={styles.card}>
      <View style={styles.header}>
        <Pressable
          accessibilityLabel={`Open ${displayName} profile`}
          accessibilityRole="button"
          disabled={!canOpenAuthorProfile}
          onPress={handleOpenAuthorProfile}
          style={({ pressed }) => [
            styles.author,
            pressed && canOpenAuthorProfile && styles.pressed,
          ]}
        >
          <Avatar
            initials={getInitials(displayName)}
            size={36}
            uri={getDisplayAvatar(blog)}
          />
          <View style={styles.authorText}>
            <Text numberOfLines={1} style={styles.displayName}>
              {displayName}
            </Text>
            <Text numberOfLines={1} style={styles.location}>
              {getLocationLabel(blog)}
            </Text>
          </View>
        </Pressable>

        <View style={styles.headerActions}>
          {shouldShowFollowButton ? (
            <Pressable
              accessibilityLabel="Follow author"
              accessibilityRole="button"
              disabled={isFollowDisabled}
              onPress={handleFollowAuthor}
              style={({ pressed }) => [
                styles.followButton,
                pressed && !isFollowDisabled && styles.pressed,
              ]}
            >
              <Text style={styles.followButtonText}>Follow</Text>
            </Pressable>
          ) : null}
          <Pressable
            accessibilityLabel="Open post options"
            accessibilityRole="button"
            hitSlop={10}
            onPress={() => setIsOptionsModalVisible(true)}
            style={({ pressed }) => [
              styles.moreButton,
              pressed && styles.pressed,
            ]}
          >
            <MoreHorizontal color={colors.foreground} size={22} strokeWidth={2.4} />
          </Pressable>
        </View>
      </View>

      <MobilePostCarousel
        imageAccessibilityLabel={`${displayName} post image`}
        imageUrls={images}
      />

      <View style={styles.actionsBlock}>
        <View style={styles.actionsRow}>
          <View style={styles.leftActions}>
            <Pressable
              accessibilityLabel={isLiked ? "Unlike post" : "Like post"}
              accessibilityRole="button"
              disabled={isLikePending}
              onPress={handleToggleLike}
              style={styles.actionButton}
            >
              <Heart
                color={isLiked ? colors.danger : colors.foreground}
                fill={isLiked ? colors.danger : "none"}
                size={24}
                strokeWidth={2.2}
              />
            </Pressable>
            <Pressable
              accessibilityLabel="Comment on post"
              accessibilityRole="button"
              onPress={() => setIsCommentModalVisible(true)}
              style={styles.actionButton}
            >
              <MessageSquare color={colors.foreground} size={24} strokeWidth={2.2} />
            </Pressable>
            <Pressable
              accessibilityLabel="Share post"
              accessibilityRole="button"
              disabled={isSharePending}
              onPress={handleShare}
              style={styles.actionButton}
            >
              <Send color={colors.foreground} size={23} strokeWidth={2.2} />
            </Pressable>
          </View>
          <Pressable
            accessibilityLabel={isSaved ? "Unsave post" : "Save post"}
            accessibilityRole="button"
            disabled={isSavePending}
            onPress={handleToggleSave}
            style={styles.actionButton}
          >
            <Bookmark
              color={isSaved ? colors.primary : colors.foreground}
              fill={isSaved ? colors.primary : "none"}
              size={24}
              strokeWidth={2.2}
            />
          </Pressable>
        </View>

        <Text style={styles.likes}>{compactCount(likeCount)} likes</Text>
        <Text numberOfLines={2} style={styles.caption}>
          <Text style={styles.captionAuthor}>{displayName} </Text>
          {blog.contentPreview ?? ""}
        </Text>
        <Pressable
          accessibilityLabel="View post comments"
          accessibilityRole="button"
          onPress={() => setIsCommentModalVisible(true)}
        >
          <Text style={styles.comments}>
            View all {compactCount(commentCount)} comments
          </Text>
        </Pressable>
        <Text style={styles.meta}>
          {formatTimeAgo(blog.createdAt)}
          {shareCount ? ` - ${compactCount(shareCount)} SHARES` : ""}
          {blog.rankPosition ? ` - #${blog.rankPosition}` : ""}
        </Text>
      </View>
      <CommentModal
        blogId={blog.blogId}
        onCommentCreated={() => setCommentCount((currentCount) => currentCount + 1)}
        onClose={() => setIsCommentModalVisible(false)}
        postAuthorName={displayName}
        visible={isCommentModalVisible}
      />
      <PostOptionsModal
        isSavePending={isSavePending}
        isSaved={isSaved}
        onClose={() => setIsOptionsModalVisible(false)}
        onReport={handleOpenReport}
        onToggleSave={handleToggleSave}
        visible={isOptionsModalVisible}
      />
      <ReportPostModal
        blogId={blog.blogId}
        onClose={() => setIsReportModalVisible(false)}
        visible={isReportModalVisible}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  actionsBlock: {
    gap: spacing.xs,
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.lg,
  },
  actionsRow: {
    alignItems: "center",
    flexDirection: "row",
    justifyContent: "space-between",
  },
  actionButton: {
    alignItems: "center",
    height: 32,
    justifyContent: "center",
    width: 32,
  },
  author: {
    alignItems: "center",
    flex: 1,
    flexDirection: "row",
    gap: spacing.md,
  },
  authorText: {
    flex: 1,
  },
  caption: {
    color: colors.secondaryStrong,
    fontSize: typography.label,
    fontStyle: "italic",
    lineHeight: 20,
  },
  captionAuthor: {
    color: colors.foreground,
    fontStyle: "normal",
    fontWeight: "800",
  },
  card: {
    backgroundColor: colors.background,
    paddingBottom: spacing.xl,
    width: "100%",
  },
  comments: {
    color: colors.secondaryStrong,
    fontSize: typography.caption,
    fontWeight: "600",
    lineHeight: 16,
  },
  displayName: {
    color: colors.foreground,
    fontSize: typography.label,
    fontWeight: "800",
    lineHeight: 20,
  },
  followButton: {
    alignItems: "center",
    backgroundColor: colors.surfaceMuted,
    borderRadius: 14,
    justifyContent: "center",
    minHeight: 38,
    minWidth: 96,
    paddingHorizontal: spacing.lg,
  },
  followButtonText: {
    color: colors.foreground,
    fontSize: typography.label,
    fontWeight: "900",
  },
  header: {
    alignItems: "center",
    flexDirection: "row",
    justifyContent: "space-between",
    minHeight: 60,
    paddingHorizontal: spacing.lg,
    paddingVertical: 2,
  },
  headerActions: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.md,
  },
  leftActions: {
    alignItems: "center",
    flexDirection: "row",
    gap: 20,
  },
  likes: {
    color: colors.foreground,
    fontSize: typography.label,
    fontWeight: "800",
    lineHeight: 20,
  },
  location: {
    color: colors.secondaryStrong,
    fontSize: typography.caption,
    fontWeight: "600",
    lineHeight: 16,
  },
  meta: {
    color: colors.muted,
    fontSize: 10,
    fontWeight: "600",
    letterSpacing: 0.5,
    lineHeight: 15,
    textTransform: "uppercase",
  },
  moreButton: {
    alignItems: "center",
    height: 40,
    justifyContent: "center",
    width: 40,
  },
  pressed: {
    opacity: 0.72,
  },
});
