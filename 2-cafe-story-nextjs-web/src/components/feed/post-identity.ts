import { DEFAULT_AVATAR_IMAGE } from "@/lib/avatar";
import type { FeedPost } from "@/types/feed";

function getUserProfileHref(username: string) {
  return `/${encodeURIComponent(username)}`;
}

function getCafePageHref(pageId: string) {
  return `/cafes/${encodeURIComponent(pageId)}`;
}

function firstNonEmpty(values: Array<string | null | undefined>) {
  return values.find((value) => value?.trim())?.trim();
}

export function getPostIdentity(post: FeedPost) {
  const authorUsername =
    firstNonEmpty([post.authorUsername, post.author]) ?? "cafestory_user";
  const authorAvatar =
    firstNonEmpty([post.authorAvatar]) ?? DEFAULT_AVATAR_IMAGE;
  const pageId = firstNonEmpty([post.pageId]);
  const pageName = firstNonEmpty([
    post.pageName,
    post.displayAuthorType === "CAFE_PAGE" ? post.displayName : undefined,
  ]);
  const pageAvatar =
    firstNonEmpty([
      post.pageAvatarUrl,
      post.displayAuthorType === "CAFE_PAGE"
        ? post.displayAvatarUrl
        : undefined,
    ]) ?? DEFAULT_AVATAR_IMAGE;
  const isPagePost =
    post.displayAuthorType === "CAFE_PAGE" &&
    Boolean(pageId && pageName && pageAvatar);

  if (isPagePost && pageId && pageName) {
    return {
      authorAvatar,
      authorHref: getUserProfileHref(authorUsername),
      authorUsername,
      isPagePost: true,
      primaryAvatar: pageAvatar,
      primaryHref: getCafePageHref(pageId),
      primaryName: pageName,
      secondaryHref: getUserProfileHref(authorUsername),
      secondaryName: authorUsername,
    };
  }

  return {
    authorAvatar,
    authorHref: getUserProfileHref(authorUsername),
    authorUsername,
    isPagePost: false,
    primaryAvatar: authorAvatar,
    primaryHref: getUserProfileHref(authorUsername),
    primaryName: authorUsername,
    secondaryHref: null,
    secondaryName: null,
  };
}
