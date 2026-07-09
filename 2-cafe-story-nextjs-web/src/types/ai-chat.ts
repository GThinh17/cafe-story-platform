export type AiChatRole = "user" | "assistant";

export type AiChatSource = {
  sourceType: string;
  sourceId: string;
  title: string;
  imageUrls: string[];
};

export type AiChatHistoryItem = {
  role: AiChatRole;
  content: string;
};

export type AskAssistantRequest = {
  query: string;
  platform?: "web" | "mobile";
  history?: AiChatHistoryItem[];
};

export type AskAssistantResponse = {
  answer: string;
  sources: AiChatSource[];
  cached?: boolean;
};
