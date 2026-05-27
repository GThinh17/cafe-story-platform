export type TrendWindowType = "HOUR_24" | "DAY_7" | "MONTH_1";

export type BlogPostStatus = "PUBLISHED" | "DRAFT" | "ARCHIVED" | string;

export type BlogFeedResponse = {
  blogId: string;
  contentPreview: string | null;
  imageUrls: string[] | null;
  likeCount: number | null;
  commentCount: number | null;
  shareCount: number | null;
  authorUserId: string;
  authorUserName: string | null;
  authorUserFullName: string | null;
  authorAvatar: string | null;
  pageId: string | null;
  pageName: string | null;
  pageAddress: string | null;
  pageAvatarUrl: string | null;
  pageCoverUrl: string | null;
  regionId: string | null;
  regionCity: string | null;
  regionProvince: string | null;
  regionArea: string | null;
  rankPosition: number | null;
  createdAt: string | null;
};

export type BlogResponse = {
  id: string;
  authorUserId: string;
  pageId: string | null;
  regionId: string | null;
  content: string;
  imageUrls: string[];
  status: BlogPostStatus;
  isPinned: boolean;
  allowComment: boolean;
  likeCount: number;
  shareCount: number;
  createdAt: string | null;
  updatedAt: string | null;
};

export type BlogTrendingResponse = {
  blogId: string;
  contentPreview: string | null;
  authorUserId: string;
  authorUserName: string | null;
  pageId: string | null;
  pageName: string | null;
  windowType: TrendWindowType;
  trendScore: number | null;
  rankPosition: number | null;
  reason: string | null;
  pinned: boolean | null;
  createdAt: string | null;
  computedAt: string | null;
};

export type BlogFeedParams = {
  page?: number;
  regionId?: string;
  size?: number;
  windowType?: TrendWindowType;
};
