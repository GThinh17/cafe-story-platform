import type {
  ChatMessageResponse,
  ConversationResponse,
  ChatMessageType,
} from "../../types";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

type MessagePageOptions = {
  page?: number;
  size?: number;
};

export type SendChatMessageRequest = {
  imageUrls?: string[];
  stickerId?: string;
  stickerUrl?: string;
  text?: string;
  type: ChatMessageType;
};

export function getConversations() {
  return apiFetch<ConversationResponse[]>(apiEndpoints.chat.conversations, {
    method: "GET",
  });
}

export function createDirectConversation(secondUserId: string) {
  return apiFetch<ConversationResponse>(apiEndpoints.chat.directConversation, {
    body: { secondUserId },
    method: "POST",
  });
}

export function createCafePageConversation(cafePageId: string) {
  return apiFetch<ConversationResponse>(apiEndpoints.chat.cafePageConversation, {
    body: { cafePageId },
    method: "POST",
  });
}

export function getConversationMessages(
  conversationId: string,
  { page = 0, size = 20 }: MessagePageOptions = {},
) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  });

  return apiFetch<ChatMessageResponse[]>(
    `${apiEndpoints.chat.messages(conversationId)}?${params.toString()}`,
    {
      method: "GET",
    },
  );
}

export function sendChatMessage(
  conversationId: string,
  request: SendChatMessageRequest,
) {
  return apiFetch<ChatMessageResponse>(
    apiEndpoints.chat.messages(conversationId),
    {
      body: request,
      method: "POST",
    },
  );
}
