import { Award, Coffee, Sparkles, UserRound } from "lucide-react-native";
import { Pressable, StyleSheet, Text, View } from "react-native";

import { colors, spacing, typography } from "../../theme";
import type { RecommendationCardResponse } from "../../types";
import { Avatar } from "../ui/avatar";

type ExploreRecommendationCardProps = {
  item: RecommendationCardResponse;
};

function getTitle(item: RecommendationCardResponse) {
  return item.fullName || item.username || "CafeStory pick";
}

function getSubtitle(item: RecommendationCardResponse) {
  if (item.targetType === "CAFE_PAGE") {
    return item.city || "Cafe suggestion";
  }

  if (item.targetType === "REVIEWER") {
    return item.username ? `@${item.username}` : "Reviewer";
  }

  return item.username ? `@${item.username}` : "CafeStory user";
}

function getReason(item: RecommendationCardResponse) {
  return item.reason || "Recommended for you";
}

function getIcon(item: RecommendationCardResponse) {
  if (item.targetType === "CAFE_PAGE") {
    return Coffee;
  }

  if (item.targetType === "REVIEWER") {
    return Award;
  }

  return UserRound;
}

export function ExploreRecommendationCard({ item }: ExploreRecommendationCardProps) {
  const Icon = getIcon(item);

  return (
    <Pressable
      accessibilityLabel={`Open ${getTitle(item)}`}
      accessibilityRole="button"
      style={({ pressed }) => [
        styles.card,
        pressed && styles.pressed,
      ]}
    >
      <Avatar size={62} uri={item.avatar} />

      <View style={styles.copy}>
        <View style={styles.titleRow}>
          <Text numberOfLines={1} style={styles.title}>
            {getTitle(item)}
          </Text>
          <View style={styles.badge}>
            <Icon color={colors.primary} size={13} strokeWidth={2.5} />
          </View>
        </View>
        <Text numberOfLines={1} style={styles.subtitle}>
          {getSubtitle(item)}
        </Text>
        <View style={styles.reasonRow}>
          <Sparkles color={colors.secondary} size={14} strokeWidth={2.4} />
          <Text numberOfLines={2} style={styles.reason}>
            {getReason(item)}
          </Text>
        </View>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  badge: {
    alignItems: "center",
    backgroundColor: colors.primarySoft,
    borderRadius: 11,
    height: 22,
    justifyContent: "center",
    width: 22,
  },
  card: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 14,
    borderWidth: 1,
    flexDirection: "row",
    gap: spacing.md,
    padding: spacing.md,
  },
  copy: {
    flex: 1,
    gap: spacing.xs,
  },
  pressed: {
    opacity: 0.74,
  },
  reason: {
    color: colors.secondaryStrong,
    flex: 1,
    fontSize: typography.caption,
    fontWeight: "700",
    lineHeight: 17,
  },
  reasonRow: {
    alignItems: "flex-start",
    flexDirection: "row",
    gap: spacing.xs,
    paddingTop: 2,
  },
  subtitle: {
    color: colors.muted,
    fontSize: typography.label,
    fontWeight: "700",
  },
  title: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.body,
    fontWeight: "900",
  },
  titleRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.sm,
  },
});
