import { ChevronLeft, Info, Phone, Video } from "lucide-react-native";
import { Pressable } from "../../features/i18n/localized-native";
import { Text } from "../../features/i18n/localized-native";
import { StyleSheet, View } from "react-native";
import { Avatar } from "../ui/avatar";
import { colors, spacing, typography } from "../../theme";
import type { ChatIdentity } from "../../types";

type ChatDetailHeaderProps = {
  conversation: ChatIdentity;
  onBackPress: () => void;
  onProfilePress?: () => void;
};

export function ChatDetailHeader({
  conversation,
  onBackPress,
  onProfilePress,
}: ChatDetailHeaderProps) {
  return (
    <View style={styles.container}>
      <Pressable
        accessibilityLabel="Go back"
        accessibilityRole="button"
        onPress={onBackPress}
        style={({ pressed }) => [styles.backButton, pressed && styles.pressed]}
      >
        <ChevronLeft color={colors.foreground} size={32} strokeWidth={2.4} />
      </Pressable>

      <Pressable
        accessibilityLabel="Open chat profile"
        accessibilityRole="button"
        disabled={!onProfilePress}
        onPress={onProfilePress}
        style={({ pressed }) => pressed && styles.pressed}
      >
        <Avatar size={40} uri={conversation.avatarUri} />
      </Pressable>

      <View style={styles.identity}>
        <Text numberOfLines={1} style={styles.name}>
          {conversation.name}
        </Text>
        <Text numberOfLines={1} style={styles.status}>
          {conversation.isOnline ? "Active now" : conversation.userName}
        </Text>
      </View>

      <View style={styles.actions}>
        <Phone color={colors.foreground} size={22} strokeWidth={2.2} />
        <Video color={colors.foreground} size={23} strokeWidth={2.2} />
        <Info color={colors.foreground} size={22} strokeWidth={2.2} />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  actions: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.md,
  },
  backButton: {
    alignItems: "center",
    borderRadius: 22,
    height: 44,
    justifyContent: "center",
    width: 44,
  },
  container: {
    alignItems: "center",
    backgroundColor: colors.background,
    flexDirection: "row",
    gap: spacing.sm,
    minHeight: 66,
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.sm,
  },
  identity: {
    flex: 1,
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
