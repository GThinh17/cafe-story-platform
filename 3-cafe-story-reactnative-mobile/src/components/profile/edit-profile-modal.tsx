import { ArrowLeft, Camera, ChevronDown, UserRound } from "lucide-react-native";
import { useEffect, useState } from "react";
import {
  ActivityIndicator,
  KeyboardAvoidingView,
  Modal,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Switch,
  Text,
  TextInput,
  View,
} from "react-native";
import { colors, spacing, typography } from "../../theme";
import type { UserResponse, UserUpdateRequest } from "../../types";

const BIO_MAX_LENGTH = 150;

type EditProfileModalProps = {
  error?: string | null;
  isUploadingAvatar?: boolean;
  isSaving: boolean;
  onAvatarPress?: () => void;
  onClose: () => void;
  onSave: (request: UserUpdateRequest) => void;
  profile: UserResponse | null;
  visible: boolean;
};

export function EditProfileModal({
  error,
  isUploadingAvatar = false,
  isSaving,
  onAvatarPress,
  onClose,
  onSave,
  profile,
  visible,
}: EditProfileModalProps) {
  const [fullName, setFullName] = useState("");
  const [userName, setUserName] = useState("");
  const [pronouns, setPronouns] = useState("");
  const [bio, setBio] = useState("");
  const [gender, setGender] = useState("Male");
  const [isAiCreator, setIsAiCreator] = useState(false);

  useEffect(() => {
    if (visible) {
      setFullName(profile?.userFullName ?? "");
      setUserName(profile?.userName ?? "");
      setPronouns("");
      setBio(profile?.userDescription ?? "");
      setGender("Male");
      setIsAiCreator(false);
    }
  }, [profile, visible]);

  function handleSave() {
    const nextUserName = userName.trim();

    onSave({
      userDescription: bio.trim(),
      userFullName: fullName.trim() || undefined,
      userName: nextUserName || profile?.userName,
    });
  }

  return (
    <Modal animationType="slide" onRequestClose={onClose} visible={visible}>
      <KeyboardAvoidingView
        behavior={Platform.OS === "ios" ? "padding" : undefined}
        style={styles.modal}
      >
        <View style={styles.header}>
          <Pressable
            accessibilityLabel="Close edit profile"
            accessibilityRole="button"
            disabled={isSaving}
            onPress={onClose}
            style={({ pressed }) => [
              styles.headerIconButton,
              pressed && styles.pressed,
              isSaving && styles.disabled,
            ]}
          >
            <ArrowLeft color={colors.foreground} size={32} strokeWidth={2.5} />
          </Pressable>

          <Text numberOfLines={1} style={styles.headerTitle}>
            Edit profile
          </Text>

          <Pressable
            accessibilityLabel="Save profile"
            accessibilityRole="button"
            disabled={isSaving}
            onPress={handleSave}
            style={({ pressed }) => [
              styles.saveButton,
              pressed && styles.pressed,
              isSaving && styles.disabled,
            ]}
          >
            {isSaving ? (
              <ActivityIndicator color={colors.link} />
            ) : (
              <Text style={styles.saveText}>Save</Text>
            )}
          </Pressable>
        </View>

        <ScrollView
          contentContainerStyle={styles.content}
          keyboardShouldPersistTaps="handled"
          showsVerticalScrollIndicator={false}
        >
          <View style={styles.avatarSection}>
            <View style={styles.avatarActions}>
              <View style={styles.avatarCircle}>
                <Camera color={colors.foreground} size={34} strokeWidth={2.7} />
              </View>
              <View style={styles.avatarCircle}>
                <UserRound color={colors.foreground} size={36} strokeWidth={2.5} />
              </View>
            </View>

            <Pressable
              accessibilityLabel="Edit photo or avatar"
              accessibilityRole="button"
              disabled={isSaving || isUploadingAvatar}
              onPress={onAvatarPress}
              style={({ pressed }) => [styles.photoLink, pressed && styles.pressed]}
            >
              {isUploadingAvatar ? (
                <ActivityIndicator color={colors.link} />
              ) : (
                <Text style={styles.photoLinkText}>Edit photo or avatar</Text>
              )}
            </Pressable>
          </View>

          <View style={styles.fields}>
            <ProfileField
              label="Name"
              onChangeText={setFullName}
              value={fullName}
            />
            <ProfileField
              autoCapitalize="none"
              label="Username"
              onChangeText={setUserName}
              value={userName}
            />
            <ProfileField
              label="Pronouns"
              onChangeText={setPronouns}
              placeholder="Add pronouns"
              value={pronouns}
            />
            <ProfileField
              label="Bio"
              maxLength={BIO_MAX_LENGTH}
              onChangeText={setBio}
              placeholder="Add a bio"
              value={bio}
            />
          </View>

          <Pressable
            accessibilityLabel="Add link"
            accessibilityRole="button"
            style={({ pressed }) => [styles.sectionAction, pressed && styles.pressed]}
          >
            <Text style={styles.sectionActionText}>Add link</Text>
          </Pressable>

          <View style={styles.musicRow}>
            <View style={styles.musicCopy}>
              <Text style={styles.sectionTitle}>Music</Text>
              <Text style={styles.sectionDescription}>
                Add songs, profiles, and more.
              </Text>
            </View>
            <Text style={styles.musicCount}>1</Text>
          </View>

          <Pressable
            accessibilityLabel="Select gender"
            accessibilityRole="button"
            style={({ pressed }) => [styles.selectField, pressed && styles.pressed]}
          >
            <View>
              <Text style={styles.fieldLabel}>Gender</Text>
              <Text style={styles.fieldValue}>{gender}</Text>
            </View>
            <ChevronDown color={colors.muted} size={28} strokeWidth={2.4} />
          </Pressable>

          <Pressable
            accessibilityLabel="Reorder grid"
            accessibilityRole="button"
            style={({ pressed }) => [styles.sectionAction, pressed && styles.pressed]}
          >
            <Text style={styles.sectionActionText}>Reorder grid</Text>
          </Pressable>

          <View style={styles.aiRow}>
            <View style={styles.aiCopy}>
              <Text style={styles.sectionTitle}>AI creator</Text>
              <Text style={styles.sectionDescription}>
                Add this label to your profile if your content frequently uses AI.{" "}
                <Text style={styles.inlineLink}>Learn more</Text>
              </Text>
            </View>
            <Switch
              onValueChange={setIsAiCreator}
              thumbColor={colors.white}
              trackColor={{
                false: colors.muted,
                true: colors.link,
              }}
              value={isAiCreator}
            />
          </View>

          {error ? <Text style={styles.errorText}>{error}</Text> : null}

          <View style={styles.linkRows}>
            <SettingsLink label="Switch to professional account" />
            <SettingsLink label="Personal information settings" />
            <SettingsLink label="Show verified profile badge" />
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </Modal>
  );
}

