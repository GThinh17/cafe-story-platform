import { RouteProp, useNavigation, useRoute } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { ArrowLeft, Megaphone, RefreshCw } from "lucide-react-native";
import { useCallback, useEffect, useMemo, useState } from "react";
import { Pressable, ScrollView, StyleSheet, Text, View } from "react-native";

import { AdCampaignCard, AdCampaignForm, Button, EmptyState, LoadingState, Screen } from "../../components";
import { useAuth } from "../../features/auth";
import { routes } from "../../navigation";
import type { RootStackParamList } from "../../navigation";
import { getAdCampaigns, getCafePagesByOwner, getPayments } from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type { AdCampaignResponse, CafePageResponse, PaymentResponse } from "../../types";

type AdsManagerRouteProp = RouteProp<RootStackParamList, typeof routes.adsManager>;
type CampaignFilter = "ALL" | "DRAFT" | "ACTIVE" | "PAUSED" | "EXPIRED";

const filters: CampaignFilter[] = ["ALL", "DRAFT", "ACTIVE", "PAUSED", "EXPIRED"];

export function AdsManagerScreen() {
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const route = useRoute<AdsManagerRouteProp>();
  const { user } = useAuth();
  const [cafePages, setCafePages] = useState<CafePageResponse[]>([]);
  const [payments, setPayments] = useState<PaymentResponse[]>([]);
  const [campaigns, setCampaigns] = useState<AdCampaignResponse[]>([]);
  const [filter, setFilter] = useState<CampaignFilter>("ALL");
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadDashboard = useCallback(async () => {
    if (!user?.userId) {
      setError("Sign in as a cafe owner to manage Ads.");
      setIsLoading(false);
      return;
    }
    setIsLoading(true);
    setError(null);
    try {
      const [ownedPages, paidPayments] = await Promise.all([
        getCafePagesByOwner(user.userId),
        getPayments("PAID"),
      ]);
      const campaignGroups = await Promise.all(ownedPages.map((page) => getAdCampaigns(page.id)));
      setCafePages(ownedPages.filter((page) => page.pageActive !== false));
      setPayments(paidPayments.filter((payment) => Boolean(payment.adFeeId)));
      setCampaigns(campaignGroups.flat());
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "Unable to load Ads Manager.");
    } finally {
      setIsLoading(false);
    }
  }, [user?.userId]);

  useEffect(() => {
    void loadDashboard();
  }, [loadDashboard]);

  const availablePayments = useMemo(() => {
    const used = new Set(campaigns.map((campaign) => campaign.paymentId));
    return payments.filter((payment) => !used.has(payment.paymentId));
  }, [campaigns, payments]);

  const visibleCampaigns = useMemo(
    () => campaigns.filter((campaign) => filter === "ALL" || campaign.status === filter),
    [campaigns, filter],
  );

  return (
    <Screen padded={false}>
      <View style={styles.header}>
        <Pressable accessibilityLabel="Back" hitSlop={10} onPress={() => navigation.goBack()} style={styles.iconButton}>
          <ArrowLeft color={colors.foreground} size={28} />
        </Pressable>
        <View style={styles.headerIcon}><Megaphone color={colors.primary} size={21} /></View>
        <View style={styles.headerCopy}>
          <Text style={styles.headerTitle}>Ads Manager</Text>
          <Text style={styles.headerSubtitle}>Campaigns, delivery and performance</Text>
        </View>
        <Pressable accessibilityLabel="Refresh Ads Manager" hitSlop={10} onPress={() => void loadDashboard()} style={styles.iconButton}>
          <RefreshCw color={colors.foreground} size={21} />
        </Pressable>
      </View>

      {isLoading ? (
        <View style={styles.center}><LoadingState label="Loading Ads Manager..." /></View>
      ) : error ? (
        <View style={styles.center}>
          <EmptyState description="Check your connection and cafe owner access, then try again." title={error} />
          <Button label="Retry" onPress={loadDashboard} variant="outlined" />
        </View>
      ) : (
        <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
          {route.params?.paymentId ? (
            <View style={styles.paidBanner}>
              <Text style={styles.paidTitle}>Ads payment confirmed</Text>
              <Text style={styles.paidText}>Use the selected paid package below to create your campaign.</Text>
            </View>
          ) : null}

          <View style={styles.summary}>
            <Text style={styles.eyebrow}>ADVERTISER WORKSPACE</Text>
            <Text style={styles.summaryTitle}>Grow your cafe without leaving CafeStory</Text>
            <Text style={styles.summaryText}>Every campaign includes up to 10,000 served impressions or 30 days of delivery.</Text>
            <View style={styles.summaryMetrics}>
              <SummaryMetric label="Campaigns" value={String(campaigns.length)} />
              <SummaryMetric label="Active" value={String(campaigns.filter((item) => item.status === "ACTIVE").length)} />
              <SummaryMetric label="Unused packages" value={String(availablePayments.length)} />
            </View>
            <Button label="Buy another Ads package" onPress={() => navigation.navigate(routes.paymentOptions, { initialTab: "ads" })} variant="secondary" />
          </View>

          {!cafePages.length ? (
            <View style={styles.stateCard}>
              <EmptyState description="Create and activate a cafe page before launching a campaign." title="No active cafe page" />
            </View>
          ) : !availablePayments.length ? (
            <View style={styles.stateCard}>
              <EmptyState description="Buy an Ads package or finish a pending Stripe payment to create another campaign." title="No unused paid package" />
              <Button label="View Ads packages" onPress={() => navigation.navigate(routes.paymentOptions, { initialTab: "ads" })} />
            </View>
          ) : (
            <AdCampaignForm
              cafePages={cafePages}
              initialPaymentId={route.params?.paymentId}
              onCreated={loadDashboard}
              payments={availablePayments}
            />
          )}

          <View style={styles.campaignHeading}>
            <View>
              <Text style={styles.eyebrow}>CAMPAIGNS</Text>
              <Text style={styles.sectionTitle}>Delivery status</Text>
            </View>
            <Text style={styles.campaignCount}>{visibleCampaigns.length}</Text>
          </View>
          <ScrollView horizontal showsHorizontalScrollIndicator={false}>
            <View style={styles.filters}>
              {filters.map((value) => (
                <Pressable key={value} onPress={() => setFilter(value)} style={[styles.filter, filter === value && styles.activeFilter]}>
                  <Text style={[styles.filterText, filter === value && styles.activeFilterText]}>{value}</Text>
                </Pressable>
              ))}
            </View>
          </ScrollView>

          {visibleCampaigns.length ? (
            visibleCampaigns.map((campaign) => (
              <AdCampaignCard campaign={campaign} key={campaign.adCampaignId} onChanged={loadDashboard} />
            ))
          ) : (
            <View style={styles.stateCard}>
              <EmptyState description="Create a campaign above or choose a different status filter." title="No campaigns in this view" />
            </View>
          )}
        </ScrollView>
      )}
    </Screen>
  );
}

