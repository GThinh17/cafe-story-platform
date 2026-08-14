import { ActivityIndicator, StyleSheet, View } from "react-native";
import { Text } from "../../features/i18n/localized-native";
import { colors, spacing, typography } from "../../theme";
import type { AiAssistantMessageListItem, AiChatSource } from "../../types";

type AiAssistantMessageBubbleProps = {
  message: AiAssistantMessageListItem;
};

export function AiAssistantMessageBubble({
  message,
}: AiAssistantMessageBubbleProps) {
  const isUserMessage = message.role === "user";
  const isPending = message.status === "sending";
  const isError = message.status === "error";

  return (
    <View
      style={[
        styles.row,
        isUserMessage ? styles.rowMine : styles.rowAssistant,
      ]}
    >
      <View
        style={[
          styles.bubble,
          isUserMessage ? styles.bubbleMine : styles.bubbleAssistant,
          isError ? styles.bubbleError : null,
        ]}
      >
        {isPending ? (
          <View style={styles.pendingContent}>
            <ActivityIndicator color={colors.muted} size="small" />
            <Text style={styles.pendingText}>Thinking...</Text>
          </View>
        ) : (
          <Text
            style={[
              styles.messageText,
              isUserMessage ? styles.textMine : styles.textAssistant,
              isError ? styles.textError : null,
            ]}
          >
            {message.body}
          </Text>
        )}
      </View>

      {!isUserMessage && message.sources?.length ? (
        <View style={styles.sources}>{message.sources.map(renderSourceChip)}</View>
      ) : null}

      {message.time ? <Text style={styles.time}>{message.time}</Text> : null}
    </View>
  );
}

function renderSourceChip(source: AiChatSource, index: number) {
  const label = source.title || source.sourceId || source.sourceType || "Source";

  return (
    <View key={`${source.sourceType}-${source.sourceId}-${index}`} style={styles.sourceChip}>
      <Text numberOfLines={1} style={styles.sourceText}>
        {label}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  bubble: {
    borderRadius: 20,
    maxWidth: "84%",
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
  },
  bubbleAssistant: {
    backgroundColor: colors.surface,
    borderBottomLeftRadius: 6,
  },
  bubbleError: {
    backgroundColor: colors.secondarySoft,
    borderColor: colors.danger,
    borderWidth: 1,
  },
  bubbleMine: {
    backgroundColor: colors.primary,
    borderBottomRightRadius: 6,
  },
  messageText: {
    fontSize: typography.body,
    lineHeight: 22,
  },
  pendingContent: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.sm,
  },
  pendingText: {
    color: colors.muted,
    fontSize: typography.label,
    fontWeight: "700",
  },
  row: {
    gap: spacing.xs,
    marginVertical: spacing.xs,
  },
  rowAssistant: {
    alignItems: "flex-start",
  },
  rowMine: {
    alignItems: "flex-end",
  },
  sourceChip: {
    backgroundColor: colors.tertiarySoft,
    borderColor: colors.border,
    borderRadius: 14,
    borderWidth: 1,
    maxWidth: 220,
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs,
  },
  sourceText: {
    color: colors.tertiaryStrong,
    fontSize: typography.caption,
    fontWeight: "800",
  },
  sources: {
    alignItems: "flex-start",
    gap: spacing.xs,
    maxWidth: "84%",
  },
  textAssistant: {
    color: colors.foreground,
  },
  textError: {
    color: colors.danger,
    fontWeight: "700",
  },
  textMine: {
    color: colors.white,
  },
  time: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "600",
  },
});
