import { PageShell } from "@/components/layout/page-shell";
import { PaymentReturnStatus } from "@/components/payment/payment-return-status";
import { getServerTranslator } from "@/lib/i18n/server";

export default async function LegacyStripePaymentSuccessPage({
  searchParams,
}: {
  searchParams: Promise<{ paymentId?: string | string[] }>;
}) {
  const t = await getServerTranslator();
  const params = await searchParams;
  const paymentId = Array.isArray(params.paymentId)
    ? params.paymentId[0]
    : params.paymentId;

  return (
    <PageShell
      title={t("payments.status.title")}
      description={t("payments.status.stripeDescription")}
    >
      <PaymentReturnStatus flow="stripe" paymentId={paymentId} />
    </PageShell>
  );
}
