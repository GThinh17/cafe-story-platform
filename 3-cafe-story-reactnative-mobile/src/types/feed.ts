export type StoryItem = {
  avatarUri?: string | null;
  id: string;
  initials: string;
  isSelf?: boolean;
  label: string;
  targetId?: string | null;
  targetType?: "USER" | "REVIEWER" | "CAFE_PAGE" | "CREATE_POST" | string;
};

export type BlogDisplayAuthorType = "USER" | "CAFE_PAGE";

export type TrendWindowType = "HOUR_24" | "DAY_7" | "MONTH_1";

export type ShareType = "PUBLIC" | "PRIVATE" | "PAGE_ONLY";

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
  isFollow?: boolean | null;
  isLike?: boolean | null;
  isSave?: boolean | null;
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

export type FeedItemType = "USER_BLOG" | "CAFE_PAGE_BLOG" | "SPONSORED_CAFE" | string;

export type SponsoredCafeResponse = {
  campaignId: string;
  cafeAvatarUrl: string | null;
  cafeCoverUrl: string | null;
  imageUrl: string | null;
  cafeName: string | null;
  cafePageId: string;
  ctaLabel: string | null;
  description: string | null;
  headline: string | null;
  targetUrl: string | null;
  trackingToken: string | null;
};

export type FeedItemResponse = {
  ad: SponsoredCafeResponse | null;
  blog: BlogFeedResponse | null;
  itemType: FeedItemType;
  position: number | null;
  trackingToken: string | null;
};

export type FeedResponse = {
  hasMore: boolean | null;
  items: FeedItemResponse[] | null;
  nextCursor: string | null;
};

export type FeedParams = {
  bypassCache?: boolean;
  cursor?: string | null;
  size?: number;
};

export type FeedImpressionItemRequest = {
  blogId: string;
  position: number;
};

export type FeedImpressionBatchRequest = {
  items: FeedImpressionItemRequest[];
};

export type FeedImpressionResponse = {
  recordedCount: number | null;
};

export type BlogPostStatus = "PUBLISHED" | "DRAFT" | "ARCHIVED" | string;

export type BlogResponse = {
  allowComment: boolean | null;
  authorUserAvatar?: string | null;
  authorUserFullName?: string | null;
  authorUserId: string;
  authorUserName?: string | null;
  commentCount?: number | null;
  content: string;
  createdAt: string | null;
  displayAuthorType?: BlogDisplayAuthorType | null;
  displayAvatarUrl?: string | null;
  displayName?: string | null;
  id: string;
  imageUrls: string[] | null;
  isLike?: boolean | null;
  isPinned: boolean | null;
  isRating?: boolean | null;
  isSave?: boolean | null;
  likeCount: number | null;
  myRating?: number | null;
  pageAvatarUrl?: string | null;
  pageId: string | null;
  pageName?: string | null;
  ratingCount?: number | null;
  ratingScore?: number | null;
  regionId: string | null;
  saveCount?: number | null;
  shareCount: number | null;
  status: BlogPostStatus;
  taggedUsers?: unknown[] | null;
  updatedAt: string | null;
};

export type BlogTrendingResponse = {
  authorUserId: string;
  authorUserAvatar?: string | null;
  authorUserFullName?: string | null;
  authorUserName: string | null;
  blogId: string;
  commentCount?: number | null;
  computedAt: string | null;
  contentPreview: string | null;
  createdAt: string | null;
  displayAuthorType?: BlogDisplayAuthorType | null;
  displayAvatarUrl?: string | null;
  displayName?: string | null;
  imageUrls?: string[] | null;
  isLike?: boolean | null;
  isSave?: boolean | null;
  likeCount?: number | null;
  pageId: string | null;
  pageAvatarUrl?: string | null;
  pageCoverUrl?: string | null;
  pageName: string | null;
  pinned: boolean | null;
  rankPosition: number | null;
  reason: string | null;
  shareCount?: number | null;
  trendScore: number | null;
  windowType: TrendWindowType;
};

export type BlogCreateRequest = {
  allowComment?: boolean;
  content: string;
  imageUrls?: string[];
  isPinned?: boolean;
  pageId?: string;
  regionId?: string;
  taggedUserIds?: string[];
};

export type PostVisibility = "PUBLIC" | "PRIVATE";

export type CreatePostDraft = {
  allowComments: boolean;
  cafePageId?: string;
  caption: string;
  location?: {
    latitude?: number;
    longitude?: number;
    name: string;
    regionId?: string;
  };
  mediaAspectRatio: number;
  mediaUrls: string[];
  pinToProfile: boolean;
  taggedUserIds: string[];
  tags: string[];
  visibility: PostVisibility;
};

export type BlogFeedParams = {
  page?: number;
  regionId?: string;
  size?: number;
  windowType?: TrendWindowType;
};

export type CommentResponse = {
  actorAvatarUrl?: string | null;
  actorCafePageId?: string | null;
  actorContextType?: "USER" | "CAFE_PAGE" | string | null;
  actorDisplayName?: string | null;
  authorUserAvatar?: string | null;
  authorUserName?: string | null;
  blogId: string;
  content: string;
  createdAt: string | null;
  id: string;
  imageUrls: string[] | null;
  parentCommentId: string | null;
  status: string | null;
  updatedAt: string | null;
  userId: string;
};

export type CommentCreateRequest = {
  actorCafePageId?: string;
  actorContextType?: "USER" | "CAFE_PAGE";
  blogId: string;
  content: string;
  imageUrls?: string[];
  parentCommentId?: string;
};

export type BlogLikeResponse = {
  actorAvatarUrl?: string | null;
  actorCafePageId?: string | null;
  actorContextType?: "USER" | "CAFE_PAGE" | string | null;
  actorDisplayName?: string | null;
  blogId: string;
  createdAt: string | null;
  id: string;
  userId: string;
};

export type BlogSaveResponse = {
  blogId: string;
  createdAt: string | null;
  id: string;
  saveCount?: number | null;
  saved?: boolean | null;
  userId: string;
};

export type BlogShareRequest = {
  actorCafePageId?: string;
  actorContextType?: "USER" | "CAFE_PAGE";
  shareType?: ShareType;
};

export type BlogShareResponse = {
  actorAvatarUrl?: string | null;
  actorCafePageId?: string | null;
  actorContextType?: "USER" | "CAFE_PAGE" | string | null;
  actorDisplayName?: string | null;
  blogId: string;
  createdAt: string | null;
  id: string;
  shareType: ShareType;
  userId: string;
};

export type UserFollowResponse = {
  createdAt: string | null;
  followerUserId: string;
  followingUserId: string;
  id: string;
};
