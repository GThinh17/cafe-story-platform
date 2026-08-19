import { Megaphone, Store } from "lucide-react-native";
import { Pressable, Text } from "react-native";
import { useState } from "react";
import { Image, Linking, StyleSheet, View } from "react-native";

import { Avatar } from "../ui/avatar";
import { recordAdClick } from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type { SponsoredCafeResponse } from "../../types";
import { t } from "../../features/i18n";

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
    <View style={styles.card}>
      <View style={styles.header}>
        <View style={styles.author}>
          <Avatar initials={getInitials(ad.cafeName)} size={36} uri={ad.cafeAvatarUrl} />
          <View style={styles.authorCopy}>
            <View style={styles.displayNameRow}>
              <Text numberOfLines={1} style={styles.displayName}>
                {ad.cafeName || "Sponsored cafe"}
              </Text>
              <View style={styles.cafeBadge}>
                <Store color={colors.primary} size={12} strokeWidth={2.5} />
              </View>
            </View>
            <View style={styles.sponsoredRow}>
              <Megaphone color={colors.primary} size={13} strokeWidth={2.5} />
              <Text style={styles.sponsoredText}>{t("Sponsored")}</Text>
            </View>
          </View>
        </View>
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
        <Text numberOfLines={2} style={styles.title}>
          {title}
        </Text>
        <Text numberOfLines={3} style={styles.description}>
          {ad.description || "Explore this sponsored cafe on CafeStory."}
        </Text>
        <Pressable
          accessibilityLabel={`${ctaLabel}: ${ad.cafeName || "Sponsored cafe"}`}
          accessibilityRole="button"
          disabled={isClickPending}
          onPress={() => void handlePress()}
          style={({ pressed }) => [styles.cta, pressed && styles.pressed]}
        >
          <Text style={styles.ctaText}>{isClickPending ? t("Opening...") : ctaLabel}</Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  author: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.md,
    minWidth: 0,
  },
  authorCopy: {
    flex: 1,
    minWidth: 0,
  },
  card: {
    backgroundColor: colors.background,
    paddingBottom: spacing.xl,
    width: "100%",
  },
  cafeBadge: {
    alignItems: "center",
    backgroundColor: colors.primarySoft,
    borderRadius: 9,
    height: 18,
    justifyContent: "center",
    width: 18,
  },
  cover: {
    aspectRatio: 1,
    width: "100%",
  },
  coverFallback: {
    alignItems: "center",
    aspectRatio: 1,
    backgroundColor: colors.surfaceMuted,
    gap: spacing.sm,
    justifyContent: "center",
    paddingHorizontal: spacing.lg,
    width: "100%",
  },
  coverFallbackText: {
    color: colors.primary,
    fontSize: typography.label,
    fontWeight: "800",
    textAlign: "center",
  },
  description: {
    color: colors.secondaryStrong,
    fontSize: typography.label,
    lineHeight: 20,
  },
  cta: {
    alignItems: "center",
    backgroundColor: colors.primary,
    borderRadius: 14,
    justifyContent: "center",
    marginTop: spacing.md,
    minHeight: 46,
    paddingHorizontal: spacing.lg,
    width: "100%",
  },
  ctaText: {
    color: colors.white,
    fontSize: typography.label,
    fontWeight: "900",
  },
  body: {
    gap: spacing.xs,
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.lg,
  },
  displayName: {
    color: colors.foreground,
    flexShrink: 1,
    fontSize: typography.label,
    fontWeight: "800",
    lineHeight: 20,
  },
  displayNameRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.xs,
    minWidth: 0,
  },
  header: {
    alignItems: "center",
    flexDirection: "row",
    minHeight: 60,
    paddingHorizontal: spacing.lg,
    paddingVertical: 2,
  },
  sponsoredRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.xs,
  },
  sponsoredText: {
    color: colors.primary,
    fontSize: typography.label,
    fontWeight: "800",
  },
  title: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
    lineHeight: 22,
  },
  pressed: {
    opacity: 0.72,
  },
});
