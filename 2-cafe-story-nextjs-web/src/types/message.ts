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
  userId: string;
  role: "OWNER" | "ADMIN" | "MEMBER" | string;
  joinedAt: string | null;
};

export type ConversationResponse = {
  id: string;
  type: "DIRECT" | "GROUP" | string;
  groupName: string | null;
  groupAvatar: string | null;
  latestMessageId: string | null;
  latestMessagePreview: string | null;
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
