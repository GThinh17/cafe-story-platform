import { apiFetch } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type {
  ChatMessageResponse,
  ConversationResponse,
  SendChatMessageRequest,
} from "@/types/message";

type MessagePageOptions = {
  page?: number;
  size?: number;
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

export function getConversationMessages(
  conversationId: string,
  { page = 0, size = 20 }: MessagePageOptions = {},
) {
  const searchParams = new URLSearchParams({
    page: String(page),
    size: String(size),
  });

  return apiFetch<ChatMessageResponse[]>(
    `${apiEndpoints.chat.messages(conversationId)}?${searchParams.toString()}`,
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
