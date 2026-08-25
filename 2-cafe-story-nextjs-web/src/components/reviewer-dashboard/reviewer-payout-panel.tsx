import { WalletIcon } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import type { ReviewerPayout } from "@/features/reviewer-dashboard/reviewer-dashboard.types";
import { useI18n } from "@/components/providers/locale-provider";

type ReviewerPayoutPanelProps = {
  payouts: ReviewerPayout[];
};

const vndFormatter = new Intl.NumberFormat("vi-VN");

function formatVnd(value: number) {
  return `${vndFormatter.format(value)}đ`;
}

export function ReviewerPayoutPanel({ payouts }: ReviewerPayoutPanelProps) {
  const { t } = useI18n();
  const current = payouts[0];

  return (
    <Card className="p-5">
      <div className="flex items-center justify-between gap-4">
        <div>
          <p className="text-sm font-black text-muted">
            {t("reviewer.earnings.title")}
          </p>
          <h2 className="mt-1 text-2xl font-black text-espresso">
            {formatVnd(current.totalFinalAmount)}
          </h2>
        </div>
        <span className="grid size-12 place-items-center rounded-md bg-primary/10 text-primary">
          <WalletIcon className="size-6" />
        </span>
      </div>

      <div className="mt-5 grid gap-3 sm:grid-cols-3">
        <div className="rounded-md bg-background p-3">
          <p className="text-xs font-semibold text-muted">
            {t("reviewer.table.likeAmount")}
          </p>
          <p className="mt-1 font-black text-espresso">
            {formatVnd(current.likeAmount)}
          </p>
        </div>
        <div className="rounded-md bg-background p-3">
          <p className="text-xs font-semibold text-muted">
            {t("reviewer.table.shareAmount")}
          </p>
          <p className="mt-1 font-black text-espresso">
            {formatVnd(current.shareAmount)}
          </p>
        </div>
        <div className="rounded-md bg-background p-3">
          <p className="text-xs font-semibold text-muted">
            {t("reviewer.table.commentAmount")}
          </p>
          <p className="mt-1 font-black text-espresso">
            {formatVnd(current.commentAmount)}
          </p>
        </div>
      </div>

      <div className="mt-5 overflow-x-auto">
        <table className="w-full min-w-[520px] text-left text-sm">
          <thead className="text-xs uppercase text-muted">
            <tr>
              <th className="py-2 pr-3">{t("reviewer.table.month")}</th>
              <th className="py-2 pr-3">{t("reviewer.table.engagement")}</th>
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
                <td className="py-3 pr-3 text-muted">
                  {t("reviewer.earnings.engagementShort", {
                    likes: payout.likeCount,
                    shares: payout.shareCount,
                    comments: payout.commentCount,
                  })}
                </td>
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
    </Card>
  );
}
