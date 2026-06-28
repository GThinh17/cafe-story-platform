export type NotificationType = "LIKE" | "SHARE" | "COMMENT" | "MESSAGE" | "FOLLOW" | "TAG";

export type NotificationNavigation = {
  targetType: "BLOG" | "CONVERSATION" | "USER";
  targetId: string;
  action: string;
};

export type NotificationResponse = {
  id: string;
  recipientId: string;
  actorId: string;
  type: NotificationType;
  blogId: string | null;
  conversationId: string | null;
  userId: string | null;
  commentId: string | null;
  messageId: string | null;
  isRead: boolean;
  createdAt: string;
  updatedAt: string | null;
  navigation: NotificationNavigation;
};

export type UnreadCountResponse = {
  unreadCount: number;
};

export type NotificationSocketEvent =
  | { type: "notification:new"; data: NotificationResponse }
  | { type: "notification:unread_count_updated"; data: UnreadCountResponse };
