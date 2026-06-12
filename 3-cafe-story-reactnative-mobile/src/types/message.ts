export type ChatMessageType = "TEXT" | "IMAGE" | "STICKER" | "MIXED";

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

export type MockOnlineUser = {
  avatarUri: string | null;
  id: string;
  isOnline: boolean;
  name: string;
};

export type MockConversation = {
  avatarUri: string | null;
  id: string;
  isMuted?: boolean;
  isOnline?: boolean;
  lastMessage: string;
  name: string;
  time: string;
  userName: string;
};

export type MockChatMessage = {
  id: string;
  isMine: boolean;
  text: string;
  time: string;
};

export type ConversationListItem = {
  avatarUri: string | null;
  id: string;
  isMuted?: boolean;
  isOnline?: boolean;
  lastMessage: string;
  name: string;
  time: string;
  userName: string;
};

export type ChatIdentity = {
  avatarUri: string | null;
  id: string;
  isOnline?: boolean;
  name: string;
  userName: string;
};

export type ChatMessageListItem = {
  id: string;
  isMine: boolean;
  text: string;
  time: string;
};
