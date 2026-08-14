import { Check, Eye, Lock, MessageCircle, Pin } from "lucide-react-native";
import { Switch } from "../../features/i18n/localized-native";
import { Pressable, Text } from "../../features/i18n/localized-native";
import { Image, ScrollView, StyleSheet, View } from "react-native";

import { Avatar } from "../ui/avatar";
import { colors, spacing, typography } from "../../theme";
import type { AuthUser, CreatePostDraft, PostVisibility } from "../../types";

type CreatePostSettingsStepProps = {
  draft: CreatePostDraft;
  onUpdateDraft: (patch: Partial<CreatePostDraft>) => void;
  postingIdentity?: {
    avatarUrl: string | null;
    id: string;
    name: string | null;
  } | null;
  user: AuthUser | null;
};

const visibilityOptions: Array<{
  description: string;
  Icon: typeof Eye;
  label: string;
  value: PostVisibility;
}> = [
  {
    description: "Everyone can see this post",
    Icon: Eye,
    label: "Public",
    value: "PUBLIC",
  },
  {
    description: "Only you can see this post",
    Icon: Lock,
    label: "Private",
    value: "PRIVATE",
  },
];

export function CreatePostSettingsStep({
  draft,
  onUpdateDraft,
  postingIdentity,
  user,
}: CreatePostSettingsStepProps) {
  const displayName =
    postingIdentity?.name || user?.userFullName || user?.userName || "CafeStory user";
  const avatarUri = postingIdentity?.avatarUrl ?? user?.userAvatar;
  const previewImage = draft.mediaUrls[0];

  return (
    <ScrollView
      contentContainerStyle={styles.content}
      showsVerticalScrollIndicator={false}
    >
      <View style={styles.previewCard}>
        <View style={styles.previewHeader}>
          <Avatar size={46} uri={avatarUri} />
          <View style={styles.previewCopy}>
            <Text numberOfLines={1} style={styles.previewName}>
              {displayName}
            </Text>
            <Text style={styles.previewMeta}>
              {postingIdentity ? "Cafe page post" : "Just now"}
            </Text>
          </View>
        </View>
        {previewImage ? (
          <Image
            accessibilityLabel="Post preview"
            resizeMode="cover"
            source={{ uri: previewImage }}
            style={[
              styles.previewImage,
              {
                aspectRatio: draft.mediaAspectRatio,
              },
            ]}
          />
        ) : null}
        <Text numberOfLines={3} style={styles.previewCaption}>
          {draft.caption}
        </Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Visibility</Text>
        <View style={styles.visibilityGrid}>
          {visibilityOptions.map(({ description, Icon, label, value }) => {
            const selected = draft.visibility === value;
            return (
              <Pressable
                accessibilityLabel={`${label}: ${description}`}
                accessibilityRole="radio"
                accessibilityState={{ checked: selected }}
                key={value}
                onPress={() => onUpdateDraft({ visibility: value })}
                style={({ pressed }) => [
                  styles.visibilityCard,
                  selected && styles.visibilityCardActive,
                  pressed && styles.pressed,
                ]}
              >
                <View style={styles.visibilityTop}>
                  <Icon
                    color={selected ? colors.primary : colors.foreground}
                    size={24}
                    strokeWidth={2.4}
                  />
                  {selected ? (
                    <View style={styles.checkDot}>
                      <Check color={colors.white} size={14} strokeWidth={3} />
                    </View>
                  ) : null}
                </View>
                <Text style={styles.visibilityLabel}>{label}</Text>
                <Text style={styles.visibilityDescription}>{description}</Text>
              </Pressable>
            );
          })}
        </View>
      </View>

      <View style={styles.settingsCard}>
        <SettingToggle
          Icon={MessageCircle}
          description="People can reply under this post."
          label="Allow comments"
          onValueChange={(allowComments) => onUpdateDraft({ allowComments })}
          value={draft.allowComments}
        />
        <View style={styles.divider} />
        <SettingToggle
          Icon={Pin}
          description="Keep this post near the top of your profile."
          label="Pin to profile"
          onValueChange={(pinToProfile) => onUpdateDraft({ pinToProfile })}
          value={draft.pinToProfile}
        />
      </View>

      <View style={styles.payloadNote}>
        <Text style={styles.payloadTitle}>Ready to post</Text>
        <Text style={styles.payloadText}>
          CafeStory will publish your caption, selected photos, comments setting, profile pin, location, cafe page, and tagged people where available.
        </Text>
      </View>
    </ScrollView>
  );
}

