"use client";

import { SparklesIcon } from "lucide-react";
import { useI18n } from "@/components/providers/locale-provider";
import type { TranslationKey } from "@/lib/i18n";
import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import type { ReviewerSegmentItem } from "@/features/reviewer-dashboard/reviewer-dashboard.types";

type ReviewerSegmentCardProps = {
  segment: ReviewerSegmentItem;
};

const segmentNameKeys: Record<string, TranslationKey> = {
  active: "reviewer.segmentName.active",
  elite: "reviewer.segmentName.elite",
  inactive: "reviewer.segmentName.inactive",
  new: "reviewer.segmentName.new",
  strong: "reviewer.segmentName.strong",
  top: "reviewer.segmentName.top",
};

const segmentDescriptionKeys: Record<string, TranslationKey> = {
  active: "reviewer.segment.active",
  elite: "reviewer.segment.elite",
  inactive: "reviewer.segment.inactive",
  new: "reviewer.segment.new",
  strong: "reviewer.segment.strong",
  top: "reviewer.segment.top",
};

export function ReviewerSegmentCard({ segment }: ReviewerSegmentCardProps) {
  const { t } = useI18n();

  return (
    <Card className="p-5">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm font-black text-muted">
            {t("reviewer.segment.title")}
          </p>
          <h2 className="mt-1 text-xl font-black text-espresso">
            {t(segmentNameKeys[segment.segment])}
          </h2>
        </div>
        <span className="grid size-12 place-items-center rounded-md bg-primary/10 text-primary">
          <SparklesIcon className="size-6" />
        </span>
      </div>
      <p className="mt-4 text-sm leading-6 text-coffee-muted">
        {t(segmentDescriptionKeys[segment.segment])}
      </p>
      <div className="mt-5 flex flex-wrap gap-2">
        <Badge variant="secondary">
          {t("reviewer.badgeProgress.scoreBadge", { score: segment.score })}
        </Badge>
        <Badge variant="outline">
          {t("reviewer.segment.likes", { count: segment.likeCount })}
        </Badge>
        <Badge variant="outline">
          {t("reviewer.segment.shares", { count: segment.shareCount })}
        </Badge>
        <Badge variant="outline">
          {t("reviewer.segment.comments", { count: segment.commentCount })}
        </Badge>
      </div>
    </Card>
  );
}
