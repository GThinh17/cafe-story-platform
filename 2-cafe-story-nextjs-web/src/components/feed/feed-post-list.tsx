import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { PostCard } from "@/components/feed/post-card";
import type { FeedPost } from "@/types/feed";

type FeedPostListProps = {
  errorMessage?: string;
  posts: FeedPost[];
};

export function FeedPostList({ errorMessage, posts }: FeedPostListProps) {
  if (errorMessage) {
    return (
      <Alert>
        <AlertTitle>Feed unavailable</AlertTitle>
        <AlertDescription>{errorMessage}</AlertDescription>
      </Alert>
    );
  }

  if (posts.length === 0) {
    return (
      <Alert>
        <AlertTitle>No posts yet</AlertTitle>
        <AlertDescription>
          Your personalized Cafe Story feed will appear here once there are posts
          to recommend.
        </AlertDescription>
      </Alert>
    );
  }

  return (
    <div className="space-y-6">
      {posts.map((post) => (
        <PostCard key={post.id ?? post.cafe} post={post} />
      ))}
    </div>
  );
}