type ProfileFieldProps = {
  autoCapitalize?: "none" | "sentences" | "words" | "characters";
  label: string;
  maxLength?: number;
  onChangeText: (value: string) => void;
  placeholder?: string;
  value: string;
};

function ProfileField({
  autoCapitalize = "sentences",
  label,
  maxLength,
  onChangeText,
  placeholder,
  value,
}: ProfileFieldProps) {
  return (
    <View style={styles.fieldBox}>
      <Text style={styles.fieldLabel}>{label}</Text>
      <TextInput
        autoCapitalize={autoCapitalize}
        maxLength={maxLength}
        onChangeText={onChangeText}
        placeholder={placeholder ?? label}
        placeholderTextColor={colors.muted}
        style={styles.fieldInput}
        value={value}
      />
    </View>
  );
}

type SettingsLinkProps = {
  label: string;
};

function SettingsLink({ label }: SettingsLinkProps) {
  return (
    <Pressable
      accessibilityLabel={label}
      accessibilityRole="button"
      style={({ pressed }) => [styles.settingsLink, pressed && styles.pressed]}
    >
      <Text style={styles.settingsLinkText}>{label}</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  aiCopy: {
    flex: 1,
    gap: 3,
  },
  aiRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.lg,
    paddingHorizontal: spacing.xl,
  },
  avatarActions: {
    flexDirection: "row",
    gap: spacing.xl,
  },
  avatarCircle: {
    alignItems: "center",
    backgroundColor: colors.secondarySoft,
    borderRadius: 52,
    height: 104,
    justifyContent: "center",
    width: 104,
  },
  avatarSection: {
    alignItems: "center",
    gap: spacing.lg,
    paddingTop: spacing.xl,
  },
  content: {
    gap: spacing.lg,
    paddingBottom: 56,
  },
  disabled: {
    opacity: 0.5,
  },
  errorText: {
    color: colors.danger,
    fontSize: typography.label,
    fontWeight: "700",
    lineHeight: 20,
    paddingHorizontal: spacing.xl,
  },
  fieldBox: {
    borderColor: colors.border,
    borderRadius: 14,
    borderWidth: 1,
    minHeight: 74,
    justifyContent: "center",
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.sm,
  },
  fieldInput: {
    color: colors.foreground,
    fontSize: typography.title,
    fontWeight: "500",
    padding: 0,
  },
  fieldLabel: {
    color: colors.muted,
    fontSize: typography.body,
    fontWeight: "600",
  },
  fieldValue: {
    color: colors.foreground,
    fontSize: typography.title,
    fontWeight: "500",
  },
  fields: {
    gap: spacing.md,
    paddingHorizontal: spacing.xl,
  },
  header: {
    alignItems: "center",
    backgroundColor: colors.white,
    borderBottomColor: colors.border,
    borderBottomWidth: 1,
    flexDirection: "row",
    minHeight: 80,
    paddingHorizontal: spacing.lg,
  },
  headerIconButton: {
    alignItems: "center",
    height: 48,
    justifyContent: "center",
    width: 48,
  },
  headerTitle: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.heading,
    fontWeight: "900",
    marginLeft: spacing.md,
  },
  inlineLink: {
    color: colors.link,
  },
  linkRows: {
    borderTopColor: colors.border,
    borderTopWidth: 1,
  },
  modal: {
    backgroundColor: colors.white,
    flex: 1,
  },
  musicCopy: {
    flex: 1,
    gap: 3,
  },
  musicCount: {
    color: colors.muted,
    fontSize: typography.body,
    fontWeight: "700",
  },
  musicRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.md,
    paddingHorizontal: spacing.xl,
  },
  photoLink: {
    minHeight: 36,
    justifyContent: "center",
  },
  photoLinkText: {
    color: colors.link,
    fontSize: typography.title,
    fontWeight: "900",
  },
  pressed: {
    opacity: 0.72,
  },
  saveButton: {
    alignItems: "center",
    minHeight: 44,
    justifyContent: "center",
    minWidth: 58,
  },
  saveText: {
    color: colors.link,
    fontSize: typography.body,
    fontWeight: "900",
  },
  sectionAction: {
    minHeight: 42,
    justifyContent: "center",
    paddingHorizontal: spacing.xl,
  },
  sectionActionText: {
    color: colors.foreground,
    fontSize: typography.title,
    fontWeight: "500",
  },
  sectionDescription: {
    color: colors.muted,
    fontSize: typography.body,
    lineHeight: 24,
  },
  sectionTitle: {
    color: colors.foreground,
    fontSize: typography.title,
    fontWeight: "500",
  },
  selectField: {
    alignItems: "center",
    borderColor: colors.border,
    borderRadius: 14,
    borderWidth: 1,
    flexDirection: "row",
    justifyContent: "space-between",
    minHeight: 74,
    marginHorizontal: spacing.xl,
    paddingHorizontal: spacing.lg,
  },
  settingsLink: {
    borderBottomColor: colors.border,
    borderBottomWidth: 1,
    minHeight: 72,
    justifyContent: "center",
    paddingHorizontal: spacing.xl,
  },
  settingsLinkText: {
    color: colors.link,
    fontSize: typography.title,
    fontWeight: "500",
  },
});
