"use client";

import { useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { AlertTriangle, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useI18n } from "@/components/providers/locale-provider";
import { refreshOnboardingLink } from "@/lib/api/reviewers";

/**
 * Stripe đá người dùng về đây khi account link hết hạn giữa chừng.
 *
 * Trước đây refresh-url trỏ về trang return, mà trang đó chỉ sync trạng thái
 * rồi quay lại dashboard — người dùng không bao giờ khai báo xong được. Trang
 * này xin link mới rồi đẩy thẳng trở lại Stripe.
 */
export default function StripeConnectRefreshPage() {
  const router = useRouter();
  const { t } = useI18n();
  const [failed, setFailed] = useState(false);

  const requestNewLink = useCallback(async () => {
    setFailed(false);
    try {
      const result = await refreshOnboardingLink();
      window.location.href = result.onboardingUrl;
    } catch {
      setFailed(true);
    }
  }, []);

  useEffect(() => {
    void requestNewLink();
  }, [requestNewLink]);

  if (failed) {
    return (
      <div className="flex min-h-[60vh] flex-col items-center justify-center gap-4 text-center">
        <span className="grid size-14 place-items-center rounded-full bg-accent/10 text-accent">
          <AlertTriangle className="size-7" />
        </span>
        <h1 className="text-xl font-black text-espresso">
          {t("connect.refresh.failedTitle")}
        </h1>
        <div className="flex gap-2">
          <Button onClick={() => void requestNewLink()} type="button">
            {t("connect.refresh.retry")}
          </Button>
          <Button
            onClick={() => router.replace("/reviewer-dashboard")}
            type="button"
            variant="outline"
          >
            {t("connect.return.backToDashboard")}
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center gap-4 text-center">
      <span className="grid size-14 place-items-center rounded-full bg-surface-muted text-muted-foreground">
        <Loader2 className="size-7 animate-spin" />
      </span>
      <h1 className="text-xl font-black text-espresso">
        {t("connect.refresh.title")}
      </h1>
      <p className="text-sm text-muted-foreground">{t("connect.refresh.body")}</p>
    </div>
  );
}
