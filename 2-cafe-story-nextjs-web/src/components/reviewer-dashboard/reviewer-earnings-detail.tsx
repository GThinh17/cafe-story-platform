"use client";

import { useCallback, useEffect, useState } from "react";
import { Loader2 } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import { ReviewerPayoutPanel } from "@/components/reviewer-dashboard/reviewer-payout-panel";
import type { ReviewerPayout } from "@/features/reviewer-dashboard/reviewer-dashboard.types";
import { useCurrentUser } from "@/hooks/use-current-user";
import { getReviewerByUserId, getReviewerPayouts } from "@/lib/api/reviewers";
import { useI18n } from "@/components/providers/locale-provider";

const vndFormatter = new Intl.NumberFormat("vi-VN");

function formatVnd(value: number) {
  return `${vndFormatter.format(value)}đ`;
}

export function ReviewerEarningsDetail() {
  const { t } = useI18n();
  const { user } = useCurrentUser();
  const [payouts, setPayouts] = useState<ReviewerPayout[]>([]);
  const [loading, setLoading] = useState(true);

  const loadData = useCallback(async (userId: string) => {
    try {
      const reviewer = await getReviewerByUserId(userId);
      const data = await getReviewerPayouts(reviewer.reviewerId);
      setPayouts(data ?? []);
    } catch {
      // reviewer not found
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (user?.userId) void loadData(user.userId);
    else setLoading(false);
  }, [user?.userId, loadData]);

  if (loading) {
    return (
      <div className="flex items-center justify-center py-16">
        <Loader2 className="size-6 animate-spin text-muted-foreground" />
      </div>
    );
  }

  const current = payouts[0] ?? null;
  const allTimeTotal = payouts.reduce((total, p) => total + p.totalAmount, 0);
  const totalLikeAmount = payouts.reduce((total, p) => total + p.likeAmount, 0);
  const totalShareAmount = payouts.reduce((total, p) => total + p.shareAmount, 0);
  const totalCommentAmount = payouts.reduce((total, p) => total + p.commentAmount, 0);
  const maxAmount = Math.max(...payouts.map((p) => p.totalAmount), 1);

  return (
    <div className="flex flex-col gap-6">
      <section>
        <p className="text-xs font-black uppercase tracking-[0.14em] text-primary">
          {t("reviewer.earnings.eyebrow")}
        </p>
        <h2 className="mt-1 text-2xl font-black text-espresso">
          {t("reviewer.earnings.subtitle")}
        </h2>
      </section>

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
                {current ? formatVnd(current.totalAmount) : "—"}
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
                        style={{ height: `${(item.totalAmount / maxAmount) * 100}%` }}
                      />
                    </div>
                    <div className="text-center">
                      <p className="text-xs font-black text-espresso">
                        {item.payoutMonth.slice(5)}
                      </p>
                      <p className="text-xs font-semibold text-muted">
                        {formatVnd(item.totalAmount)}
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
              <table className="w-full min-w-[920px] text-left text-sm">
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
                      <td className="py-3 pr-3 font-black text-primary">
                        {formatVnd(payout.totalAmount)}
                      </td>
                      <td className="py-3">
                        <Badge variant="outline">{payout.payoutStatus}</Badge>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </Card>
        </>
      )}
    </div>
  );
}
