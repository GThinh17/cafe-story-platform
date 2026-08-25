"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import { Loader2 } from "lucide-react";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { ReviewerPayoutPanel } from "@/components/reviewer-dashboard/reviewer-payout-panel";
import type { ReviewerPayout } from "@/features/reviewer-dashboard/reviewer-dashboard.types";
import { useCurrentUser } from "@/hooks/use-current-user";
import { ApiError } from "@/lib/api/client";
import { getReviewerByUserId, getReviewerEarnings } from "@/lib/api/reviewers";
import { mockReviewerPayouts } from "@/mocks/reviewer-earnings";
import { useI18n } from "@/components/providers/locale-provider";

/**
 * Screenshot escape hatch: the monthly payout job has not run for every
 * reviewer yet, so the page is empty on demo accounts. With
 * NEXT_PUBLIC_ENABLE_DEMO_DATA=true the page falls back to `mockReviewerPayouts`
 * only when the API returned nothing — real payouts always win.
 */
const isDemoDataEnabled = process.env.NEXT_PUBLIC_ENABLE_DEMO_DATA === "true";

/**
 * Ba trạng thái tách bạch. Trước đây mọi lỗi đều bị `catch {}` nuốt nên 403,
 * 404 và 500 hiển thị y hệt "chưa có dữ liệu" — không cách nào chẩn đoán.
 */
type LoadState =
  | { kind: "loading" }
  | { kind: "ready" }
  | { kind: "not-reviewer" }
  | { kind: "error"; message: string };

const vndFormatter = new Intl.NumberFormat("vi-VN");

function formatVnd(value: number) {
  return `${vndFormatter.format(value)}đ`;
}

function withDemoFallback(payouts: ReviewerPayout[]) {
  return payouts.length === 0 && isDemoDataEnabled ? mockReviewerPayouts : payouts;
}

