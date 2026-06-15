import { X } from "lucide-react-native";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { Avatar } from "../ui/avatar";
import { colors, spacing, typography } from "../../theme";

export type NewChatSuggestionUser = {
  userAvatar: string | null;
  userFullName: string | null;
  userId: string;
  userName: string;
};

type NewChatSuggestionRowProps = {
  disabled?: boolean;
  onPress: (user: NewChatSuggestionUser) => void;
  user: NewChatSuggestionUser;
};

export function NewChatSuggestionRow({
  disabled = false,
  onPress,
  user,
}: NewChatSuggestionRowProps) {
  const displayName = user.userFullName || user.userName;

  return (
    <Pressable
      accessibilityRole="button"
      disabled={disabled}
      onPress={() => onPress(user)}
      style={({ pressed }) => [
        styles.container,
        pressed && !disabled ? styles.pressed : null,
        disabled ? styles.disabled : null,
      ]}
    >
      <Avatar size={58} uri={user.userAvatar} />
      <View style={styles.identity}>
        <Text numberOfLines={1} style={styles.name}>
          {displayName}
        </Text>
        <Text numberOfLines={1} style={styles.username}>
          {user.userName}
        </Text>
      </View>
      <X color={colors.muted} size={22} strokeWidth={2.1} />
    </Pressable>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.md,
    minHeight: 78,
    paddingVertical: spacing.sm,
  },
  disabled: {
    opacity: 0.56,
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
    opacity: 0.74,
  },
  username: {
    color: colors.muted,
    fontSize: typography.body,
    fontWeight: "600",
    marginTop: spacing.xs,
  },
});
