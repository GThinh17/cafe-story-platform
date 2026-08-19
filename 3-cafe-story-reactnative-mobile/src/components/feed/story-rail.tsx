import { Plus } from "lucide-react-native";
import { Pressable } from "react-native";
import { Text } from "react-native";
import { ScrollView, StyleSheet, View } from "react-native";

import { Avatar } from "../ui/avatar";
import { colors, spacing, typography } from "../../theme";
import type { StoryItem } from "../../types";
import { t } from "../../features/i18n";

type StoryRailProps = {
  onStoryPress?: (story: StoryItem) => void;
  stories: StoryItem[];
};

export function StoryRail({ onStoryPress, stories }: StoryRailProps) {
  return (
    <View style={styles.wrapper}>
      <ScrollView
        contentContainerStyle={styles.content}
        horizontal
        showsHorizontalScrollIndicator={false}
      >
        {stories.map((story) => (
          <Pressable
            accessibilityLabel={t("story.a11y.open", { name: story.label })}
            accessibilityRole="button"
            key={story.id}
            onPress={() => onStoryPress?.(story)}
            style={({ pressed }) => [
              styles.item,
              pressed ? styles.itemPressed : null,
            ]}
          >
            <View style={[styles.ring, story.isSelf && styles.selfRing]}>
              <Avatar
                initials={story.initials}
                size={58}
                uri={story.avatarUri}
              />
              {story.isSelf ? (
                <View style={styles.addBadge}>
                  <Plus color={colors.white} size={13} strokeWidth={3} />
                </View>
              ) : null}
            </View>
            <Text numberOfLines={1} style={styles.label}>
              {story.label}
            </Text>
          </Pressable>
        ))}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  addBadge: {
    alignItems: "center",
    backgroundColor: colors.primary,
    borderColor: colors.background,
    borderRadius: 11,
    borderWidth: 2,
    bottom: 1,
    height: 22,
    justifyContent: "center",
    position: "absolute",
    right: 1,
    width: 22,
  },
  content: {
    gap: spacing.md,
    paddingBottom: 2,
    paddingHorizontal: spacing.xl,
    paddingTop: 2,
  },
  item: {
    alignItems: "center",
    gap: spacing.xs,
    width: 72,
  },
  itemPressed: {
    opacity: 0.72,
    transform: [{ scale: 0.98 }],
  },
  label: {
    color: colors.foreground,
    fontSize: typography.caption,
    fontWeight: "700",
    maxWidth: 72,
    textAlign: "center",
  },
  ring: {
    alignItems: "center",
    borderColor: colors.secondary,
    borderRadius: 35,
    borderWidth: 2,
    height: 70,
    justifyContent: "center",
    position: "relative",
    width: 70,
  },
  selfRing: {
    borderColor: colors.primary,
  },
  wrapper: {
    width: "100%",
  },
});
