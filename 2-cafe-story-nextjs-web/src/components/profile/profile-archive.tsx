"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { useCurrentUser } from "@/hooks/use-current-user";
import { useCommentModal } from "@/context/comment-modal-context";
import { isCurrentUserProfile } from "@/lib/profile/is-current-user-profile";
import { getBlogsByUser } from "@/lib/api/blogs";
import { getUserByUsername } from "@/lib/api/users";
import { mapBlogResponsesToFeedPosts } from "@/features/blogs/blog-feed-adapter";
import type { FeedPost } from "@/types/feed";
import { useI18n } from "@/components/providers/locale-provider";

type ProfileArchiveProps = {
  routeUsername: string;
};

export function ProfileArchive({ routeUsername }: ProfileArchiveProps) {
  const { t } = useI18n();
  const router = useRouter();
  const { user, isLoading: isAuthLoading } = useCurrentUser();
  const { openByBlogId } = useCommentModal();

  const [posts, setPosts] = useState<FeedPost[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const isOwner = useMemo(
    () => isCurrentUserProfile(routeUsername, user),
    [routeUsername, user],
  );

  useEffect(() => {
    if (isAuthLoading) return;

    if (!isOwner) {
      router.replace(`/${encodeURIComponent(routeUsername)}`);
      return;
    }

    let isActive = true;

    async function load() {
      setIsLoading(true);
      setErrorMessage(null);

      try {
        const viewedUser = await getUserByUsername(routeUsername);
        if (!isActive) return;

        const hidden = await getBlogsByUser(viewedUser.userId, "HIDDEN");
        if (!isActive) return;

        setPosts(mapBlogResponsesToFeedPosts(hidden.filter((b) => !b.pageId), t));
      } catch (error) {
        if (!isActive) return;
        setErrorMessage(
          error instanceof Error ? error.message : t("profile.archive.loadError"),
        );
      } finally {
        if (isActive) setIsLoading(false);
      }
    }

    void load();

    return () => {
      isActive = false;
    };
  }, [isAuthLoading, isOwner, routeUsername, router]);

  return (
    <div className="flex w-full justify-center overflow-x-clip px-4 py-12 sm:px-8 xl:pr-80">
      <div className="w-full max-w-[935px] space-y-6">
        <div className="flex items-center gap-3">
          <Button
            aria-label={t("profile.archive.back")}
            onClick={() => router.push(`/${encodeURIComponent(routeUsername)}`)}
            size="icon-sm"
            type="button"
            variant="ghost"
          >
            <ArrowLeft className="size-5" />
          </Button>
          <div className="flex flex-col">
            <h1 className="text-lg font-bold leading-tight text-foreground">
              Archive
            </h1>
            <p className="text-sm text-muted">
              Hidden posts — only you can see this.
            </p>
          </div>
        </div>

        <div className="border-t border-border pt-3">
          {isLoading ? (
            <div className="grid grid-cols-3 gap-0.5 sm:grid-cols-4">
              {Array.from({ length: 8 }, (_, index) => (
                <Skeleton className="aspect-square rounded-none" key={index} />
              ))}
            </div>
          ) : errorMessage ? (
            <p className="py-10 text-center text-sm text-muted">{errorMessage}</p>
          ) : posts.length === 0 ? (
            <p className="py-10 text-center text-sm text-muted">
              No hidden posts yet.
            </p>
          ) : (
            <div className="grid grid-cols-3 gap-0.5 sm:grid-cols-4">
              {posts.map((post) => (
                <button
                  aria-label={`Open archived post from ${post.cafe}`}
                  className="group relative aspect-square overflow-hidden bg-surface-muted text-left"
                  key={post.id ?? post.image}
                  onClick={() => {
                    if (post.id) openByBlogId(post.id);
                  }}
                  type="button"
                >
                  <img
                    alt={`${post.cafe} post`}
                    className="h-full w-full object-cover transition duration-200 group-hover:scale-105"
                    decoding="async"
                    loading="lazy"
                    src={post.image}
                  />
                </button>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
