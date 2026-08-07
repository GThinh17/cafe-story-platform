"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { AlertTriangle, CheckCircle2, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useI18n } from "@/components/providers/locale-provider";
import { syncConnectStatus } from "@/lib/api/reviewers";

type SyncState = "syncing" | "done" | "failed";

export default function StripeConnectReturnPage() {
  const router = useRouter();
  const { t } = useI18n();
  const [state, setState] = useState<SyncState>("syncing");

  useEffect(() => {
    let cancelled = false;
    let redirectTimer: ReturnType<typeof setTimeout> | undefined;

    syncConnectStatus()
      .then(() => {
        if (cancelled) return;
        setState("done");
        redirectTimer = setTimeout(() => {
          router.replace("/reviewer-dashboard");
        }, 1500);
      })
      .catch(() => {
        // Sync hỏng nghĩa là ta KHÔNG biết onboarding đã xong hay chưa. Trước
        // đây chỗ này nuốt lỗi rồi vẫn hiện dấu tích xanh — người dùng tưởng
        // đã kết nối xong trong khi payouts_enabled vẫn false.
        if (!cancelled) setState("failed");
      });

    return () => {
      cancelled = true;
      if (redirectTimer) clearTimeout(redirectTimer);
    };
  }, [router]);

  if (state === "failed") {
    return (
      <div className="flex min-h-[60vh] flex-col items-center justify-center gap-4 text-center">
        <span className="grid size-14 place-items-center rounded-full bg-accent/10 text-accent">
          <AlertTriangle className="size-7" />
        </span>
        <h1 className="text-xl font-black text-espresso">
          {t("connect.return.failedTitle")}
        </h1>
        <p className="max-w-md text-sm text-muted-foreground">
          {t("connect.return.failedBody")}
        </p>
        <Button
          onClick={() => router.replace("/reviewer-dashboard")}
          type="button"
          variant="outline"
        >
          {t("connect.return.backToDashboard")}
        </Button>
      </div>
    );
  }

  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center gap-4 text-center">
      <span className="grid size-14 place-items-center rounded-full bg-green-500/10 text-green-500">
        {state === "syncing" ? (
          <Loader2 className="size-7 animate-spin" />
        ) : (
          <CheckCircle2 className="size-7" />
        )}
      </span>
      <h1 className="text-xl font-black text-espresso">
        {t("connect.return.title")}
      </h1>
      <p className="text-sm text-muted-foreground">
        {state === "syncing"
          ? t("connect.return.syncing")
          : t("connect.return.redirecting")}
      </p>
    </div>
  );
}
