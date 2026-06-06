import { PageShell } from "@/components/layout/page-shell";
import { PaymentReturnStatus } from "@/components/payment/payment-return-status";

export default async function LegacyStripePaymentSuccessPage({
  searchParams,
}: {
  searchParams: Promise<{ paymentId?: string | string[] }>;
}) {
  const params = await searchParams;
  const paymentId = Array.isArray(params.paymentId)
    ? params.paymentId[0]
    : params.paymentId;

  return (
    <PageShell title="Payment status" description="Verifying your Stripe payment.">
      <PaymentReturnStatus flow="stripe" paymentId={paymentId} />
    </PageShell>
  );
}