type SettingToggleProps = {
  description: string;
  Icon: typeof MessageCircle;
  label: string;
  onValueChange: (value: boolean) => void;
  value: boolean;
};

function SettingToggle({
  description,
  Icon,
  label,
  onValueChange,
  value,
}: SettingToggleProps) {
  return (
    <View style={styles.settingRow}>
      <View style={styles.settingIcon}>
        <Icon color={colors.foreground} size={22} strokeWidth={2.4} />
      </View>
      <View style={styles.settingCopy}>
        <Text style={styles.settingLabel}>{label}</Text>
        <Text style={styles.settingDescription}>{description}</Text>
      </View>
      <Switch
        onValueChange={onValueChange}
        thumbColor={colors.white}
        trackColor={{
          false: colors.border,
          true: colors.link,
        }}
        value={value}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  checkDot: {
    alignItems: "center",
    backgroundColor: colors.primary,
    borderRadius: 12,
    height: 24,
    justifyContent: "center",
    width: 24,
  },
  content: {
    gap: spacing.xl,
    padding: spacing.lg,
    paddingBottom: 140,
  },
  divider: {
    backgroundColor: colors.border,
    height: StyleSheet.hairlineWidth,
    marginLeft: 58,
  },
  payloadNote: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 16,
    borderWidth: 1,
    gap: spacing.xs,
    padding: spacing.lg,
  },
  payloadText: {
    color: colors.muted,
    fontSize: typography.label,
    lineHeight: 20,
  },
  payloadTitle: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },
  pressed: {
    opacity: 0.72,
  },
  previewCaption: {
    color: colors.foreground,
    fontSize: typography.body,
    lineHeight: 23,
  },
  previewCard: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 18,
    borderWidth: 1,
    gap: spacing.md,
    overflow: "hidden",
    padding: spacing.md,
  },
  previewCopy: {
    flex: 1,
  },
  previewHeader: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.md,
  },
  previewImage: {
    backgroundColor: colors.surfaceMuted,
    borderRadius: 14,
    width: "100%",
  },
  previewMeta: {
    color: colors.muted,
    fontSize: typography.label,
    fontWeight: "600",
  },
  previewName: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },
  section: {
    gap: spacing.md,
  },
  sectionTitle: {
    color: colors.foreground,
    fontSize: typography.title,
    fontWeight: "900",
  },
  settingCopy: {
    flex: 1,
    gap: 2,
  },
  settingDescription: {
    color: colors.muted,
    fontSize: typography.label,
    lineHeight: 18,
  },
  settingIcon: {
    alignItems: "center",
    height: 42,
    justifyContent: "center",
    width: 42,
  },
  settingLabel: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },
  settingRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.md,
    minHeight: 76,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.sm,
  },
  settingsCard: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 18,
    borderWidth: 1,
    overflow: "hidden",
  },
  visibilityCard: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 18,
    borderWidth: 1,
    flex: 1,
    gap: spacing.sm,
    minHeight: 150,
    padding: spacing.lg,
  },
  visibilityCardActive: {
    borderColor: colors.primary,
    borderWidth: 2,
  },
  visibilityDescription: {
    color: colors.muted,
    fontSize: typography.label,
    lineHeight: 19,
  },
  visibilityGrid: {
    flexDirection: "row",
    gap: spacing.md,
  },
  visibilityLabel: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },
  visibilityTop: {
    alignItems: "center",
    flexDirection: "row",
    justifyContent: "space-between",
  },
});
