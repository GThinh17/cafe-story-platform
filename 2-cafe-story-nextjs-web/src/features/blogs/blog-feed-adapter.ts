import { imageWidths, optimizeImageUrl } from "@/lib/image-optimizer";
import type {
  BlogFeedResponse,
  BlogResponse,
  BlogTrendingResponse,
} from "@/types/blog";
import type { Translate } from "@/lib/i18n";
import type {
  FeedPost,
  FeedPostMedia,
  FeedRenderableItem,
  MixedFeedResponse,
} from "@/types/feed";

const fallbackImages = [
  "/images/cafes/velvet-roast/latte-art.jpg",
  "/images/cafes/velvet-roast/minimal-interior.jpg",
  "/images/cafes/velvet-roast/espresso-machine.jpg",
  "/images/cafes/velvet-roast/croissant-flatlay.jpg",
];

function formatRelativeTime(value: string | null, t: Translate) {
  if (!value) {
    return t("feed.justNow");
  }

  const createdAt = new Date(value);

  if (Number.isNaN(createdAt.getTime())) {
    return t("feed.justNow");
  }

  const diffMinutes = Math.max(
    0,
    Math.floor((Date.now() - createdAt.getTime()) / 60000),
  );

  if (diffMinutes < 1) {
    return t("feed.justNow");
  }

  if (diffMinutes < 60) {
    return t("feed.time.minutes", { value: diffMinutes });
  }

  const diffHours = Math.floor(diffMinutes / 60);

  if (diffHours < 24) {
    return t("feed.time.hours", { value: diffHours });
  }

  return t("feed.time.days", { value: Math.floor(diffHours / 24) });
}

/**
 * Tag hiển thị = tag do AI blog moderation cấp, cộng thêm chip khu vực.
 * Bài chưa có tag thì trả mảng rỗng để post card không render chip nào.
 */
function buildTags(
  aiTags: string[] | null | undefined,
  regionCity: string | null | undefined,
) {
  const tags = [...(aiTags ?? []), regionCity]
    .map((tag) => tag?.trim())
    .filter((tag): tag is string => Boolean(tag));

  return Array.from(new Set(tags));
}

function formatCount(value: number | null | undefined) {
  return new Intl.NumberFormat("en", {
    notation: "compact",
    maximumFractionDigits: 1,
  }).format(value ?? 0);
}

function formatLocation(item: BlogFeedResponse) {
  return [
    item.pageAddress,
    item.regionArea,
    item.regionCity,
    item.regionProvince,
  ]
    .filter(Boolean)
    .join(", ");
}

function firstNonEmpty(values: Array<string | null | undefined>) {
  return values.find((value) => value?.trim())?.trim();
}

function getNonEmptyImageUrls(imageUrls: string[] | null | undefined) {
  return (imageUrls ?? [])
    .map((url) => url.trim())
    .filter((url) => url.length > 0);
}

function mapImageUrlsToMedia(
  imageUrls: string[] | null | undefined,
  postId: string,
  t: Translate,
  authorUsername?: string | null,
): FeedPostMedia[] {
  const altPrefix = authorUsername?.trim() || "Cafe Story";

  return getNonEmptyImageUrls(imageUrls).map((url, index) => ({
    id: `${postId}-${index}`,
    src: optimizeImageUrl(url, { width: imageWidths.postMedia }),
    alt: t("feed.postPhotoAlt", { name: altPrefix, index: index + 1 }),
    type: "image",
  }));
}

