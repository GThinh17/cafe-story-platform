import { ImagePlus } from "lucide-react-native";
import { ActivityIndicator, Pressable, StyleSheet, Text, View } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";

import { colors, spacing, typography } from "../../theme";

type CreatePostBottomBarProps = {
  actionDisabled?: boolean;
  actionLabel: string;
  isSubmitting?: boolean;
  onAction: () => void;
  onAddMedia?: () => void;
  showMediaAction?: boolean;
};

export function CreatePostBottomBar({
  actionDisabled = false,
  actionLabel,
  isSubmitting = false,
  onAction,
  onAddMedia,
  showMediaAction = false,
}: CreatePostBottomBarProps) {
  const insets = useSafeAreaInsets();
  const disabled = actionDisabled || isSubmitting;

  return (
    <View style={[styles.shell, { paddingBottom: Math.max(insets.bottom, 10) }]}>
      <View style={styles.container}>
        {showMediaAction ? (
          <Pressable
            accessibilityLabel="Add photos"
            accessibilityRole="button"
            onPress={onAddMedia}
            style={({ pressed }) => [
              styles.secondaryButton,
              pressed && styles.pressed,
            ]}
          >
            <ImagePlus color={colors.foreground} size={23} strokeWidth={2.4} />
          </Pressable>
        ) : null}

        <Pressable
          accessibilityLabel={actionLabel}
          accessibilityRole="button"
          disabled={disabled}
          onPress={onAction}
          style={({ pressed }) => [
            styles.primaryButton,
            disabled && styles.disabled,
            pressed && !disabled && styles.pressed,
          ]}
        >
          {isSubmitting ? (
            <ActivityIndicator color={colors.white} size="small" />
          ) : null}
          <Text style={styles.primaryText}>
            {isSubmitting ? "Posting" : actionLabel}
          </Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: "center",
    alignSelf: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 28,
    borderWidth: 1,
    elevation: 8,
    flexDirection: "row",
    gap: spacing.sm,
    minHeight: 62,
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs,
    shadowColor: colors.primary,
    shadowOffset: {
      height: 6,
      width: 0,
    },
    shadowOpacity: 0.12,
    shadowRadius: 16,
  },
  disabled: {
    opacity: 0.4,
  },
  pressed: {
    opacity: 0.72,
  },
  primaryButton: {
    alignItems: "center",
    backgroundColor: colors.primary,
    borderRadius: 24,
    flexDirection: "row",
    gap: spacing.xs,
    height: 48,
    justifyContent: "center",
    minWidth: 164,
    paddingHorizontal: spacing.xl,
  },
  primaryText: {
    color: colors.white,
    fontSize: typography.label,
    fontWeight: "900",
  },
  secondaryButton: {
    alignItems: "center",
    borderRadius: 24,
    height: 48,
    justifyContent: "center",
    width: 52,
  },
  shell: {
    backgroundColor: "transparent",
    bottom: 0,
    left: 0,
    paddingHorizontal: spacing.xl,
    paddingTop: spacing.sm,
    position: "absolute",
    right: 0,
  },
});
