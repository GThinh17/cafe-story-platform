export type NotificationType =
  | "LIKE"
  | "SHARE"
  | "COMMENT"
  | "MESSAGE"
  | "FOLLOW"
  | "TAG"
  | string;

export type NotificationTargetType = "BLOG" | "CONVERSATION" | "USER" | string;

export type NavigationTargetResponse = {
  action: string | null;
  targetId: string | null;
  targetType: NotificationTargetType | null;
};

export type NotificationResponse = {
  actorId: string | null;
  blogId: string | null;
  commentId: string | null;
  conversationId: string | null;
  createdAt: string | null;
  id: string;
  isRead: boolean | null;
  messageId: string | null;
  navigation: NavigationTargetResponse | null;
  recipientId: string | null;
  type: NotificationType;
  updatedAt: string | null;
  userId: string | null;
};

export type UnreadCountResponse = {
  unreadCount: number;
};
