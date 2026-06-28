"use client";

import { useRouter } from "next/navigation";
import { ActivityList } from "@/components/notification/activity-list";
import { useCommentModal } from "@/context/comment-modal-context";
import { useNotifications } from "@/hooks/use-notifications";
import type { NotificationResponse } from "@/types/notification";

export default function NotificationsPage() {
  const router = useRouter();
  const commentModal = useCommentModal();
  const state = useNotifications();
  const { actors, markRead } = state;

  function handleNotificationClick(notification: NotificationResponse) {
    if (!notification.isRead) void markRead(notification.id);

    switch (notification.type) {
      case "MESSAGE":
        if (notification.conversationId) {
          router.push(`/messages?conversationId=${notification.conversationId}`);
        }
        break;
      case "LIKE":
      case "SHARE":
      case "COMMENT":
      case "TAG":
        if (notification.blogId) commentModal.openByBlogId(notification.blogId);
        break;
      case "FOLLOW": {
        const actor = actors[notification.actorId];
        if (actor?.userName) {
          router.push(`/${encodeURIComponent(actor.userName)}`);
        }
        break;
      }
    }
  }

  return (
    <ActivityList
      notifications={state.notifications}
      unreadCount={state.unreadCount}
      isLoading={state.isLoading}
      error={state.error}
      activeFilter={state.activeFilter}
      actors={state.actors}
      onFilterChange={state.setActiveFilter}
      onMarkAllRead={state.markAllRead}
      onItemClick={handleNotificationClick}
    />
  );
}
