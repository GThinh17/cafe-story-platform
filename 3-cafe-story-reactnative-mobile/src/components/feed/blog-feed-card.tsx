import {
  Bookmark,
  Heart,
  MessageSquare,
  MoreHorizontal,
  Send,
} from "lucide-react-native";
import { useState } from "react";
import {
  Pressable,
  StyleSheet,
  Text,
  View,
} from "react-native";

import { Avatar } from "../ui/avatar";
import { CommentModal } from "./comment-modal";
import { MobilePostCarousel } from "./mobile-post-carousel";
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

function getDisplayName(blog: BlogFeedResponse) {
  return (
    firstNonBlank(
      blog.displayName,
      blog.pageName,
      blog.authorUserName,
      blog.authorUserFullName,
    ) ||
    "CafeStory"
  );
}

function firstNonBlank(...values: Array<string | null | undefined>) {
  return values.find((value) => value?.trim())?.trim() ?? null;
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
  const [isCommentModalVisible, setIsCommentModalVisible] = useState(false);
  const displayName = getDisplayName(blog);
  const images = (
    blog.imageUrls?.length ? blog.imageUrls : [blog.pageCoverUrl]
  ).filter((uri): uri is string => Boolean(uri));

  return (
    <View style={styles.card}>
      <View style={styles.header}>
        <View style={styles.author}>
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
        </View>
        <Pressable accessibilityLabel="Open post options" accessibilityRole="button">
          <MoreHorizontal color={colors.foreground} size={22} strokeWidth={2.4} />
        </Pressable>
      </View>

      <MobilePostCarousel
        imageAccessibilityLabel={`${displayName} post image`}
        imageUrls={images}
      />

      <View style={styles.actionsBlock}>
        <View style={styles.actionsRow}>
          <View style={styles.leftActions}>
            <Pressable accessibilityLabel="Like post" accessibilityRole="button">
              <Heart color={colors.foreground} size={24} strokeWidth={2.2} />
            </Pressable>
            <Pressable
              accessibilityLabel="Comment on post"
              accessibilityRole="button"
              onPress={() => setIsCommentModalVisible(true)}
            >
              <MessageSquare color={colors.foreground} size={24} strokeWidth={2.2} />
            </Pressable>
            <Pressable accessibilityLabel="Share post" accessibilityRole="button">
              <Send color={colors.foreground} size={23} strokeWidth={2.2} />
            </Pressable>
          </View>
          <Pressable accessibilityLabel="Save post" accessibilityRole="button">
            <Bookmark color={colors.foreground} size={24} strokeWidth={2.2} />
          </Pressable>
        </View>

        <Text style={styles.likes}>{compactCount(blog.likeCount)} likes</Text>
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
            View all {compactCount(blog.commentCount)} comments
          </Text>
        </Pressable>
        <Text style={styles.meta}>
          {formatTimeAgo(blog.createdAt)}
          {blog.shareCount ? ` · ${compactCount(blog.shareCount)} SHARES` : ""}
          {blog.rankPosition ? ` · #${blog.rankPosition}` : ""}
        </Text>
      </View>
      <CommentModal
        blogId={blog.blogId}
        onClose={() => setIsCommentModalVisible(false)}
        postAuthorName={displayName}
        visible={isCommentModalVisible}
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
  header: {
    alignItems: "center",
    flexDirection: "row",
    justifyContent: "space-between",
    minHeight: 60,
    paddingHorizontal: spacing.lg,
    paddingVertical: 2,
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
});
