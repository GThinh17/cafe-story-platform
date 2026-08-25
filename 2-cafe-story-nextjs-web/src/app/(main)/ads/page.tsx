import { AdsDashboard } from "@/components/ads/ads-dashboard";
import { PageShell } from "@/components/layout/page-shell";
import { getServerTranslator } from "@/lib/i18n/server";

type AdsSearchParams = Promise<{
  checkout?: string | string[];
  paymentId?: string | string[];
}>;

export default async function AdsPage({ searchParams }: { searchParams: AdsSearchParams }) {
  const t = await getServerTranslator();
  const params = await searchParams;
  const paymentId = Array.isArray(params.paymentId) ? params.paymentId[0] : params.paymentId;
  const checkout = Array.isArray(params.checkout) ? params.checkout[0] : params.checkout;

  return (
    <PageShell
      description={t("ads.page.description")}
      title={t("ads.page.title")}
    >
      <AdsDashboard checkoutStatus={checkout} initialPaymentId={paymentId} />
    </PageShell>
  );
}
