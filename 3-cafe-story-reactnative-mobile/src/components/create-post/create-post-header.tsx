import { ChevronLeft, X } from "lucide-react-native";
import { Pressable, StyleSheet, Text, View } from "react-native";

import { colors, spacing, typography } from "../../theme";

type CreatePostHeaderProps = {
  actionDisabled?: boolean;
  actionLabel: string;
  isBack?: boolean;
  isSubmitting?: boolean;
  onAction: () => void;
  onLeftPress: () => void;
  title: string;
};

export function CreatePostHeader({
  actionDisabled = false,
  actionLabel,
  isBack = false,
  isSubmitting = false,
  onAction,
  onLeftPress,
  title,
}: CreatePostHeaderProps) {
  const LeftIcon = isBack ? ChevronLeft : X;
  const disabled = actionDisabled || isSubmitting;

  return (
    <View style={styles.header}>
      <Pressable
        accessibilityLabel={isBack ? "Back to compose" : "Cancel post"}
        accessibilityRole="button"
        onPress={onLeftPress}
        style={({ pressed }) => [styles.iconButton, pressed && styles.pressed]}
      >
        <LeftIcon color={colors.foreground} size={32} strokeWidth={2.6} />
      </Pressable>

      <Text numberOfLines={1} style={styles.title}>
        {title}
      </Text>

      <Pressable
        accessibilityLabel={actionLabel}
        accessibilityRole="button"
        disabled={disabled}
        onPress={onAction}
        style={({ pressed }) => [
          styles.actionButton,
          pressed && !disabled && styles.pressed,
          disabled && styles.disabled,
        ]}
      >
        <Text style={styles.actionText}>
          {isSubmitting ? "Posting" : actionLabel}
        </Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  actionButton: {
    alignItems: "center",
    minHeight: 44,
    justifyContent: "center",
    minWidth: 72,
    paddingHorizontal: spacing.sm,
  },
  actionText: {
    color: colors.link,
    fontSize: typography.body,
    fontWeight: "900",
  },
  disabled: {
    opacity: 0.38,
  },
  header: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderBottomColor: colors.border,
    borderBottomWidth: StyleSheet.hairlineWidth,
    flexDirection: "row",
    minHeight: 64,
    paddingHorizontal: spacing.md,
  },
  iconButton: {
    alignItems: "center",
    height: 48,
    justifyContent: "center",
    width: 56,
  },
  pressed: {
    opacity: 0.68,
  },
  title: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.title,
    fontWeight: "900",
    textAlign: "center",
  },
});
