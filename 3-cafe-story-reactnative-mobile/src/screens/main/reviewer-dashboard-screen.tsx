import { useNavigation } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import {
  ArrowLeft,
  Award,
  BadgeCheck,
  BarChart3,
  Heart,
  MessageCircle,
  Share2,
  Sparkles,
  Trophy,
  Wallet,
} from "lucide-react-native";
import { useMemo, useState } from "react";
import { Pressable, ScrollView, StyleSheet, Text, View } from "react-native";

import { Avatar, Button, Screen } from "../../components";
import { useAuth } from "../../features/auth";
import {
  mockReviewerDashboardActivities,
  mockReviewerDashboardBadges,
  mockReviewerDashboardPayouts,
  mockReviewerDashboardPerformance,
  mockReviewerDashboardProfile,
  mockReviewerDashboardRanking,
  mockReviewerDashboardSegment,
  mockReviewerDashboardStats,
} from "../../mocks";
import { routes } from "../../navigation";
import type { RootStackParamList } from "../../navigation";
import { colors, spacing, typography } from "../../theme";
import type {
  AuthUser,
  ReviewerDashboardActivity,
  ReviewerDashboardBadge,
  ReviewerDashboardPeriod,
  ReviewerDashboardPayout,
  ReviewerDashboardRankingItem,
} from "../../types";

const periods: {
  label: string;
  value: ReviewerDashboardPeriod;
}[] = [
  { label: "Day", value: "day" },
  { label: "Week", value: "week" },
  { label: "Month", value: "month" },
  { label: "3M", value: "3months" },
];

function normalizeRole(role: string) {
  return role.replace(/^ROLE_/, "").toUpperCase();
}

function hasReviewerRole(user: AuthUser | null | undefined) {
  return Boolean(
    user?.roles?.some((role) => normalizeRole(role) === "REVIEWER"),
  );
}

function formatCount(value: number) {
  if (value >= 1000000) {
    return `${(value / 1000000).toFixed(value >= 10000000 ? 0 : 1)}m`;
  }

  if (value >= 1000) {
    return `${(value / 1000).toFixed(value >= 10000 ? 0 : 1)}k`;
  }

  return String(value);
}

function formatVnd(value: number) {
  return `${String(value).replace(/\B(?=(\d{3})+(?!\d))/g, ",")} VND`;
}

