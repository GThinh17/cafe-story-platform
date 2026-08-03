import { imageWidths, optimizeImageUrl } from "@/lib/image-optimizer";
import type { BlogFeedResponse, BlogResponse } from "@/types/blog";
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

function formatRelativeTime(value: string | null) {
  if (!value) {
    return "Just now";
  }

  const createdAt = new Date(value);

  if (Number.isNaN(createdAt.getTime())) {
    return "Just now";
  }

  const diffMinutes = Math.max(
    0,
    Math.floor((Date.now() - createdAt.getTime()) / 60000),
  );

  if (diffMinutes < 1) {
    return "Just now";
  }

  if (diffMinutes < 60) {
    return `${diffMinutes} min`;
  }

  const diffHours = Math.floor(diffMinutes / 60);

  if (diffHours < 24) {
    return `${diffHours} hr`;
  }

  return `${Math.floor(diffHours / 24)} d`;
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
  authorUsername?: string | null,
): FeedPostMedia[] {
  const altPrefix = authorUsername?.trim() || "Cafe Story";

  return getNonEmptyImageUrls(imageUrls).map((url, index) => ({
    id: `${postId}-${index}`,
    src: optimizeImageUrl(url, { width: imageWidths.postMedia }),
    alt: `${altPrefix} post photo ${index + 1}`,
    type: "image",
  }));
}

export function mapBlogFeedToFeedPosts(feed: BlogFeedResponse[]): FeedPost[] {
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
      caption: item.contentPreview?.trim() || "A new Cafe Story post is ready.",
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
      time: formatRelativeTime(item.createdAt),
      isAuthorFollowing: item.isAuthorFollowing ?? false,
      isPageFollowing: item.isPageFollowing ?? false,
    };
  });
}

export function mapMixedFeedToFeedPosts(feed: MixedFeedResponse): FeedPost[] {
  const blogItems = (feed.items ?? [])
    .map((item) => item.blog)
    .filter((item): item is BlogFeedResponse => item !== null);

  return mapBlogFeedToFeedPosts(blogItems);
}

export function mapMixedFeedToRenderableItems(
  feed: MixedFeedResponse,
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
    const post = mapBlogFeedToFeedPosts([item.blog])[0];
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

export function mapSharedBlogResponsesToFeedPosts(blogs: BlogResponse[]): FeedPost[] {
  return mapBlogResponsesToFeedPosts(blogs).map((post) => ({
    ...post,
    isShared: true,
  }));
}

export function mapBlogResponsesToFeedPosts(blogs: BlogResponse[]): FeedPost[] {
  return blogs.map((item, index) => {
    const authorUsername = firstNonEmpty([
      item.authorUserName,
      item.authorUserFullName,
      item.displayName,
    ]);
    const media = mapImageUrlsToMedia(item.imageUrls, item.id, authorUsername);
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
      time: formatRelativeTime(item.createdAt),
    };
  });
}
