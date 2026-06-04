import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import { ReviewerLeaderboardPanel } from "@/features/reviewer-dashboard/components/reviewer-leaderboard-panel";
import {
  mockReviewerProfile,
  mockReviewerRanking,
} from "@/features/reviewer-dashboard/reviewer-dashboard.mock";

const numberFormatter = new Intl.NumberFormat("en", {
  notation: "compact",
  maximumFractionDigits: 1,
});

export function ReviewerRankingDetail() {
  const current = mockReviewerRanking.find(
    (item) => item.reviewerId === mockReviewerProfile.reviewerId,
  );
  const maxScore = Math.max(...mockReviewerRanking.map((item) => item.score), 1);

  return (
    <div className="flex flex-col gap-6">
      <section>
        <p className="text-xs font-black uppercase tracking-[0.14em] text-primary">
          Ranking detail
        </p>
        <h2 className="mt-1 text-2xl font-black text-espresso">
          Leaderboard and score comparison
        </h2>
      </section>

      <section className="grid gap-6 xl:grid-cols-[340px_minmax(0,1fr)]">
        <ReviewerLeaderboardPanel
          profile={mockReviewerProfile}
          ranking={mockReviewerRanking}
        />
        <Card className="p-5">
          <p className="text-sm font-black text-muted">Current rank</p>
          <h3 className="mt-2 text-4xl font-black text-primary">
            #{current?.rank ?? "-"}
          </h3>
          <p className="mt-2 text-sm leading-6 text-coffee-muted">
            {mockReviewerProfile.name} is highlighted when the current reviewer
            appears in the ranking response.
          </p>
          <div className="mt-6 flex flex-col gap-3">
            {mockReviewerRanking.map((item) => (
              <div className="flex items-center gap-3" key={item.reviewerId}>
                <span className="w-10 text-sm font-black text-espresso">
                  #{item.rank}
                </span>
                <span className="h-3 flex-1 overflow-hidden rounded-full bg-surface-muted">
                  <span
                    className="block h-full rounded-full bg-primary"
                    style={{ width: `${(item.score / maxScore) * 100}%` }}
                  />
                </span>
                <span className="w-16 text-right text-sm font-black text-primary">
                  {numberFormatter.format(item.score)}
                </span>
              </div>
            ))}
          </div>
        </Card>
      </section>

      <Card className="p-5">
        <h3 className="text-xl font-black text-espresso">Full leaderboard</h3>
        <div className="mt-5 overflow-x-auto">
          <table className="w-full min-w-[760px] text-left text-sm">
            <thead className="text-xs uppercase text-muted">
              <tr>
                <th className="py-2 pr-3">Rank</th>
                <th className="py-2 pr-3">Badge</th>
                <th className="py-2 pr-3">Location</th>
                <th className="py-2 pr-3">Score</th>
                <th className="py-2 pr-3">Likes</th>
                <th className="py-2 pr-3">Shares</th>
                <th className="py-2">Comments</th>
              </tr>
            </thead>
            <tbody>
              {mockReviewerRanking.map((item) => {
                const isCurrent =
                  item.reviewerId === mockReviewerProfile.reviewerId;

                return (
                  <tr
                    className={`border-t border-line-soft ${
                      isCurrent ? "bg-primary/10" : ""
                    }`}
                    key={item.reviewerId}
                  >
                    <td className="py-3 pr-3 font-black text-espresso">
                      #{item.rank}
                    </td>
                    <td className="py-3 pr-3">
                      <Badge variant={isCurrent ? "default" : "secondary"}>
                        {isCurrent ? "You" : item.badge}
                      </Badge>
                    </td>
                    <td className="py-3 pr-3 text-muted">{item.location}</td>
                    <td className="py-3 pr-3 font-black text-primary">
                      {item.score}
                    </td>
                    <td className="py-3 pr-3 text-muted">{item.likeCount}</td>
                    <td className="py-3 pr-3 text-muted">{item.shareCount}</td>
                    <td className="py-3 text-muted">{item.commentCount}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
}
