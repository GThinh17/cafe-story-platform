import {
  BadgeIcon,
  HeartIcon,
  Repeat2Icon,
  TrendingUpIcon,
  WalletIcon,
} from "lucide-react";
import { Card } from "@/components/ui/card";
import type { ReviewerActivity } from "@/features/reviewer-dashboard/reviewer-dashboard.types";

type ReviewerRecentActivityPanelProps = {
  activities: ReviewerActivity[];
};

const activityIcons = {
  badge: BadgeIcon,
  like: HeartIcon,
  payout: WalletIcon,
  ranking: TrendingUpIcon,
  share: Repeat2Icon,
};

export function ReviewerRecentActivityPanel({
  activities,
}: ReviewerRecentActivityPanelProps) {
  return (
    <Card className="p-5">
      <p className="text-sm font-black text-muted">Recent activity</p>
      <h2 className="mt-1 text-xl font-black text-espresso">Latest signals</h2>

      {activities.length === 0 ? (
        <p className="mt-4 text-sm text-muted-foreground">No activity yet.</p>
      ) : (
        <div className="mt-5 flex flex-col gap-4">
          {activities.map((activity) => {
            const Icon = activityIcons[activity.type];

            return (
              <article className="flex gap-3" key={activity.id}>
                <span className="grid size-10 shrink-0 place-items-center rounded-md bg-surface-muted text-primary">
                  <Icon className="size-5" />
                </span>
                <div className="min-w-0">
                  <p className="text-sm font-black text-espresso">
                    {activity.title}
                  </p>
                  <p className="mt-1 text-sm leading-6 text-coffee-muted">
                    {activity.description}
                  </p>
                  <p className="mt-1 text-xs font-semibold text-muted">
                    {activity.time}
                  </p>
                </div>
              </article>
            );
          })}
        </div>
      )}
    </Card>
  );
}
