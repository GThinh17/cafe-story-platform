import { Store, X } from "lucide-react-native";
import { Pressable } from "../../features/i18n/localized-native";
import { Text } from "../../features/i18n/localized-native";
import { ScrollView, StyleSheet, View } from "react-native";

import { colors, spacing, typography } from "../../theme";
import type { RecommendationCardResponse } from "../../types";
import { Avatar } from "../ui/avatar";

type ProfileSuggestionsProps = {
  disabledUserIds?: string[];
  error?: string | null;
  isLoading?: boolean;
  onDismiss: (suggestion: RecommendationCardResponse) => void;
  onFollow: (suggestion: RecommendationCardResponse) => void;
  onProfilePress: (suggestion: RecommendationCardResponse) => void;
  suggestions: RecommendationCardResponse[];
};

function getDisplayName(suggestion: RecommendationCardResponse) {
  return suggestion.fullName || suggestion.username || "CafeStory user";
}

function getSubtitle(suggestion: RecommendationCardResponse) {
  return suggestion.reason || suggestion.city || "Suggested for you";
}

export function ProfileSuggestions({
  disabledUserIds = [],
  error,
  isLoading = false,
  onDismiss,
  onFollow,
  onProfilePress,
  suggestions,
}: ProfileSuggestionsProps) {
  const disabledIds = new Set(disabledUserIds);

  return (
    <View style={styles.section}>
      <View style={styles.header}>
        <Text style={styles.title}>Discovery</Text>
        <Text style={styles.link}>See all</Text>
      </View>

      {isLoading ? (
        <ProfileSuggestionSkeleton />
      ) : error ? (
        <View style={styles.stateCard}>
          <Text style={styles.stateTitle}>{error}</Text>
        </View>
      ) : suggestions.length === 0 ? (
        <View style={styles.stateCard}>
          <Text style={styles.stateTitle}>No suggestions right now</Text>
          <Text style={styles.stateDescription}>
            We will show more people as your CafeStory network grows.
          </Text>
        </View>
      ) : (
        <ScrollView
          contentContainerStyle={styles.list}
          horizontal
          showsHorizontalScrollIndicator={false}
        >
          {suggestions.map((suggestion) => {
            const isDisabled = disabledIds.has(suggestion.targetId);
            const isCafePage = suggestion.targetType === "CAFE_PAGE";

            return (
              <View key={`${suggestion.targetType}-${suggestion.targetId}`} style={styles.card}>
                <Pressable
                  accessibilityLabel={`Hide ${getDisplayName(suggestion)} suggestion`}
                  accessibilityRole="button"
                  onPress={() => onDismiss(suggestion)}
                  style={({ pressed }) => [
                    styles.dismissButton,
                    pressed && styles.pressed,
                  ]}
                >
                  <X color={colors.foreground} size={18} strokeWidth={2.3} />
                </Pressable>

                <Pressable
                  accessibilityLabel={`Open ${getDisplayName(suggestion)} profile`}
                  accessibilityRole="button"
                  onPress={() => onProfilePress(suggestion)}
                  style={({ pressed }) => [
                    styles.profileAction,
                    pressed && styles.pressed,
                  ]}
                >
                  <Avatar size={92} uri={suggestion.avatar} />
                  <View style={styles.nameRow}>
                    <Text numberOfLines={1} style={styles.name}>
                      {getDisplayName(suggestion)}
                    </Text>
                    {isCafePage ? (
                      <View style={styles.cafeBadge}>
                        <Store color={colors.primary} size={12} strokeWidth={2.5} />
                      </View>
                    ) : null}
                  </View>
                  <Text numberOfLines={1} style={styles.reason}>
                    {getSubtitle(suggestion)}
                  </Text>
                </Pressable>

                <Pressable
                  accessibilityLabel={`${isCafePage ? "View" : "Follow"} ${getDisplayName(suggestion)}`}
                  accessibilityRole="button"
                  disabled={!isCafePage && isDisabled}
                  onPress={() => {
                    if (isCafePage) {
                      onProfilePress(suggestion);
                      return;
                    }

                    onFollow(suggestion);
                  }}
                  style={({ pressed }) => [
                    styles.followButton,
                    !isCafePage && isDisabled && styles.followButtonDisabled,
                    pressed && (isCafePage || !isDisabled) && styles.pressed,
                  ]}
                >
                  <Text
                    style={[
                      styles.followText,
                      !isCafePage && isDisabled && styles.followTextDisabled,
                    ]}
                  >
                    {isCafePage ? "View" : isDisabled ? "Following" : "Follow"}
                  </Text>
                </Pressable>
              </View>
            );
          })}
        </ScrollView>
      )}
    </View>
  );
}

