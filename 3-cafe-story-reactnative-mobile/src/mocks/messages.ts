import type { MockChatMessage, MockConversation, MockOnlineUser } from "../types";

export const mockOnlineUsers: MockOnlineUser[] = [
  {
    avatarUri:
      "https://images.unsplash.com/photo-1544725176-7c40e5a71c5e?auto=format&fit=crop&w=180&q=80",
    id: "online-1",
    isOnline: true,
    name: "An",
  },
  {
    avatarUri:
      "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=180&q=80",
    id: "online-2",
    isOnline: true,
    name: "Mia",
  },
  {
    avatarUri:
      "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=180&q=80",
    id: "online-3",
    isOnline: true,
    name: "Thinh",
  },
  {
    avatarUri:
      "https://images.unsplash.com/photo-1547425260-76bcadfb4f2c?auto=format&fit=crop&w=180&q=80",
    id: "online-4",
    isOnline: true,
    name: "Khoa",
  },
];

export const mockConversations: MockConversation[] = [
  {
    avatarUri:
      "https://images.unsplash.com/photo-1544725176-7c40e5a71c5e?auto=format&fit=crop&w=180&q=80",
    id: "conversation-1",
    isOnline: true,
    lastMessage: "I saved that cafe for tonight.",
    name: "An Nguyen",
    time: "2m",
    userName: "an.nguyen",
  },
  {
    avatarUri:
      "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=180&q=80",
    id: "conversation-2",
    lastMessage: "The matcha latte looks perfect.",
    name: "Mia Tran",
    time: "18m",
    userName: "mia.tran",
  },
  {
    avatarUri:
      "https://images.unsplash.com/photo-1547425260-76bcadfb4f2c?auto=format&fit=crop&w=180&q=80",
    id: "conversation-3",
    isMuted: true,
    lastMessage: "Let's meet near the window seats.",
    name: "Khoa Le",
    time: "1h",
    userName: "khoa.le",
  },
  {
    avatarUri:
      "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=180&q=80",
    id: "conversation-4",
    lastMessage: "Thanks for the recommendation.",
    name: "Linh Pham",
    time: "3h",
    userName: "linh.pham",
  },
];

export const mockChatMessages: Record<string, MockChatMessage[]> = {
  "conversation-1": [
    {
      id: "m-1",
      isMine: false,
      text: "Are you still going to The Hidden Bean tonight?",
      time: "18:12",
    },
    {
      id: "m-2",
      isMine: true,
      text: "Yes, I want to try the balcony table.",
      time: "18:13",
    },
    {
      id: "m-3",
      isMine: false,
      text: "I saved that cafe for tonight.",
      time: "18:14",
    },
    {
      id: "m-4",
      isMine: true,
      text: "Great. I will send the post after dinner.",
      time: "18:15",
    },
  ],
  "conversation-2": [
    {
      id: "m-5",
      isMine: false,
      text: "The matcha latte looks perfect.",
      time: "17:42",
    },
    {
      id: "m-6",
      isMine: true,
      text: "The foam was really smooth.",
      time: "17:44",
    },
  ],
  "conversation-3": [
    {
      id: "m-7",
      isMine: false,
      text: "Let's meet near the window seats.",
      time: "16:30",
    },
  ],
  "conversation-4": [
    {
      id: "m-8",
      isMine: false,
      text: "Thanks for the recommendation.",
      time: "14:06",
    },
    {
      id: "m-9",
      isMine: true,
      text: "Anytime. Their cold brew is worth it.",
      time: "14:08",
    },
  ],
};
