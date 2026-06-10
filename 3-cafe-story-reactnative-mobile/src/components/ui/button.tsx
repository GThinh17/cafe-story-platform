import { ActivityIndicator, Pressable, StyleSheet, Text } from "react-native";
import { colors, spacing, typography } from "../../theme";

type ButtonProps = {
  disabled?: boolean;
  isLoading?: boolean;
  label: string;
  onPress?: () => void;
  variant?: "primary" | "secondary";
};

export function Button({
  disabled = false,
  isLoading = false,
  label,
  onPress,
  variant = "primary",
}: ButtonProps) {
  const isDisabled = disabled || isLoading;

  return (
    <Pressable
      accessibilityRole="button"
      disabled={isDisabled}
      onPress={onPress}
      style={({ pressed }) => [
        styles.button,
        variant === "primary" ? styles.primary : styles.secondary,
        pressed && !isDisabled && styles.pressed,
        isDisabled && styles.disabled,
      ]}
    >
      {isLoading ? (
        <ActivityIndicator color={variant === "primary" ? colors.white : colors.espresso} />
      ) : (
        <Text
          style={[
            styles.label,
            variant === "primary" ? styles.primaryLabel : styles.secondaryLabel,
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
    backgroundColor: colors.espresso,
  },
  primaryLabel: {
    color: colors.white,
  },
  secondary: {
    backgroundColor: colors.surfaceMuted,
  },
  secondaryLabel: {
    color: colors.espresso,
  },
});
