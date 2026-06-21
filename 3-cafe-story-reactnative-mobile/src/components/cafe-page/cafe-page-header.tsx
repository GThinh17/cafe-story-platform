import {
  CalendarDays,
  Heart,
  MapPin,
  MessageCircle,
  Sparkles,
  Star,
  Store,
  Users,
} from "lucide-react-native";
import { Image, Pressable, StyleSheet, Text, View } from "react-native";

import { Avatar } from "../ui/avatar";
import { colors, spacing, typography } from "../../theme";
import type { CafePageResponse } from "../../types";

type CafePageHeaderProps = {
  cafePage: CafePageResponse;
  canManage?: boolean;
  isFollowPending?: boolean;
  isLikePending?: boolean;
  isOwner: boolean;
  onEditPress?: () => void;
  onFollowPress?: () => void;
  onLikePress?: () => void;
  onMessagePress?: () => void;
  onSharePress?: () => void;
  onSuggestPress?: () => void;
};

function formatCount(value?: number | null) {
  const safeValue = value ?? 0;

  if (safeValue >= 1000000) {
    return `${(safeValue / 1000000).toFixed(safeValue >= 10000000 ? 0 : 1)}m`;
  }

  if (safeValue >= 1000) {
    return `${(safeValue / 1000).toFixed(safeValue >= 10000 ? 0 : 1)}k`;
  }

  return String(safeValue);
}

function formatDate(value?: string | null) {
  if (!value) {
    return null;
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return null;
  }

  return date.toLocaleDateString(undefined, {
    day: "numeric",
    month: "short",
    year: "numeric",
  });
}

function regionLabel(cafePage: CafePageResponse) {
  return [
    cafePage.regionWard,
    cafePage.regionCity,
    cafePage.regionProvince,
  ]
    .filter(Boolean)
    .join(", ");
}

export function CafePageHeader({
  cafePage,
  canManage = false,
  isFollowPending = false,
  isLikePending = false,
  isOwner,
  onEditPress,
  onFollowPress,
  onLikePress,
  onMessagePress,
  onSharePress,
  onSuggestPress,
}: CafePageHeaderProps) {
  const location = regionLabel(cafePage);
  const expiresAt = formatDate(cafePage.pageExpiresAt);
  const isFollowing = Boolean(cafePage.isFollowing);
  const isLiked = Boolean(cafePage.isLiked);
  const managerBadge = isOwner ? "Owner" : canManage ? "Manager" : null;

  return (
    <View style={styles.container}>
      <View style={styles.cover}>
        {cafePage.coverUrl ? (
          <Image resizeMode="cover" source={{ uri: cafePage.coverUrl }} style={styles.coverImage} />
        ) : (
          <View style={styles.coverFallback}>
            <Store color={colors.secondaryStrong} size={44} strokeWidth={2.2} />
          </View>
        )}
      </View>

      <View style={styles.identityBlock}>
        <View style={styles.identityRow}>
          <View style={styles.avatarRing}>
            <Avatar size={88} uri={cafePage.avatarUrl} />
          </View>

          <View style={styles.identityCopy}>
            <View style={styles.nameRow}>
              <Text numberOfLines={1} style={styles.name}>
                {cafePage.name || "Cafe Page"}
              </Text>
              {managerBadge ? <Text style={styles.ownerBadge}>{managerBadge}</Text> : null}
            </View>

            <View style={styles.stats}>
              <Metric icon={Heart} label="likes" value={formatCount(cafePage.likeCount)} />
              <Metric icon={Users} label="followers" value={formatCount(cafePage.followerCount)} />
              <Metric
                icon={Star}
                label="rating"
                value={(cafePage.ratingScore ?? 0).toFixed(1)}
              />
            </View>
          </View>
        </View>
      </View>

      <View style={styles.copy}>
        {cafePage.description ? (
          <Text style={styles.description}>{cafePage.description}</Text>
        ) : (
          <Text style={styles.mutedDescription}>No description yet.</Text>
        )}

        <View style={styles.metaList}>
          {location ? <Meta icon={MapPin} text={location} /> : null}
          {cafePage.address ? <Meta icon={Store} text={cafePage.address} /> : null}
          {canManage && expiresAt ? <Meta icon={CalendarDays} text={`Active until ${expiresAt}`} /> : null}
        </View>
      </View>

      <View style={styles.actions}>
        {canManage ? (
          <>
            <Pressable
              accessibilityLabel="Edit cafe page"
              accessibilityRole="button"
              onPress={onEditPress}
              style={({ pressed }) => [
                styles.profileActionButton,
                pressed && styles.pressed,
              ]}
            >
              <Text style={styles.profileActionText}>Edit Page</Text>
            </Pressable>

            <Pressable
              accessibilityLabel="Share cafe page"
              accessibilityRole="button"
              onPress={onSharePress}
              style={({ pressed }) => [
                styles.profileActionButton,
                pressed && styles.pressed,
              ]}
            >
              <Text style={styles.profileActionText}>Share Page</Text>
            </Pressable>

            <Pressable
              accessibilityLabel="Open cafe page suggestions"
              accessibilityRole="button"
              onPress={onSuggestPress}
              style={({ pressed }) => [
                styles.suggestButton,
                pressed && styles.pressed,
              ]}
            >
              <Sparkles color={colors.foreground} size={19} strokeWidth={2.5} />
            </Pressable>
          </>
        ) : (
          <>
            <Pressable
              accessibilityLabel={isFollowing ? "Unfollow cafe page" : "Follow cafe page"}
              accessibilityRole="button"
              disabled={isFollowPending}
              onPress={onFollowPress}
              style={({ pressed }) => [
                styles.primaryActionButton,
                isFollowing && styles.secondaryActionButton,
                pressed && !isFollowPending && styles.pressed,
                isFollowPending && styles.disabled,
              ]}
            >
              <Text
                style={[
                  styles.primaryActionText,
                  isFollowing && styles.secondaryActionText,
                ]}
              >
                {isFollowing ? "Following" : "Follow"}
              </Text>
            </Pressable>

            <Pressable
              accessibilityLabel="Message cafe page"
              accessibilityRole="button"
              onPress={onMessagePress}
              style={({ pressed }) => [
                styles.suggestButton,
                pressed && styles.pressed,
              ]}
            >
              <MessageCircle color={colors.foreground} size={19} strokeWidth={2.5} />
            </Pressable>

            <Pressable
              accessibilityLabel={isLiked ? "Unlike cafe page" : "Like cafe page"}
              accessibilityRole="button"
              disabled={isLikePending}
              onPress={onLikePress}
              style={({ pressed }) => [
                styles.likeButton,
                isLiked && styles.likeButtonActive,
                pressed && styles.pressed,
                isLikePending && styles.disabled,
              ]}
            >
              <Heart
                color={isLiked ? colors.white : colors.primary}
                fill={isLiked ? colors.primary : "transparent"}
                size={22}
                strokeWidth={2.5}
              />
            </Pressable>
          </>
        )}
      </View>
    </View>
  );
}

