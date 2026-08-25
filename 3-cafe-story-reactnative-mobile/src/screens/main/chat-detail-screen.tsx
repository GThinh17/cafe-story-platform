import { useCallback, useEffect, useMemo, useState } from "react";
import { Pressable, Text } from "react-native";
import {
  FlatList, Keyboard, KeyboardAvoidingView, Platform, StyleSheet, View } from "react-native";
import { useNavigation, useRoute } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import type { RouteProp } from "@react-navigation/native";
import {
  CHAT_COMPOSER_INPUT_HEIGHT,
  ChatComposer,
  ChatDetailHeader,
  ChatMessageBubble,
  LoadingState,
  Screen,
} from "../../components";
import { Avatar } from "../../components/ui/avatar";
import { useAuth } from "../../features/auth";
import { formatCurrentTime } from "../../features/i18n";
import { routes } from "../../navigation";
import type { RootStackParamList } from "../../navigation";
import { getConversationMessages, sendChatMessage } from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type { ChatIdentity, ChatMessageListItem, ChatMessageResponse } from "../../types";
import type { AuthUser } from "../../types";
import { t } from "../../features/i18n";

function formatMessageTime(value: string | null) {
  if (!value) {
    return "";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "";
  }

  return formatCurrentTime(date, {
    hour: "2-digit",
    minute: "2-digit",
  });
}

function mapMessageToListItem(
  message: ChatMessageResponse,
  currentUserId?: string,
  managedCafePageId?: string | null,
): ChatMessageListItem {
  const isCafePageSender =
    message.senderContextType === "CAFE_PAGE" &&
    Boolean(managedCafePageId) &&
    message.senderCafePageId === managedCafePageId;

  return {
    id: message.id,
    isMine: message.senderId === currentUserId || isCafePageSender,
    text:
      message.text ||
      (message.imageUrls?.length ? "[image]" : null) ||
      (message.stickerId || message.stickerUrl ? "[sticker]" : "[message]"),
    time: formatMessageTime(message.createdAt),
  };
}

function getManagedCafePageId(user: AuthUser | null | undefined) {
  return user?.cafePageId || user?.pageId || null;
}

