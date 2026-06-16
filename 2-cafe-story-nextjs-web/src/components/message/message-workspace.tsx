"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { ChatPanel } from "@/components/message/chat-panel";
import { ConversationList } from "@/components/message/conversation-list";
import {
  getMessageUserAvatarImage,
  getMessageUserDisplayName,
  getMessageUserInitials,
} from "@/components/message/message-user-utils";
import { useCurrentUser } from "@/hooks/use-current-user";
import {
  createDirectConversation,
  getConversationMessages,
  getConversations,
  sendChatMessage,
} from "@/lib/api/chat";
import { uploadChatImageToCloudinary } from "@/lib/api/cloudinary";
import { ApiError } from "@/lib/api/client";
import { getFollowingByUser, getUserById } from "@/lib/api/users";
import type {
  ChatMessage,
  ChatMessageResponse,
  ChatMessageType,
  Conversation,
  ConversationResponse,
  SendMessageDraft,
} from "@/types/message";
import type { UserResponse } from "@/types/user";
import type { Client, StompSubscription } from "@stomp/stompjs";
import { createStompClient } from "@/lib/api/websocket";
import type { SocketEvent } from "@/types/message";

function formatTime(value: string | null | undefined) {
  const date = value ? new Date(value) : new Date();

  if (Number.isNaN(date.getTime())) {
    return "";
  }

  return date.toLocaleTimeString("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
  });
}

function getOtherMemberId(
  conversation: ConversationResponse,
  currentUserId: string,
) {
  return conversation.members.find((member) => member.userId !== currentUserId)
    ?.userId;
}

function getApiErrorMessage(error: unknown, fallback: string) {
  return error instanceof ApiError || error instanceof Error
    ? error.message
    : fallback;
}

function createLocalId(prefix: string) {
  return `${prefix}-${crypto.randomUUID()}`;
}

function getSendType(text: string, hasImage: boolean): ChatMessageType {
  if (text && hasImage) {
    return "MIXED";
  }

  return hasImage ? "IMAGE" : "TEXT";
}

function getPreviewFromMessage(
  type: ChatMessageType,
  text?: string,
  imageUrls?: string[],
  stickerUrl?: string,
  stickerId?: string,
) {
  const trimmedText = text?.trim();

  if (trimmedText) {
    return trimmedText;
  }

  if (type === "IMAGE" || type === "MIXED" || imageUrls?.length) {
    return "Photo";
  }

  if (type === "STICKER" || stickerUrl || stickerId) {
    return "Sticker";
  }

  return "Message";
}

function mapUserToConversation(
  user: UserResponse,
  overrides: Partial<Conversation> = {},
): Conversation {
  const name = getMessageUserDisplayName(user);

  return {
    avatarImage: getMessageUserAvatarImage(user),
    id: user.userId,
    initials: getMessageUserInitials(name),
    localStatus: "ready",
    name,
    participantUserId: user.userId,
    preview: "Start a conversation",
    time: "",
    username: user.userName || "cafestory_user",
    ...overrides,
  };
}

function mapConversationResponseToConversation(
  conversation: ConversationResponse,
  participant: UserResponse,
): Conversation {
  return mapUserToConversation(participant, {
    hasMessages: Boolean(
      conversation.latestMessageId ||
        conversation.latestMessagePreview ||
        conversation.lastMessage ||
        conversation.lastMessageAt,
    ),
    id: conversation.id,
    localStatus: "ready",
    preview: conversation.latestMessagePreview || "Open conversation",
    serverId: conversation.id,
    time: formatTime(conversation.updatedAt),
  });
}

