import { RouteProp, useNavigation, useRoute } from "@react-navigation/native";
import { Pressable, Text } from "../../features/i18n/localized-native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import {
  AlertCircle, ArrowLeft, BadgeCheck, CheckCircle2, CreditCard, Landmark, Megaphone, RefreshCw, Store, X, } from "lucide-react-native";
import { useCallback, useEffect, useMemo, useState } from "react";
import {
  ActivityIndicator, Linking, Modal, ScrollView, StyleSheet, View } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";

import { Button, EmptyState, LoadingState, Screen } from "../../components";
import { useAuth } from "../../features/auth";
import {
  formatCurrentCurrency,
  getCurrentLocale,
} from "../../features/i18n";
import { routes } from "../../navigation";
import type { RootStackParamList } from "../../navigation";
import {
  createPayment,
  getAdFees,
  getExtraFees,
  getPayment,
} from "../../services/api";
import { colors, spacing, typography } from "../../theme";
import type {
  AdFeeResponse,
  AuthUser,
  CheckoutPaymentMethod,
  ExtraFeeResponse,
  PaymentPlanTab,
  PaymentResponse,
} from "../../types";

type PaymentOptionsRouteProp = RouteProp<
  RootStackParamList,
  typeof routes.paymentOptions
>;

type PaymentDisplayPlan = {
  adFeeId?: string;
  badge?: string;
  billingLabel: string;
  ctaLabel: string;
  durationLabel: string;
  extraFeeId?: string;
  features: string[];
  highlighted?: boolean;
  id: string;
  note?: string;
  priceVnd: number;
  source: "adFee" | "extraFee";
  subtitle: string;
  tab: PaymentPlanTab;
  title: string;
};

type PaymentFlowState =
  | {
      status: "idle";
    }
  | {
      error?: string | null;
      message: string;
      method: CheckoutPaymentMethod;
      payment: PaymentResponse;
      paymentId: string;
      plan: PaymentDisplayPlan;
      status: "checking" | "externalPaymentOpened" | "failed" | "pending" | "success";
    };

type PaymentTabMeta = {
  label: string;
  subtitle: string;
  value: PaymentPlanTab;
};

const PAYMENT_HEADER_HEIGHT = 72;

const paymentPlanTabs: PaymentTabMeta[] = [
  {
    label: "Reviewer",
    subtitle: "Become a paid reviewer",
    value: "reviewer",
  },
  {
    label: "Cafe page",
    subtitle: "Open and manage a cafe page",
    value: "cafe-page",
  },
  {
    label: "Ads",
    subtitle: "Promote posts and cafes",
    value: "ads",
  },
];

const paymentMethods: {
  description: string;
  label: string;
  method: CheckoutPaymentMethod;
  Icon: typeof CreditCard;
}[] = [
  {
    description: "Pay by card through Stripe checkout.",
    Icon: CreditCard,
    label: "Stripe",
    method: "STRIPE_CARD",
  },
  {
    description: "Pay through the VNPAY hosted payment page.",
    Icon: Landmark,
    label: "VNPAY",
    method: "VNPAY",
  },
];

function formatVnd(value: number | string | null | undefined) {
  const safeValue = Number(value ?? 0);
  return formatCurrentCurrency(safeValue);
}

function formatMonths(value: number | null | undefined) {
  if (!value || value <= 0) {
    return getCurrentLocale() === "vi" ? "Không hết hạn" : "No expiry";
  }

  if (getCurrentLocale() === "vi") {
    return `${value} tháng`;
  }
  return `${value} ${value === 1 ? "month" : "months"}`;
}

