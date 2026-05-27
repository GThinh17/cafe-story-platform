import type { BlogFeedResponse } from "@/types/blog";
import type { FeedPost } from "@/types/feed";

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

export function mapBlogFeedToFeedPosts(feed: BlogFeedResponse[]): FeedPost[] {
  return feed.map((item, index) => {
    const image = firstNonEmpty([
      ...(item.imageUrls ?? []),
      item.pageCoverUrl,
      item.pageAvatarUrl,
    ]);

    return {
      id: item.blogId,
      author:
        firstNonEmpty([item.authorUserFullName, item.authorUserName]) ??
        "cafestory_user",
      authorAvatar: firstNonEmpty([item.authorAvatar]) ?? "/images/default-avatar.svg",
      cafe: firstNonEmpty([item.pageName]) ?? "Cafe Story",
      caption: item.contentPreview?.trim() || "A new Cafe Story post is ready.",
      comments: formatCount(item.commentCount),
      image: image ?? fallbackImages[index % fallbackImages.length],
      likes: formatCount(item.likeCount),
      location: formatLocation(item),
      rating: item.rankPosition ? `#${item.rankPosition}` : "Feed",
      tags: buildTags(item),
      time: formatRelativeTime(item.createdAt),
    };
  });
}
