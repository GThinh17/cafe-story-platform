import { RouteProp, useNavigation, useRoute } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import {
  ArrowLeft,
  BadgeCheck,
  CheckCircle2,
  CreditCard,
  Megaphone,
  Store,
} from "lucide-react-native";
import { useState } from "react";
import { Alert, Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";

import { Button, Screen } from "../../components";
import { mockPaymentPlans, paymentPlanTabs } from "../../mocks";
import { routes } from "../../navigation";
import type { RootStackParamList } from "../../navigation";
import { colors, spacing, typography } from "../../theme";
import type { PaymentPlan, PaymentPlanTab } from "../../types";

type PaymentOptionsRouteProp = RouteProp<
  RootStackParamList,
  typeof routes.paymentOptions
>;

const PAYMENT_HEADER_HEIGHT = 72;

function formatVnd(value: number) {
  return `${String(value).replace(/\B(?=(\d{3})+(?!\d))/g, ",")} VND`;
}

function getTabIcon(tab: PaymentPlanTab) {
  switch (tab) {
    case "cafe-page":
      return Store;
    case "ads":
      return Megaphone;
    default:
      return BadgeCheck;
  }
}

function handleChoosePlan(plan: PaymentPlan) {
  Alert.alert(
    "Mock payment",
    `${plan.title} is selected. Payment API is not connected on mobile yet.`,
  );
}

export function PaymentOptionsScreen() {
  const navigation =
    useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const route = useRoute<PaymentOptionsRouteProp>();
  const insets = useSafeAreaInsets();
  const [activeTab, setActiveTab] = useState<PaymentPlanTab>(
    route.params?.initialTab ?? "reviewer",
  );
  const plans = mockPaymentPlans.filter((plan) => plan.tab === activeTab);
  const activeTabMeta = paymentPlanTabs.find((tab) => tab.value === activeTab);

  return (
    <Screen padded={false}>
      <View style={styles.header}>
        <Pressable
          accessibilityLabel="Back to profile"
          accessibilityRole="button"
          hitSlop={10}
          onPress={() => navigation.goBack()}
          style={({ pressed }) => [
            styles.backButton,
            pressed && styles.pressed,
          ]}
        >
          <ArrowLeft color={colors.foreground} size={31} strokeWidth={2.5} />
        </Pressable>
        <View style={styles.headerIcon}>
          <CreditCard color={colors.primary} size={22} strokeWidth={2.5} />
        </View>
        <View style={styles.headerCopy}>
          <Text style={styles.title}>Payment options</Text>
          <Text style={styles.subtitle}>
            {activeTabMeta?.subtitle ?? "Choose a CafeStory package"}
          </Text>
        </View>
      </View>

      <ScrollView
        contentContainerStyle={styles.planList}
        showsVerticalScrollIndicator={false}
      >
        {plans.map((plan) => (
          <PlanCard key={plan.id} plan={plan} />
        ))}
      </ScrollView>

      <View
        style={[
          styles.tabsShell,
          { paddingBottom: Math.max(insets.bottom, 10) },
        ]}
      >
        <View style={styles.tabs}>
          {paymentPlanTabs.map((tab) => {
            const Icon = getTabIcon(tab.value);
            const isActive = activeTab === tab.value;

            return (
              <Pressable
                accessibilityLabel={`Show ${tab.label} packages`}
                accessibilityRole="tab"
                accessibilityState={{ selected: isActive }}
                key={tab.value}
                onPress={() => setActiveTab(tab.value)}
                style={({ pressed }) => [
                  styles.tab,
                  isActive && styles.activeTab,
                  pressed && styles.pressed,
                ]}
              >
                <Icon
                  color={isActive ? colors.white : colors.foreground}
                  size={22}
                  strokeWidth={isActive ? 2.7 : 2.1}
                />
                {isActive ? (
                  <Text numberOfLines={1} style={styles.activeTabText}>
                    {tab.label}
                  </Text>
                ) : null}
              </Pressable>
            );
          })}
        </View>
      </View>
    </Screen>
  );
}

function PlanCard({ plan }: { plan: PaymentPlan }) {
  return (
    <View style={[styles.planCard, plan.highlighted && styles.highlightedCard]}>
      <View style={styles.planHeader}>
        <View style={styles.planTitleBlock}>
          {plan.badge ? (
            <Text
              style={[
                styles.badge,
                plan.highlighted && styles.highlightedBadge,
              ]}
            >
              {plan.badge}
            </Text>
          ) : null}
          <Text
            style={[
              styles.planTitle,
              plan.highlighted && styles.highlightedText,
            ]}
          >
            {plan.title}
          </Text>
          <Text
            style={[
              styles.planSubtitle,
              plan.highlighted && styles.highlightedMutedText,
            ]}
          >
            {plan.subtitle}
          </Text>
        </View>
        <View style={styles.priceBlock}>
          <Text
            style={[
              styles.price,
              plan.highlighted && styles.highlightedText,
            ]}
          >
            {formatVnd(plan.priceVnd)}
          </Text>
          <Text
            style={[
              styles.billing,
              plan.highlighted && styles.highlightedMutedText,
            ]}
          >
            {plan.durationLabel} - {plan.billingLabel}
          </Text>
        </View>
      </View>

      <View style={styles.features}>
        {plan.features.map((feature) => (
          <View key={feature} style={styles.featureRow}>
            <CheckCircle2
              color={plan.highlighted ? colors.white : colors.primary}
              size={17}
              strokeWidth={2.5}
            />
            <Text
              style={[
                styles.featureText,
                plan.highlighted && styles.highlightedMutedText,
              ]}
            >
              {feature}
            </Text>
          </View>
        ))}
      </View>

      {plan.note ? (
        <Text
          style={[styles.note, plan.highlighted && styles.highlightedMutedText]}
        >
          {plan.note}
        </Text>
      ) : null}

      <Button
        label={plan.ctaLabel}
        onPress={() => handleChoosePlan(plan)}
        variant={plan.highlighted ? "secondary" : "primary"}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  activeTab: {
    backgroundColor: colors.primary,
    gap: spacing.xs,
    minWidth: 116,
  },
  backButton: {
    alignItems: "center",
    height: 48,
    justifyContent: "center",
    width: 48,
  },
  badge: {
    alignSelf: "flex-start",
    backgroundColor: colors.surfaceMuted,
    borderRadius: 999,
    color: colors.primary,
    fontSize: typography.caption,
    fontWeight: "900",
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.xs,
    textTransform: "uppercase",
  },
  billing: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "800",
    textAlign: "right",
  },
  featureRow: {
    alignItems: "flex-start",
    flexDirection: "row",
    gap: spacing.md,
  },
  featureText: {
    color: colors.foreground,
    flex: 1,
    fontSize: typography.label,
    lineHeight: 20,
  },
  features: {
    gap: spacing.md,
  },
  header: {
    alignItems: "center",
    backgroundColor: colors.background,
    borderBottomColor: colors.border,
    borderBottomWidth: 1,
    flexDirection: "row",
    gap: spacing.md,
    minHeight: PAYMENT_HEADER_HEIGHT,
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
  highlightedBadge: {
    backgroundColor: "rgba(255,255,255,0.16)",
    color: colors.white,
  },
  highlightedCard: {
    backgroundColor: colors.primary,
    borderColor: colors.primary,
  },
  highlightedMutedText: {
    color: "rgba(255,255,255,0.78)",
  },
  highlightedText: {
    color: colors.white,
  },
  note: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "700",
    lineHeight: 18,
  },
  planCard: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    gap: spacing.xl,
    padding: spacing.xl,
  },
  planHeader: {
    gap: spacing.lg,
  },
  planList: {
    gap: spacing.lg,
    paddingBottom: 132,
    paddingHorizontal: spacing.xl,
    paddingTop: spacing.xl,
  },
  planSubtitle: {
    color: colors.muted,
    fontSize: typography.label,
    lineHeight: 20,
  },
  planTitle: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },
  planTitleBlock: {
    gap: spacing.md,
  },
  pressed: {
    opacity: 0.72,
  },
  price: {
    color: colors.foreground,
    fontSize: 24,
    fontWeight: "900",
    textAlign: "right",
  },
  priceBlock: {
    alignItems: "flex-end",
    gap: spacing.xs,
  },
  subtitle: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "700",
  },
  tab: {
    alignItems: "center",
    borderRadius: 999,
    flexDirection: "row",
    height: 46,
    justifyContent: "center",
    minWidth: 46,
    paddingHorizontal: spacing.md,
  },
  activeTabText: {
    color: colors.white,
    fontSize: typography.caption,
    fontWeight: "800",
  },
  tabs: {
    alignItems: "center",
    alignSelf: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 28,
    borderWidth: 1,
    elevation: 8,
    flexDirection: "row",
    gap: spacing.xs,
    minHeight: 58,
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs,
    shadowColor: colors.primary,
    shadowOffset: {
      height: 6,
      width: 0,
    },
    shadowOpacity: 0.12,
    shadowRadius: 16,
  },
  tabsShell: {
    backgroundColor: "transparent",
    bottom: 0,
    elevation: 12,
    left: 0,
    paddingHorizontal: spacing.xl,
    paddingTop: spacing.sm,
    position: "absolute",
    right: 0,
    zIndex: 20,
  },
  title: {
    color: colors.foreground,
    fontSize: typography.title,
    fontWeight: "900",
  },
});
