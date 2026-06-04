import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import { ReviewerBadgeProgress } from "@/features/reviewer-dashboard/components/reviewer-badge-progress";
import {
  mockReviewerBadges,
  mockReviewerProfile,
} from "@/features/reviewer-dashboard/reviewer-dashboard.mock";
import type { ReviewerBadge } from "@/features/reviewer-dashboard/reviewer-dashboard.types";

const badgeRules: Array<{ badge: ReviewerBadge; range: string }> = [
  { badge: "IRON", range: "0-99" },
  { badge: "BRONZE", range: "100-299" },
  { badge: "SILVER", range: "300-699" },
  { badge: "GOLD", range: "700-1499" },
  { badge: "DIAMOND", range: "1500+" },
];

export function ReviewerBadgesDetail() {
  const maxScore = Math.max(...mockReviewerBadges.map((item) => item.score), 1);

  return (
    <div className="flex flex-col gap-6">
      <section>
        <p className="text-xs font-black uppercase tracking-[0.14em] text-primary">
          Badge detail
        </p>
        <h2 className="mt-1 text-2xl font-black text-espresso">
          Badge progress and history
        </h2>
      </section>

      <section className="grid gap-6 xl:grid-cols-[420px_minmax(0,1fr)]">
        <ReviewerBadgeProgress
          badges={mockReviewerBadges}
          profile={mockReviewerProfile}
        />
        <Card className="p-5">
          <p className="text-sm font-black text-muted">Score by month</p>
          <div className="mt-6 flex h-72 items-end gap-4 rounded-md bg-surface-muted/55 p-4">
            {mockReviewerBadges
              .slice()
              .reverse()
              .map((item) => (
                <div className="flex min-w-0 flex-1 flex-col items-center gap-3" key={item.id}>
                  <div className="flex h-52 w-full items-end rounded-md bg-background">
                    <div
                      className="w-full rounded-md bg-primary"
                      style={{ height: `${(item.score / maxScore) * 100}%` }}
                    />
                  </div>
                  <div className="text-center">
                    <p className="text-xs font-black text-espresso">
                      {item.month.slice(5)}
                    </p>
                    <p className="text-xs font-semibold text-muted">
                      {item.score}
                    </p>
                  </div>
                </div>
              ))}
          </div>
        </Card>
      </section>

      <section className="grid gap-6 xl:grid-cols-[320px_minmax(0,1fr)]">
        <Card className="p-5">
          <h3 className="text-xl font-black text-espresso">Badge rules</h3>
          <div className="mt-5 flex flex-col gap-3">
            {badgeRules.map((rule) => (
              <div
                className="flex items-center justify-between gap-3 rounded-md bg-background px-3 py-3"
                key={rule.badge}
              >
                <Badge
                  variant={rule.badge === mockReviewerProfile.badge ? "default" : "secondary"}
                >
                  {rule.badge}
                </Badge>
                <p className="text-sm font-black text-muted">{rule.range}</p>
              </div>
            ))}
          </div>
        </Card>

        <Card className="p-5">
          <h3 className="text-xl font-black text-espresso">Badge timeline</h3>
          <div className="mt-5 overflow-x-auto">
            <table className="w-full min-w-[720px] text-left text-sm">
              <thead className="text-xs uppercase text-muted">
                <tr>
                  <th className="py-2 pr-3">Month</th>
                  <th className="py-2 pr-3">Score</th>
                  <th className="py-2 pr-3">Badge</th>
                  <th className="py-2 pr-3">Likes</th>
                  <th className="py-2 pr-3">Shares</th>
                  <th className="py-2">Comments</th>
                </tr>
              </thead>
              <tbody>
                {mockReviewerBadges.map((item) => (
                  <tr className="border-t border-line-soft" key={item.id}>
                    <td className="py-3 pr-3 font-black text-espresso">
                      {item.month}
                    </td>
                    <td className="py-3 pr-3 font-black text-primary">
                      {item.score}
                    </td>
                    <td className="py-3 pr-3">
                      <Badge variant="secondary">{item.badge}</Badge>
                    </td>
                    <td className="py-3 pr-3 text-muted">{item.likeCount}</td>
                    <td className="py-3 pr-3 text-muted">{item.shareCount}</td>
                    <td className="py-3 text-muted">{item.commentCount}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      </section>
    </div>
  );
}
