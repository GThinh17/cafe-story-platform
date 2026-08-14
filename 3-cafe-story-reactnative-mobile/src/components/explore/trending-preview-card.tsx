import { ChevronRight, Flame, Pin, TrendingUp } from "lucide-react-native";
import { Pressable } from "../../features/i18n/localized-native";
import { Text } from "../../features/i18n/localized-native";
import { StyleSheet, View } from "react-native";

import { colors, spacing, typography } from "../../theme";
import { formatCurrentCompactNumber, formatCurrentNumber } from "../../features/i18n";
import type { BlogTrendingResponse } from "../../types";

type TrendingPreviewCardProps = {
  item: BlogTrendingResponse;
  onPress: (item: BlogTrendingResponse) => void;
};

function compactScore(value: number | null) {
  if (value == null) {
    return "0";
  }

  if (value >= 1000) {
    return formatCurrentCompactNumber(value);
  }

  return formatCurrentNumber(value, {
    maximumFractionDigits: value >= 10 ? 0 : 1,
  });
}

function getAuthorLabel(item: BlogTrendingResponse) {
  return item.pageName || item.authorUserName || "CafeStory";
}

export function TrendingPreviewCard({ item, onPress }: TrendingPreviewCardProps) {
  const rank = item.rankPosition ?? 0;
  const authorLabel = getAuthorLabel(item);

  return (
    <Pressable
      accessibilityLabel={`Open trending post ${rank ? `number ${rank}` : ""}`}
      accessibilityRole="button"
      onPress={() => onPress(item)}
      style={({ pressed }) => [styles.card, pressed && styles.pressed]}
    >
      <View style={styles.rankBlock}>
        <Text style={styles.rankText}>{rank ? `#${rank}` : "Hot"}</Text>
        <TrendingUp color={colors.primary} size={18} strokeWidth={2.5} />
      </View>

      <View style={styles.copy}>
        <View style={styles.metaRow}>
          <Text numberOfLines={1} style={styles.author}>
            {authorLabel}
          </Text>
          {item.pinned ? (
            <View style={styles.pinBadge}>
              <Pin color={colors.primary} size={13} strokeWidth={2.6} />
              <Text style={styles.pinText}>Pinned</Text>
            </View>
          ) : null}
        </View>
        <Text numberOfLines={2} style={styles.preview}>
          {item.contentPreview || item.reason || "Trending cafe story"}
        </Text>
        <View style={styles.reasonRow}>
          <Flame color={colors.rating} size={16} strokeWidth={2.4} />
          <Text numberOfLines={1} style={styles.reason}>
            {item.reason || `${compactScore(item.trendScore)} trend score`}
          </Text>
        </View>
      </View>

      <ChevronRight color={colors.muted} size={22} strokeWidth={2.4} />
    </Pressable>
  );
}

const styles = StyleSheet.create({
  author: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.label,
    fontWeight: "900",
  },
  card: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 16,
    borderWidth: 1,
    flexDirection: "row",
    gap: spacing.md,
    marginHorizontal: spacing.lg,
    minHeight: 108,
    padding: spacing.md,
  },
  copy: {
    flex: 1,
    gap: spacing.xs,
  },
  metaRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.sm,
  },
  pinBadge: {
    alignItems: "center",
    backgroundColor: colors.primarySoft,
    borderRadius: 14,
    flexDirection: "row",
    gap: 3,
    paddingHorizontal: spacing.sm,
    paddingVertical: 3,
  },
  pinText: {
    color: colors.primary,
    fontSize: 10,
    fontWeight: "900",
  },
  pressed: {
    opacity: 0.76,
    transform: [{ scale: 0.99 }],
  },
  preview: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "700",
    lineHeight: 22,
  },
  rankBlock: {
    alignItems: "center",
    backgroundColor: colors.primarySoft,
    borderRadius: 14,
    gap: spacing.xs,
    height: 74,
    justifyContent: "center",
    width: 58,
  },
  rankText: {
    color: colors.primary,
    fontSize: typography.label,
    fontWeight: "900",
  },
  reason: {
    color: colors.secondaryStrong,
    flex: 1,
    fontSize: typography.caption,
    fontWeight: "800",
  },
  reasonRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.xs,
  },
});