export function ReviewerEarningsDetail() {
  const { t } = useI18n();
  const { user } = useCurrentUser();
  const [payouts, setPayouts] = useState<ReviewerPayout[]>([]);
  const [state, setState] = useState<LoadState>({ kind: "loading" });

  const loadData = useCallback(
    async (userId: string) => {
      setState({ kind: "loading" });
      try {
        const reviewer = await getReviewerByUserId(userId);
        const data = await getReviewerEarnings(reviewer.reviewerId);
        setPayouts(withDemoFallback(data ?? []));
        setState({ kind: "ready" });
      } catch (loadError) {
        const fallback = withDemoFallback([]);
        if (fallback.length > 0) {
          setPayouts(fallback);
          setState({ kind: "ready" });
          return;
        }

        // 404 ở bước getReviewerByUserId nghĩa là user chưa mua gói reviewer —
        // đó là trạng thái hợp lệ, không phải sự cố.
        if (loadError instanceof ApiError && loadError.statusCode === 404) {
          setState({ kind: "not-reviewer" });
          return;
        }

        setState({
          kind: "error",
          message:
            loadError instanceof Error
              ? loadError.message
              : t("reviewer.earnings.loadError"),
        });
      }
    },
    [t],
  );

  useEffect(() => {
    if (user?.userId) {
      void loadData(user.userId);
      return;
    }

    const fallback = withDemoFallback([]);
    setPayouts(fallback);
    setState(fallback.length > 0 ? { kind: "ready" } : { kind: "not-reviewer" });
  }, [user?.userId, loadData]);

  const header = (
    <section>
      <p className="text-xs font-black uppercase tracking-[0.14em] text-primary">
        {t("reviewer.earnings.eyebrow")}
      </p>
      <h2 className="mt-1 text-2xl font-black text-espresso">
        {t("reviewer.earnings.subtitle")}
      </h2>
    </section>
  );

  if (state.kind === "loading") {
    return (
      <div className="flex items-center justify-center py-16">
        <Loader2 className="size-6 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (state.kind === "error") {
    return (
      <div className="flex flex-col gap-6">
        {header}
        <Alert variant="destructive">
          <AlertTitle>{t("reviewer.earnings.loadError")}</AlertTitle>
          <AlertDescription className="flex flex-col items-start gap-3">
            <span>{state.message}</span>
            <Button
              onClick={() => user?.userId && void loadData(user.userId)}
              size="sm"
              type="button"
              variant="outline"
            >
              {t("common.retry")}
            </Button>
          </AlertDescription>
        </Alert>
      </div>
    );
  }

  if (state.kind === "not-reviewer") {
    return (
      <div className="flex flex-col gap-6">
        {header}
        <Card className="flex flex-col items-center gap-4 p-10 text-center text-sm text-muted-foreground">
          <span>{t("reviewer.earnings.notReviewer")}</span>
          <Button asChild size="sm" type="button" variant="outline">
            <Link href="/reviewer-dashboard">{t("reviewer.earnings.viewPlans")}</Link>
          </Button>
        </Card>
      </div>
    );
  }

  const current = payouts[0] ?? null;
  const allTimeTotal = payouts.reduce((total, p) => total + p.totalFinalAmount, 0);
  const totalLikeAmount = payouts.reduce((total, p) => total + p.likeAmount, 0);
  const totalShareAmount = payouts.reduce((total, p) => total + p.shareAmount, 0);
  const totalCommentAmount = payouts.reduce((total, p) => total + p.commentAmount, 0);
  const maxAmount = Math.max(...payouts.map((p) => p.totalFinalAmount), 1);

  return (
    <div className="flex flex-col gap-6">
      {header}

      {payouts.length === 0 ? (
        <Card className="flex items-center justify-center p-10 text-sm text-muted-foreground">
          {t("reviewer.earnings.noPayouts")}
        </Card>
      ) : (
        <>
          <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            <Card className="p-5">
              <p className="text-sm font-black text-muted">
                {t("reviewer.earnings.latest")}
              </p>
              <p className="mt-3 text-3xl font-black text-primary">
                {current ? formatVnd(current.totalFinalAmount) : "—"}
              </p>
            </Card>
            <Card className="p-5">
              <p className="text-sm font-black text-muted">
                {t("reviewer.earnings.allTime")}
              </p>
              <p className="mt-3 text-3xl font-black text-espresso">
                {formatVnd(allTimeTotal)}
              </p>
            </Card>
            <Card className="p-5 md:col-span-2">
              <p className="text-sm font-black text-muted">
                {t("reviewer.earnings.breakdown")}
              </p>
              <div className="mt-4 grid gap-3 sm:grid-cols-3">
                <div className="rounded-md bg-background p-3">
                  <p className="text-xs font-semibold text-muted">
                    {t("reviewer.table.likeAmount")}
                  </p>
                  <p className="mt-1 font-black text-espresso">
                    {formatVnd(totalLikeAmount)}
                  </p>
                </div>
                <div className="rounded-md bg-background p-3">
                  <p className="text-xs font-semibold text-muted">
                    {t("reviewer.table.shareAmount")}
                  </p>
                  <p className="mt-1 font-black text-espresso">
                    {formatVnd(totalShareAmount)}
                  </p>
                </div>
                <div className="rounded-md bg-background p-3">
                  <p className="text-xs font-semibold text-muted">
                    {t("reviewer.table.commentAmount")}
                  </p>
                  <p className="mt-1 font-black text-espresso">
                    {formatVnd(totalCommentAmount)}
                  </p>
                </div>
              </div>
            </Card>
          </section>

          <section className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_420px]">
            <ReviewerPayoutPanel payouts={payouts} />
            <Card className="p-5">
              <p className="text-sm font-black text-muted">
                {t("reviewer.earnings.monthlyChart")}
              </p>
              <div className="mt-6 flex h-72 items-end gap-4 rounded-md bg-surface-muted/55 p-4">
                {[...payouts].reverse().map((item) => (
                  <div className="flex min-w-0 flex-1 flex-col items-center gap-3" key={item.id}>
                    <div className="flex h-52 w-full items-end rounded-md bg-background">
                      <div
                        className="w-full rounded-md bg-primary"
                        style={{ height: `${(item.totalFinalAmount / maxAmount) * 100}%` }}
                      />
                    </div>
                    <div className="text-center">
                      <p className="text-xs font-black text-espresso">
                        {item.payoutMonth.slice(5)}
                      </p>
                      <p className="text-xs font-semibold text-muted">
                        {formatVnd(item.totalFinalAmount)}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            </Card>
          </section>

          <Card className="p-5">
            <h3 className="text-xl font-black text-espresso">
              {t("reviewer.earnings.history")}
            </h3>
            <div className="mt-5 overflow-x-auto">
              <table className="w-full min-w-[1040px] text-left text-sm">
                <thead className="text-xs uppercase text-muted">
                  <tr>
                    <th className="py-2 pr-3">{t("reviewer.table.month")}</th>
                    <th className="py-2 pr-3">{t("reviewer.table.likes")}</th>
                    <th className="py-2 pr-3">{t("reviewer.table.shares")}</th>
                    <th className="py-2 pr-3">{t("reviewer.table.comments")}</th>
                    <th className="py-2 pr-3">{t("reviewer.table.likeAmount")}</th>
                    <th className="py-2 pr-3">{t("reviewer.table.shareAmount")}</th>
                    <th className="py-2 pr-3">
                      {t("reviewer.table.commentAmount")}
                    </th>
                    <th className="py-2 pr-3">{t("reviewer.table.base")}</th>
                    <th className="py-2 pr-3">{t("reviewer.table.badge")}</th>
                    <th className="py-2 pr-3">{t("reviewer.table.multiplier")}</th>
                    <th className="py-2 pr-3">{t("reviewer.table.total")}</th>
                    <th className="py-2">{t("reviewer.table.status")}</th>
                  </tr>
                </thead>
                <tbody>
                  {payouts.map((payout) => (
                    <tr className="border-t border-line-soft" key={payout.id}>
                      <td className="py-3 pr-3 font-black text-espresso">
                        {payout.payoutMonth}
                      </td>
                      <td className="py-3 pr-3 text-muted">{payout.likeCount}</td>
                      <td className="py-3 pr-3 text-muted">{payout.shareCount}</td>
                      <td className="py-3 pr-3 text-muted">{payout.commentCount}</td>
                      <td className="py-3 pr-3 text-muted">{formatVnd(payout.likeAmount)}</td>
                      <td className="py-3 pr-3 text-muted">{formatVnd(payout.shareAmount)}</td>
                      <td className="py-3 pr-3 text-muted">{formatVnd(payout.commentAmount)}</td>
                      <td className="py-3 pr-3 text-muted">
                        {formatVnd(payout.totalBaseAmount)}
                      </td>
                      <td className="py-3 pr-3">
                        <Badge variant="secondary">{payout.badge ?? "—"}</Badge>
                      </td>
                      <td className="py-3 pr-3 text-muted">×{payout.badgeMultiplier}</td>
                      <td className="py-3 pr-3 font-black text-primary">
                        {formatVnd(payout.totalFinalAmount)}
                      </td>
                      <td className="py-3">
                        <Badge variant="outline">{payout.status}</Badge>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {current && current.status !== "PAID" ? (
              <p className="mt-4 text-xs text-muted">
                {t("reviewer.earnings.pendingNote")}
              </p>
            ) : null}
          </Card>
        </>
      )}
    </div>
  );
}
