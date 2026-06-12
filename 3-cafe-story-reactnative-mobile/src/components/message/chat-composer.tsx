import { Camera, Image, Mic, Send } from "lucide-react-native";
import { Pressable, StyleSheet, TextInput, View } from "react-native";
import { colors, spacing, typography } from "../../theme";

type ChatComposerProps = {
  disabled?: boolean;
  onChangeText: (value: string) => void;
  onSend?: () => void;
  value: string;
};

export function ChatComposer({
  disabled = false,
  onChangeText,
  onSend,
  value,
}: ChatComposerProps) {
  const canSend = Boolean(value.trim()) && !disabled && Boolean(onSend);

  return (
    <View style={styles.container}>
      <Camera color={colors.primary} size={24} strokeWidth={2.3} />
      <View style={styles.inputWrap}>
        <TextInput
          editable={!disabled}
          onChangeText={onChangeText}
          placeholder="Message..."
          placeholderTextColor={colors.muted}
          style={styles.input}
          value={value}
        />
        <Mic color={colors.muted} size={21} strokeWidth={2.2} />
        <Image color={colors.muted} size={21} strokeWidth={2.2} />
      </View>
      <Pressable
        accessibilityLabel="Send message"
        accessibilityRole="button"
        disabled={!canSend}
        onPress={onSend}
        style={({ pressed }) => [
          styles.sendButton,
          pressed && canSend ? styles.pressed : null,
          !canSend ? styles.sendButtonDisabled : null,
        ]}
      >
        <Send color={colors.primary} size={23} strokeWidth={2.3} />
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
    color: colors.foreground,
    flex: 1,
    fontSize: typography.body,
    paddingVertical: spacing.sm,
  },
  inputWrap: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 22,
    borderWidth: 1,
    flex: 1,
    flexDirection: "row",
    gap: spacing.sm,
    minHeight: 44,
    paddingHorizontal: spacing.md,
  },
  pressed: {
    opacity: 0.7,
    transform: [{ scale: 0.96 }],
  },
  sendButton: {
    alignItems: "center",
    borderRadius: 18,
    height: 36,
    justifyContent: "center",
    width: 36,
  },
  sendButtonDisabled: {
    opacity: 0.4,
  },
});
