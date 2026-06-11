export type StoryItem = {
  avatarUri?: string | null;
  id: string;
  initials: string;
  isSelf?: boolean;
  label: string;
};

export type BlogDisplayAuthorType = "USER" | "CAFE_PAGE";

export type BlogFeedResponse = {
  authorAvatar?: string | null;
  authorUserAvatar?: string | null;
  authorUserFullName?: string | null;
  authorUserId?: string | null;
  authorUserName?: string | null;
  blogId: string;
  commentCount: number;
  contentPreview: string;
  createdAt: string;
  displayAuthorType: BlogDisplayAuthorType;
  displayAvatarUrl?: string | null;
  displayName?: string | null;
  imageUrls: string[];
  likeCount: number;
  pageAddress?: string | null;
  pageAvatarUrl?: string | null;
  pageCoverUrl?: string | null;
  pageId?: string | null;
  pageName?: string | null;
  rankPosition?: number | null;
  regionArea?: string | null;
  regionCity?: string | null;
  regionId?: string | null;
  regionProvince?: string | null;
  shareCount: number;
};