function formatDate(value: string | null) {
  if (!value) {
    return "No expiry date";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "No expiry date";
  }

  return date.toLocaleDateString(undefined, {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

function regionLabel() {
  const region = mockReviewerDashboardProfile.region;

  return [region?.area, region?.city].filter(Boolean).join(", ") || "CafeStory";
}

function badgeLabel(badge: ReviewerDashboardBadge) {
  return badge.toLowerCase();
}

function activityIcon(type: ReviewerDashboardActivity["type"]) {
  switch (type) {
    case "badge":
      return BadgeCheck;
    case "payout":
      return Wallet;
    case "ranking":
      return Trophy;
    case "share":
      return Share2;
    default:
      return Heart;
  }
}

export function ReviewerDashboardScreen() {
  const navigation =
    useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const { user } = useAuth();
  const [period, setPeriod] = useState<ReviewerDashboardPeriod>("month");
  const isReviewer = hasReviewerRole(user);
  const stats = mockReviewerDashboardStats[period];
  const currentPayout = mockReviewerDashboardPayouts[0];
  const allTimePayout = useMemo(
    () =>
      mockReviewerDashboardPayouts.reduce(
        (total, payout) => total + payout.totalAmount,
        0,
      ),
    [],
  );
  const currentRank = mockReviewerDashboardRanking.find(
    (item) => item.reviewerId === mockReviewerDashboardProfile.reviewerId,
  );

  if (!isReviewer) {
    return (
      <Screen padded={false}>
        <DashboardHeader onBack={() => navigation.goBack()} />
        <View style={styles.upgradeContainer}>
          <View style={styles.upgradeIcon}>
            <Sparkles color={colors.primary} size={30} strokeWidth={2.4} />
          </View>
          <Text style={styles.upgradeTitle}>Reviewer access required</Text>
          <Text style={styles.upgradeDescription}>
            Upgrade to reviewer to unlock payout wallet, ranking, badges, and
            performance analytics.
          </Text>
          <Button
            label="Become reviewer"
            onPress={() =>
              navigation.navigate(routes.paymentOptions, {
                initialTab: "reviewer",
              })
            }
          />
        </View>
      </Screen>
    );
  }

  return (
    <Screen padded={false}>
      <DashboardHeader onBack={() => navigation.goBack()} />
      <ScrollView
        contentContainerStyle={styles.content}
        showsVerticalScrollIndicator={false}
      >
        <View style={styles.hero}>
          <View style={styles.heroTop}>
            <Avatar uri={mockReviewerDashboardProfile.avatar} size={76} />
            <View style={styles.heroCopy}>
              <Text style={styles.heroEyebrow}>Reviewer workspace</Text>
              <Text style={styles.heroName}>
                {mockReviewerDashboardProfile.name}
              </Text>
              <Text style={styles.heroMeta}>{regionLabel()}</Text>
            </View>
          </View>
          <View style={styles.heroBadges}>
            <InfoPill
              label={`${badgeLabel(mockReviewerDashboardProfile.badge)} badge`}
            />
            <InfoPill
              label={`Expires ${formatDate(mockReviewerDashboardProfile.expireDate)}`}
            />
          </View>
          <View style={styles.heroStats}>
            <HeroStat
              label="Followers"
              value={formatCount(mockReviewerDashboardProfile.follower)}
            />
            <HeroStat
              label="Likes"
              value={formatCount(mockReviewerDashboardProfile.like)}
            />
            <HeroStat
              label="Score"
              value={formatCount(mockReviewerDashboardProfile.score)}
            />
          </View>
        </View>

        <SectionHeader
          subtitle="Mock values mirror reviewer stats DTO periods."
          title="Performance"
        />
        <View style={styles.periods}>
          {periods.map((item) => {
            const isActive = item.value === period;

            return (
              <Pressable
                accessibilityRole="button"
                accessibilityState={{ selected: isActive }}
                key={item.value}
                onPress={() => setPeriod(item.value)}
                style={({ pressed }) => [
                  styles.periodItem,
                  isActive && styles.periodItemActive,
                  pressed && styles.pressed,
                ]}
              >
                <Text
                  style={[
                    styles.periodText,
                    isActive && styles.periodTextActive,
                  ]}
                >
                  {item.label}
                </Text>
              </Pressable>
            );
          })}
        </View>
        <View style={styles.statGrid}>
          <MetricCard
            Icon={BarChart3}
            label="Score"
            value={formatCount(stats.score)}
          />
          <MetricCard
            Icon={Heart}
            label="Likes"
            value={formatCount(stats.likeCount)}
          />
          <MetricCard
            Icon={Share2}
            label="Shares"
            value={formatCount(stats.shareCount)}
          />
          <MetricCard
            Icon={MessageCircle}
            label="Comments"
            value={formatCount(stats.commentCount)}
          />
        </View>

        <SectionHeader
          subtitle="Earnings are mock payout rows from reviewer-only data."
          title="Payout wallet"
        />
        <WalletCard payout={currentPayout} total={allTimePayout} />

        <SectionHeader
          subtitle="Monthly ranking preview with current reviewer highlighted."
          title="Ranking"
        />
        <View style={styles.rankSummary}>
          <View>
            <Text style={styles.rankLabel}>Current rank</Text>
            <Text style={styles.rankValue}>#{currentRank?.rank ?? "-"}</Text>
          </View>
          <View style={styles.segmentPill}>
            <Text style={styles.segmentText}>
              {mockReviewerDashboardSegment.segment}
            </Text>
          </View>
        </View>
        <View style={styles.rankingList}>
          {mockReviewerDashboardRanking.map((item) => (
            <RankingRow
              item={item}
              key={item.reviewerId}
              isCurrent={
                item.reviewerId === mockReviewerDashboardProfile.reviewerId
              }
            />
          ))}
        </View>

        <SectionHeader
          subtitle="Badge history and reviewer-only activity."
          title="Badges and activity"
        />
        <View style={styles.badgeList}>
          {mockReviewerDashboardBadges.map((item) => (
            <View style={styles.badgeRow} key={item.id}>
              <View style={styles.badgeIcon}>
                <Award color={colors.primary} size={18} strokeWidth={2.4} />
              </View>
              <View style={styles.badgeCopy}>
                <Text style={styles.badgeTitle}>{item.badge}</Text>
                <Text style={styles.badgeDescription}>
                  {item.month} - {formatCount(item.score)} score
                </Text>
              </View>
            </View>
          ))}
        </View>

        <View style={styles.chartCard}>
          <Text style={styles.chartTitle}>Score trend</Text>
          <View style={styles.chartBars}>
            {mockReviewerDashboardPerformance.map((item) => (
              <View style={styles.chartItem} key={item.label}>
                <View
                  style={[
                    styles.chartBar,
                    { height: Math.max(24, item.score / 22) },
                  ]}
                />
                <Text style={styles.chartLabel}>{item.label}</Text>
              </View>
            ))}
          </View>
        </View>

        <View style={styles.activityList}>
          {mockReviewerDashboardActivities.map((item) => (
            <ActivityRow activity={item} key={item.id} />
          ))}
        </View>
      </ScrollView>
    </Screen>
  );
}

function DashboardHeader({ onBack }: { onBack: () => void }) {
  return (
    <View style={styles.header}>
      <Pressable
        accessibilityLabel="Back"
        accessibilityRole="button"
        hitSlop={10}
        onPress={onBack}
        style={({ pressed }) => [styles.backButton, pressed && styles.pressed]}
      >
        <ArrowLeft color={colors.foreground} size={30} strokeWidth={2.5} />
      </Pressable>
      <View style={styles.headerIcon}>
        <Trophy color={colors.primary} size={22} strokeWidth={2.5} />
      </View>
      <View style={styles.headerCopy}>
        <Text style={styles.title}>Reviewer dashboard</Text>
        <Text style={styles.subtitle}>Mock reviewer-only workspace</Text>
      </View>
    </View>
  );
}

function InfoPill({ label }: { label: string }) {
  return (
    <View style={styles.infoPill}>
      <Text style={styles.infoPillText}>{label}</Text>
    </View>
  );
}

function HeroStat({ label, value }: { label: string; value: string }) {
  return (
    <View style={styles.heroStat}>
      <Text style={styles.heroStatValue}>{value}</Text>
      <Text style={styles.heroStatLabel}>{label}</Text>
    </View>
  );
}

function SectionHeader({
  subtitle,
  title,
}: {
  subtitle?: string;
  title: string;
}) {
  return (
    <View style={styles.sectionHeader}>
      <Text style={styles.sectionTitle}>{title}</Text>
      {subtitle ? <Text style={styles.sectionSubtitle}>{subtitle}</Text> : null}
    </View>
  );
}

function MetricCard({
  Icon,
  label,
  value,
}: {
  Icon: typeof Heart;
  label: string;
  value: string;
}) {
  return (
    <View style={styles.metricCard}>
      <View style={styles.metricIcon}>
        <Icon color={colors.primary} size={18} strokeWidth={2.4} />
      </View>
      <Text style={styles.metricValue}>{value}</Text>
      <Text style={styles.metricLabel}>{label}</Text>
    </View>
  );
}

function WalletCard({
  payout,
  total,
}: {
  payout: ReviewerDashboardPayout;
  total: number;
}) {
  return (
    <View style={styles.walletCard}>
      <View style={styles.walletHeader}>
        <View style={styles.walletIcon}>
          <Wallet color={colors.white} size={22} strokeWidth={2.5} />
        </View>
        <View style={styles.walletCopy}>
          <Text style={styles.walletLabel}>Current payout</Text>
          <Text style={styles.walletValue}>{formatVnd(payout.totalAmount)}</Text>
        </View>
      </View>
      <View style={styles.walletRows}>
        <WalletRow label="Likes" value={formatVnd(payout.likeAmount)} />
        <WalletRow label="Shares" value={formatVnd(payout.shareAmount)} />
        <WalletRow label="Comments" value={formatVnd(payout.commentAmount)} />
      </View>
      <View style={styles.walletFooter}>
        <Text style={styles.walletFooterText}>{payout.payoutStatus}</Text>
        <Text style={styles.walletFooterText}>Total {formatVnd(total)}</Text>
      </View>
    </View>
  );
}

function WalletRow({ label, value }: { label: string; value: string }) {
  return (
    <View style={styles.walletRow}>
      <Text style={styles.walletRowLabel}>{label}</Text>
      <Text style={styles.walletRowValue}>{value}</Text>
    </View>
  );
}

function RankingRow({
  isCurrent,
  item,
}: {
  isCurrent: boolean;
  item: ReviewerDashboardRankingItem;
}) {
  return (
    <View style={[styles.rankingRow, isCurrent && styles.rankingRowCurrent]}>
      <View style={styles.rankingRank}>
        <Text style={styles.rankingRankText}>#{item.rank}</Text>
      </View>
      <View style={styles.rankingCopy}>
        <Text style={styles.rankingTitle}>{isCurrent ? "You" : item.badge}</Text>
        <Text style={styles.rankingDescription}>
          {item.location} - {formatCount(item.score)} score
        </Text>
      </View>
      <Text style={styles.rankingMeta}>
        {formatCount(item.likeCount)} L / {formatCount(item.shareCount)} S
      </Text>
    </View>
  );
}

function ActivityRow({ activity }: { activity: ReviewerDashboardActivity }) {
  const Icon = activityIcon(activity.type);

  return (
    <View style={styles.activityRow}>
      <View style={styles.activityIcon}>
        <Icon color={colors.primary} size={18} strokeWidth={2.4} />
      </View>
      <View style={styles.activityCopy}>
        <View style={styles.activityTitleRow}>
          <Text style={styles.activityTitle}>{activity.title}</Text>
          <Text style={styles.activityTime}>{activity.time}</Text>
        </View>
        <Text style={styles.activityDescription}>{activity.description}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  activityCopy: {
    flex: 1,
    gap: spacing.xs,
  },
  activityDescription: {
    color: colors.muted,
    fontSize: typography.caption,
    lineHeight: 18,
  },
  activityIcon: {
    alignItems: "center",
    backgroundColor: colors.primarySoft,
    borderRadius: 18,
    height: 36,
    justifyContent: "center",
    width: 36,
  },
  activityList: {
    gap: spacing.md,
  },
  activityRow: {
    alignItems: "flex-start",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    flexDirection: "row",
    gap: spacing.md,
    padding: spacing.md,
  },
  activityTime: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "800",
  },
  activityTitle: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.label,
    fontWeight: "900",
  },
  activityTitleRow: {
    alignItems: "flex-start",
    flexDirection: "row",
    gap: spacing.sm,
  },
  backButton: {
    alignItems: "center",
    height: 48,
    justifyContent: "center",
    width: 48,
  },
  badgeCopy: {
    flex: 1,
    gap: spacing.xs,
  },
  badgeDescription: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "700",
  },
  badgeIcon: {
    alignItems: "center",
    backgroundColor: colors.primarySoft,
    borderRadius: 17,
    height: 34,
    justifyContent: "center",
    width: 34,
  },
  badgeList: {
    gap: spacing.md,
  },
  badgeRow: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    flexDirection: "row",
    gap: spacing.md,
    padding: spacing.md,
  },
  badgeTitle: {
    color: colors.foreground,
    fontSize: typography.label,
    fontWeight: "900",
  },
  chartBar: {
    backgroundColor: colors.primary,
    borderRadius: 999,
    width: 18,
  },
  chartBars: {
    alignItems: "flex-end",
    flexDirection: "row",
    gap: spacing.md,
    justifyContent: "space-between",
    minHeight: 92,
  },
  chartCard: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    gap: spacing.lg,
    padding: spacing.lg,
  },
  chartItem: {
    alignItems: "center",
    flex: 1,
    gap: spacing.sm,
    justifyContent: "flex-end",
  },
  chartLabel: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "800",
  },
  chartTitle: {
    color: colors.foreground,
    fontSize: typography.label,
    fontWeight: "900",
  },
  content: {
    gap: spacing.lg,
    padding: spacing.xl,
    paddingBottom: spacing.xxl,
  },
  header: {
    alignItems: "center",
    backgroundColor: colors.background,
    borderBottomColor: colors.border,
    borderBottomWidth: 1,
    flexDirection: "row",
    gap: spacing.md,
    minHeight: 72,
    paddingHorizontal: spacing.sm,
  },
  headerCopy: {
    flex: 1,
    gap: 2,
  },
  headerIcon: {
    alignItems: "center",
    backgroundColor: colors.primarySoft,
    borderRadius: 19,
    height: 38,
    justifyContent: "center",
    width: 38,
  },
  hero: {
    backgroundColor: colors.primary,
    borderRadius: 8,
    gap: spacing.lg,
    padding: spacing.xl,
  },
  heroBadges: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: spacing.sm,
  },
  heroCopy: {
    flex: 1,
    gap: spacing.xs,
  },
  heroEyebrow: {
    color: "rgba(255,255,255,0.74)",
    fontSize: typography.caption,
    fontWeight: "900",
    textTransform: "uppercase",
  },
  heroMeta: {
    color: "rgba(255,255,255,0.76)",
    fontSize: typography.caption,
    fontWeight: "700",
  },
  heroName: {
    color: colors.white,
    fontSize: typography.title,
    fontWeight: "900",
  },
  heroStat: {
    flex: 1,
    gap: spacing.xs,
  },
  heroStatLabel: {
    color: "rgba(255,255,255,0.68)",
    fontSize: typography.caption,
    fontWeight: "800",
  },
  heroStatValue: {
    color: colors.white,
    fontSize: typography.body,
    fontWeight: "900",
  },
  heroStats: {
    flexDirection: "row",
    gap: spacing.md,
  },
  heroTop: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.lg,
  },
  infoPill: {
    backgroundColor: "rgba(255,255,255,0.14)",
    borderRadius: 999,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.xs,
  },
  infoPillText: {
    color: colors.white,
    fontSize: typography.caption,
    fontWeight: "900",
    textTransform: "capitalize",
  },
  metricCard: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    flexBasis: "47%",
    flexGrow: 1,
    gap: spacing.xs,
    padding: spacing.lg,
  },
  metricIcon: {
    alignItems: "center",
    backgroundColor: colors.primarySoft,
    borderRadius: 17,
    height: 34,
    justifyContent: "center",
    marginBottom: spacing.sm,
    width: 34,
  },
  metricLabel: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "800",
  },
  metricValue: {
    color: colors.foreground,
    fontSize: typography.title,
    fontWeight: "900",
  },
  periodItem: {
    alignItems: "center",
    borderRadius: 999,
    flex: 1,
    minHeight: 38,
    justifyContent: "center",
  },
  periodItemActive: {
    backgroundColor: colors.primary,
  },
  periods: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 24,
    borderWidth: 1,
    flexDirection: "row",
    gap: spacing.xs,
    padding: spacing.xs,
  },
  periodText: {
    color: colors.foreground,
    fontSize: typography.caption,
    fontWeight: "900",
  },
  periodTextActive: {
    color: colors.white,
  },
  pressed: {
    opacity: 0.72,
  },
  rankLabel: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "900",
  },
  rankSummary: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    flexDirection: "row",
    justifyContent: "space-between",
    padding: spacing.lg,
  },
  rankValue: {
    color: colors.foreground,
    fontSize: typography.heading,
    fontWeight: "900",
  },
  rankingCopy: {
    flex: 1,
    gap: spacing.xs,
  },
  rankingDescription: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "700",
  },
  rankingList: {
    gap: spacing.md,
  },
  rankingMeta: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "900",
  },
  rankingRank: {
    alignItems: "center",
    backgroundColor: colors.surfaceMuted,
    borderRadius: 17,
    height: 34,
    justifyContent: "center",
    width: 34,
  },
  rankingRankText: {
    color: colors.primary,
    fontSize: typography.caption,
    fontWeight: "900",
  },
  rankingRow: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    flexDirection: "row",
    gap: spacing.md,
    padding: spacing.md,
  },
  rankingRowCurrent: {
    backgroundColor: colors.primarySoft,
    borderColor: colors.primarySoft,
  },
  rankingTitle: {
    color: colors.foreground,
    fontSize: typography.label,
    fontWeight: "900",
  },
  sectionHeader: {
    gap: spacing.xs,
  },
  sectionSubtitle: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "700",
    lineHeight: 18,
  },
  sectionTitle: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },
  segmentPill: {
    backgroundColor: colors.primarySoft,
    borderRadius: 999,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
  },
  segmentText: {
    color: colors.primary,
    fontSize: typography.caption,
    fontWeight: "900",
    textTransform: "uppercase",
  },
  statGrid: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: spacing.md,
  },
  subtitle: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "700",
  },
  title: {
    color: colors.foreground,
    fontSize: typography.title,
    fontWeight: "900",
  },
  upgradeContainer: {
    alignItems: "center",
    flex: 1,
    gap: spacing.lg,
    justifyContent: "center",
    padding: spacing.xl,
  },
  upgradeDescription: {
    color: colors.muted,
    fontSize: typography.label,
    lineHeight: 22,
    maxWidth: 320,
    textAlign: "center",
  },
  upgradeIcon: {
    alignItems: "center",
    backgroundColor: colors.primarySoft,
    borderRadius: 30,
    height: 60,
    justifyContent: "center",
    width: 60,
  },
  upgradeTitle: {
    color: colors.foreground,
    fontSize: typography.title,
    fontWeight: "900",
    textAlign: "center",
  },
  walletCard: {
    backgroundColor: colors.primary,
    borderRadius: 8,
    gap: spacing.lg,
    padding: spacing.xl,
  },
  walletCopy: {
    flex: 1,
    gap: spacing.xs,
  },
  walletFooter: {
    flexDirection: "row",
    justifyContent: "space-between",
  },
  walletFooterText: {
    color: "rgba(255,255,255,0.78)",
    fontSize: typography.caption,
    fontWeight: "900",
  },
  walletHeader: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.md,
  },
  walletIcon: {
    alignItems: "center",
    backgroundColor: "rgba(255,255,255,0.16)",
    borderRadius: 20,
    height: 40,
    justifyContent: "center",
    width: 40,
  },
  walletLabel: {
    color: "rgba(255,255,255,0.74)",
    fontSize: typography.caption,
    fontWeight: "900",
  },
  walletRow: {
    flexDirection: "row",
    justifyContent: "space-between",
  },
  walletRowLabel: {
    color: "rgba(255,255,255,0.76)",
    fontSize: typography.label,
    fontWeight: "700",
  },
  walletRows: {
    gap: spacing.sm,
  },
  walletRowValue: {
    color: colors.white,
    fontSize: typography.label,
    fontWeight: "900",
  },
  walletValue: {
    color: colors.white,
    fontSize: typography.title,
    fontWeight: "900",
  },
});
