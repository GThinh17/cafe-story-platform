import { StyleSheet, Text, View } from "react-native";
import { colors, spacing, typography } from "../../theme";
import type { ChatMessageListItem } from "../../types";

type ChatMessageBubbleProps = {
  message: ChatMessageListItem;
};

export function ChatMessageBubble({ message }: ChatMessageBubbleProps) {
  return (
    <View
      style={[
        styles.row,
        message.isMine ? styles.rowMine : styles.rowTheirs,
      ]}
    >
      <View
        style={[
          styles.bubble,
          message.isMine ? styles.bubbleMine : styles.bubbleTheirs,
        ]}
      >
        <Text
          style={[
            styles.messageText,
            message.isMine ? styles.textMine : styles.textTheirs,
          ]}
        >
          {message.text}
        </Text>
      </View>
      <Text style={styles.time}>{message.time}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  bubble: {
    borderRadius: 20,
    maxWidth: "78%",
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
  },
  bubbleMine: {
    backgroundColor: colors.primary,
    borderBottomRightRadius: 6,
  },
  bubbleTheirs: {
    backgroundColor: colors.surface,
    borderBottomLeftRadius: 6,
  },
  messageText: {
    fontSize: typography.body,
    lineHeight: 22,
  },
  row: {
    gap: spacing.xs,
    marginVertical: spacing.xs,
  },
  rowMine: {
    alignItems: "flex-end",
  },
  rowTheirs: {
    alignItems: "flex-start",
  },
  textMine: {
    color: colors.white,
  },
  textTheirs: {
    color: colors.foreground,
  },
  time: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "600",
  },
});