function formatAdFeeType(value: string) {
  return value
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function getErrorMessage(error: unknown, fallback: string) {
  if (error instanceof Error) {
    return error.message;
  }

  return fallback;
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

function normalizeRole(role: string) {
  return role.replace(/^ROLE_/, "").toUpperCase();
}

function hasAdminRole(user: AuthUser | null | undefined) {
  return Boolean(user?.roles?.some((role) => normalizeRole(role) === "ADMIN"));
}

function hasCafePageRole(user: AuthUser | null | undefined) {
  return Boolean(
    user?.roles?.some((role) => {
      const normalizedRole = normalizeRole(role);

      return normalizedRole === "CAFE_PAGE" || normalizedRole === "CAFE";
    }),
  );
}

function linkedCafePageId(user: AuthUser | null | undefined) {
  return user?.cafePageId ?? user?.pageId ?? null;
}

function isPaidStatus(status: string | null | undefined) {
  const normalizedStatus = String(status ?? "").trim().toUpperCase();

  return ["COMPLETED", "PAID", "PAID_SUCCESS", "SUCCESS"].includes(
    normalizedStatus,
  );
}

function isPendingStatus(status: string | null | undefined) {
  return String(status ?? "").trim().toUpperCase() === "PENDING";
}

function extraFeeToPlan(fee: ExtraFeeResponse): PaymentDisplayPlan | null {
  if (fee.feeType === "REVIEWER_REGISTRATION") {
    return {
      badge: "Reviewer",
      billingLabel: "one-time",
      ctaLabel: "Choose reviewer package",
      durationLabel: formatMonths(fee.durationMonths),
      extraFeeId: fee.extraFeeId,
      features: [
        "Reviewer role activates after payment is verified",
        `${formatMonths(fee.durationMonths)} reviewer access`,
        "Reviewer dashboard and payout eligibility",
        "Profile badge and reviewer discovery tools",
      ],
      highlighted: true,
      id: fee.extraFeeId,
      note: "Existing reviewer access is extended by the server payment rule.",
      priceVnd: fee.price,
      source: "extraFee",
      subtitle: fee.description || "Unlock reviewer tools on CafeStory.",
      tab: "reviewer",
      title: fee.name,
    };
  }

  if (fee.feeType === "CAFE_PAGE_OPENING") {
    return {
      badge: "Cafe",
      billingLabel: "one-time",
      ctaLabel: "Choose cafe page package",
      durationLabel: formatMonths(fee.durationMonths),
      extraFeeId: fee.extraFeeId,
      features: [
        "Cafe page role activates after payment is verified",
        `${formatMonths(fee.durationMonths)} cafe page access`,
        fee.maxMembers
          ? `Up to ${fee.maxMembers} page members`
          : "Default page member limit",
        "Draft cafe page is created if you do not have one",
      ],
      highlighted: true,
      id: fee.extraFeeId,
      note: "Existing active cafe page access is extended by the server payment rule.",
      priceVnd: fee.price,
      source: "extraFee",
      subtitle: fee.description || "Open and manage an official cafe page.",
      tab: "cafe-page",
      title: fee.name,
    };
  }

  return null;
}

function adFeeToPlan(fee: AdFeeResponse): PaymentDisplayPlan {
  return {
    adFeeId: fee.adFeeId,
    badge: "Ads",
    billingLabel: "campaign",
    ctaLabel: "Choose ads package",
    durationLabel: "30 days",
    features: [
      "Ads payment flow for cafe page promotion",
      "Feed ad package configured by backend",
      "Payment is created with adFeeId",
    ],
    id: fee.adFeeId,
    note: "Ads packages are available for cafe page and admin accounts.",
    priceVnd: Number(fee.price ?? 0),
    source: "adFee",
    subtitle: "Promote CafeStory content through an ad package.",
    tab: "ads",
    title: formatAdFeeType(fee.feeType),
  };
}

export function PaymentOptionsScreen() {
  const navigation =
    useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const route = useRoute<PaymentOptionsRouteProp>();
  const insets = useSafeAreaInsets();
  const { refreshCurrentUser, user } = useAuth();
  const isAdmin = hasAdminRole(user);
  const canAccessAds =
    isAdmin || hasCafePageRole(user) || Boolean(linkedCafePageId(user));
  const [activeTab, setActiveTab] = useState<PaymentPlanTab>(
    route.params?.initialTab ?? "reviewer",
  );
  const [extraFees, setExtraFees] = useState<ExtraFeeResponse[]>([]);
  const [adFees, setAdFees] = useState<AdFeeResponse[]>([]);
  const [isLoadingPlans, setIsLoadingPlans] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [selectedPlan, setSelectedPlan] = useState<PaymentDisplayPlan | null>(
    null,
  );
  const [modalError, setModalError] = useState<string | null>(null);
  const [activeMethod, setActiveMethod] = useState<CheckoutPaymentMethod | null>(
    null,
  );
  const [isCreatingPayment, setIsCreatingPayment] = useState(false);
  const [paymentFlow, setPaymentFlow] = useState<PaymentFlowState>({
    status: "idle",
  });

  const availableTabs = useMemo(
    () => paymentPlanTabs.filter((tab) => canAccessAds || tab.value !== "ads"),
    [canAccessAds],
  );

  const activeTabMeta =
    availableTabs.find((tab) => tab.value === activeTab) ?? availableTabs[0];

  const loadPaymentPlans = useCallback(async () => {
    setIsLoadingPlans(true);
    setLoadError(null);

    try {
      const [nextExtraFees, nextAdFees] = await Promise.all([
        getExtraFees(),
        canAccessAds ? getAdFees() : Promise.resolve<AdFeeResponse[]>([]),
      ]);

      setExtraFees(nextExtraFees);
      setAdFees(nextAdFees);
    } catch (error) {
      setLoadError(getErrorMessage(error, "Unable to load payment plans."));
    } finally {
      setIsLoadingPlans(false);
    }
  }, [canAccessAds]);

  useEffect(() => {
    void loadPaymentPlans();
  }, [loadPaymentPlans]);

  useEffect(() => {
    if (activeTab === "ads" && !canAccessAds) {
      setActiveTab("reviewer");
    }
  }, [activeTab, canAccessAds]);

  const plans = useMemo(() => {
    const extraFeePlans = extraFees
      .filter((fee) => fee.status !== false)
      .map(extraFeeToPlan)
      .filter((plan): plan is PaymentDisplayPlan => Boolean(plan));
    const adFeePlans = canAccessAds
      ? adFees.filter((fee) => fee.status !== false).map(adFeeToPlan)
      : [];

    return [...extraFeePlans, ...adFeePlans]
      .filter((plan) => plan.tab === activeTab)
      .sort((left, right) => left.priceVnd - right.priceVnd);
  }, [activeTab, adFees, canAccessAds, extraFees]);

  const openPaymentModal = useCallback((plan: PaymentDisplayPlan) => {
    setSelectedPlan(plan);
    setModalError(null);
  }, []);

  const closePaymentModal = useCallback(() => {
    if (isCreatingPayment) {
      return;
    }

    setSelectedPlan(null);
    setModalError(null);
  }, [isCreatingPayment]);

  const createExternalPayment = useCallback(
    async (method: CheckoutPaymentMethod) => {
      if (!selectedPlan) {
        return;
      }

      setActiveMethod(method);
      setIsCreatingPayment(true);
      setModalError(null);

      try {
        const payment = await createPayment({
          ...(selectedPlan.source === "extraFee"
            ? { extraFeeId: selectedPlan.extraFeeId }
            : { adFeeId: selectedPlan.adFeeId }),
          paymentMethod: method,
        });
        const paymentUrl = payment.paymentUrl?.trim();

        if (!paymentUrl) {
          setModalError("Payment URL was not returned by the server.");
          return;
        }

        const nextPaymentFlow: PaymentFlowState = {
          message: "Complete payment in the browser, then return here to check status.",
          method,
          payment,
          paymentId: payment.paymentId,
          plan: selectedPlan,
          status: "externalPaymentOpened",
        };

        setPaymentFlow(nextPaymentFlow);
        setSelectedPlan(null);

        try {
          await Linking.openURL(paymentUrl);
        } catch (openError) {
          setPaymentFlow({
            ...nextPaymentFlow,
            error: getErrorMessage(openError, "Unable to open payment URL."),
            message:
              "Payment was created, but the payment page could not be opened.",
            status: "failed",
          });
        }
      } catch (error) {
        setModalError(getErrorMessage(error, "Unable to create payment."));
      } finally {
        setActiveMethod(null);
        setIsCreatingPayment(false);
      }
    },
    [selectedPlan],
  );

  const checkPaymentStatus = useCallback(async () => {
    if (paymentFlow.status === "idle") {
      return;
    }

    setPaymentFlow({
      ...paymentFlow,
      error: null,
      message: "Checking payment status...",
      status: "checking",
    });

    try {
      const payment = await getPayment(paymentFlow.paymentId);

      if (isPaidStatus(payment.paymentStatus)) {
        await refreshCurrentUser();
        setPaymentFlow({
          ...paymentFlow,
          error: null,
          message: "Payment verified. Your account has been updated.",
          payment,
          status: "success",
        });
        return;
      }

      if (isPendingStatus(payment.paymentStatus)) {
        setPaymentFlow({
          ...paymentFlow,
          error: null,
          message: "Payment is still pending. Please check again after the provider finishes processing.",
          payment,
          status: "pending",
        });
        return;
      }

      setPaymentFlow({
        ...paymentFlow,
        error: null,
        message: `Payment status: ${payment.paymentStatus}`,
        payment,
        status: "failed",
      });
    } catch (error) {
      setPaymentFlow({
        ...paymentFlow,
        error: getErrorMessage(error, "Unable to verify payment."),
        message: "Payment verification failed.",
        status: "failed",
      });
    }
  }, [paymentFlow, refreshCurrentUser]);

  const openSuccessTarget = useCallback(() => {
    if (paymentFlow.status !== "success") {
      return;
    }

    const cafePageId = linkedCafePageId(user);

    if (paymentFlow.plan.tab === "ads") {
      navigation.navigate(routes.adsManager, {
        paymentId: paymentFlow.paymentId,
      });
      return;
    }

    if (paymentFlow.plan.tab === "cafe-page" && cafePageId) {
      navigation.navigate(routes.cafeDetail, {
        cafeId: cafePageId,
      });
      return;
    }

    navigation.navigate(routes.main, {
      screen: routes.profile,
    });
  }, [navigation, paymentFlow, user]);

  function renderPlanContent() {
    if (isLoadingPlans) {
      return (
        <View style={styles.stateCard}>
          <LoadingState label="Loading payment plans..." />
        </View>
      );
    }

    if (loadError) {
      return (
        <View style={styles.stateCard}>
          <EmptyState
            description="Pull plans from the backend again when the connection is ready."
            title={loadError}
          />
          <Button label="Retry" onPress={loadPaymentPlans} variant="outlined" />
        </View>
      );
    }

    if (!plans.length) {
      return (
        <View style={styles.stateCard}>
          <EmptyState
            description={
              activeTab === "ads"
                ? "No active ad fee packages are available for this account."
                : "No active package is available for this tab yet."
            }
            title="No packages"
          />
        </View>
      );
    }

    return (
      <>
        {activeTab === "ads" ? (
          <Button
            label="Open Ads Manager"
            onPress={() => navigation.navigate(routes.adsManager)}
            variant="outlined"
          />
        ) : null}
        {plans.map((plan) => (
          <PlanCard key={plan.id} onChoose={openPaymentModal} plan={plan} />
        ))}
      </>
    );
  }

  const isSuccess = paymentFlow.status === "success";

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
            {isSuccess
              ? "Payment completed"
              : activeTabMeta?.subtitle ?? "Choose a CafeStory package"}
          </Text>
        </View>
      </View>

      <ScrollView
        contentContainerStyle={[
          styles.planList,
          isSuccess && styles.successPlanList,
        ]}
        showsVerticalScrollIndicator={false}
      >
        {paymentFlow.status !== "idle" ? (
          <PaymentStatusCard
            cafePageId={linkedCafePageId(user)}
            flow={paymentFlow}
            onCheckStatus={checkPaymentStatus}
            onChooseAgain={() => {
              if (paymentFlow.status !== "checking") {
                setPaymentFlow({ status: "idle" });
                openPaymentModal(paymentFlow.plan);
              }
            }}
            onSuccessAction={openSuccessTarget}
          />
        ) : null}
        {!isSuccess ? renderPlanContent() : null}
      </ScrollView>

      {!isSuccess ? (
        <View
          style={[
            styles.tabsShell,
            { paddingBottom: Math.max(insets.bottom, 10) },
          ]}
        >
          <View style={styles.tabs}>
            {availableTabs.map((tab) => {
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
      ) : null}

      <PaymentMethodModal
        activeMethod={activeMethod}
        error={modalError}
        isCreatingPayment={isCreatingPayment}
        onClose={closePaymentModal}
        onPay={createExternalPayment}
        plan={selectedPlan}
      />
    </Screen>
  );
}

function PlanCard({
  onChoose,
  plan,
}: {
  onChoose: (plan: PaymentDisplayPlan) => void;
  plan: PaymentDisplayPlan;
}) {
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
        onPress={() => onChoose(plan)}
        variant={plan.highlighted ? "secondary" : "primary"}
      />
    </View>
  );
}

function PaymentMethodModal({
  activeMethod,
  error,
  isCreatingPayment,
  onClose,
  onPay,
  plan,
}: {
  activeMethod: CheckoutPaymentMethod | null;
  error: string | null;
  isCreatingPayment: boolean;
  onClose: () => void;
  onPay: (method: CheckoutPaymentMethod) => void;
  plan: PaymentDisplayPlan | null;
}) {
  return (
    <Modal
      animationType="fade"
      onRequestClose={onClose}
      transparent
      visible={Boolean(plan)}
    >
      <View style={styles.modalBackdrop}>
        <View style={styles.modalCard}>
          <View style={styles.modalHeader}>
            <View style={styles.modalTitleBlock}>
              <Text style={styles.modalTitle}>Choose payment method</Text>
              {plan ? (
                <Text style={styles.modalSubtitle}>
                  {plan.title} - {formatVnd(plan.priceVnd)}
                </Text>
              ) : null}
            </View>
            <Pressable
              accessibilityLabel="Close payment method"
              accessibilityRole="button"
              disabled={isCreatingPayment}
              onPress={onClose}
              style={({ pressed }) => [
                styles.modalCloseButton,
                pressed && styles.pressed,
              ]}
            >
              <X color={colors.foreground} size={22} strokeWidth={2.4} />
            </Pressable>
          </View>

          {plan ? (
            <View style={styles.modalSummary}>
              <Text style={styles.modalSummaryLabel}>Package duration</Text>
              <Text style={styles.modalSummaryValue}>{plan.durationLabel}</Text>
            </View>
          ) : null}

          {error ? (
            <View style={styles.errorBanner}>
              <AlertCircle color={colors.danger} size={18} strokeWidth={2.4} />
              <Text style={styles.errorText}>{error}</Text>
            </View>
          ) : null}

          <View style={styles.methodList}>
            {paymentMethods.map(({ Icon, description, label, method }) => {
              const isActive = activeMethod === method;

              return (
                <Pressable
                  accessibilityLabel={`Pay with ${label}`}
                  accessibilityRole="button"
                  disabled={isCreatingPayment}
                  key={method}
                  onPress={() => onPay(method)}
                  style={({ pressed }) => [
                    styles.methodItem,
                    pressed && !isCreatingPayment && styles.pressed,
                    isCreatingPayment && styles.disabled,
                  ]}
                >
                  <View style={styles.methodIcon}>
                    {isActive ? (
                      <ActivityIndicator color={colors.primary} />
                    ) : (
                      <Icon
                        color={colors.primary}
                        size={22}
                        strokeWidth={2.4}
                      />
                    )}
                  </View>
                  <View style={styles.methodCopy}>
                    <Text style={styles.methodTitle}>
                      {isActive ? `Redirecting to ${label}...` : label}
                    </Text>
                    <Text style={styles.methodDescription}>{description}</Text>
                  </View>
                </Pressable>
              );
            })}
          </View>
        </View>
      </View>
    </Modal>
  );
}

function PaymentStatusCard({
  cafePageId,
  flow,
  onCheckStatus,
  onChooseAgain,
  onSuccessAction,
}: {
  cafePageId: string | null;
  flow: Exclude<PaymentFlowState, { status: "idle" }>;
  onCheckStatus: () => void;
  onChooseAgain: () => void;
  onSuccessAction: () => void;
}) {
  const isChecking = flow.status === "checking";
  const isSuccess = flow.status === "success";
  const isFailure = flow.status === "failed";
  const Icon = isFailure ? AlertCircle : isSuccess ? CheckCircle2 : RefreshCw;

  return (
    <View
      style={[
        styles.statusCard,
        isSuccess && styles.successStatusCard,
        isFailure && styles.failedStatusCard,
      ]}
    >
      <View style={styles.statusHeader}>
        <View style={styles.statusIcon}>
          {isChecking ? (
            <ActivityIndicator color={colors.primary} />
          ) : (
            <Icon
              color={isFailure ? colors.danger : colors.primary}
              size={24}
              strokeWidth={2.6}
            />
          )}
        </View>
        <View style={styles.statusCopy}>
          <Text style={styles.statusTitle}>
            {isChecking
              ? "Checking payment..."
              : isSuccess
                ? "Payment successful"
                : isFailure
                  ? "Payment not completed"
                  : "Finish payment in browser"}
          </Text>
          <Text style={styles.statusDescription}>
            {flow.error ?? flow.message}
          </Text>
        </View>
      </View>

      <View style={styles.statusMeta}>
        <Text style={styles.statusMetaText}>{flow.plan.title}</Text>
        <Text style={styles.statusMetaText}>
          {formatVnd(flow.payment.amount ?? flow.plan.priceVnd)} -{" "}
          {flow.payment.paymentStatus}
        </Text>
      </View>

      {isSuccess ? (
        <Button
          label={
            flow.plan.tab === "ads"
              ? "Open Ads Manager"
              : flow.plan.tab === "cafe-page" && cafePageId
              ? "Open cafe page"
              : "Back to profile"
          }
          onPress={onSuccessAction}
        />
      ) : (
        <View style={styles.statusActions}>
          <Button
            isLoading={isChecking}
            label={isChecking ? "Checking..." : "Check status"}
            onPress={onCheckStatus}
          />
          {isFailure ? (
            <Button
              disabled={isChecking}
              label="Choose another method"
              onPress={onChooseAgain}
              variant="outlined"
            />
          ) : null}
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  activeTab: {
    backgroundColor: colors.primary,
    gap: spacing.xs,
    minWidth: 116,
  },
  activeTabText: {
    color: colors.white,
    fontSize: typography.caption,
    fontWeight: "800",
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
  disabled: {
    opacity: 0.65,
  },
  errorBanner: {
    alignItems: "flex-start",
    backgroundColor: colors.surfaceMuted,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    flexDirection: "row",
    gap: spacing.sm,
    padding: spacing.md,
  },
  errorText: {
    color: colors.danger,
    flex: 1,
    fontSize: typography.caption,
    fontWeight: "700",
    lineHeight: 18,
  },
  failedStatusCard: {
    borderColor: colors.border,
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
  methodCopy: {
    flex: 1,
    gap: spacing.xs,
  },
  methodDescription: {
    color: colors.muted,
    fontSize: typography.caption,
    lineHeight: 18,
  },
  methodIcon: {
    alignItems: "center",
    backgroundColor: colors.primarySoft,
    borderRadius: 18,
    height: 36,
    justifyContent: "center",
    width: 36,
  },
  methodItem: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    flexDirection: "row",
    gap: spacing.md,
    padding: spacing.md,
  },
  methodList: {
    gap: spacing.md,
  },
  methodTitle: {
    color: colors.foreground,
    fontSize: typography.label,
    fontWeight: "900",
  },
  modalBackdrop: {
    alignItems: "center",
    backgroundColor: "rgba(33,29,28,0.42)",
    flex: 1,
    justifyContent: "flex-end",
    padding: spacing.xl,
  },
  modalCard: {
    backgroundColor: colors.surface,
    borderRadius: 8,
    gap: spacing.lg,
    maxWidth: 520,
    padding: spacing.xl,
    width: "100%",
  },
  modalCloseButton: {
    alignItems: "center",
    height: 40,
    justifyContent: "center",
    width: 40,
  },
  modalHeader: {
    alignItems: "flex-start",
    flexDirection: "row",
    gap: spacing.md,
    justifyContent: "space-between",
  },
  modalSubtitle: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "700",
    lineHeight: 18,
  },
  modalSummary: {
    backgroundColor: colors.surfaceMuted,
    borderRadius: 8,
    gap: spacing.xs,
    padding: spacing.md,
  },
  modalSummaryLabel: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "800",
  },
  modalSummaryValue: {
    color: colors.foreground,
    fontSize: typography.label,
    fontWeight: "900",
  },
  modalTitle: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },
  modalTitleBlock: {
    flex: 1,
    gap: spacing.xs,
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
  stateCard: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    gap: spacing.md,
    minHeight: 220,
    overflow: "hidden",
    padding: spacing.lg,
  },
  statusActions: {
    gap: spacing.md,
  },
  statusCard: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    gap: spacing.lg,
    padding: spacing.xl,
  },
  statusCopy: {
    flex: 1,
    gap: spacing.xs,
  },
  statusDescription: {
    color: colors.muted,
    fontSize: typography.label,
    lineHeight: 21,
  },
  statusHeader: {
    alignItems: "flex-start",
    flexDirection: "row",
    gap: spacing.md,
  },
  statusIcon: {
    alignItems: "center",
    backgroundColor: colors.primarySoft,
    borderRadius: 22,
    height: 44,
    justifyContent: "center",
    width: 44,
  },
  statusMeta: {
    backgroundColor: colors.surfaceMuted,
    borderRadius: 8,
    gap: spacing.xs,
    padding: spacing.md,
  },
  statusMetaText: {
    color: colors.foreground,
    fontSize: typography.caption,
    fontWeight: "800",
  },
  statusTitle: {
    color: colors.foreground,
    fontSize: typography.body,
    fontWeight: "900",
  },
  subtitle: {
    color: colors.muted,
    fontSize: typography.caption,
    fontWeight: "700",
  },
  successPlanList: {
    paddingBottom: spacing.xl,
  },
  successStatusCard: {
    borderColor: colors.primarySoft,
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
