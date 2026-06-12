import { Check, X } from "lucide-react-native";
import { useEffect, useState } from "react";
import {
  KeyboardAvoidingView,
  Modal,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import { colors, spacing, typography } from "../../theme";

const BIO_MAX_LENGTH = 150;

type BioEditorModalProps = {
  error?: string | null;
  initialBio?: string | null;
  isSaving: boolean;
  onClose: () => void;
  onSave: (bio: string) => void;
  visible: boolean;
};

export function BioEditorModal({
  error,
  initialBio,
  isSaving,
  onClose,
  onSave,
  visible,
}: BioEditorModalProps) {
  const [bioDraft, setBioDraft] = useState("");

  useEffect(() => {
    if (visible) {
      setBioDraft(initialBio?.trim() ?? "");
    }
  }, [initialBio, visible]);

  function handleClose() {
    if (!isSaving) {
      onClose();
    }
  }

  return (
    <Modal
      animationType="slide"
      onRequestClose={handleClose}
      presentationStyle="fullScreen"
      visible={visible}
    >
      <KeyboardAvoidingView
        behavior={Platform.OS === "ios" ? "padding" : undefined}
        style={styles.modal}
      >
        <View style={styles.topBar}>
          <Pressable
            accessibilityLabel="Close bio editor"
            accessibilityRole="button"
            disabled={isSaving}
            onPress={handleClose}
            style={({ pressed }) => [
              styles.iconButton,
              pressed && styles.pressed,
              isSaving && styles.disabled,
            ]}
          >
            <X color={colors.foreground} size={34} strokeWidth={2.2} />
          </Pressable>

          <Text style={styles.title}>Bio</Text>

          <Pressable
            accessibilityLabel="Save bio"
            accessibilityRole="button"
            disabled={isSaving}
            onPress={() => onSave(bioDraft)}
            style={({ pressed }) => [
              styles.iconButton,
              pressed && styles.pressed,
              isSaving && styles.disabled,
            ]}
          >
            <Check color={colors.link} size={36} strokeWidth={2.4} />
          </Pressable>
        </View>

        <View style={styles.inputBox}>
          <View style={styles.inputHeader}>
            <Text style={styles.inputLabel}>Bio</Text>
            <Text style={styles.counter}>
              {bioDraft.length}/{BIO_MAX_LENGTH}
            </Text>
          </View>

          <TextInput
            autoCapitalize="sentences"
            autoFocus
            maxLength={BIO_MAX_LENGTH}
            multiline
            onChangeText={setBioDraft}
            placeholder="Add a bio"
            placeholderTextColor={colors.muted}
            selectionColor={colors.tertiary}
            style={styles.input}
            textAlignVertical="top"
            value={bioDraft}
          />
        </View>

        {error ? <Text style={styles.error}>{error}</Text> : null}
      </KeyboardAvoidingView>
    </Modal>
  );
}

const styles = StyleSheet.create({
  counter: {
    color: colors.muted,
    fontSize: typography.body,
    fontWeight: "700",
  },
  disabled: {
    opacity: 0.44,
  },
  error: {
    color: colors.danger,
    fontSize: typography.label,
    fontWeight: "700",
    lineHeight: 20,
    paddingHorizontal: spacing.xl,
  },
  iconButton: {
    alignItems: "center",
    height: 56,
    justifyContent: "center",
    width: 56,
  },
  input: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.title,
    fontWeight: "500",
    lineHeight: 30,
    padding: 0,
  },
  inputBox: {
    borderColor: colors.foreground,
    borderRadius: 18,
    borderWidth: 2,
    height: 168,
    marginHorizontal: spacing.xl,
    marginTop: spacing.xl,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.md,
  },
  inputHeader: {
    alignItems: "center",
    flexDirection: "row",
    justifyContent: "space-between",
    marginBottom: spacing.sm,
  },
  inputLabel: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "700",
  },
  modal: {
    backgroundColor: colors.white,
    flex: 1,
  },
  pressed: {
    opacity: 0.72,
  },
  title: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.heading,
    fontWeight: "900",
  },
  topBar: {
    alignItems: "center",
    flexDirection: "row",
    minHeight: 92,
    paddingHorizontal: spacing.md,
    paddingTop: spacing.xl,
  },
});
