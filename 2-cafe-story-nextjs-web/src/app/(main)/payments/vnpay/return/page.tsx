import { PageShell } from "@/components/layout/page-shell";
import { PaymentReturnStatus } from "@/components/payment/payment-return-status";

type VnpaySearchParams = Promise<{
  [key: string]: string | string[] | undefined;
}>;

export default async function VnpayPaymentReturnPage({
  searchParams,
}: {
  searchParams: VnpaySearchParams;
}) {
  const params = await searchParams;

  return (
    <PageShell title="Payment status" description="Verifying your VNPAY payment.">
      <PaymentReturnStatus flow="vnpay" searchParams={params} />
    </PageShell>
  );
}
