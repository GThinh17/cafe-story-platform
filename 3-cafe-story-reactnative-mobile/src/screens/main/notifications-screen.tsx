import { useFocusEffect, useNavigation } from "@react-navigation/native";
import type { BottomTabNavigationProp } from "@react-navigation/bottom-tabs";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import {
  Bell,
  Bookmark,
  CheckCheck,
  Heart,
  MessageCircle,
  Tag,
  Trash2,
  UserPlus,
} from "lucide-react-native";
import { useCallback, useMemo, useState } from "react";
import {
  Pressable,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";

import { Button, EmptyState, LoadingState, Screen } from "../../components";
import { routes } from "../../navigation";
import type { MainTabParamList, RootStackParamList } from "../../navigation";
import {
  deleteNotification,
  getNotifications,
  getUnreadNotificationCount,
  markAllNotificationsRead,
  markNotificationRead,
} from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type { NotificationResponse } from "../../types";

const NOTIFICATION_PAGE_SIZE = 30;

function formatNotificationTitle(notification: NotificationResponse) {
  switch (notification.type) {
    case "LIKE":
      return "New like";
    case "COMMENT":
      return "New comment";
    case "SHARE":
      return "Post shared";
    case "MESSAGE":
      return "New message";
    case "FOLLOW":
      return "New follower";
    case "TAG":
      return "You were tagged";
    default:
      return "Notification";
  }
}

function formatNotificationDescription(notification: NotificationResponse) {
  switch (notification.type) {
    case "LIKE":
      return "Someone liked your cafe story.";
    case "COMMENT":
      return "Someone commented on your post.";
    case "SHARE":
      return "Someone shared your post.";
    case "MESSAGE":
      return "Open the conversation to continue chatting.";
    case "FOLLOW":
      return "Someone started following your profile.";
    case "TAG":
      return "Open the tagged post to view the mention.";
    default:
      return "Open this notification for more details.";
  }
}

function formatTime(value: string | null) {
  if (!value) {
    return "Just now";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "Just now";
  }

  const diffMinutes = Math.max(0, Math.floor((Date.now() - date.getTime()) / 60000));
  if (diffMinutes < 1) {
    return "Just now";
  }
  if (diffMinutes < 60) {
    return `${diffMinutes}m ago`;
  }

  const diffHours = Math.floor(diffMinutes / 60);
  if (diffHours < 24) {
    return `${diffHours}h ago`;
  }

  return `${Math.floor(diffHours / 24)}d ago`;
}

function getNotificationIcon(type: string) {
  switch (type) {
    case "LIKE":
      return Heart;
    case "COMMENT":
      return MessageCircle;
    case "SHARE":
      return Bookmark;
    case "MESSAGE":
      return MessageCircle;
    case "FOLLOW":
      return UserPlus;
    case "TAG":
      return Tag;
    default:
      return Bell;
  }
}

export function NotificationsScreen() {
  const navigation =
    useNavigation<
      BottomTabNavigationProp<MainTabParamList, typeof routes.notifications> &
        NativeStackNavigationProp<RootStackParamList>
    >();
  const [notifications, setNotifications] = useState<NotificationResponse[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [error, setError] = useState("");
  const [isInitialLoading, setIsInitialLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [isMutating, setIsMutating] = useState(false);

  const unreadLabel = useMemo(() => {
    if (!unreadCount) {
      return "All caught up";
    }

    return `${unreadCount} unread`;
  }, [unreadCount]);

  const loadNotifications = useCallback(async (refreshing = false) => {
    if (refreshing) {
      setIsRefreshing(true);
    } else {
      setIsInitialLoading(true);
    }
    setError("");

    try {
      const [nextNotifications, nextUnread] = await Promise.all([
        getNotifications({ limit: NOTIFICATION_PAGE_SIZE, page: 0 }),
        getUnreadNotificationCount(),
      ]);
      setNotifications(nextNotifications);
      setUnreadCount(nextUnread.unreadCount ?? 0);
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to load notifications.",
      );
    } finally {
      setIsInitialLoading(false);
      setIsRefreshing(false);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      void loadNotifications();
    }, [loadNotifications]),
  );

  const handleOpenNotification = useCallback(async (notification: NotificationResponse) => {
    if (!notification.isRead) {
      setNotifications((currentNotifications) =>
        currentNotifications.map((item) =>
          item.id === notification.id ? { ...item, isRead: true } : item,
        ),
      );
      setUnreadCount((currentCount) => Math.max(0, currentCount - 1));
      void markNotificationRead(notification.id).catch(() => {
        void loadNotifications(true);
      });
    }

    const navigationTarget = notification.navigation;
    const targetId = navigationTarget?.targetId;

    if (!navigationTarget?.targetType || !targetId) {
      return;
    }

    if (navigationTarget.targetType === "BLOG") {
      navigation.navigate(routes.blogDetail, {
        blogId: targetId,
      });
      return;
    }

    if (navigationTarget.targetType === "CONVERSATION") {
      navigation.navigate(routes.chatDetail, {
        conversationId: targetId,
      });
      return;
    }

    if (navigationTarget.targetType === "USER") {
      navigation.navigate(routes.otherUserProfile, {
        userId: targetId,
      });
    }
  }, [loadNotifications, navigation]);

  const handleMarkAllRead = useCallback(async () => {
    if (!unreadCount || isMutating) {
      return;
    }

    setIsMutating(true);
    try {
      await markAllNotificationsRead();
      setNotifications((currentNotifications) =>
        currentNotifications.map((notification) => ({
          ...notification,
          isRead: true,
        })),
      );
      setUnreadCount(0);
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to mark notifications as read.",
      );
    } finally {
      setIsMutating(false);
    }
  }, [isMutating, unreadCount]);

  const handleDeleteNotification = useCallback(async (notificationId: string) => {
    const deletedNotification = notifications.find((item) => item.id === notificationId);
    setNotifications((currentNotifications) =>
      currentNotifications.filter((item) => item.id !== notificationId),
    );
    if (deletedNotification && !deletedNotification.isRead) {
      setUnreadCount((currentCount) => Math.max(0, currentCount - 1));
    }

    try {
      await deleteNotification(notificationId);
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to delete notification.",
      );
      void loadNotifications(true);
    }
  }, [loadNotifications, notifications]);

  if (isInitialLoading && !notifications.length) {
    return (
      <Screen padded={false}>
        <Header unreadLabel={unreadLabel} />
        <LoadingState label="Loading notifications..." />
      </Screen>
    );
  }

  return (
    <Screen padded={false}>
      <Header
        onMarkAllRead={handleMarkAllRead}
        showMarkAll={unreadCount > 0}
        unreadLabel={unreadLabel}
      />
      <ScrollView
        contentContainerStyle={styles.content}
        refreshControl={
          <RefreshControl
            onRefresh={() => void loadNotifications(true)}
            refreshing={isRefreshing}
          />
        }
        showsVerticalScrollIndicator={false}
      >
        {error ? <Text style={styles.errorText}>{error}</Text> : null}
        {notifications.length ? (
          <View style={styles.list}>
            {notifications.map((notification) => (
              <NotificationRow
                key={notification.id}
                notification={notification}
                onDelete={handleDeleteNotification}
                onPress={handleOpenNotification}
              />
            ))}
          </View>
        ) : (
          <EmptyState
            description="Likes, follows, comments, and cafe updates will appear here."
            title="No notifications yet"
          />
        )}
      </ScrollView>
    </Screen>
  );
}

