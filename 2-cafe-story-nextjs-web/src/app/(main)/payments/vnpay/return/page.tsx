import { PageShell } from "@/components/layout/page-shell";
import { PaymentReturnStatus } from "@/components/payment/payment-return-status";
import { getServerTranslator } from "@/lib/i18n/server";

type VnpaySearchParams = Promise<{
  [key: string]: string | string[] | undefined;
}>;

export default async function VnpayPaymentReturnPage({
  searchParams,
}: {
  searchParams: VnpaySearchParams;
}) {
  const t = await getServerTranslator();
  const params = await searchParams;

  return (
    <PageShell
      title={t("payments.status.title")}
      description={t("payments.status.vnpayDescription")}
    >
      <PaymentReturnStatus flow="vnpay" searchParams={params} />
    </PageShell>
  );
}
