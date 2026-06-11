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
