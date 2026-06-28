import type {
  ChatMessage,
  Conversation,
  MessageDockData,
  MessageThread,
} from "@/types/message";

export const mockMessageDock: MessageDockData = {
  title: "Messages",
  unreadCount: 3,
  contacts: [
    { id: "mock-1", name: "Jessica Brew", initials: "JB" },
    { id: "mock-2", name: "Marco Explorer", initials: "ME" },
    { id: "mock-3", name: "Gia Thinh", initials: "GT" },
  ],
};

export const mockConversations: Conversation[] = [
  {
    id: "jessica",
    name: "Jessica Brew",
    participantUserId: "jessica",
    username: "jessica_brew",
    initials: "JB",
    avatarImage:
      "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=160&q=85",
    preview: "That roastery looks calm. Was it laptop friendly?",
    time: "2m",
    status: "Active now",
    active: true,
    unread: true,
  },
  {
    id: "marco",
    name: "Marco Explorer",
    participantUserId: "marco",
    username: "marco_explorer",
    initials: "ME",
    avatarImage:
      "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=160&q=85",
    preview: "Saved the list for Paris, thanks.",
    time: "18m",
    status: "Active 18m ago",
  },
  {
    id: "batch",
    name: "Batch Baby",
    participantUserId: "batch",
    username: "batchbaby",
    initials: "BB",
    avatarImage:
      "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=160&q=85",
    preview: "We have the Kenya filter back tomorrow.",
    time: "1h",
    status: "Cafe account",
  },
  {
    id: "nora",
    name: "Nora Cups",
    participantUserId: "nora",
    username: "nora_cups",
    initials: "NC",
    avatarImage:
      "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=160&q=85",
    preview: "Can you send the address?",
    time: "4h",
    status: "Active 4h ago",
  },
];

export const mockChatMessages: ChatMessage[] = [
  {
    id: "chat-1",
    author: "them",
    body: "That roastery looks calm. Was it laptop friendly?",
    time: "09:42",
    type: "TEXT",
  },
  {
    id: "chat-2",
    author: "me",
    body: "Yes, left wall has sockets and the music stayed soft.",
    time: "09:44",
    type: "TEXT",
  },
  {
    id: "chat-3",
    author: "them",
    body: "Perfect. Adding it to Saturday.",
    time: "09:45",
    type: "TEXT",
  },
];

export const mockMessageThread: MessageThread = {
  recipientName: "Jessica Brew",
  recipientUsername: "jessica_brew",
  recipientAvatar:
    "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=160&q=85",
  recipientStatus: "Active now",
  messages: mockChatMessages,
};