function mapMessageResponseToChatMessage(
  message: ChatMessageResponse,
  currentUserId: string,
): ChatMessage {
  const timestamp = message.createdAt
    ? new Date(message.createdAt).getTime()
    : undefined;

  return {
    author: message.senderId === currentUserId ? "me" : "them",
    body: message.text ?? undefined,
    id: message.id,
    imageUrls: message.imageUrls ?? undefined,
    localStatus: "sent",
    serverId: message.id,
    stickerId: message.stickerId ?? undefined,
    stickerUrl: message.stickerUrl ?? undefined,
    time: formatTime(message.createdAt),
    timestamp: timestamp && !Number.isNaN(timestamp) ? timestamp : undefined,
    type: message.type,
  };
}

function moveConversationToTop(
  conversations: Conversation[],
  conversationId: string,
) {
  const selectedConversation = conversations.find(
    (conversation) => conversation.id === conversationId,
  );

  if (!selectedConversation) {
    return conversations;
  }

  return [
    selectedConversation,
    ...conversations.filter((conversation) => conversation.id !== conversationId),
  ];
}

function upsertConversationAtTop(
  conversations: Conversation[],
  nextConversation: Conversation,
) {
  return [
    nextConversation,
    ...conversations.filter(
      (conversation) =>
        conversation.id !== nextConversation.id &&
        conversation.serverId !== nextConversation.serverId &&
        conversation.participantUserId !== nextConversation.participantUserId,
    ),
  ];
}

function replaceConversationInPlace(
  conversations: Conversation[],
  nextConversation: Conversation,
) {
  let didReplace = false;
  const replacedConversations = conversations.map((conversation) => {
    const shouldReplace =
      conversation.id === nextConversation.id ||
      conversation.serverId === nextConversation.serverId ||
      conversation.participantUserId === nextConversation.participantUserId;

    if (!shouldReplace) {
      return conversation;
    }

    didReplace = true;
    return nextConversation;
  });

  return didReplace ? replacedConversations : [...replacedConversations, nextConversation];
}

function mergeLoadedMessages(
  loadedMessages: ChatMessage[],
  currentMessages: ChatMessage[],
) {
  const loadedServerIds = new Set(
    loadedMessages
      .map((message) => message.serverId)
      .filter((serverId): serverId is string => Boolean(serverId)),
  );
  const localMessagesToKeep = currentMessages.filter(
    (message) => !message.serverId || !loadedServerIds.has(message.serverId),
  );

  return [...loadedMessages, ...localMessagesToKeep];
}

function hasVisibleMessages(
  conversation: Conversation,
  messagesByConversationId: Record<string, ChatMessage[]>,
) {
  return (
    conversation.hasMessages ||
    (messagesByConversationId[conversation.id]?.length ?? 0) > 0
  );
}

