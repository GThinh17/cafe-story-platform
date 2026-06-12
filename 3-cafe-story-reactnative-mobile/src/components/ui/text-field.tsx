import { StyleSheet, Text, TextInput, TextInputProps, View } from "react-native";
import { colors, spacing, typography } from "../../theme";

type TextFieldProps = TextInputProps & {
  label: string;
};

export function TextField({ label, style, ...props }: TextFieldProps) {
  return (
    <View style={styles.wrapper}>
      <Text style={styles.label}>{label}</Text>
      <TextInput
        placeholderTextColor={colors.muted}
        style={[styles.input, style]}
        {...props}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  input: {
    borderColor: colors.border,
    borderRadius: 12,
    borderWidth: 1,
    color: colors.foreground,
    fontSize: typography.body,
    minHeight: 52,
    paddingHorizontal: spacing.lg,
  },
  label: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "800",
    letterSpacing: 0.8,
    textTransform: "uppercase",
  },
  wrapper: {
    gap: spacing.sm,
  },
});