function Header({
  onMarkAllRead,
  showMarkAll = false,
  unreadLabel,
}: {
  onMarkAllRead?: () => void;
  showMarkAll?: boolean;
  unreadLabel: string;
}) {
  return (
    <View style={styles.header}>
      <View style={styles.headerCopy}>
        <Text style={styles.title}>Notifications</Text>
        <Text style={styles.subtitle}>{unreadLabel}</Text>
      </View>
      {showMarkAll ? (
        <Pressable
          accessibilityLabel="Mark all notifications as read"
          accessibilityRole="button"
          onPress={onMarkAllRead}
          style={({ pressed }) => [styles.markAllButton, pressed && styles.pressed]}
        >
          <CheckCheck color={colors.primary} size={20} strokeWidth={2.5} />
        </Pressable>
      ) : null}
    </View>
  );
}

function NotificationRow({
  notification,
  onDelete,
  onPress,
}: {
  notification: NotificationResponse;
  onDelete: (notificationId: string) => void;
  onPress: (notification: NotificationResponse) => void;
}) {
  const Icon = getNotificationIcon(notification.type);
  const isUnread = !notification.isRead;

  return (
    <Pressable
      accessibilityLabel={formatNotificationTitle(notification)}
      accessibilityRole="button"
      onPress={() => onPress(notification)}
      style={({ pressed }) => [
        styles.notificationRow,
        isUnread && styles.notificationRowUnread,
        pressed && styles.pressed,
      ]}
    >
      <View style={[styles.iconWrap, isUnread && styles.iconWrapUnread]}>
        <Icon
          color={isUnread ? colors.white : colors.primary}
          size={20}
          strokeWidth={2.5}
        />
      </View>
      <View style={styles.notificationCopy}>
        <Text numberOfLines={1} style={styles.notificationTitle}>
          {formatNotificationTitle(notification)}
        </Text>
        <Text numberOfLines={2} style={styles.notificationDescription}>
          {formatNotificationDescription(notification)}
        </Text>
        <Text style={styles.notificationTime}>
          {formatTime(notification.createdAt)}
        </Text>
      </View>
      <Pressable
        accessibilityLabel="Delete notification"
        accessibilityRole="button"
        hitSlop={8}
        onPress={() => onDelete(notification.id)}
        style={({ pressed }) => [styles.deleteButton, pressed && styles.pressed]}
      >
        <Trash2 color={colors.muted} size={18} strokeWidth={2.3} />
      </Pressable>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  content: {
    flexGrow: 1,
    padding: spacing.lg,
    paddingBottom: 112,
  },
  deleteButton: {
    alignItems: "center",
    height: 36,
    justifyContent: "center",
    width: 36,
  },
  errorText: {
    backgroundColor: colors.secondarySoft,
    borderRadius: 8,
    color: colors.primaryStrong,
    fontSize: typography.label,
    fontWeight: "800",
    lineHeight: 20,
    marginBottom: spacing.md,
    padding: spacing.md,
    textAlign: "center",
  },
  header: {
    alignItems: "center",
    backgroundColor: colors.background,
    borderBottomColor: colors.border,
    borderBottomWidth: StyleSheet.hairlineWidth,
    flexDirection: "row",
    gap: spacing.md,
    minHeight: 84,
    paddingHorizontal: spacing.lg,
  },
  headerCopy: {
    flex: 1,
    gap: spacing.xs,
  },
  iconWrap: {
    alignItems: "center",
    backgroundColor: colors.primarySoft,
    borderRadius: 20,
    height: 40,
    justifyContent: "center",
    width: 40,
  },
  iconWrapUnread: {
    backgroundColor: colors.primary,
  },
  list: {
    gap: spacing.sm,
  },
  markAllButton: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 20,
    borderWidth: 1,
    height: 40,
    justifyContent: "center",
    width: 40,
  },
  notificationCopy: {
    flex: 1,
    gap: spacing.xs,
  },
  notificationDescription: {
    color: colors.secondaryStrong,
    fontSize: typography.label,
    lineHeight: 20,
  },
  notificationRow: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    flexDirection: "row",
    gap: spacing.md,
    minHeight: 88,
    padding: spacing.md,
  },
  notificationRowUnread: {
    borderColor: colors.primary,
  },
  notificationTime: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "800",
  },
  notificationTitle: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },
  pressed: {
    opacity: 0.72,
  },
  subtitle: {
    color: colors.muted,
    fontSize: typography.label,
    fontWeight: "800",
  },
  title: {
    color: colors.foreground,
    fontSize: typography.title,
    fontWeight: "900",
  },
});