export function ChatDetailScreen() {
  const navigation =
    useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const route =
    useRoute<RouteProp<RootStackParamList, typeof routes.chatDetail>>();
  const { user } = useAuth();
  const [draft, setDraft] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isSending, setIsSending] = useState(false);
  const [messages, setMessages] = useState<ChatMessageListItem[]>([]);
  const managedCafePageId = getManagedCafePageId(user);

  const conversation = useMemo<ChatIdentity>(
    () => ({
      avatarUri: route.params.chatAvatar ?? null,
      canReplyAsCafePage: Boolean(route.params.canReplyAsCafePage),
      id: route.params.conversationId,
      name: route.params.chatName || "CafeStory user",
      targetCafePageId: route.params.targetCafePageId ?? null,
      targetType: route.params.targetType ?? "USER",
      targetUserId: route.params.targetUserId ?? null,
      userName: route.params.userName || "",
    }),
    [
      route.params.chatAvatar,
      route.params.canReplyAsCafePage,
      route.params.chatName,
      route.params.conversationId,
      route.params.targetCafePageId,
      route.params.targetType,
      route.params.targetUserId,
      route.params.userName,
    ],
  );

  const shouldSendAsCafePage =
    conversation.targetType === "CAFE_PAGE" &&
    conversation.canReplyAsCafePage &&
    Boolean(conversation.targetCafePageId);
  const isCafePageInboxConversation =
    shouldSendAsCafePage && Boolean(conversation.targetUserId);
  const senderCafePageId = shouldSendAsCafePage
    ? conversation.targetCafePageId
    : managedCafePageId;

  const loadMessages = useCallback(async () => {
    setError("");
    setIsLoading(true);

    try {
      const response = await getConversationMessages(route.params.conversationId);
      setMessages(
        response
          .slice()
          .reverse()
          .map((message) => mapMessageToListItem(message, user?.userId, senderCafePageId)),
      );
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to load messages.",
      );
    } finally {
      setIsLoading(false);
    }
  }, [route.params.conversationId, senderCafePageId, user?.userId]);

  useEffect(() => {
    void loadMessages();
  }, [loadMessages]);

  async function handleSendMessage() {
    const text = draft.trim();

    if (!text || isSending) {
      return;
    }

    setIsSending(true);
    setError("");

    try {
      const response = await sendChatMessage(route.params.conversationId, {
        ...(shouldSendAsCafePage && conversation.targetCafePageId
          ? {
              senderCafePageId: conversation.targetCafePageId,
              senderContextType: "CAFE_PAGE",
            }
          : {}),
        text,
        type: "TEXT",
      });

      setMessages((currentMessages) => [
        ...currentMessages,
        mapMessageToListItem(response, user?.userId, senderCafePageId),
      ]);
      setDraft("");
      Keyboard.dismiss();
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to send message.",
      );
    } finally {
      setIsSending(false);
    }
  }

  function handleOpenProfile() {
    if (isCafePageInboxConversation && conversation.targetUserId) {
      navigation.navigate(routes.otherUserProfile, {
        userId: conversation.targetUserId,
        userName: conversation.userName,
      });
      return;
    }

    if (conversation.targetType === "CAFE_PAGE" && conversation.targetCafePageId) {
      navigation.navigate(routes.cafeDetail, {
        cafeId: conversation.targetCafePageId,
      });
      return;
    }

    if (conversation.targetType !== "USER" || !conversation.targetUserId) {
      return;
    }

    navigation.navigate(routes.otherUserProfile, {
      userId: conversation.targetUserId,
      userName: conversation.userName,
    });
  }

  return (
    <Screen padded={false}>
      <KeyboardAvoidingView
        behavior={Platform.OS === "ios" ? "padding" : "height"}
        keyboardVerticalOffset={
          Platform.OS === "android" ? CHAT_COMPOSER_INPUT_HEIGHT : 0
        }
        style={styles.keyboardAvoidingView}
      >
        <ChatDetailHeader
          conversation={conversation}
          onBackPress={() => navigation.goBack()}
          onProfilePress={
            conversation.targetUserId || conversation.targetCafePageId
              ? handleOpenProfile
              : undefined
          }
        />

        <FlatList
          ListHeaderComponent={
            <>
              <View style={styles.profileIntro}>
                <Avatar size={148} uri={conversation.avatarUri} />
                <Text style={styles.profileName}>{conversation.name}</Text>
                {conversation.userName ? (
                  <Text style={styles.profileUsername}>
                    {conversation.userName}
                  </Text>
                ) : null}
                <Text style={styles.profileMeta}>
                  {conversation.targetType === "CAFE_PAGE"
                    ? shouldSendAsCafePage
                      ? "Replying as this cafe page to this customer."
                      : "Message this cafe page here."
                    : t("Start the conversation here.")}
                </Text>
                <Pressable
                  accessibilityLabel={t("View chat profile")}
                  accessibilityRole="button"
                  disabled={!conversation.targetUserId && !conversation.targetCafePageId}
                  onPress={handleOpenProfile}
                  style={({ pressed }) => [
                    styles.profileButton,
                    pressed &&
                      (conversation.targetUserId || conversation.targetCafePageId) &&
                      styles.pressed,
                  ]}
                >
                  <Text style={styles.profileButtonText}>
                    {conversation.targetType === "CAFE_PAGE" && !isCafePageInboxConversation
                      ? t("View cafe page")
                      : t("View profile")}
                  </Text>
                </Pressable>
              </View>
              {isLoading ? <LoadingState label={t("Loading messages...")} /> : null}
              {error ? <Text style={styles.errorText}>{error}</Text> : null}
            </>
          }
          contentContainerStyle={styles.messageContent}
          data={messages}
          keyExtractor={(item) => item.id}
          renderItem={({ item }) => <ChatMessageBubble message={item} />}
          showsVerticalScrollIndicator={false}
        />

        <ChatComposer
          disabled={isSending}
          onChangeText={setDraft}
          onSend={handleSendMessage}
          value={draft}
        />
      </KeyboardAvoidingView>
    </Screen>
  );
}

const styles = StyleSheet.create({
  keyboardAvoidingView: {
    flex: 1,
  },
  errorText: {
    color: colors.danger,
    fontSize: typography.label,
    fontWeight: "600",
    paddingBottom: spacing.md,
    textAlign: "center",
  },
  messageContent: {
    gap: spacing.xs,
    paddingBottom: spacing.xl,
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.lg,
  },
  profileButton: {
    backgroundColor: colors.surfaceMuted,
    borderRadius: 8,
    marginTop: spacing.sm,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.sm,
  },
  profileButtonText: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "800",
  },
  profileIntro: {
    alignItems: "center",
    gap: spacing.xs,
    paddingBottom: spacing.xxl,
    paddingTop: spacing.xxl,
  },
  profileMeta: {
    color: colors.muted,
    fontSize: typography.body,
    fontWeight: "500",
    textAlign: "center",
  },
  profileName: {
    color: colors.foreground,
    fontSize: typography.heading,
    fontWeight: "800",
    textAlign: "center",
  },
  profileUsername: {
    color: colors.muted,
    fontSize: typography.label,
    fontWeight: "600",
    textAlign: "center",
  },
  pressed: {
    opacity: 0.72,
  },
});
