import { Camera, Image as ImageIcon, MapPin, Music, SmilePlus, Store, Tag, Users, } from "lucide-react-native";
import { Pressable } from "../../features/i18n/localized-native";
import { Text, TextInput } from "../../features/i18n/localized-native";
import { ScrollView, StyleSheet, View } from "react-native";

import { MobilePostCarousel } from "../feed/mobile-post-carousel";
import { Avatar } from "../ui/avatar";
import { colors, spacing, typography } from "../../theme";
import type { AuthUser, CreatePostDraft, UserResponse } from "../../types";

type CreatePostComposeStepProps = {
  draft: CreatePostDraft;
  onAddMedia: () => void;
  onOpenLocationPicker: () => void;
  onOpenPeoplePicker: () => void;
  onRemoveMedia: () => void;
  onToggleTag: (tag: string) => void;
  onUpdateDraft: (patch: Partial<CreatePostDraft>) => void;
  postingIdentity?: {
    avatarUrl: string | null;
    id: string;
    name: string | null;
  } | null;
  selectedTaggedUsers: UserResponse[];
  user: AuthUser | null;
};

const commonTags = ["Coffee", "Brunch", "Quiet", "Work friendly", "Hidden gem"];
const mediaRatioOptions = [
  { label: "1:1", value: 1 },
  { label: "4:3", value: 4 / 3 },
  { label: "16:9", value: 16 / 9 },
  { label: "10:16", value: 10 / 16 },
];