export function mapBlogFeedToFeedPosts(
  feed: BlogFeedResponse[],
  t: Translate,
): FeedPost[] {
  return feed.map((item, index) => {
    const authorUsername = firstNonEmpty([item.authorUserName]);
    const authorAvatar = optimizeImageUrl(
      firstNonEmpty([item.authorAvatar, item.authorUserAvatar]) ??
        "/images/default-avatar.svg",
      { width: imageWidths.avatar },
    );
    const pageName = firstNonEmpty([item.pageName]);
    const pageAvatarUrl = optimizeImageUrl(firstNonEmpty([item.pageAvatarUrl]), {
      width: imageWidths.avatar,
    }) || undefined;
    const locationLabel = formatLocation(item);
    const media = mapImageUrlsToMedia(
      item.imageUrls,
      item.blogId,
      t,
      firstNonEmpty([item.displayName, pageName, authorUsername]),
    );
    const image = optimizeImageUrl(
      firstNonEmpty([
        ...media.map((mediaItem) => mediaItem.src),
        item.pageCoverUrl,
        item.pageAvatarUrl,
      ]),
      { width: imageWidths.postMedia },
    ) || undefined;

    return {
      id: item.blogId,
      allowComment: item.allowComment ?? true,
      author: firstNonEmpty([item.authorUserName, item.authorUserFullName]) ?? "cafestory_user",
      authorUserId: item.authorUserId,
      authorUsername,
      authorAvatar,
      cafe: pageName ?? "",
      caption: item.contentPreview?.trim() || t("feed.defaultCaption"),
      commentCount: item.commentCount ?? 0,
      comments: formatCount(item.commentCount),
      displayAuthorType: item.displayAuthorType,
      displayAvatarUrl:
        optimizeImageUrl(firstNonEmpty([item.displayAvatarUrl]), {
          width: imageWidths.avatar,
        }) || undefined,
      displayName: firstNonEmpty([item.displayName]),
      image: image ?? fallbackImages[index % fallbackImages.length],
      isLiked: item.isLike ?? false,
      likeCount: item.likeCount ?? 0,
      likes: formatCount(item.likeCount),
      location: locationLabel,
      locationLabel,
      media,
      pageAvatarUrl,
      pageId: item.pageId,
      pageName,
      rating: item.rankPosition ? `#${item.rankPosition}` : "Feed",
      shares: formatCount(item.shareCount),
      saves: formatCount(item.saveCount),
      saveCount: item.saveCount ?? 0,
      isSaved: item.isSave ?? false,
      tags: buildTags(item.tags, item.regionCity),
      time: formatRelativeTime(item.createdAt, t),
      isAuthorFollowing: item.isAuthorFollowing ?? false,
      isPageFollowing: item.isPageFollowing ?? false,
    };
  });
}

/**
 * `GET /api/blogs/trending` already returns images, counters and the viewer's
 * like/save state, so a trending card renders from the list response alone.
 *
 * Two fields the trending DTO genuinely does not carry:
 * - `allowComment` stays undefined on purpose. The comments modal treats that as
 *   "unknown" and looks it up once, which is the only way a locked-comment post
 *   opened from /explore is honoured.
 * - there is no region on the DTO, so `location`/`tags` are empty rather than wrong.
 */
export function mapBlogTrendingToFeedPosts(
  trending: BlogTrendingResponse[],
  t: Translate,
): FeedPost[] {
  return trending.map((item, index) => {
    const authorUsername = firstNonEmpty([item.authorUserName]);
    const pageName = firstNonEmpty([item.pageName]);
    const media = mapImageUrlsToMedia(
      item.imageUrls,
      item.blogId,
      t,
      firstNonEmpty([item.displayName, pageName, authorUsername]),
    );
    const image = optimizeImageUrl(
      firstNonEmpty([
        ...media.map((mediaItem) => mediaItem.src),
        item.pageCoverUrl,
        item.pageAvatarUrl,
      ]),
      { width: imageWidths.postMedia },
    ) || undefined;

    return {
      id: item.blogId,
      author:
        firstNonEmpty([item.authorUserName, item.authorUserFullName]) ??
        "cafestory_user",
      authorUserId: item.authorUserId,
      authorUsername,
      authorAvatar: optimizeImageUrl(
        firstNonEmpty([item.displayAvatarUrl, item.authorUserAvatar]) ??
          "/images/default-avatar.svg",
        { width: imageWidths.avatar },
      ),
      cafe: pageName ?? "",
      caption: item.contentPreview?.trim() || t("feed.defaultCaption"),
      commentCount: item.commentCount ?? 0,
      comments: formatCount(item.commentCount),
      displayAuthorType: item.displayAuthorType ?? undefined,
      displayAvatarUrl:
        optimizeImageUrl(firstNonEmpty([item.displayAvatarUrl]), {
          width: imageWidths.avatar,
        }) || undefined,
      displayName: firstNonEmpty([item.displayName]),
      image: image ?? fallbackImages[index % fallbackImages.length],
      isLiked: item.isLike ?? false,
      likeCount: item.likeCount ?? 0,
      likes: formatCount(item.likeCount),
      location: "",
      media,
      pageAvatarUrl:
        optimizeImageUrl(firstNonEmpty([item.pageAvatarUrl]), {
          width: imageWidths.avatar,
        }) || undefined,
      pageId: item.pageId,
      pageName,
      rating: item.rankPosition ? `#${item.rankPosition}` : "Trending",
      shares: formatCount(item.shareCount),
      saves: formatCount(0),
      saveCount: 0,
      isSaved: item.isSave ?? false,
      isAuthorFollowing: false,
      isPageFollowing: false,
      tags: [],
      time: formatRelativeTime(item.createdAt, t),
    };
  });
}