function ProfileSuggestionSkeleton() {
  return (
    <ScrollView
      accessibilityLabel="Loading suggestions"
      accessibilityRole="progressbar"
      contentContainerStyle={styles.list}
      horizontal
      showsHorizontalScrollIndicator={false}
    >
      {[0, 1, 2].map((item) => (
        <View key={item} style={styles.skeletonCard}>
          <View style={styles.skeletonAvatar} />
          <View style={styles.skeletonName} />
          <View style={styles.skeletonReason} />
          <View style={styles.skeletonButton} />
        </View>
      ))}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  card: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 10,
    borderWidth: 1,
    minHeight: 218,
    padding: spacing.md,
    position: "relative",
    width: 172,
  },
  cafeBadge: {
    alignItems: "center",
    backgroundColor: colors.primarySoft,
    borderRadius: 9,
    height: 18,
    justifyContent: "center",
    width: 18,
  },
  dismissButton: {
    alignItems: "center",
    height: 32,
    justifyContent: "center",
    position: "absolute",
    right: spacing.xs,
    top: spacing.xs,
    width: 32,
    zIndex: 2,
  },
  followButton: {
    alignItems: "center",
    backgroundColor: colors.link,
    borderRadius: 8,
    height: 40,
    justifyContent: "center",
    marginTop: spacing.md,
    width: "100%",
  },
  followButtonDisabled: {
    backgroundColor: colors.surfaceMuted,
  },
  followText: {
    color: colors.white,
    fontSize: typography.label,
    fontWeight: "900",
  },
  followTextDisabled: {
    color: colors.foreground,
  },
  header: {
    alignItems: "center",
    flexDirection: "row",
    justifyContent: "space-between",
    paddingHorizontal: spacing.lg,
  },
  link: {
    color: colors.link,
    fontSize: typography.caption,
    fontWeight: "800",
  },
  list: {
    gap: spacing.sm,
    paddingHorizontal: spacing.lg,
  },
  name: {
    color: colors.foreground,
    flexShrink: 1,
    fontSize: typography.body,
    fontWeight: "900",
    maxWidth: "100%",
  },
  nameRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.xs,
    marginTop: spacing.md,
    maxWidth: "100%",
    minWidth: 0,
  },
  pressed: {
    opacity: 0.72,
  },
  profileAction: {
    alignItems: "center",
    flex: 1,
    paddingTop: spacing.lg,
    width: "100%",
  },
  reason: {
    color: colors.muted,
    fontSize: typography.label,
    fontWeight: "600",
    marginTop: 2,
    maxWidth: "100%",
  },
  section: {
    gap: spacing.md,
  },
  skeletonAvatar: {
    backgroundColor: colors.surfaceMuted,
    borderRadius: 46,
    height: 92,
    width: 92,
  },
  skeletonButton: {
    backgroundColor: colors.surfaceMuted,
    borderRadius: 8,
    height: 40,
    marginTop: spacing.md,
    width: "100%",
  },
  skeletonCard: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 10,
    borderWidth: 1,
    minHeight: 218,
    padding: spacing.md,
    width: 172,
  },
  skeletonName: {
    backgroundColor: colors.surfaceMuted,
    borderRadius: 999,
    height: 14,
    marginTop: spacing.lg,
    width: "78%",
  },
  skeletonReason: {
    backgroundColor: colors.surfaceMuted,
    borderRadius: 999,
    height: 12,
    marginTop: spacing.sm,
    width: "62%",
  },
  stateCard: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 10,
    borderWidth: 1,
    marginHorizontal: spacing.lg,
    minHeight: 96,
    justifyContent: "center",
    padding: spacing.lg,
  },
  stateDescription: {
    color: colors.muted,
    fontSize: typography.caption,
    lineHeight: 18,
    marginTop: spacing.xs,
  },
  stateTitle: {
    color: colors.foreground,
    fontSize: typography.label,
    fontWeight: "800",
  },
  title: {
    color: colors.foreground,
    fontSize: typography.caption,
    fontWeight: "800",
  },
});
