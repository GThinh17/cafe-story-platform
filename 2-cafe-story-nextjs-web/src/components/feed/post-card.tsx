import {
  HeartIcon,
  MessageCircleIcon,
  BookmarkIcon,
  Repeat2Icon,
} from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import type { FeedPost } from "@/types/feed";
import { DEFAULT_AVATAR_IMAGE } from "@/lib/avatar";

export type { FeedPost };

type PostCardProps = {
  onCommentClick?: (post: FeedPost) => void;
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
  return Array.isArray(post.comments)
    ? new Intl.NumberFormat("en", {
        notation: "compact",
        maximumFractionDigits: 1,
      }).format(post.comments.length)
    : post.comments;
}

export function PostCard({ onCommentClick, post }: PostCardProps) {
  const authorAvatar = post.authorAvatar?.trim() || DEFAULT_AVATAR_IMAGE;
  const commentCount = formatPostCommentCount(post);
  const actionCounts: Record<string, string> = {
    Comment: commentCount,
    Like: post.likes,
    Share: post.shares ?? "0",
  };

  return (
    <Card className="mx-auto w-[85%] max-w-full overflow-hidden [contain-intrinsic-size:765px] [content-visibility:auto]">
      <CardHeader className="flex flex-row items-center justify-between gap-4 px-4 py-4">
        <div className="flex min-w-0 items-center gap-3">
          <span className="block size-11 shrink-0 overflow-hidden rounded-full border border-border/40 bg-surface-muted shadow-[inset_0_0_0_999px_rgba(217,119,6,0.10)]">
            <img
              alt={`${post.author} avatar`}
              className="block size-full max-w-none rounded-full object-cover object-center"
              decoding="async"
              loading="lazy"
              src={authorAvatar}
            />
          </span>
          <div className="min-w-0">
            <CardTitle className="truncate text-base font-bold">
              {post.author}
            </CardTitle>
            <CardDescription className="truncate text-xs font-medium">
              {post.cafe} - {post.location} - {post.time}
            </CardDescription>
          </div>
        </div>
        <Badge className="text-sm font-bold" variant="rating">
          {post.rating}
        </Badge>
      </CardHeader>

      <img
        alt={`${post.cafe} cafe interior`}
        className="aspect-[4/5] w-full object-cover sm:aspect-[5/4]"
        decoding="async"
        loading="lazy"
        src={post.image}
      />

      <CardContent className="flex flex-col gap-4 px-4 py-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            {postActions.map(({ icon: Icon, label }) => (
              <Button
                aria-label={label}
                className="h-auto gap-1.5 px-0 py-0 text-sm font-bold text-foreground hover:bg-transparent hover:text-primary data-[state=active]:bg-transparent"
                key={label}
                onClick={
                  label === "Comment" ? () => onCommentClick?.(post) : undefined
                }
                type="button"
                variant="ghost"
              >
                <Icon className="size-6" strokeWidth={2.2} />
                <span>{actionCounts[label]}</span>
              </Button>
            ))}
          </div>
          <Button
            aria-label="Bookmark"
            className="h-auto px-0 py-0 text-foreground hover:bg-transparent hover:text-primary data-[state=active]:bg-transparent"
            type="button"
            variant="ghost"
          >
            <BookmarkIcon className="size-6" strokeWidth={2.2} />
          </Button>
        </div>

        <p className="text-sm leading-6 text-foreground">
          <span className="font-bold">{post.author}</span> {post.caption}
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