export function CreatePostComposeStep({
  draft,
  onAddMedia,
  onOpenLocationPicker,
  onOpenPeoplePicker,
  onRemoveMedia,
  onToggleTag,
  onUpdateDraft,
  postingIdentity,
  selectedTaggedUsers,
  user,
}: CreatePostComposeStepProps) {
  const displayName =
    postingIdentity?.name || user?.userFullName || user?.userName || "CafeStory user";
  const avatarUri = postingIdentity?.avatarUrl ?? user?.userAvatar;
  const hasMedia = draft.mediaUrls.length > 0;
  const quickActions = [
    { Icon: Music, label: "Music" },
    {
      Icon: Users,
      label: selectedTaggedUsers.length
        ? `${selectedTaggedUsers.length} people`
        : "Everyone",
      onPress: onOpenPeoplePicker,
    },
    {
      Icon: MapPin,
      label: draft.location?.name ?? "Location",
      onPress: onOpenLocationPicker,
    },
    { Icon: SmilePlus, label: "Mood" },
  ];

  return (
    <View style={styles.container}>
      <ScrollView
        contentContainerStyle={styles.content}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}
      >
        <View style={styles.identityRow}>
          <Avatar size={74} uri={avatarUri} />
          <View style={styles.identityCopy}>
            <Text numberOfLines={1} style={styles.name}>
              {displayName}
            </Text>
            <Text numberOfLines={1} style={styles.username}>
              {postingIdentity
                ? "Posting as cafe page"
                : user?.userName
                  ? `@${user.userName}`
                  : "New cafe story"}
            </Text>
          </View>
        </View>

        <ScrollView
          contentContainerStyle={styles.quickActions}
          horizontal
          showsHorizontalScrollIndicator={false}
        >
          {quickActions.map(({ Icon, label, onPress }) => (
            <Pressable
              accessibilityLabel={label}
              accessibilityRole={onPress ? "button" : undefined}
              disabled={!onPress}
              key={label}
              onPress={onPress}
              style={({ pressed }) => [
                styles.quickChip,
                pressed && onPress && styles.pressed,
              ]}
            >
              <Icon color={colors.foreground} size={18} strokeWidth={2.4} />
              <Text numberOfLines={1} style={styles.quickChipText}>
                {label}
              </Text>
            </Pressable>
          ))}
        </ScrollView>

        <TextInput
          multiline
          onChangeText={(caption) => onUpdateDraft({ caption })}
          placeholder="Write a review..."
          placeholderTextColor={colors.muted}
          style={styles.captionInput}
          textAlignVertical="top"
          value={draft.caption}
        />

        <View style={styles.mediaSection}>
          {hasMedia ? (
            <>
              <MobilePostCarousel
                aspectRatio={draft.mediaAspectRatio}
                imageAccessibilityLabel="Selected post media"
                imageUrls={draft.mediaUrls}
                insetHorizontal={0}
              />
              <View style={styles.mediaActions}>
                <Pressable
                  accessibilityLabel="Add more photos"
                  accessibilityRole="button"
                  onPress={onAddMedia}
                  style={({ pressed }) => [
                    styles.mediaActionButton,
                    pressed && styles.pressed,
                  ]}
                >
                  <ImageIcon color={colors.foreground} size={20} strokeWidth={2.4} />
                  <Text style={styles.mediaActionText}>Add photo</Text>
                </Pressable>
                <Pressable
                  accessibilityLabel="Remove selected photos"
                  accessibilityRole="button"
                  onPress={onRemoveMedia}
                  style={({ pressed }) => [
                    styles.mediaActionButton,
                    pressed && styles.pressed,
                  ]}
                >
                  <Text style={styles.mediaActionText}>Clear</Text>
                </Pressable>
              </View>
            </>
          ) : (
            <Pressable
              accessibilityLabel="Choose photos from library"
              accessibilityRole="button"
              onPress={onAddMedia}
              style={({ pressed }) => [
                styles.emptyMedia,
                pressed && styles.pressed,
              ]}
            >
              <View style={styles.cameraIconWrap}>
                <Camera color={colors.primary} size={34} strokeWidth={2.5} />
              </View>
              <Text style={styles.emptyMediaTitle}>Add photos</Text>
              <Text style={styles.emptyMediaDescription}>
                Choose images from your device.
              </Text>
            </Pressable>
          )}
        </View>

        <View style={styles.ratioSection}>
          <Text style={styles.ratioTitle}>Image ratio</Text>
          <View style={styles.ratioOptions}>
            {mediaRatioOptions.map((option) => {
              const selected = draft.mediaAspectRatio === option.value;

              return (
                <Pressable
                  accessibilityLabel={`Use ${option.label} image ratio`}
                  accessibilityRole="button"
                  key={option.label}
                  onPress={() => onUpdateDraft({ mediaAspectRatio: option.value })}
                  style={({ pressed }) => [
                    styles.ratioChip,
                    selected && styles.ratioChipActive,
                    pressed && styles.pressed,
                  ]}
                >
                  <Text
                    style={[
                      styles.ratioChipText,
                      selected && styles.ratioChipTextActive,
                    ]}
                  >
                    {option.label}
                  </Text>
                </Pressable>
              );
            })}
          </View>
        </View>

        <View style={styles.rowsCard}>
          <OptionRow
            Icon={Store}
            label="Tag cafe page"
            value={postingIdentity?.name || (draft.cafePageId ? "Cafe page selected" : "Optional")}
          />
          <OptionRow
            Icon={Users}
            label="Tag people"
            onPress={onOpenPeoplePicker}
            value={
              selectedTaggedUsers.length
                ? `${selectedTaggedUsers.length} selected`
                : "Optional"
            }
          />
          <OptionRow
            Icon={MapPin}
            label="Location"
            onPress={onOpenLocationPicker}
            value={draft.location?.name ?? "Use profile location"}
          />
        </View>

        {selectedTaggedUsers.length || draft.location?.name ? (
          <View style={styles.selectedSection}>
            {selectedTaggedUsers.length ? (
              <View style={styles.selectedGroup}>
                <Text style={styles.selectedLabel}>Tagged people</Text>
                <View style={styles.selectedChips}>
                  {selectedTaggedUsers.map((profile) => (
                    <View key={profile.userId} style={styles.selectedChip}>
                      <Avatar
                        initials={profile.userName.slice(0, 2).toUpperCase()}
                        size={24}
                        uri={profile.userAvatar}
                      />
                      <Text numberOfLines={1} style={styles.selectedChipText}>
                        {profile.userName}
                      </Text>
                    </View>
                  ))}
                </View>
              </View>
            ) : null}

            {draft.location?.name ? (
              <View style={styles.selectedGroup}>
                <Text style={styles.selectedLabel}>Location</Text>
                <View style={styles.selectedLocation}>
                  <MapPin color={colors.primary} size={17} strokeWidth={2.5} />
                  <Text numberOfLines={2} style={styles.selectedLocationText}>
                    {draft.location.name}
                  </Text>
                </View>
              </View>
            ) : null}
          </View>
        ) : null}

        <View style={styles.tagsSection}>
          <View style={styles.sectionTitleRow}>
            <Tag color={colors.foreground} size={20} strokeWidth={2.4} />
            <Text style={styles.sectionTitle}>Common tags</Text>
          </View>
          <View style={styles.tagWrap}>
            {commonTags.map((tag) => {
              const selected = draft.tags.includes(tag);
              return (
                <Pressable
                  accessibilityLabel={`Toggle ${tag}`}
                  accessibilityRole="button"
                  key={tag}
                  onPress={() => onToggleTag(tag)}
                  style={({ pressed }) => [
                    styles.tagChip,
                    selected && styles.tagChipActive,
                    pressed && styles.pressed,
                  ]}
                >
                  <Text
                    style={[
                      styles.tagChipText,
                      selected && styles.tagChipTextActive,
                    ]}
                  >
                    {tag}
                  </Text>
                </Pressable>
              );
            })}
          </View>
        </View>
      </ScrollView>
    </View>
  );
}

