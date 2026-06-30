export type TrendWindowType = "HOUR_24" | "DAY_7" | "MONTH_1";

export type BlogPostStatus = "PUBLISHED" | "DRAFT" | "HIDDEN" | "REMOVED" | string;

export type BlogDisplayAuthorType = "USER" | "CAFE_PAGE";

export type BlogFeedResponse = {
  blogId: string;
  contentPreview: string | null;
  imageUrls: string[] | null;
  allowComment?: boolean | null;
  likeCount: number | null;
  commentCount: number | null;
  shareCount: number | null;
  saveCount?: number | null;
  isSave?: boolean | null;
  authorUserId: string;
  authorUserName: string | null;
  authorUserFullName: string | null;
  authorAvatar: string | null;
  authorUserAvatar?: string | null;
  pageId: string | null;
  pageName: string | null;
  pageAddress: string | null;
  pageAvatarUrl: string | null;
  pageCoverUrl: string | null;
  displayAuthorType?: BlogDisplayAuthorType | null;
  displayName?: string | null;
  displayAvatarUrl?: string | null;
  regionId: string | null;
  regionCity: string | null;
  regionProvince: string | null;
  regionArea: string | null;
  rankPosition: number | null;
  isAuthorFollowing: boolean | null;
  isPageFollowing: boolean | null;
  createdAt: string | null;
};

export type BlogResponse = {
  id: string;
  authorUserId: string;
  authorUserName?: string | null;
  authorUserFullName?: string | null;
  authorUserAvatar?: string | null;
  pageId: string | null;
  pageName?: string | null;
  pageAvatarUrl?: string | null;
  regionId: string | null;
  content: string;
  imageUrls: string[] | null;
  status: BlogPostStatus;
  isPinned: boolean | null;
  allowComment: boolean | null;
  likeCount: number | null;
  shareCount: number | null;
  commentCount?: number | null;
  isLike?: boolean | null;
  isSave?: boolean | null;
  isRating?: boolean | null;
  myRating?: number | null;
  ratingScore?: number | null;
  ratingCount?: number | null;
  saveCount?: number | null;
  displayAuthorType?: BlogDisplayAuthorType | null;
  displayName?: string | null;
  displayAvatarUrl?: string | null;
  isAuthorFollowing?: boolean | null;
  isPageFollowing?: boolean | null;
  createdAt: string | null;
  updatedAt: string | null;
};

export type BlogCreateRequest = {
  authorUserId?: string;
  content: string;
  imageUrls?: string[];
  allowComment?: boolean;
  pageId?: string;
  regionId?: string;
  taggedUserIds?: string[];
  taggedCafePageIds?: string[];
  isPinned?: boolean;
};

export type BlogLikeResponse = {
  id: string;
  userId: string;
  blogId: string;
  actorContextType?: "USER" | "CAFE_PAGE" | string | null;
  actorCafePageId?: string | null;
  actorDisplayName?: string | null;
  actorAvatarUrl?: string | null;
  createdAt: string | null;
};

export type BlogSaveResponse = {
  id: string;
  userId: string;
  blogId: string;
  createdAt: string | null;
};

export type CommentResponse = {
  id: string;
  blogId: string;
  userId: string;
  actorContextType?: "USER" | "CAFE_PAGE" | string | null;
  actorCafePageId?: string | null;
  actorDisplayName?: string | null;
  actorAvatarUrl?: string | null;
  username?: string | null;
  userName?: string | null;
  authorUserAvatar?: string | null;
  authorUsername?: string | null;
  authorUserName?: string | null;
  replyToUsername?: string | null;
  parentCommentId: string | null;
  content: string;
  imageUrls: string[] | null;
  status: string | null;
  createdAt: string | null;
  updatedAt: string | null;
};

export type CommentCreateRequest = {
  blogId: string;
  actorContextType?: "USER" | "CAFE_PAGE";
  actorCafePageId?: string;
  parentCommentId?: string;
  content: string;
  imageUrls?: string[];
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
