import Link from "next/link";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { buttonVariants } from "@/components/ui/button";
import { PageShell } from "@/components/layout/page-shell";
import { getServerTranslator } from "@/lib/i18n/server";
import { cn } from "@/lib/utils";

export default async function PaymentCancelPage() {
  const t = await getServerTranslator();

  return (
    <PageShell
      description={t("payments.cancel.description")}
      title={t("payments.cancel.title")}
    >
      <Alert>
        <AlertTitle>{t("payments.cancel.alertTitle")}</AlertTitle>
        <AlertDescription>
          {t("payments.cancel.alertDescription")}
        </AlertDescription>
      </Alert>
      <Link className={cn(buttonVariants(), "mt-4 no-underline")} href="/ads">
        {t("payments.cancel.returnToAds")}
      </Link>
    </PageShell>
  );
}
