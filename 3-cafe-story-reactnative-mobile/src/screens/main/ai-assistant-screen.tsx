import { useEffect, useRef, useState } from "react";
import { Pressable, Text } from "../../features/i18n/localized-native";
import {
  FlatList, Keyboard, KeyboardAvoidingView, Platform, StyleSheet, View } from "react-native";
import { ChevronLeft, Sparkles } from "lucide-react-native";
import { useNavigation } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import {
  AiAssistantComposer,
  AiAssistantMessageBubble,
  Screen,
} from "../../components";
import { routes } from "../../navigation";
import { formatCurrentTime } from "../../features/i18n";
import type { RootStackParamList } from "../../navigation";
import { AiChatError, askAssistant } from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type {
  AiAssistantMessageListItem,
  AiChatHistoryItem,
} from "../../types";

const ASSISTANT_HISTORY_LIMIT = 10;
const ASSISTANT_WELCOME_MESSAGE_ID = "assistant-welcome";

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

function createLocalId(prefix: string) {
  return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
}

function createWelcomeMessage(): AiAssistantMessageListItem {
  return {
    body:
      "Hi! I am CafeStory Assistant. Ask me about cafes, cafe pages, reviewers, payments, reports, or how to use CafeStory.",
    id: ASSISTANT_WELCOME_MESSAGE_ID,
    role: "assistant",
    status: "sent",
    time: "",
  };
}

function buildAssistantHistory(
  messages: AiAssistantMessageListItem[],
): AiChatHistoryItem[] {
  return messages
    .filter((message) => message.id !== ASSISTANT_WELCOME_MESSAGE_ID)
    .filter((message) => Boolean(message.body.trim()))
    .filter((message) => message.status !== "sending")
    .filter((message) => message.status !== "error")
    .slice(-ASSISTANT_HISTORY_LIMIT)
    .map((message) => ({
      content: message.body.trim(),
      role: message.role,
    }));
}

export function AiAssistantScreen() {
  const navigation =
    useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const listRef = useRef<FlatList<AiAssistantMessageListItem>>(null);
  const [draft, setDraft] = useState("");
  const [isSending, setIsSending] = useState(false);
  const [messages, setMessages] = useState<AiAssistantMessageListItem[]>([
    createWelcomeMessage(),
  ]);

  useEffect(() => {
    requestAnimationFrame(() => {
      listRef.current?.scrollToEnd({ animated: true });
    });
  }, [messages.length]);

  async function handleSendMessage() {
    const text = draft.trim();

    if (!text || isSending) {
      return;
    }

    const nowIso = new Date().toISOString();
    const userMessage: AiAssistantMessageListItem = {
      body: text,
      id: createLocalId("assistant-user"),
      role: "user",
      status: "sent",
      time: formatMessageTime(nowIso),
    };
    const pendingMessage: AiAssistantMessageListItem = {
      body: "",
      id: createLocalId("assistant-reply"),
      role: "assistant",
      status: "sending",
      time: formatMessageTime(nowIso),
    };
    const nextMessages = [...messages, userMessage, pendingMessage];
    const history = buildAssistantHistory([...messages, userMessage]);

    setMessages(nextMessages);
    setDraft("");
    setIsSending(true);
    Keyboard.dismiss();

    try {
      const response = await askAssistant({
        history,
        platform: "mobile",
        query: text,
      });
      const answeredAt = new Date().toISOString();

      setMessages((currentMessages) =>
        currentMessages.map((message) =>
          message.id === pendingMessage.id
            ? {
                ...message,
                body: response.answer,
                sources: response.sources,
                status: "sent",
                time: formatMessageTime(answeredAt),
              }
            : message,
        ),
      );
    } catch (requestError) {
      const errorMessage =
        requestError instanceof AiChatError
          ? requestError.message
          : "Unable to reach assistant. Please try again.";

      setMessages((currentMessages) =>
        currentMessages.map((message) =>
          message.id === pendingMessage.id
            ? {
                ...message,
                body: errorMessage,
                status: "error",
              }
            : message,
        ),
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
        <View style={styles.header}>
          <Pressable
            accessibilityLabel="Go back"
            accessibilityRole="button"
            onPress={() => navigation.goBack()}
            style={({ pressed }) => [styles.backButton, pressed && styles.pressed]}
          >
            <ChevronLeft color={colors.foreground} size={32} strokeWidth={2.4} />
          </Pressable>

          <View style={styles.avatar}>
            <Sparkles color={colors.tertiaryStrong} size={21} strokeWidth={2.4} />
          </View>

          <View style={styles.identity}>
            <Text numberOfLines={1} style={styles.name}>
              CafeStory Assistant
            </Text>
            <Text numberOfLines={1} style={styles.status}>
              CafeStory AI
            </Text>
          </View>
        </View>

        <FlatList
          contentContainerStyle={styles.messageContent}
          data={messages}
          keyExtractor={(item) => item.id}
          ref={listRef}
          renderItem={({ item }) => (
            <AiAssistantMessageBubble message={item} />
          )}
          showsVerticalScrollIndicator={false}
        />

        <AiAssistantComposer
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
  avatar: {
    alignItems: "center",
    backgroundColor: colors.tertiarySoft,
    borderColor: colors.border,
    borderRadius: 20,
    borderWidth: 1,
    height: 40,
    justifyContent: "center",
    width: 40,
  },
  backButton: {
    alignItems: "center",
    borderRadius: 22,
    height: 44,
    justifyContent: "center",
    width: 44,
  },
  header: {
    alignItems: "center",
    backgroundColor: colors.background,
    borderBottomColor: colors.border,
    borderBottomWidth: StyleSheet.hairlineWidth,
    flexDirection: "row",
    gap: spacing.sm,
    minHeight: 66,
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.sm,
  },
  identity: {
    flex: 1,
  },
  keyboardAvoidingView: {
    flex: 1,
  },
  messageContent: {
    gap: spacing.xs,
    paddingBottom: spacing.xl,
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.lg,
  },
  name: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "800",
  },
  pressed: {
    opacity: 0.72,
    transform: [{ scale: 0.98 }],
  },
  status: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "600",
  },
});