type OptionRowProps = {
  Icon: typeof Store;
  label: string;
  onPress?: () => void;
  value: string;
};

function OptionRow({ Icon, label, onPress, value }: OptionRowProps) {
  return (
    <Pressable
      accessibilityLabel={label}
      accessibilityRole={onPress ? "button" : undefined}
      disabled={!onPress}
      onPress={onPress}
      style={({ pressed }) => [
        styles.optionRow,
        pressed && onPress && styles.pressed,
      ]}
    >
      <View style={styles.optionIcon}>
        <Icon color={colors.foreground} size={20} strokeWidth={2.4} />
      </View>
      <View style={styles.optionCopy}>
        <Text style={styles.optionLabel}>{label}</Text>
        <Text numberOfLines={1} style={styles.optionValue}>
          {value}
        </Text>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  cameraIconWrap: {
    alignItems: "center",
    backgroundColor: colors.primarySoft,
    borderRadius: 34,
    height: 68,
    justifyContent: "center",
    width: 68,
  },
  captionInput: {
    color: colors.foreground,
    fontSize: 24,
    lineHeight: 32,
    minHeight: 150,
    paddingHorizontal: spacing.xl,
    paddingVertical: spacing.lg,
  },
  container: {
    flex: 1,
  },
  content: {
    gap: spacing.lg,
    paddingBottom: 120,
  },
  emptyMedia: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 18,
    borderStyle: "dashed",
    borderWidth: 1,
    gap: spacing.sm,
    minHeight: 220,
    justifyContent: "center",
    marginHorizontal: spacing.lg,
    padding: spacing.xl,
  },
  emptyMediaDescription: {
    color: colors.muted,
    fontSize: typography.label,
    textAlign: "center",
  },
  emptyMediaTitle: {
    color: colors.foreground,
    fontSize: typography.title,
    fontWeight: "900",
  },
  identityCopy: {
    flex: 1,
    gap: spacing.xs,
  },
  identityRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.lg,
    paddingHorizontal: spacing.xl,
    paddingTop: spacing.xl,
  },
  mediaActionButton: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 18,
    borderWidth: 1,
    flexDirection: "row",
    gap: spacing.xs,
    minHeight: 40,
    paddingHorizontal: spacing.md,
  },
  mediaActionText: {
    color: colors.foreground,
    fontSize: typography.label,
    fontWeight: "800",
  },
  mediaActions: {
    flexDirection: "row",
    gap: spacing.sm,
    justifyContent: "space-between",
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.md,
  },
  mediaSection: {
    gap: spacing.sm,
  },
  name: {
    color: colors.foreground,
    fontSize: typography.title,
    fontWeight: "900",
  },
  optionCopy: {
    flex: 1,
    gap: 2,
  },
  optionIcon: {
    alignItems: "center",
    height: 34,
    justifyContent: "center",
    width: 34,
  },
  optionLabel: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "800",
  },
  optionRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.md,
    minHeight: 64,
    paddingHorizontal: spacing.lg,
  },
  optionValue: {
    color: colors.muted,
    fontSize: typography.label,
    fontWeight: "600",
  },
  pressed: {
    opacity: 0.72,
  },
  quickActions: {
    gap: spacing.sm,
    paddingHorizontal: spacing.xl,
  },
  quickChip: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 24,
    borderWidth: 1,
    flexDirection: "row",
    gap: spacing.xs,
    minHeight: 42,
    maxWidth: 180,
    paddingHorizontal: spacing.md,
  },
  quickChipText: {
    color: colors.foreground,
    fontSize: typography.label,
    fontWeight: "800",
  },
  ratioChip: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 16,
    borderWidth: 1,
    minHeight: 34,
    minWidth: 58,
    justifyContent: "center",
    paddingHorizontal: spacing.md,
  },
  ratioChipActive: {
    backgroundColor: colors.primary,
    borderColor: colors.primary,
  },
  ratioChipText: {
    color: colors.foreground,
    fontSize: typography.caption,
    fontWeight: "900",
  },
  ratioChipTextActive: {
    color: colors.white,
  },
  ratioOptions: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: spacing.sm,
  },
  ratioSection: {
    gap: spacing.sm,
    paddingHorizontal: spacing.lg,
  },
  ratioTitle: {
    color: colors.foreground,
    fontSize: typography.label,
    fontWeight: "900",
  },
  rowsCard: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 16,
    borderWidth: 1,
    marginHorizontal: spacing.lg,
    overflow: "hidden",
  },
  selectedChip: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 18,
    borderWidth: 1,
    flexDirection: "row",
    gap: spacing.xs,
    maxWidth: 180,
    minHeight: 36,
    paddingHorizontal: spacing.sm,
  },
  selectedChipText: {
    color: colors.foreground,
    fontSize: typography.label,
    fontWeight: "800",
  },
  selectedChips: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: spacing.sm,
  },
  selectedGroup: {
    gap: spacing.sm,
  },
  selectedLabel: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "900",
    textTransform: "uppercase",
  },
  selectedLocation: {
    alignItems: "center",
    backgroundColor: colors.primarySoft,
    borderColor: colors.primary,
    borderRadius: 14,
    borderWidth: 1,
    flexDirection: "row",
    gap: spacing.sm,
    minHeight: 44,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
  },
  selectedLocationText: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.label,
    fontWeight: "900",
    lineHeight: 20,
  },
  selectedSection: {
    gap: spacing.md,
    paddingHorizontal: spacing.lg,
  },
  sectionTitle: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },
  sectionTitleRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.sm,
  },
  tagChip: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 18,
    borderWidth: 1,
    minHeight: 36,
    justifyContent: "center",
    paddingHorizontal: spacing.md,
  },
  tagChipActive: {
    backgroundColor: colors.primary,
    borderColor: colors.primary,
  },
  tagChipText: {
    color: colors.foreground,
    fontSize: typography.label,
    fontWeight: "800",
  },
  tagChipTextActive: {
    color: colors.white,
  },
  tagWrap: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: spacing.sm,
  },
  tagsSection: {
    gap: spacing.md,
    paddingHorizontal: spacing.lg,
  },
  username: {
    color: colors.muted,
    fontSize: typography.label,
    fontWeight: "600",
  },
});
