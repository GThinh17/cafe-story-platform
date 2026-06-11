export type StoryItem = {
  avatarUri?: string | null;
  id: string;
  initials: string;
  isSelf?: boolean;
  label: string;
};

export type BlogDisplayAuthorType = "USER" | "CAFE_PAGE";

export type TrendWindowType = "HOUR_24" | "DAY_7" | "MONTH_1";

export type BlogFeedResponse = {
  authorAvatar?: string | null;
  authorUserAvatar?: string | null;
  authorUserFullName?: string | null;
  authorUserId: string;
  authorUserName?: string | null;
  blogId: string;
  commentCount: number | null;
  contentPreview: string | null;
  createdAt: string | null;
  displayAuthorType?: BlogDisplayAuthorType | null;
  displayAvatarUrl?: string | null;
  displayName?: string | null;
  imageUrls: string[] | null;
  likeCount: number | null;
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
  shareCount: number | null;
};

export type BlogFeedParams = {
  page?: number;
  regionId?: string;
  size?: number;
  windowType?: TrendWindowType;
};

export type BlogFeedComment = {
  authorAvatar?: string | null;
  authorName: string;
  content: string;
  createdAt: string;
  id: string;
  likeCount: number;
  replyCount?: number;
};
