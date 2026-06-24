"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { CheckCircle2, Loader2 } from "lucide-react";
import { syncConnectStatus } from "@/lib/api/reviewers";

export default function StripeConnectReturnPage() {
  const router = useRouter();
  const [syncing, setSyncing] = useState(true);

  useEffect(() => {
    let cancelled = false;

    syncConnectStatus()
      .catch(() => {/* sync failed — redirect anyway */})
      .finally(() => {
        if (!cancelled) {
          setSyncing(false);
          setTimeout(() => {
            router.replace("/reviewer-dashboard");
          }, 1500);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [router]);

  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center gap-4 text-center">
      <span className="grid size-14 place-items-center rounded-full bg-green-500/10 text-green-500">
        {syncing ? (
          <Loader2 className="size-7 animate-spin" />
        ) : (
          <CheckCircle2 className="size-7" />
        )}
      </span>
      <h1 className="text-xl font-black text-espresso">Stripe account connected</h1>
      <p className="text-sm text-muted-foreground">
        {syncing ? "Syncing account status..." : "Returning to dashboard..."}
      </p>
    </div>
  );
}
