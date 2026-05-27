import type { FeedPost } from "@/types/feed";

export type { FeedPost };

type PostCardProps = {
  post: FeedPost;
};

type PostActionIconName = "bookmark" | "heart" | "message" | "send";

const postActions: { label: string; icon: PostActionIconName }[] = [
  { label: "Like post", icon: "heart" },
  { label: "Comment on post", icon: "message" },
  { label: "Share post", icon: "send" },
];

function PostActionIcon({ name }: { name: PostActionIconName }) {
  const paths: Record<PostActionIconName, string[]> = {
    bookmark: ["M6 4h12v17l-6-3-6 3V4Z"],
    heart: [
      "M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.6l-1-1a5.5 5.5 0 0 0-7.8 7.8l1 1L12 21l7.8-7.6 1-1a5.5 5.5 0 0 0 0-7.8Z",
    ],
    message: [
      "M4 5.5h16v11H8l-4 4v-15Z",
      "M8 9h8",
      "M8 13h5",
    ],
    send: ["M22 2 11 13", "m22 2-7 20-4-9-9-4 20-7Z"],
  };

  return (
    <svg
      aria-hidden="true"
      className="h-5 w-5"
      fill="none"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth="1.7"
      viewBox="0 0 24 24"
    >
      {paths[name].map((path) => (
        <path d={path} key={path} />
      ))}
    </svg>
  );
}

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
                aria-label={action.label}
                className="grid h-10 w-10 place-items-center rounded-md border border-border text-foreground transition hover:border-primary hover:bg-surface-muted hover:text-primary"
                key={action.label}
                type="button"
              >
                <PostActionIcon name={action.icon} />
              </button>
            ))}
          </div>
          <button
            aria-label="Save post"
            className="grid h-10 w-10 place-items-center rounded-md text-muted transition hover:bg-surface-muted hover:text-primary"
            type="button"
          >
            <PostActionIcon name="bookmark" />
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
