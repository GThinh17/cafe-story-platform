import Link from "next/link";
import type { FeedPost } from "@/types/feed";

type ExploreBlogGridProps = {
  error?: string;
  isLoading: boolean;
  onPostClick?: (post: FeedPost) => void;
  posts: FeedPost[];
};

function SkeletonCell() {
  return (
    <div className="aspect-square animate-pulse bg-surface-muted" />
  );
}

export function ExploreBlogGrid({ error, isLoading, onPostClick, posts }: ExploreBlogGridProps) {
  if (isLoading) {
    return (
      <div className="grid grid-cols-3 gap-0.5 sm:grid-cols-4">
        {Array.from({ length: 12 }).map((_, i) => (
          <SkeletonCell key={i} />
        ))}
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col items-center gap-2 py-16 text-center">
        <p className="text-sm font-semibold text-foreground">{error}</p>
        <p className="text-xs text-muted">Try refreshing the page.</p>
      </div>
    );
  }

  if (posts.length === 0) {
    return (
      <div className="flex flex-col items-center gap-2 py-16 text-center">
        <p className="text-sm font-semibold text-foreground">No trending posts</p>
        <p className="text-xs text-muted">Check back later for new content.</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-3 gap-0.5 sm:grid-cols-4">
      {posts.map((post) => {
        const inner = (
          <>
            {post.image ? (
              <img
                alt={post.caption}
                className="size-full object-cover transition-transform duration-200 group-hover:scale-105"
                decoding="async"
                loading="lazy"
                src={post.image}
              />
            ) : (
              <div className="flex size-full items-center justify-center bg-surface-muted p-2">
                <p className="line-clamp-4 text-center text-xs font-medium text-muted">
                  {post.caption}
                </p>
              </div>
            )}
          </>
        );

        if (onPostClick) {
          return (
            <button
              className="group relative block aspect-square w-full overflow-hidden bg-surface-muted cursor-pointer border-0 p-0"
              key={post.id ?? post.caption}
              onClick={() => onPostClick(post)}
              type="button"
            >
              {inner}
            </button>
          );
        }

        return (
          <Link
            className="group relative block aspect-square overflow-hidden bg-surface-muted no-underline"
            href={post.id ? `/reviews/${post.id}` : "#"}
            key={post.id ?? post.caption}
          >
            {inner}
          </Link>
        );
      })}
    </div>
  );
}