function SummaryMetric({ label, value }: { label: string; value: string }) {
  return <View style={styles.summaryMetric}><Text style={styles.summaryValue}>{value}</Text><Text style={styles.summaryLabel}>{label}</Text></View>;
}

const styles = StyleSheet.create({
  activeFilter: { backgroundColor: colors.primary, borderColor: colors.primary },
  activeFilterText: { color: colors.white },
  campaignCount: { backgroundColor: colors.primarySoft, borderRadius: 999, color: colors.primary, fontSize: typography.caption, fontWeight: "900", overflow: "hidden", paddingHorizontal: spacing.md, paddingVertical: spacing.xs },
  campaignHeading: { alignItems: "center", flexDirection: "row", justifyContent: "space-between", marginTop: spacing.sm },
  center: { flex: 1, gap: spacing.lg, justifyContent: "center", padding: spacing.xl },
  content: { gap: spacing.lg, padding: spacing.lg, paddingBottom: spacing.xxl },
  eyebrow: { color: colors.secondary, fontSize: 10, fontWeight: "900", letterSpacing: 1.2 },
  filter: { borderColor: colors.border, borderRadius: 999, borderWidth: 1, paddingHorizontal: spacing.lg, paddingVertical: spacing.sm },
  filterText: { color: colors.foreground, fontSize: 11, fontWeight: "800" },
  filters: { flexDirection: "row", gap: spacing.sm },
  header: { alignItems: "center", backgroundColor: colors.background, borderBottomColor: colors.border, borderBottomWidth: 1, flexDirection: "row", gap: spacing.sm, minHeight: 72, paddingHorizontal: spacing.sm },
  headerCopy: { flex: 1, gap: 2 },
  headerIcon: { alignItems: "center", backgroundColor: colors.primarySoft, borderRadius: 18, height: 36, justifyContent: "center", width: 36 },
  headerSubtitle: { color: colors.muted, fontSize: 11, fontWeight: "700" },
  headerTitle: { color: colors.foreground, fontSize: 20, fontWeight: "900" },
  iconButton: { alignItems: "center", height: 44, justifyContent: "center", width: 44 },
  paidBanner: { backgroundColor: colors.tertiarySoft, borderColor: colors.tertiary, borderRadius: 12, borderWidth: 1, gap: spacing.xs, padding: spacing.lg },
  paidText: { color: colors.tertiary, fontSize: typography.caption, lineHeight: 18 },
  paidTitle: { color: colors.tertiary, fontSize: typography.label, fontWeight: "900" },
  sectionTitle: { color: colors.foreground, fontSize: 20, fontWeight: "900", marginTop: 2 },
  stateCard: { backgroundColor: colors.surface, borderColor: colors.border, borderRadius: 12, borderWidth: 1, gap: spacing.lg, minHeight: 200, padding: spacing.lg },
  summary: { backgroundColor: colors.primary, borderRadius: 14, gap: spacing.lg, padding: spacing.xl },
  summaryLabel: { color: "rgba(255,255,255,0.68)", fontSize: 10, textAlign: "center" },
  summaryMetric: { alignItems: "center", flex: 1, gap: 2 },
  summaryMetrics: { flexDirection: "row", gap: spacing.md },
  summaryText: { color: "rgba(255,255,255,0.75)", fontSize: typography.caption, lineHeight: 19 },
  summaryTitle: { color: colors.white, fontSize: 24, fontWeight: "900", lineHeight: 30 },
  summaryValue: { color: colors.white, fontSize: 20, fontWeight: "900" },
});
