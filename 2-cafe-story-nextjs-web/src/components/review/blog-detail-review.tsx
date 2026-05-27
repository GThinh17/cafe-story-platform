import type { BlogDetailReview as BlogDetailReviewModel } from "@/types/review";
import {AvatarImage} from "@/components/ui/avatar-image"; 
type BlogDetailReviewProps = {
  review: BlogDetailReviewModel;
};

type ReviewIconName =
  | "bookmark"
  | "chevronLeft"
  | "chevronRight"
  | "heart" 
  | "message"
  | "send"
  | "star";

function ReviewIcon({
  className = "h-5 w-5",
  name,
}: {
  className?: string;
  name: ReviewIconName;
}) {
  const paths: Record<ReviewIconName, string[]> = {
    bookmark: ["M6 4h12v17l-6-3-6 3V4Z"],
    chevronLeft: ["m15 18-6-6 6-6"],
    chevronRight: ["m9 18 6-6-6-6"],
    heart: [
      "M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.6l-1-1a5.5 5.5 0 0 0-7.8 7.8l1 1L12 21l7.8-7.6 1-1a5.5 5.5 0 0 0 0-7.8Z",
    ],
    message: ["M4 5.5h16v11H8l-4 4v-15Z"],
    send: ["M22 2 11 13", "m22 2-7 20-4-9-9-4 20-7Z"],
    star: [
      "m12 3 2.8 5.7 6.2.9-4.5 4.4 1.1 6.2L12 17.9 6.4 21.2 7.5 15 3 10.6l6.2-.9L12 3Z",
    ],
  };

  return (
    <svg
      aria-hidden="true"
      className={className}
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

export function BlogDetailReview({ review }: BlogDetailReviewProps) {
  const heroImage = review.images[0];

  return (
    <article className="grid w-full overflow-hidden border border-line-soft bg-surface text-espresso shadow-sm lg:h-[min(760px,calc(100vh-96px))] lg:min-h-[620px] lg:grid-cols-[minmax(0,640px)_420px] xl:grid-cols-[minmax(0,660px)_440px]">
      <section className="relative min-h-[520px] overflow-hidden bg-espresso lg:h-full lg:min-h-0">
        <img
          alt={heroImage.alt}
          className="h-full min-h-[520px] w-full object-cover opacity-95 lg:min-h-0"
          decoding="async"
          fetchPriority="high"
          src={heroImage.src}
        />

        <button
          aria-label="Previous photo"
          className="absolute left-6 top-1/2 grid h-14 w-14 -translate-y-1/2 place-items-center rounded-md bg-white/10 text-white backdrop-blur-md transition hover:bg-white/20"
          type="button"
        >
          <ReviewIcon className="h-6 w-6" name="chevronLeft" />
        </button>
        <button
          aria-label="Next photo"
          className="absolute right-6 top-1/2 grid h-14 w-14 -translate-y-1/2 place-items-center rounded-md bg-white/10 text-white backdrop-blur-md transition hover:bg-white/20"
          type="button"
        >
          <ReviewIcon className="h-6 w-6" name="chevronRight" />
        </button>

        <div className="absolute bottom-8 left-1/2 flex -translate-x-1/2 gap-2">
          {review.images.map((image, index) => (
            <span
              aria-label={`Photo ${index + 1}`}
              className={`h-2.5 w-2.5 rounded-full ${
                index === 0 ? "bg-white" : "bg-white/40"
              }`}
              key={image.id}
              role="img"
            />
          ))}
        </div>
      </section>

      <aside className="flex min-h-0 flex-col border-l border-line-soft bg-surface">
        <header className="flex items-center justify-between gap-4 border-b border-line-soft px-5 py-4">
          <div className="flex min-w-0 items-center gap-4">
            <img
              alt={`${review.reviewerName} avatar`}
              className="h-11 w-11 shrink-0 rounded-full object-cover"
              decoding="async"
              src={review.reviewerAvatar}
            />
            <div className="min-w-0">
              <h1 className="truncate font-serif text-2xl font-medium leading-tight text-espresso">
                {review.cafe}
              </h1>
              <p className="truncate text-sm font-medium text-coffee-muted">
                Review by {review.reviewerName}
              </p>
            </div>
          </div>

          <button
            className="h-9 shrink-0 rounded-sm bg-espresso px-5 text-sm font-black text-white transition hover:bg-primary-strong"
            type="button"
          >
            Follow
          </button>
        </header>

        <div className="min-h-0 flex-1 overflow-y-auto px-5 py-5">
          <div className="space-y-7">
            <div className="flex items-center justify-between gap-4">
              <div className="flex items-center gap-3">
                <ReviewIcon className="h-6 w-6 text-espresso" name="star" />
                <span className="font-serif text-2xl font-medium">
                  {review.rating}
                </span>
                <span className="text-sm font-black text-coffee-muted">
                  / {review.ratingMax}
                </span>
              </div>
              <time className="text-xs font-black uppercase tracking-[0.12em] text-coffee-muted">
                {review.date}
              </time>
            </div>

            <div className="space-y-7">
              <p className="font-serif text-xl leading-relaxed text-espresso">
                {review.paragraphs[0]}
              </p>
              {review.paragraphs.slice(1).map((paragraph) => (
                <p
                  className="text-base leading-8 text-coffee-muted"
                  key={paragraph}
                >
                  {paragraph}
                </p>
              ))}
            </div>

            <div className="flex flex-wrap gap-2">
              {review.tags.map((tag) => (
                <span
                  className="rounded-full bg-surface-muted px-4 py-1.5 text-xs font-black text-espresso"
                  key={tag}
                >
                  {tag}
                </span>
              ))}
            </div>

            <section className="space-y-6">
              <h2 className="font-serif text-3xl font-medium text-espresso">
                Comments
              </h2>

              <div className="space-y-5 pb-2">
                {review.comments.map((comment) => (
                  <article className="flex gap-3" key={comment.id}>
                    <img
                      alt={`${comment.author} avatar`}
                      className="h-9 w-9 shrink-0 rounded-full object-cover"
                      decoding="async"
                      loading="lazy"
                      src={comment.avatar}
                    />
                    <div className="min-w-0 flex-1">
                      <p className="text-sm leading-6">
                        <span className="font-black text-espresso">
                          {comment.author}
                        </span>{" "}
                        <span className="text-coffee-muted">{comment.body}</span>
                      </p>
                      <p className="mt-1 text-xs font-medium text-muted">
                        {comment.time}
                      </p>
                    </div>
                  </article>
                ))}
              </div>
            </section>
          </div>
        </div>

        <footer className="border-t border-line-soft bg-surface px-5 py-4">
          <div className="flex items-center gap-6 text-coffee-muted">
            <button
              aria-label="Like review"
              className="flex items-center gap-2 font-black transition hover:text-accent"
              type="button"
            >
              <ReviewIcon name="heart" />
              <span>{review.likes}</span>
            </button>
            <button
              aria-label="View comments"
              className="flex items-center gap-2 font-black transition hover:text-primary"
              type="button"
            >
              <ReviewIcon name="message" />
              <span>{review.commentsCount}</span>
            </button>
            <button
              aria-label="Share review"
              className="transition hover:text-primary"
              type="button"
            >
              <ReviewIcon name="send" />
            </button>
            <button
              aria-label="Save review"
              className="ml-auto transition hover:text-primary"
              type="button"
            >
              <ReviewIcon name="bookmark" />
            </button>
          </div>

          <p className="mt-3 text-xs font-medium text-muted">1 day ago</p>

          <div className="mt-4 flex gap-3 border-t border-line-soft pt-4">
            <img
              alt="Your avatar"
              className="h-9 w-9 shrink-0 rounded-full object-cover"
              decoding="async"
              src={review.viewerAvatar}
            />
            <label className="flex min-w-0 flex-1 items-center gap-3">
              <span className="sr-only">Add a comment</span>
              <input
                className="h-10 min-w-0 flex-1 border-0 bg-transparent text-sm outline-none placeholder:text-coffee-muted"
                placeholder="Add a comment..."
                type="text"
              />
              <button
                aria-label="Post comment"
                className="grid h-9 w-9 shrink-0 place-items-center rounded-full text-primary transition hover:bg-surface-muted hover:text-primary-strong"
                type="button"
              >
                <ReviewIcon className="h-4 w-4" name="send" />
              </button>
            </label>
          </div>
        </footer>
      </aside>
    </article>
  );
}
