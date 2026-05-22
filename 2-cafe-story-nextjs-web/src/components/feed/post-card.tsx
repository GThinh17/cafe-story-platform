import type { FeedPost } from "@/types/feed";

export type { FeedPost };

type PostCardProps = {
  post: FeedPost;
};

const postActions = ["Like", "Comment", "Share"];

export function PostCard({ post }: PostCardProps) {
  return (
    <article className="overflow-hidden rounded-md border border-border bg-surface shadow-sm [contain-intrinsic-size:900px] [content-visibility:auto]">
      <div className="flex items-center justify-between px-4 py-4">
        <div className="flex min-w-0 items-center gap-3">
          <div className="grid h-11 w-11 shrink-0 place-items-center rounded-full bg-surface-muted text-sm font-bold text-primary-strong">
            {post.cafe.slice(0, 2).toUpperCase()}
          </div>
          <div className="min-w-0">
            <h2 className="truncate text-base font-bold">{post.cafe}</h2>
            <p className="truncate text-xs font-medium text-muted">
              {post.location} - {post.time}
            </p>
          </div>
        </div>
        <div className="rounded-md bg-[#fff4d6] px-2.5 py-1 text-sm font-bold text-rating">
          {post.rating}
        </div>
      </div>

      <img
        alt={`${post.cafe} cafe interior`}
        className="aspect-[4/5] w-full object-cover sm:aspect-[5/4]"
        decoding="async"
        loading="lazy"
        src={post.image}
      />

      <div className="space-y-4 px-4 py-4">
        <div className="flex items-center justify-between">
          <div className="flex gap-2">
            {postActions.map((action) => (
              <button
                className="rounded-md border border-border px-3 py-2 text-xs font-bold text-foreground transition hover:border-primary hover:text-primary"
                key={action}
                type="button"
              >
                {action}
              </button>
            ))}
          </div>
          <button
            className="rounded-md px-3 py-2 text-xs font-bold text-muted transition hover:bg-surface-muted"
            type="button"
          >
            Save
          </button>
        </div>

        <p className="text-sm font-semibold">
          {post.likes} likes - {post.comments} comments
        </p>
        <p className="text-sm leading-6 text-foreground">
          <span className="font-bold">{post.author}</span> {post.caption}
        </p>
        <div className="flex flex-wrap gap-2">
          {post.tags.map((tag) => (
            <span
              className="rounded-md bg-surface-muted px-2.5 py-1 text-xs font-bold text-primary-strong"
              key={tag}
            >
              {tag}
            </span>
          ))}
        </div>
      </div>
    </article>
  );
}
