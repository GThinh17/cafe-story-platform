export type MessageContact = {
  name: string;
  initials: string;
};

export type MessageDockData = {
  title: string;
  unreadCount: number;
  contacts: MessageContact[];
};

export type ChatMessageType = "TEXT" | "IMAGE" | "STICKER" | "MIXED";
export type LocalMessageStatus = "sent" | "sending" | "error";
export type LocalConversationStatus = "creating" | "ready" | "error";

export type ChatMemberResponse = {
  id: string;
  userAvatar: string | null;
  userFullName: string | null;
  userId: string;
  userName: string | null;
  role: "OWNER" | "ADMIN" | "MEMBER" | string;
  joinedAt: string | null;
};

export type ConversationResponse = {
  id: string;
  type: "DIRECT" | "GROUP" | string;
  groupName: string | null;
  groupAvatar: string | null;
  chatName: string | null;
  userName: string | null;
  chatAvatar: string | null;
  latestMessageId: string | null;
  latestMessagePreview: string | null;
  lastMessage: string | null;
  lastMessageAt: string | null;
  isRead: boolean;
  unreadCount: number;
  createdAt: string | null;
  updatedAt: string | null;
  members: ChatMemberResponse[];
};

export type ChatMessageResponse = {
  id: string;
  conversationId: string;
  senderId: string;
  type: ChatMessageType;
  text: string | null;
  imageUrls: string[] | null;
  stickerUrl: string | null;
  stickerId: string | null;
  createdAt: string | null;
  updatedAt: string | null;
  status: string | null;
  isRead: boolean;
  readAt: string | null;
};

export type SendChatMessageRequest = {
  type: ChatMessageType;
  text?: string;
  imageUrls?: string[];
  stickerUrl?: string;
  stickerId?: string;
};

export type ChatMessage = {
  id: string;
  serverId?: string;
  author: "me" | "them";
  body?: string;
  imageUrls?: string[];
  stickerUrl?: string;
  stickerId?: string;
  type: ChatMessageType;
  time: string;
  timestamp?: number;
  localStatus?: LocalMessageStatus;
};

export type Conversation = {
  id: string;
  serverId?: string;
  participantUserId: string;
  name: string;
  username: string;
  avatarImage: string;
  preview: string;
  time: string;
  active?: boolean;
  unread?: boolean;
  isTemporary?: boolean;
  hasMessages?: boolean;
  localStatus?: LocalConversationStatus;
  initials?: string;
  status?: string;
};

export type MessageThread = {
  recipientName: string;
  recipientUsername: string;
  recipientAvatar: string;
  recipientStatus: string;
  messages: ChatMessage[];
};

export type SendMessageDraft = {
  text: string;
  file: File | null;
};

// WebSocket STOMP event types
export type SocketEventType =
  | "receive_message"
  | "message_sent"
  | "message_failed"
  | "join_conversation"
  | "leave_conversation"
  | "typing_start"
  | "typing_stop";

export type SocketEvent = {
  type: SocketEventType;
  conversationId: string | null;
  userId: string | null;
  data: ChatMessageResponse | string | null;
};
