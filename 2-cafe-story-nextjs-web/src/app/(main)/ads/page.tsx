import { AdsDashboard } from "@/components/ads/ads-dashboard";
import { PageShell } from "@/components/layout/page-shell";

type AdsSearchParams = Promise<{
  checkout?: string | string[];
  paymentId?: string | string[];
}>;

export default async function AdsPage({ searchParams }: { searchParams: AdsSearchParams }) {
  const params = await searchParams;
  const paymentId = Array.isArray(params.paymentId) ? params.paymentId[0] : params.paymentId;
  const checkout = Array.isArray(params.checkout) ? params.checkout[0] : params.checkout;

  return (
    <PageShell
      description="Purchase a fixed package, create sponsored campaigns, and track served impressions and clicks."
      title="CafeStory Ads"
    >
      <AdsDashboard checkoutStatus={checkout} initialPaymentId={paymentId} />
    </PageShell>
  );
}
