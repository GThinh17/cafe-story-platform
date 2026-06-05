import type { BlogFeedResponse, BlogResponse } from "@/types/blog";
import type { FeedPost, FeedPostMedia } from "@/types/feed";

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

function buildTags(item: BlogFeedResponse) {
  const tags = ["For you"];

  if (item.regionCity) {
    tags.push(item.regionCity);
  }

  if (item.rankPosition) {
    tags.push(`Rank #${item.rankPosition}`);
  }

  return tags;
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
    .join(", ") || "Cafe Story";
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
    src: url,
    alt: `${altPrefix} post photo ${index + 1}`,
    type: "image",
  }));
}

export function mapBlogFeedToFeedPosts(feed: BlogFeedResponse[]): FeedPost[] {
  return feed.map((item, index) => {
    const authorUsername = firstNonEmpty([item.authorUserName]);
    const media = mapImageUrlsToMedia(
      item.imageUrls,
      item.blogId,
      authorUsername,
    );
    const image = firstNonEmpty([
      ...media.map((mediaItem) => mediaItem.src),
      item.pageCoverUrl,
      item.pageAvatarUrl,
    ]);

    return {
      id: item.blogId,
      allowComment: item.allowComment ?? true,
      author: firstNonEmpty([item.authorUserName, item.authorUserFullName]) ?? "cafestory_user",
      authorUserId: item.authorUserId,
      authorUsername,
      authorAvatar: firstNonEmpty([item.authorAvatar]) ?? "/images/default-avatar.svg",
      cafe: firstNonEmpty([item.pageName]) ?? "Cafe Story",
      caption: item.contentPreview?.trim() || "A new Cafe Story post is ready.",
      commentCount: item.commentCount ?? 0,
      comments: formatCount(item.commentCount),
      image: image ?? fallbackImages[index % fallbackImages.length],
      likeCount: item.likeCount ?? 0,
      likes: formatCount(item.likeCount),
      location: formatLocation(item),
      media,
      rating: item.rankPosition ? `#${item.rankPosition}` : "Feed",
      tags: buildTags(item),
      time: formatRelativeTime(item.createdAt),
    };
  });
}

export function mapBlogResponsesToFeedPosts(blogs: BlogResponse[]): FeedPost[] {
  return blogs.map((item, index) => {
    const authorUsername = firstNonEmpty([
      item.authorUserName,
      item.authorUserFullName,
      item.displayName,
    ]);
    const media = mapImageUrlsToMedia(item.imageUrls, item.id, authorUsername);
    const image = firstNonEmpty([
      ...media.map((mediaItem) => mediaItem.src),
      item.pageAvatarUrl,
      item.displayAvatarUrl,
    ]);
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
      authorAvatar:
        firstNonEmpty([
          item.displayAvatarUrl,
          item.authorUserAvatar,
          item.pageAvatarUrl,
        ]) ?? "/images/default-avatar.svg",
      cafe: firstNonEmpty([item.pageName]) ?? "Cafe Story",
      caption: item.content?.trim() || "A new Cafe Story post is ready.",
      commentCount: item.commentCount ?? 0,
      comments: formatCount(item.commentCount),
      image: image ?? fallbackImages[index % fallbackImages.length],
      isLiked: item.isLike ?? false,
      likeCount: item.likeCount ?? 0,
      likes: formatCount(item.likeCount),
      location: "Cafe Story",
      media,
      rating,
      shares: formatCount(item.shareCount),
      tags: [
        "Profile",
        ...[item.pageName, item.status].filter(
          (value): value is string => Boolean(value?.trim()),
        ),
      ],
      time: formatRelativeTime(item.createdAt),
    };
  });
}
