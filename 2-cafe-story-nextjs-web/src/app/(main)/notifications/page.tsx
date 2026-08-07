"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { ActivityList } from "@/components/notification/activity-list";
import { ModerationReasonDialog } from "@/components/notification/moderation-reason-dialog";
import { useCommentModal } from "@/context/comment-modal-context";
import { useNotifications } from "@/hooks/use-notifications";
import type {
  ModerationStatus,
  NotificationResponse,
} from "@/types/notification";

export default function NotificationsPage() {
  const router = useRouter();
  const commentModal = useCommentModal();
  const state = useNotifications();
  const { actors, markRead } = state;
  const [moderationDialog, setModerationDialog] = useState<{
    open: boolean;
    status: ModerationStatus | null;
    reason: string | null;
  }>({ open: false, status: null, reason: null });

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
      case "BLOG_MODERATION":
        if (notification.moderationStatus === "APPROVED") {
          if (notification.blogId) commentModal.openByBlogId(notification.blogId);
        } else {
          setModerationDialog({
            open: true,
            status: notification.moderationStatus,
            reason: notification.moderationReason,
          });
        }
        break;
    }
  }

  return (
    <>
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
      <ModerationReasonDialog
        open={moderationDialog.open}
        status={moderationDialog.status}
        reason={moderationDialog.reason}
        onClose={() =>
          setModerationDialog({ open: false, status: null, reason: null })
        }
      />
    </>
  );
}
