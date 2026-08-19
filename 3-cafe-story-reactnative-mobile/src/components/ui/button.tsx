import { ActivityIndicator, StyleSheet } from "react-native";
import { Pressable, Text } from "react-native";
import { colors, spacing, typography } from "../../theme";
import { t } from "../../features/i18n";

type ButtonProps = {
  disabled?: boolean;
  isLoading?: boolean;
  label: string;
  onPress?: () => void;
  variant?: "primary" | "secondary" | "inverted" | "outlined";
};

export function Button({
  disabled = false,
  isLoading = false,
  label,
  onPress,
  variant = "primary",
}: ButtonProps) {
  const isDisabled = disabled || isLoading;
  const isDarkVariant = variant === "primary" || variant === "inverted";

  return (
    <Pressable
      accessibilityRole="button"
      disabled={isDisabled}
      onPress={onPress}
      style={({ pressed }) => [
        styles.button,
        styles[variant],
        pressed && !isDisabled && styles.pressed,
        isDisabled && styles.disabled,
      ]}
    >
      {isLoading ? (
        <ActivityIndicator color={isDarkVariant ? colors.white : colors.primary} />
      ) : (
        <Text
          style={[
            styles.label,
            isDarkVariant ? styles.darkLabel : styles.lightLabel,
          ]}
        >
          {label}
        </Text>
      )}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  button: {
    alignItems: "center",
    borderRadius: 12,
    minHeight: 52,
    justifyContent: "center",
    paddingHorizontal: spacing.xl,
  },
  disabled: {
    opacity: 0.65,
  },
  label: {
    fontSize: typography.label,
    fontWeight: "800",
  },
  pressed: {
    transform: [{ scale: 0.99 }],
  },
  primary: {
    backgroundColor: colors.primary,
  },
  darkLabel: {
    color: colors.white,
  },
  secondary: {
    backgroundColor: colors.white,
    borderColor: colors.border,
    borderWidth: 1,
  },
  inverted: {
    backgroundColor: colors.tertiary,
  },
  lightLabel: {
    color: colors.primary,
  },
  outlined: {
    backgroundColor: colors.white,
    borderColor: colors.secondary,
    borderWidth: 1,
  },
});
