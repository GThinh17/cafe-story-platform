import { ChevronLeft, Edit3 } from "lucide-react-native";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { colors, spacing, typography } from "../../theme";

type ConversationTopBarProps = {
  onBackPress: () => void;
  onEditPress?: () => void;
  title: string;
};

export function ConversationTopBar({
  onBackPress,
  onEditPress,
  title,
}: ConversationTopBarProps) {
  return (
    <View style={styles.container}>
      <Pressable
        accessibilityLabel="Go back"
        accessibilityRole="button"
        onPress={onBackPress}
        style={({ pressed }) => [styles.iconButton, pressed && styles.pressed]}
      >
        <ChevronLeft color={colors.foreground} size={32} strokeWidth={2.4} />
      </Pressable>

      <Text numberOfLines={1} style={styles.title}>
        {title}
      </Text>

      <Pressable
        accessibilityLabel="New message"
        accessibilityRole="button"
        disabled={!onEditPress}
        onPress={onEditPress}
        style={({ pressed }) => [styles.iconButton, pressed && styles.pressed]}
      >
        {onEditPress ? (
          <Edit3 color={colors.foreground} size={24} strokeWidth={2.2} />
        ) : null}
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: "center",
    backgroundColor: colors.background,
    flexDirection: "row",
    gap: spacing.md,
    minHeight: 64,
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.sm,
  },
  iconButton: {
    alignItems: "center",
    borderRadius: 24,
    height: 48,
    justifyContent: "center",
    width: 48,
  },
  pressed: {
    opacity: 0.72,
    transform: [{ scale: 0.98 }],
  },
  title: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.title,
    fontWeight: "800",
  },
});
