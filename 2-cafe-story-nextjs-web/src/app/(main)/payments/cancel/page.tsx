import Link from "next/link";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { buttonVariants } from "@/components/ui/button";
import { PageShell } from "@/components/layout/page-shell";
import { cn } from "@/lib/utils";

export default function PaymentCancelPage() {
  return (
    <PageShell description="No charge was completed. Your pending session can safely expire." title="Payment cancelled">
      <Alert>
        <AlertTitle>Checkout was cancelled</AlertTitle>
        <AlertDescription>Return to Ads when you are ready to try again.</AlertDescription>
      </Alert>
      <Link className={cn(buttonVariants(), "mt-4 no-underline")} href="/ads">
        Return to Ads
      </Link>
    </PageShell>
  );
}
