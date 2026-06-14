import { useCallback, useEffect, useMemo, useState } from "react";
import {
  FlatList,
  Keyboard,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { useNavigation, useRoute } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import type { RouteProp } from "@react-navigation/native";
import {
  ChatComposer,
  ChatDetailHeader,
  ChatMessageBubble,
  LoadingState,
  Screen,
} from "../../components";
import { Avatar } from "../../components/ui/avatar";
import { useAuth } from "../../features/auth";
import { routes } from "../../navigation";
import type { RootStackParamList } from "../../navigation";
import { getConversationMessages, sendChatMessage } from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type { ChatIdentity, ChatMessageListItem, ChatMessageResponse } from "../../types";

function formatMessageTime(value: string | null) {
  if (!value) {
    return "";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "";
  }

  return date.toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
  });
}

function mapMessageToListItem(
  message: ChatMessageResponse,
  currentUserId?: string,
): ChatMessageListItem {
  return {
    id: message.id,
    isMine: message.senderId === currentUserId,
    text:
      message.text ||
      (message.imageUrls?.length ? "[image]" : null) ||
      (message.stickerId || message.stickerUrl ? "[sticker]" : "[message]"),
    time: formatMessageTime(message.createdAt),
  };
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

  const conversation = useMemo<ChatIdentity>(
    () => ({
      avatarUri: route.params.chatAvatar ?? null,
      id: route.params.conversationId,
      name: route.params.chatName || "CafeStory user",
      userName: route.params.userName || "",
    }),
    [
      route.params.chatAvatar,
      route.params.chatName,
      route.params.conversationId,
      route.params.userName,
    ],
  );

  const loadMessages = useCallback(async () => {
    setError("");
    setIsLoading(true);

    try {
      const response = await getConversationMessages(route.params.conversationId);
      setMessages(
        response
          .slice()
          .reverse()
          .map((message) => mapMessageToListItem(message, user?.userId)),
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
  }, [route.params.conversationId, user?.userId]);

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
        text,
        type: "TEXT",
      });

      setMessages((currentMessages) => [
        ...currentMessages,
        mapMessageToListItem(response, user?.userId),
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

  return (
    <Screen padded={false}>
      <KeyboardAvoidingView
        behavior={Platform.OS === "ios" ? "padding" : undefined}
        style={styles.keyboardAvoidingView}
      >
        <ChatDetailHeader
          conversation={conversation}
          onBackPress={() => navigation.goBack()}
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
                  Start the conversation here.
                </Text>
                <Pressable style={styles.profileButton}>
                  <Text style={styles.profileButtonText}>View profile</Text>
                </Pressable>
              </View>
              {isLoading ? <LoadingState label="Loading messages..." /> : null}
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
});