export function mapMixedFeedToFeedPosts(
  feed: MixedFeedResponse,
  t: Translate,
): FeedPost[] {
  const blogItems = (feed.items ?? [])
    .map((item) => item.blog)
    .filter((item): item is BlogFeedResponse => item !== null);

  return mapBlogFeedToFeedPosts(blogItems, t);
}

export function mapMixedFeedToRenderableItems(
  feed: MixedFeedResponse,
  t: Translate,
): FeedRenderableItem[] {
  const result: FeedRenderableItem[] = [];
  (feed.items ?? []).forEach((item, index) => {
    if (item.ad?.campaignId) {
      result.push({
        ad: item.ad,
        id: `ad-${item.ad.campaignId}-${item.position ?? index}`,
        kind: "ad",
      });
      return;
    }
    if (!item.blog) {
      return;
    }
    const post = mapBlogFeedToFeedPosts([item.blog], t)[0];
    if (!post) {
      return;
    }
    result.push({
      id: `post-${post.id ?? item.position ?? index}`,
      kind: "post",
      post,
    });
  });
  return result;
}

export function mapSharedBlogResponsesToFeedPosts(
  blogs: BlogResponse[],
  t: Translate,
): FeedPost[] {
  return mapBlogResponsesToFeedPosts(blogs, t).map((post) => ({
    ...post,
    isShared: true,
  }));
}

export function mapBlogResponsesToFeedPosts(
  blogs: BlogResponse[],
  t: Translate,
): FeedPost[] {
  return blogs.map((item, index) => {
    const authorUsername = firstNonEmpty([
      item.authorUserName,
      item.authorUserFullName,
      item.displayName,
    ]);
    const media = mapImageUrlsToMedia(item.imageUrls, item.id, t, authorUsername);
    const image = optimizeImageUrl(
      firstNonEmpty([
        ...media.map((mediaItem) => mediaItem.src),
        item.pageAvatarUrl,
        item.displayAvatarUrl,
      ]),
      { width: imageWidths.postMedia },
    ) || undefined;
    const rating =
      typeof item.ratingScore === "number"
        ? item.ratingScore.toFixed(1)
        : "Profile";

    return {
      id: item.id,
      allowComment: item.allowComment ?? true,
      author: authorUsername ?? "cafestory_user",
      authorUserId: item.authorUserId,
      authorUsername: firstNonEmpty([item.authorUserName]),
      authorAvatar: optimizeImageUrl(
        firstNonEmpty([
          item.displayAvatarUrl,
          item.authorUserAvatar,
          item.pageAvatarUrl,
        ]) ?? "/images/default-avatar.svg",
        { width: imageWidths.avatar },
      ),
      cafe: firstNonEmpty([item.pageName]) ?? "",
      caption: item.content?.trim() || "A new Cafe Story post is ready.",
      commentCount: item.commentCount ?? 0,
      comments: formatCount(item.commentCount),
      displayAuthorType: item.displayAuthorType,
      displayAvatarUrl:
        optimizeImageUrl(firstNonEmpty([item.displayAvatarUrl]), {
          width: imageWidths.avatar,
        }) || undefined,
      displayName: firstNonEmpty([item.displayName]),
      image: image ?? fallbackImages[index % fallbackImages.length],
      isLiked: item.isLike ?? false,
      likeCount: item.likeCount ?? 0,
      likes: formatCount(item.likeCount),
      location: "",
      media,
      pageAvatarUrl:
        optimizeImageUrl(firstNonEmpty([item.pageAvatarUrl]), {
          width: imageWidths.avatar,
        }) || undefined,
      pageId: item.pageId,
      pageName: firstNonEmpty([item.pageName]),
      rating,
      shares: formatCount(item.shareCount),
      saves: formatCount(item.saveCount),
      saveCount: item.saveCount ?? 0,
      isSaved: item.isSave ?? false,
      status: item.status,
      isAuthorFollowing: item.isAuthorFollowing ?? false,
      isPageFollowing: item.isPageFollowing ?? false,
      tags: buildTags(item.tags, item.regionCity),
      time: formatRelativeTime(item.createdAt, t),
    };
  });
}