export function MessageWorkspace() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const queryConversationId = searchParams.get("conversationId");
  const { user: currentUser, isLoading: isCurrentUserLoading } = useCurrentUser();
  const [activeConversationId, setActiveConversationId] = useState<string | null>(
    queryConversationId,
  );
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [messagesByConversationId, setMessagesByConversationId] = useState<
    Record<string, ChatMessage[]>
  >({});
  const [loadedConversationIds, setLoadedConversationIds] = useState<
    Record<string, boolean>
  >({});
  const [loadingMessagesConversationId, setLoadingMessagesConversationId] =
    useState<string | null>(null);
  const [messageLoadErrors, setMessageLoadErrors] = useState<
    Record<string, string | null>
  >({});
  const [sendErrors, setSendErrors] = useState<Record<string, string | null>>({});
  const [isInitialLoading, setIsInitialLoading] = useState(true);
  const [conversationErrorMessage, setConversationErrorMessage] = useState<
    string | null
  >(null);

  useEffect(() => {
    if (queryConversationId && queryConversationId !== activeConversationId) {
      setActiveConversationId(queryConversationId);
    }
  }, [activeConversationId, queryConversationId]);

  const loadWorkspace = useCallback(async () => {
    if (!currentUser?.userId) {
      setConversations([]);
      setMessagesByConversationId({});
      setLoadedConversationIds({});
      setIsInitialLoading(false);
      return;
    }

    setIsInitialLoading(true);
    setConversationErrorMessage(null);

    try {
      const [conversationResponse, followingResponse] = await Promise.all([
        getConversations(),
        getFollowingByUser(currentUser.userId),
      ]);
      const followingIds = followingResponse
        .map((follow) => follow.followingUserId)
        .filter((userId) => userId && userId !== currentUser.userId);
      const participantIds = conversationResponse
        .map((conversation) => getOtherMemberId(conversation, currentUser.userId))
        .filter((userId): userId is string => Boolean(userId));
      const userIds = Array.from(new Set([...participantIds, ...followingIds]));
      const userResponses = await Promise.all(
        userIds.map(async (userId) => {
          try {
            return [userId, await getUserById(userId)] as const;
          } catch {
            return null;
          }
        }),
      );
      const userDetails: Record<string, UserResponse> = {};

      userResponses.forEach((entry) => {
        if (entry) {
          userDetails[entry[0]] = entry[1];
        }
      });

      const seenUserIds = new Set<string>();
      const conversationItems: Conversation[] = [];

      conversationResponse.forEach((conversation) => {
        const participantId = getOtherMemberId(conversation, currentUser.userId);
        const participant = participantId ? userDetails[participantId] : null;

        if (!participantId || !participant || seenUserIds.has(participantId)) {
          return;
        }

        seenUserIds.add(participantId);
        conversationItems.push(
          mapConversationResponseToConversation(conversation, participant),
        );
      });

      const followedItems = followingIds
        .filter((userId) => !seenUserIds.has(userId) && userDetails[userId])
        .map((userId) => mapUserToConversation(userDetails[userId]));

      setConversations([...conversationItems, ...followedItems]);
    } catch (requestError) {
      setConversations([]);
      setConversationErrorMessage(
        getApiErrorMessage(requestError, "Unable to load messages."),
      );
    } finally {
      setIsInitialLoading(false);
    }
  }, [currentUser?.userId]);

  useEffect(() => {
    if (isCurrentUserLoading) {
      return;
    }

    void loadWorkspace();
  }, [isCurrentUserLoading, loadWorkspace]);

  const stompClientRef = useRef<Client | null>(null);
  const stompSubscriptionRef = useRef<StompSubscription | null>(null);

  const activeConversation = useMemo(
    () =>
      conversations.find(
        (conversation) =>
          conversation.id === activeConversationId ||
          conversation.serverId === activeConversationId,
      ) ?? null,
    [activeConversationId, conversations],
  );

  const loadMessagesForConversation = useCallback(
    async (conversation: Conversation, force = false) => {
      if (!currentUser?.userId || !conversation.serverId) {
        return;
      }

      if (!force && loadedConversationIds[conversation.id]) {
        return;
      }

      setLoadingMessagesConversationId(conversation.id);
      setMessageLoadErrors((currentErrors) => ({
        ...currentErrors,
        [conversation.id]: null,
      }));

      try {
        const response = await getConversationMessages(conversation.serverId, {
          page: 0,
          size: 50,
        });
        const mappedMessages = response
          .map((message) =>
            mapMessageResponseToChatMessage(message, currentUser.userId),
          )
          .reverse();

        setMessagesByConversationId((currentMessages) => ({
          ...currentMessages,
          [conversation.id]: mergeLoadedMessages(
            mappedMessages,
            currentMessages[conversation.id] ?? [],
          ),
        }));
        setLoadedConversationIds((currentLoaded) => ({
          ...currentLoaded,
          [conversation.id]: true,
        }));
      } catch (requestError) {
        setMessageLoadErrors((currentErrors) => ({
          ...currentErrors,
          [conversation.id]: getApiErrorMessage(
            requestError,
            "Unable to load messages.",
          ),
        }));
      } finally {
        setLoadingMessagesConversationId((currentLoadingId) =>
          currentLoadingId === conversation.id ? null : currentLoadingId,
        );
      }
    },
    [currentUser?.userId, loadedConversationIds],
  );

  useEffect(() => {
    if (activeConversation) {
      void loadMessagesForConversation(activeConversation);
    }
  }, [activeConversation, loadMessagesForConversation]);

  // STOMP WebSocket — nhận tin nhắn real-time
  useEffect(() => {
    const conversationId = activeConversation?.serverId;
    const conversationKey = activeConversation?.id;
    const userId = currentUser?.userId;

    // Hủy subscription cũ khi đổi conversation
    if (stompSubscriptionRef.current) {
      stompSubscriptionRef.current.unsubscribe();
      stompSubscriptionRef.current = null;
    }

    if (!conversationId || !conversationKey || !userId) {
      return;
    }

    // Capture as definite strings — TypeScript doesn't narrow inside closures
    const safeConversationId: string = conversationId;
    const safeConversationKey: string = conversationKey;
    const safeUserId: string = userId;

    // Tạo client nếu chưa có hoặc đã disconnect
    if (!stompClientRef.current || !stompClientRef.current.connected) {
      const client = createStompClient();
      stompClientRef.current = client;
      client.activate();
    }

    const client = stompClientRef.current;

    function subscribe() {
      stompSubscriptionRef.current = client.subscribe(
        `/topic/conversations/${safeConversationId}`,
        (frame) => {
          try {
            const event: SocketEvent = JSON.parse(frame.body);

            if (
              event.type !== "receive_message" ||
              !event.data ||
              typeof event.data === "string"
            ) {
              return;
            }

            const incoming = event.data;

            // Skip tin nhắn của chính mình — đã có optimistic update
            if (incoming.senderId === safeUserId) {
              return;
            }

            const mapped = mapMessageResponseToChatMessage(incoming, safeUserId);

            setMessagesByConversationId((current) => {
              const existing = current[safeConversationKey] ?? [];

              // Dedup theo serverId
              if (existing.some((m: ChatMessage) => m.serverId === mapped.serverId)) {
                return current;
              }

              return {
                ...current,
                [safeConversationKey]: [...existing, mapped],
              };
            });

            updateConversationPreview(
              safeConversationKey,
              mapped.body || ((mapped.imageUrls ?? []).length > 0 ? "Photo" : "Message"),
              mapped.time,
            );
          } catch {
            // Frame parse lỗi — bỏ qua
          }
        },
      );
    }

    if (client.connected) {
      subscribe();
    } else {
      const originalOnConnect = client.onConnect;
      client.onConnect = (receipt) => {
        originalOnConnect?.(receipt);
        subscribe();
      };
    }

    return () => {
      if (stompSubscriptionRef.current) {
        stompSubscriptionRef.current.unsubscribe();
        stompSubscriptionRef.current = null;
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeConversation?.serverId, activeConversation?.id, currentUser?.userId]);

  // Cleanup STOMP khi unmount
  useEffect(() => {
    return () => {
      stompSubscriptionRef.current?.unsubscribe();
      stompClientRef.current?.deactivate();
    };
  }, []);

  function handleSelectConversation(conversation: Conversation) {
    setConversationErrorMessage(null);

    if (conversation.serverId) {
      setActiveConversationId(conversation.id);
      router.push(`/messages?conversationId=${conversation.serverId}`);
      return;
    }

    const temporaryConversation: Conversation = {
      ...conversation,
      id: createLocalId("temporary"),
      isTemporary: true,
      localStatus: "creating",
      preview: "Opening...",
      time: "Now",
    };

    setActiveConversationId(temporaryConversation.id);
    setConversations((currentConversations) =>
      replaceConversationInPlace(currentConversations, temporaryConversation),
    );
    router.push(`/messages?conversationId=${temporaryConversation.id}`);

    void (async () => {
      try {
        const createdConversation = await createDirectConversation(
          conversation.participantUserId,
        );
        const readyConversation: Conversation = {
          ...temporaryConversation,
          id: createdConversation.id,
          isTemporary: false,
          localStatus: "ready",
          preview:
            createdConversation.latestMessagePreview ||
            conversation.preview ||
            "Open conversation",
          serverId: createdConversation.id,
          time: formatTime(createdConversation.updatedAt),
        };

        setConversations((currentConversations) =>
          replaceConversationInPlace(currentConversations, readyConversation),
        );
        setMessagesByConversationId((currentMessages) => {
          const temporaryMessages =
            currentMessages[temporaryConversation.id] ?? [];
          const { [temporaryConversation.id]: _removed, ...remainingMessages } =
            currentMessages;

          return {
            ...remainingMessages,
            [readyConversation.id]: temporaryMessages,
          };
        });
        setLoadedConversationIds((currentLoaded) => {
          const { [temporaryConversation.id]: _removed, ...remainingLoaded } =
            currentLoaded;

          return remainingLoaded;
        });
        setActiveConversationId((currentActiveId) =>
          currentActiveId === temporaryConversation.id
            ? readyConversation.id
            : currentActiveId,
        );
        router.replace(`/messages?conversationId=${createdConversation.id}`);
      } catch (requestError) {
        setConversationErrorMessage(
          getApiErrorMessage(requestError, "Unable to open conversation."),
        );
        setConversations((currentConversations) =>
          currentConversations.map((currentConversation) =>
            currentConversation.id === temporaryConversation.id
              ? {
                  ...currentConversation,
                  localStatus: "error",
                  preview: "Unable to open conversation.",
                }
              : currentConversation,
          ),
        );
      }
    })();
  }

  function handleRetryMessages() {
    if (activeConversation) {
      void loadMessagesForConversation(activeConversation, true);
    }
  }

  function updateConversationPreview(
    conversationId: string,
    preview: string,
    time: string,
  ) {
    setConversations((currentConversations) => {
      const updatedConversations = currentConversations.map((conversation) =>
        conversation.id === conversationId
          ? {
              ...conversation,
              hasMessages: true,
              preview,
              time,
            }
          : conversation,
      );

      return moveConversationToTop(updatedConversations, conversationId);
    });
  }

  function handleSendMessage(draft: SendMessageDraft) {
    const conversation = activeConversation;
    const text = draft.text.trim();

    if (!currentUser?.userId || !conversation?.serverId || (!text && !draft.file)) {
      return false;
    }

    const currentUserId = currentUser.userId;
    const serverConversationId = conversation.serverId;
    const optimisticId = createLocalId("optimistic");
    const hasImage = Boolean(draft.file);
    const messageType = getSendType(text, hasImage);
    const createdAt = new Date();
    const localImageUrl = draft.file ? URL.createObjectURL(draft.file) : null;
    const optimisticMessage: ChatMessage = {
      author: "me",
      body: text || undefined,
      id: optimisticId,
      imageUrls: localImageUrl ? [localImageUrl] : undefined,
      localStatus: "sending",
      time: formatTime(createdAt.toISOString()),
      timestamp: createdAt.getTime(),
      type: messageType,
    };
    const preview = getPreviewFromMessage(
      messageType,
      text,
      localImageUrl ? [localImageUrl] : undefined,
    );

    setSendErrors((currentErrors) => ({
      ...currentErrors,
      [conversation.id]: null,
    }));
    setMessagesByConversationId((currentMessages) => ({
      ...currentMessages,
      [conversation.id]: [
        ...(currentMessages[conversation.id] ?? []),
        optimisticMessage,
      ],
    }));
    setLoadingMessagesConversationId((currentLoadingId) =>
      currentLoadingId === conversation.id ? null : currentLoadingId,
    );
    setLoadedConversationIds((currentLoaded) => ({
      ...currentLoaded,
      [conversation.id]: true,
    }));
    updateConversationPreview(conversation.id, preview, optimisticMessage.time);

    void (async () => {
      try {
        const uploadedImageUrls = draft.file
          ? [await uploadChatImageToCloudinary(draft.file)]
          : [];
        const sentMessage = await sendChatMessage(serverConversationId, {
          imageUrls:
            uploadedImageUrls.length > 0 ? uploadedImageUrls : undefined,
          text: text || undefined,
          type: getSendType(text, uploadedImageUrls.length > 0),
        });
        const confirmedMessage = mapMessageResponseToChatMessage(
          sentMessage,
          currentUserId,
        );
        const confirmedPreview = getPreviewFromMessage(
          sentMessage.type,
          sentMessage.text ?? undefined,
          sentMessage.imageUrls ?? undefined,
          sentMessage.stickerUrl ?? undefined,
          sentMessage.stickerId ?? undefined,
        );

        setMessagesByConversationId((currentMessages) => ({
          ...currentMessages,
          [conversation.id]: (currentMessages[conversation.id] ?? []).map(
            (message) =>
              message.id === optimisticId ? confirmedMessage : message,
          ),
        }));
        updateConversationPreview(
          conversation.id,
          confirmedPreview,
          confirmedMessage.time,
        );

        if (localImageUrl) {
          URL.revokeObjectURL(localImageUrl);
        }
      } catch (requestError) {
        setSendErrors((currentErrors) => ({
          ...currentErrors,
          [conversation.id]: getApiErrorMessage(
            requestError,
            "Unable to send message.",
          ),
        }));
        setMessagesByConversationId((currentMessages) => ({
          ...currentMessages,
          [conversation.id]: (currentMessages[conversation.id] ?? []).map(
            (message) =>
              message.id === optimisticId
                ? { ...message, localStatus: "error" }
                : message,
          ),
        }));
      }
    })();

    return true;
  }

  const currentUsername = currentUser?.userName || "cafestory_user";
  const visibleConversations = useMemo(
    () =>
      conversations.filter((conversation) =>
        hasVisibleMessages(conversation, messagesByConversationId),
      ),
    [conversations, messagesByConversationId],
  );
  const activeMessages = activeConversation
    ? messagesByConversationId[activeConversation.id] ?? []
    : [];
  const activeLoadError = activeConversation
    ? messageLoadErrors[activeConversation.id] ?? null
    : null;
  const activeSendError = activeConversation
    ? sendErrors[activeConversation.id] ?? null
    : null;
  const canSend = Boolean(
    activeConversation?.serverId &&
      activeConversation.localStatus !== "creating" &&
      activeConversation.localStatus !== "error",
  );

  return (
    <main className="-ml-8 h-screen w-[calc(100vw-64px)] max-w-none overflow-hidden bg-background sm:-ml-14 sm:w-[calc(100vw-72px)] xl:-ml-[248px]">
      <section className="grid h-screen min-h-0 w-full grid-cols-1 overflow-hidden border-l border-r border-border bg-surface lg:grid-cols-[360px_minmax(0,1fr)]">
        <ConversationList
          activeConversationId={activeConversation?.id ?? activeConversationId}
          conversations={visibleConversations}
          currentUsername={currentUsername}
          errorMessage={conversationErrorMessage}
          isLoading={isCurrentUserLoading || isInitialLoading}
          onRetry={loadWorkspace}
          onSelectConversation={handleSelectConversation}
        />
        <ChatPanel
          canSend={canSend}
          conversation={activeConversation}
          isLoading={
            Boolean(activeConversation) &&
            loadingMessagesConversationId === activeConversation?.id
          }
          loadErrorMessage={activeLoadError}
          messages={activeMessages}
          onRetryMessages={handleRetryMessages}
          onSendMessage={handleSendMessage}
          sendErrorMessage={activeSendError}
        />
      </section>
    </main>
  );
}
