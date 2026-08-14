import { BellOff } from "lucide-react-native";
import { Pressable } from "../../features/i18n/localized-native";
import { Text } from "../../features/i18n/localized-native";
import { StyleSheet, View } from "react-native";
import { Avatar } from "../ui/avatar";
import { colors, spacing, typography } from "../../theme";
import type { ConversationListItem } from "../../types";

type ConversationRowProps = {
  conversation: ConversationListItem;
  onPress: (conversation: ConversationListItem) => void;
};

export function ConversationRow({ conversation, onPress }: ConversationRowProps) {
  return (
    <Pressable
      accessibilityRole="button"
      onPress={() => onPress(conversation)}
      style={({ pressed }) => [styles.container, pressed && styles.pressed]}
    >
      <View style={styles.avatarWrap}>
        <Avatar
          initials={conversation.initials}
          size={58}
          uri={conversation.avatarUri}
        />
        {conversation.isOnline ? <View style={styles.onlineDot} /> : null}
      </View>

      <View style={styles.content}>
        <View style={styles.titleRow}>
          <Text numberOfLines={1} style={styles.name}>
            {conversation.name}
          </Text>
          <Text style={styles.time}>{conversation.time}</Text>
        </View>
        <Text numberOfLines={1} style={styles.preview}>
          {conversation.lastMessage}
        </Text>
      </View>

      {conversation.isMuted ? (
        <BellOff color={colors.muted} size={18} strokeWidth={2.1} />
      ) : null}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  avatarWrap: {
    position: "relative",
  },
  container: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.md,
    minHeight: 74,
    paddingVertical: spacing.sm,
  },
  content: {
    flex: 1,
    gap: spacing.xs,
  },
  name: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.body,
    fontWeight: "700",
  },
  onlineDot: {
    backgroundColor: colors.tertiary,
    borderColor: colors.background,
    borderRadius: 7,
    borderWidth: 2,
    bottom: 2,
    height: 14,
    position: "absolute",
    right: 2,
    width: 14,
  },
  pressed: {
    opacity: 0.74,
  },
  preview: {
    color: colors.muted,
    fontSize: typography.label,
  },
  time: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "600",
  },
  titleRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.sm,
  },
});
