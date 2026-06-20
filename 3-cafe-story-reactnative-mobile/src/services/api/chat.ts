import type {
  ChatSenderContextType,
  ChatMessageResponse,
  ConversationResponse,
  ChatMessageType,
} from "../../types";
import { apiCacheTtl, cachedApiCall, invalidateApiCache } from "./api-cache";
import { apiFetch } from "./client";
import { apiEndpoints } from "./endpoints";

type MessagePageOptions = {
  page?: number;
  size?: number;
};

export type SendChatMessageRequest = {
  imageUrls?: string[];
  senderCafePageId?: string;
  senderContextType?: ChatSenderContextType;
  stickerId?: string;
  stickerUrl?: string;
  text?: string;
  type: ChatMessageType;
};

export function getConversations() {
  return cachedApiCall("chat:conversations", apiCacheTtl.chatActive, () =>
    apiFetch<ConversationResponse[]>(apiEndpoints.chat.conversations, {
      method: "GET",
    }),
  );
}

export async function createDirectConversation(secondUserId: string) {
  const response = await apiFetch<ConversationResponse>(apiEndpoints.chat.directConversation, {
    body: { secondUserId },
    method: "POST",
  });
  invalidateApiCache("chat:conversations");
  return response;
}

export async function createCafePageConversation(cafePageId: string) {
  const response = await apiFetch<ConversationResponse>(apiEndpoints.chat.cafePageConversation, {
    body: { cafePageId },
    method: "POST",
  });
  invalidateApiCache("chat:conversations");
  return response;
}

export function getConversationMessages(
  conversationId: string,
  { page = 0, size = 20 }: MessagePageOptions = {},
) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  });

  const path = `${apiEndpoints.chat.messages(conversationId)}?${params.toString()}`;
  const ttl = page === 0 ? apiCacheTtl.chatActive : apiCacheTtl.chatHistory;

  return cachedApiCall(`chat:messages:${conversationId}:${page}:${size}`, ttl, () =>
    apiFetch<ChatMessageResponse[]>(path, {
      method: "GET",
    }),
  );
}

export async function sendChatMessage(
  conversationId: string,
  request: SendChatMessageRequest,
) {
  const response = await apiFetch<ChatMessageResponse>(
    apiEndpoints.chat.messages(conversationId),
    {
      body: request,
      method: "POST",
    },
  );
  invalidateApiCache("chat:conversations");
  invalidateApiCache(`chat:messages:${conversationId}:`);
  return response;
}
