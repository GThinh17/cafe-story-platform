export type StoryItem = {
  avatarUri?: string | null;
  id: string;
  initials: string;
  isSelf?: boolean;
  label: string;
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

export type BlogFeedParams = {
  page?: number;
  regionId?: string;
  size?: number;
  windowType?: TrendWindowType;
};

export type CommentResponse = {
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
  blogId: string;
  content: string;
  imageUrls?: string[];
  parentCommentId?: string;
};

export type BlogLikeResponse = {
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
  shareType?: ShareType;
};

export type BlogShareResponse = {
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
