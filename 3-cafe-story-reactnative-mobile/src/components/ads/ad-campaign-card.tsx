import { BarChart3, CalendarDays, Eye, MousePointerClick } from "lucide-react-native";
import { Text } from "react-native";
import { useCallback, useEffect, useState } from "react";
import { StyleSheet, View } from "react-native";

import { activateAdCampaign, getAdCampaignStats, pauseAdCampaign } from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import { formatCurrentDate, formatCurrentNumber } from "../../features/i18n";
import type { AdCampaignResponse, AdCampaignStatsResponse } from "../../types";
import { Button } from "../ui/button";
import { t } from "../../features/i18n";

type AdCampaignCardProps = {
  campaign: AdCampaignResponse;
  onChanged: () => Promise<void> | void;
};

function statusColor(status: string | null) {
  if (status === "ACTIVE") return colors.tertiary;
  if (status === "PAUSED") return colors.rating;
  if (status === "EXPIRED" || status === "REJECTED") return colors.danger;
  return colors.secondary;
}

function formatDate(value: string | null) {
  if (!value) return "Not started";
  return formatCurrentDate(value, { dateStyle: "medium" });
}

export function AdCampaignCard({ campaign, onChanged }: AdCampaignCardProps) {
  const [stats, setStats] = useState<AdCampaignStatsResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isPending, setIsPending] = useState(false);

  const loadStats = useCallback(async () => {
    try {
      setStats(await getAdCampaignStats(campaign.adCampaignId));
      setError(null);
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "Unable to load campaign statistics.");
    }
  }, [campaign.adCampaignId]);

  useEffect(() => {
    void loadStats();
  }, [loadStats]);

  async function changeStatus() {
    setIsPending(true);
    setError(null);
    try {
      if (campaign.status === "ACTIVE") {
        await pauseAdCampaign(campaign.adCampaignId);
      } else {
        await activateAdCampaign(campaign.adCampaignId);
      }
      await onChanged();
    } catch (actionError) {
      setError(actionError instanceof Error ? actionError.message : "Unable to update the campaign.");
    } finally {
      setIsPending(false);
    }
  }

  const canChangeStatus = ["DRAFT", "ACTIVE", "PAUSED"].includes(campaign.status ?? "");

  return (
    <View style={styles.card}>
      <View style={styles.headingRow}>
        <View style={styles.titleBlock}>
          <Text numberOfLines={2} style={styles.title}>{campaign.title || "Untitled campaign"}</Text>
          <Text numberOfLines={2} style={styles.description}>
            {campaign.description || "No description provided."}
          </Text>
        </View>
        <View style={[styles.badge, { backgroundColor: statusColor(campaign.status) }]}>
          <Text style={styles.badgeText}>{campaign.status || "DRAFT"}</Text>
        </View>
      </View>

      <View style={styles.metrics}>
        <Metric Icon={Eye} label={t("Impressions")} value={String(stats?.servedImpressions ?? campaign.servedImpressions ?? 0)} />
        <Metric Icon={MousePointerClick} label={t("Clicks")} value={String(stats?.totalClicks ?? 0)} />
        <Metric
          Icon={BarChart3}
          label="CTR"
          value={`${formatCurrentNumber(stats?.ctrPercent ?? 0, {
            maximumFractionDigits: 2,
            minimumFractionDigits: 2,
          })}%`}
        />
        <Metric Icon={CalendarDays} label={t("Days left")} value={String(stats?.remainingDays ?? 0)} />
      </View>

      <View style={styles.progressTrack}>
        <View
          style={[
            styles.progressFill,
            {
              width: `${Math.min(
                100,
                ((stats?.servedImpressions ?? campaign.servedImpressions ?? 0) /
                  Math.max(1, stats?.maxImpressions ?? campaign.maxImpressions ?? 10000)) *
                  100,
              )}%`,
            },
          ]}
        />
      </View>
      <Text style={styles.meta}>
        {formatDate(campaign.startAt)}{t("to")}{formatDate(campaign.endAt)} · {stats?.remainingImpressions ?? 0}{t("impressions left")}</Text>

      {error ? <Text style={styles.error}>{error}</Text> : null}

      {canChangeStatus ? (
        <Button
          isLoading={isPending}
          label={campaign.status === "ACTIVE" ? t("Pause campaign") : t("Activate campaign")}
          onPress={changeStatus}
          variant={campaign.status === "ACTIVE" ? "outlined" : "primary"}
        />
      ) : null}
    </View>
  );
}

function Metric({ Icon, label, value }: { Icon: typeof Eye; label: string; value: string }) {
  return (
    <View style={styles.metric}>
      <Icon color={colors.secondary} size={17} strokeWidth={2.3} />
      <Text style={styles.metricValue}>{value}</Text>
      <Text style={styles.metricLabel}>{label}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  badge: { borderRadius: 999, paddingHorizontal: spacing.md, paddingVertical: spacing.xs },
  badgeText: { color: colors.white, fontSize: 10, fontWeight: "900", letterSpacing: 0.6 },
  card: { backgroundColor: colors.surface, borderColor: colors.border, borderRadius: 12, borderWidth: 1, gap: spacing.lg, padding: spacing.lg },
  description: { color: colors.muted, fontSize: typography.caption, lineHeight: 18 },
  error: { color: colors.danger, fontSize: typography.caption, fontWeight: "700" },
  headingRow: { alignItems: "flex-start", flexDirection: "row", gap: spacing.md, justifyContent: "space-between" },
  meta: { color: colors.muted, fontSize: 11, lineHeight: 17 },
  metric: { alignItems: "center", flex: 1, gap: 2, minWidth: 68 },
  metricLabel: { color: colors.muted, fontSize: 10, textAlign: "center" },
  metricValue: { color: colors.foreground, fontSize: typography.label, fontWeight: "900" },
  metrics: { flexDirection: "row", flexWrap: "wrap", gap: spacing.sm, justifyContent: "space-between" },
  progressFill: { backgroundColor: colors.primary, borderRadius: 999, height: "100%" },
  progressTrack: { backgroundColor: colors.surfaceMuted, borderRadius: 999, height: 7, overflow: "hidden" },
  title: { color: colors.foreground, fontSize: typography.body, fontWeight: "900" },
  titleBlock: { flex: 1, gap: spacing.xs },
});
