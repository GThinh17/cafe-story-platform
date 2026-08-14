import { Send } from "lucide-react-native";
import { Pressable } from "../../features/i18n/localized-native";
import { TextInput } from "../../features/i18n/localized-native";
import { StyleSheet, View } from "react-native";
import { colors, spacing, typography } from "../../theme";

type AiAssistantComposerProps = {
  disabled?: boolean;
  onChangeText: (value: string) => void;
  onSend: () => void;
  value: string;
};

export function AiAssistantComposer({
  disabled = false,
  onChangeText,
  onSend,
  value,
}: AiAssistantComposerProps) {
  const canSend = Boolean(value.trim()) && !disabled;

  return (
    <View style={styles.container}>
      <TextInput
        editable={!disabled}
        onChangeText={onChangeText}
        onSubmitEditing={canSend ? onSend : undefined}
        placeholder="Ask CafeStory Assistant..."
        placeholderTextColor={colors.muted}
        returnKeyType="send"
        style={styles.input}
        value={value}
      />

      <Pressable
        accessibilityLabel="Send assistant message"
        accessibilityRole="button"
        disabled={!canSend}
        onPress={onSend}
        style={({ pressed }) => [
          styles.sendButton,
          pressed && canSend ? styles.pressed : null,
          !canSend ? styles.sendButtonDisabled : null,
        ]}
      >
        <Send color={colors.white} size={21} strokeWidth={2.4} />
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: "center",
    backgroundColor: colors.background,
    borderTopColor: colors.border,
    borderTopWidth: StyleSheet.hairlineWidth,
    flexDirection: "row",
    gap: spacing.sm,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
  },
  input: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 22,
    borderWidth: 1,
    color: colors.foreground,
    flex: 1,
    fontSize: typography.body,
    minHeight: 44,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
  },
  pressed: {
    opacity: 0.72,
    transform: [{ scale: 0.96 }],
  },
  sendButton: {
    alignItems: "center",
    backgroundColor: colors.primary,
    borderRadius: 19,
    height: 38,
    justifyContent: "center",
    width: 38,
  },
  sendButtonDisabled: {
    opacity: 0.42,
  },
});
