import { Megaphone, Store } from "lucide-react-native";
import { useState } from "react";
import { Image, Linking, Pressable, StyleSheet, Text, View } from "react-native";

import { Avatar } from "../ui/avatar";
import { recordAdClick } from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type { SponsoredCafeResponse } from "../../types";

type SponsoredCafeCardProps = {
  ad: SponsoredCafeResponse;
  onOpenCafePage: (cafePageId: string) => void;
};

function getInitials(name: string | null | undefined) {
  return (name || "Cafe")
    .split(/\s|_/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join("") || "CF";
}

export function SponsoredCafeCard({ ad, onOpenCafePage }: SponsoredCafeCardProps) {
  const [isClickPending, setIsClickPending] = useState(false);
  const coverUrl = ad.imageUrl || ad.cafeCoverUrl || ad.cafeAvatarUrl;
  const title = ad.headline || ad.cafeName || "Sponsored cafe";
  const ctaLabel = ad.ctaLabel || "View cafe";

  async function handlePress() {
    if (isClickPending) {
      return;
    }

    setIsClickPending(true);
    try {
      await recordAdClick(ad.campaignId);
    } catch {
      // Click tracking should not block navigation.
    } finally {
      setIsClickPending(false);
      if (ad.targetUrl && /^https?:\/\//i.test(ad.targetUrl)) {
        try {
          await Linking.openURL(ad.targetUrl);
          return;
        } catch {
          // Fall back to the cafe page when the external target cannot open.
        }
      }
      onOpenCafePage(ad.cafePageId);
    }
  }

  return (
    <Pressable
      accessibilityLabel={`Open sponsored cafe ${ad.cafeName ?? title}`}
      accessibilityRole="button"
      onPress={handlePress}
      style={({ pressed }) => [styles.card, pressed && styles.pressed]}
    >
      <View style={styles.badgeRow}>
        <Megaphone color={colors.primary} size={16} strokeWidth={2.5} />
        <Text style={styles.badgeText}>Sponsored</Text>
      </View>

      {coverUrl ? (
        <Image
          accessibilityLabel={`${ad.cafeName ?? "Sponsored cafe"} cover`}
          resizeMode="cover"
          source={{ uri: coverUrl }}
          style={styles.cover}
        />
      ) : (
        <View style={styles.coverFallback}>
          <Store color={colors.primary} size={42} strokeWidth={2.3} />
          <Text style={styles.coverFallbackText}>
            {ad.cafeName || "Sponsored cafe"}
          </Text>
        </View>
      )}

      <View style={styles.body}>
        <Avatar initials={getInitials(ad.cafeName)} size={42} uri={ad.cafeAvatarUrl} />
        <View style={styles.copy}>
          <Text numberOfLines={1} style={styles.title}>
            {title}
          </Text>
          <Text numberOfLines={2} style={styles.description}>
            {ad.description || ad.cafeName || "Discover this cafe on CafeStory."}
          </Text>
        </View>
      </View>

      <View style={styles.cta}>
        <Text style={styles.ctaText}>{isClickPending ? "Opening..." : ctaLabel}</Text>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  badgeRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.xs,
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.md,
  },
  badgeText: {
    color: colors.primary,
    fontSize: typography.caption,
    fontWeight: "900",
    letterSpacing: 0.4,
    textTransform: "uppercase",
  },
  body: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.md,
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.md,
  },
  card: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 18,
    borderWidth: 1,
    marginHorizontal: 0,
    overflow: "hidden",
    width: "100%",
  },
  copy: {
    flex: 1,
    gap: spacing.xs,
  },
  cover: {
    aspectRatio: 1,
    marginTop: spacing.sm,
    width: "100%",
  },
  coverFallback: {
    alignItems: "center",
    aspectRatio: 1,
    backgroundColor: colors.primarySoft,
    gap: spacing.sm,
    justifyContent: "center",
    marginTop: spacing.sm,
    paddingHorizontal: spacing.lg,
    width: "100%",
  },
  coverFallbackText: {
    color: colors.primary,
    fontSize: typography.label,
    fontWeight: "800",
    textAlign: "center",
  },
  cta: {
    alignItems: "center",
    backgroundColor: colors.primary,
    borderRadius: 14,
    justifyContent: "center",
    margin: spacing.lg,
    minHeight: 46,
  },
  ctaText: {
    color: colors.white,
    fontSize: typography.label,
    fontWeight: "900",
  },
  description: {
    color: colors.secondaryStrong,
    fontSize: typography.label,
    lineHeight: 20,
  },
  pressed: {
    opacity: 0.78,
  },
  title: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },
});
