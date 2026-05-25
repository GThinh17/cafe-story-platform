export type MessageContact = {
  name: string;
  initials: string;
};

export type MessageDockData = {
  title: string;
  unreadCount: number;
  contacts: MessageContact[];
};

export type Conversation = {
  id: string;
  name: string;
  username: string;
  initials: string;
  avatarImage: string;
  preview: string;
  time: string;
  status: string;
  active?: boolean;
  unread?: boolean;
};

export type ChatMessage = {
  id: string;
  author: "me" | "them";
  body: string;
  time: string;
};

export type MessageThread = {
  recipientName: string;
  recipientUsername: string;
  recipientAvatar: string;
  recipientStatus: string;
  messages: ChatMessage[];
};
