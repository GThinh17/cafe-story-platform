export type PaymentPlanTab = "reviewer" | "cafe-page" | "ads";

export type PaymentPlan = {
  badge?: string;
  billingLabel: string;
  ctaLabel: string;
  durationLabel: string;
  features: string[];
  highlighted?: boolean;
  id: string;
  note?: string;
  priceVnd: number;
  subtitle: string;
  tab: PaymentPlanTab;
  title: string;
};
