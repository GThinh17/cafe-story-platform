import { SparklesIcon } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import type { ReviewerSegmentItem } from "@/features/reviewer-dashboard/reviewer-dashboard.types";

type ReviewerSegmentCardProps = {
  segment: ReviewerSegmentItem;
};

const segmentDescriptions = {
  active: "You are maintaining steady engagement in the community.",
  elite: "You are leading the reviewer community.",
  inactive: "Start posting again to rebuild reviewer momentum.",
  new: "You are building your first visible reviewer signals.",
  strong: "Your content is showing strong influence.",
  top: "You are among the standout reviewers.",
};

export function ReviewerSegmentCard({ segment }: ReviewerSegmentCardProps) {
  return (
    <Card className="p-5">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm font-black text-muted">Reviewer Segment</p>
          <h2 className="mt-1 text-xl font-black capitalize text-espresso">
            {segment.segment}
          </h2>
        </div>
        <span className="grid size-12 place-items-center rounded-md bg-primary/10 text-primary">
          <SparklesIcon className="size-6" />
        </span>
      </div>
      <p className="mt-4 text-sm leading-6 text-coffee-muted">
        {segmentDescriptions[segment.segment]}
      </p>
      <div className="mt-5 flex flex-wrap gap-2">
        <Badge variant="secondary">{segment.score} score</Badge>
        <Badge variant="outline">{segment.likeCount} likes</Badge>
        <Badge variant="outline">{segment.shareCount} shares</Badge>
        <Badge variant="outline">{segment.commentCount} comments</Badge>
      </div>
    </Card>
  );
}