type IconComponent = typeof Users;

function Metric({
  icon: Icon,
  label,
  value,
}: {
  icon: IconComponent;
  label: string;
  value: string;
}) {
  return (
    <View style={styles.metric}>
      <Icon color={colors.secondaryStrong} size={17} strokeWidth={2.4} />
      <Text style={styles.metricValue}>{value}</Text>
      <Text numberOfLines={1} style={styles.metricLabel}>
        {label}
      </Text>
    </View>
  );
}

function Meta({ icon: Icon, text }: { icon: IconComponent; text: string }) {
  return (
    <View style={styles.metaRow}>
      <Icon color={colors.muted} size={16} strokeWidth={2.2} />
      <Text style={styles.metaText}>{text}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  actionButton: {
    flex: 1,
  },
  actions: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.sm,
    paddingHorizontal: spacing.lg,
  },
  avatarRing: {
    backgroundColor: colors.background,
    borderColor: colors.background,
    borderRadius: 50,
    borderWidth: 4,
  },
  container: {
    gap: spacing.md,
    paddingBottom: spacing.md,
  },
  copy: {
    gap: spacing.sm,
    paddingHorizontal: spacing.lg,
  },
  cover: {
    backgroundColor: colors.surfaceMuted,
    height: 168,
    overflow: "hidden",
    width: "100%",
  },
  coverFallback: {
    alignItems: "center",
    backgroundColor: colors.secondarySoft,
    height: "100%",
    justifyContent: "center",
    width: "100%",
  },
  coverImage: {
    height: "100%",
    width: "100%",
  },
  description: {
    color: colors.foreground,
    fontSize: typography.label,
    fontWeight: "600",
    lineHeight: 20,
  },
  disabled: {
    opacity: 0.6,
  },
  identityRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.md,
  },
  identityBlock: {
    marginTop: -38,
    paddingHorizontal: spacing.lg,
  },
  identityCopy: {
    flex: 1,
    gap: spacing.sm,
    paddingTop: 30,
  },
  likeButton: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 14,
    borderWidth: 1,
    height: 36,
    justifyContent: "center",
    width: 42,
  },
  likeButtonActive: {
    backgroundColor: colors.primary,
    borderColor: colors.primary,
  },
  metaList: {
    gap: spacing.xs,
  },
  metaRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.xs,
  },
  metaText: {
    color: colors.muted,
    flex: 1,
    fontSize: typography.caption,
    fontWeight: "700",
    lineHeight: 18,
  },
  metric: {
    alignItems: "center",
    flexDirection: "row",
    gap: 4,
    minWidth: 0,
  },
  metricLabel: {
    color: colors.muted,
    fontSize: 11,
    fontWeight: "700",
  },
  metricValue: {
    color: colors.foreground,
    fontSize: typography.caption,
    fontWeight: "900",
  },
  mutedDescription: {
    color: colors.muted,
    fontSize: typography.label,
    fontWeight: "700",
  },
  name: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.body,
    fontWeight: "900",
  },
  nameRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.sm,
  },
  ownerBadge: {
    backgroundColor: colors.tertiarySoft,
    borderRadius: 999,
    color: colors.tertiaryStrong,
    fontSize: typography.caption,
    fontWeight: "900",
    overflow: "hidden",
    paddingHorizontal: spacing.sm,
    paddingVertical: 4,
  },
  pressed: {
    opacity: 0.72,
  },
  primaryActionButton: {
    alignItems: "center",
    backgroundColor: colors.primary,
    borderRadius: 10,
    flex: 1,
    height: 36,
    justifyContent: "center",
  },
  primaryActionText: {
    color: colors.white,
    fontSize: typography.caption,
    fontWeight: "900",
  },
  profileActionButton: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 10,
    borderWidth: 1,
    flex: 1,
    height: 36,
    justifyContent: "center",
  },
  profileActionText: {
    color: colors.foreground,
    fontSize: typography.caption,
    fontWeight: "800",
  },
  secondaryActionButton: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderWidth: 1,
  },
  secondaryActionText: {
    color: colors.foreground,
  },
  suggestButton: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 10,
    borderWidth: 1,
    height: 36,
    justifyContent: "center",
    width: 42,
  },
  stats: {
    alignItems: "center",
    flexDirection: "row",
    flexWrap: "nowrap",
    gap: spacing.md,
  },
});
