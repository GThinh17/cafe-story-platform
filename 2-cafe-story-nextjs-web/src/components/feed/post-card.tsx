import {
  HeartIcon,
  MessageCircleIcon,
  Repeat2Icon,
} from "lucide-react";
import Link from "next/link";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { FollowButton } from "@/components/ui/follow-button";
import { ReviewerBadgeChip } from "@/components/ui/reviewer-badge-chip";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  getFeedPostMediaList,
  PostMediaCarousel,
} from "@/components/feed/post-media-carousel";
import { getPostIdentity } from "@/components/feed/post-identity";
import { MentionText } from "@/components/feed/mention-text";
import { SaveButton } from "@/components/feed/save-button";
import type { FeedPost } from "@/types/feed";
import { cn } from "@/lib/utils";

export type { FeedPost };

type PostCardProps = {
  currentUserId?: string;
  onCommentClick?: (post: FeedPost) => void;
  onLikeClick?: (post: FeedPost) => void;
  onSaveClick?: (post: FeedPost) => void;
  onShareClick?: (post: FeedPost) => void;
  post: FeedPost;
};

const postActions = [
  {
    label: "Like",
    icon: HeartIcon,
  },
  {
    label: "Comment",
    icon: MessageCircleIcon,
  },
  {
    label: "Share",
    icon: Repeat2Icon,
  },
];

function formatPostCommentCount(post: FeedPost) {
  return typeof post.commentCount === "number"
    ? formatCount(post.commentCount)
    : Array.isArray(post.comments)
    ? new Intl.NumberFormat("en", {
        notation: "compact",
        maximumFractionDigits: 1,
      }).format(post.comments.length)
    : post.comments;
}

function formatCount(value: number) {
  return new Intl.NumberFormat("en", {
    notation: "compact",
    maximumFractionDigits: 1,
  }).format(value);
}

function formatPostLikeCount(post: FeedPost) {
  return typeof post.likeCount === "number" ? formatCount(post.likeCount) : post.likes;
}

export function PostCard({ currentUserId, onCommentClick, onLikeClick, onSaveClick, onShareClick, post }: PostCardProps) {
  const identity = getPostIdentity(post);
  const locationLabel = post.locationLabel?.trim() || post.location?.trim();
  const commentCount = formatPostCommentCount(post);
  const actionCounts: Record<string, string> = {
    Comment: commentCount,
    Like: formatPostLikeCount(post),
    Share: post.shares ?? "0",
  };
  const media = getFeedPostMediaList(post);

  const isOwnPost = Boolean(currentUserId && post.authorUserId && currentUserId === post.authorUserId);
  const visibleActions = isOwnPost
    ? postActions.filter((a) => a.label !== "Share")
    : postActions;

  return (
    <Card className="mx-auto w-[85%] max-w-full overflow-hidden [contain-intrinsic-size:765px] [content-visibility:auto] shadow-none">
      <CardHeader className="flex flex-row items-center justify-between gap-4 px-4 py-4">
        <div className="flex min-w-0 items-center gap-3">
          <Link
            aria-label={`View ${identity.primaryName}`}
            className="block size-11 shrink-0 cursor-pointer overflow-hidden rounded-full border border-border/40 bg-surface-muted shadow-[inset_0_0_0_999px_rgba(217,119,6,0.10)]"
            href={identity.primaryHref}
          >
            <img
              alt={`${identity.primaryName} avatar`}
              className="block size-full max-w-none rounded-full object-cover object-center"
              decoding="async"
              loading="lazy"
              src={identity.primaryAvatar}
            />
          </Link>
          <div className="min-w-0 flex-1">
            <div className="flex items-center gap-1.5">
              <CardTitle className="truncate text-base font-bold">
                <Link className="cursor-pointer" href={identity.primaryHref}>
                  {identity.primaryName}
                </Link>
              </CardTitle>
              <ReviewerBadgeChip badge={post.authorBadge} />
              {!isOwnPost && (post.pageId ?? post.authorUserId) ? (
                <FollowButton
                  className="ml-auto !h-[24px] shrink-0 !px-2 !text-[12px]"
                  isFollowing={post.pageId ? post.isPageFollowing : post.isAuthorFollowing}
                  targetId={(post.pageId ?? post.authorUserId)!}
                  targetType={post.pageId ? "cafe" : "user"}
                />
              ) : null}
            </div>
            {identity.isPagePost && identity.secondaryHref ? (
              <Link
                className="block cursor-pointer truncate text-xs font-bold text-coffee-muted"
                href={identity.secondaryHref}
              >
                {identity.secondaryName}
              </Link>
            ) : null}
          </div>
        </div>
      </CardHeader>

      {media.length > 0 ? (
        <PostMediaCarousel
          frame="adaptive"
          imageClassName="bg-espresso"
          media={media}
        />
      ) : null}

      <CardContent className="flex flex-col gap-4 px-4 py-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            {visibleActions.map(({ icon: Icon, label }) => (
              <Button
                aria-label={label}
                className="h-auto cursor-pointer gap-1.5 px-0 py-0 text-sm font-bold text-foreground hover:bg-transparent hover:text-primary data-[state=active]:bg-transparent"
                key={label}
                onClick={
                  label === "Comment"
                    ? () => onCommentClick?.(post)
                    : label === "Like"
                      ? () => onLikeClick?.(post)
                      : label === "Share"
                        ? () => onShareClick?.(post)
                        : undefined
                }
                type="button"
                variant="ghost"
              >
                <Icon
                  className={cn(
                    "size-6",
                    label === "Like" && post.isLiked && "fill-accent text-accent",
                    label === "Share" && post.isShared && "fill-primary text-primary",
                  )}
                  strokeWidth={2.2}
                />
                <span>{actionCounts[label]}</span>
              </Button>
            ))}
          </div>
          <SaveButton
            isSaved={post.isSaved}
            onToggle={() => onSaveClick?.(post)}
          />
        </div>

        {locationLabel ? (
          <p className="text-xs font-semibold text-muted">{locationLabel}</p>
        ) : null}

        <p className="text-sm leading-6 text-foreground">
          <Link className="cursor-pointer font-bold" href={identity.primaryHref}>
            {identity.primaryName}
          </Link>{" "}
          <MentionText text={post.caption} />
        </p>
        <div className="flex flex-wrap gap-2">
          {post.tags.map((tag) => (
            <Badge className="font-bold" key={tag} variant="secondary">
              {tag}
            </Badge>
          ))}
        </div>
      </CardContent>
    </Card>
  );
}
