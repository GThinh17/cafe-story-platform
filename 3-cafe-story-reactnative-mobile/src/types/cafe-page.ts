import type { BlogResponse } from "./feed";

export type CafePageStatus = "DRAFT" | "ACTIVE" | "SUSPENDED" | string;

export type CafePageResponse = {
  address: string | null;
  avatarUrl: string | null;
  coverUrl: string | null;
  createdAt: string | null;
  description: string | null;
  followerCount: number | null;
  id: string;
  isFollowing: boolean | null;
  isLiked: boolean | null;
  isRating: boolean | null;
  likeCount: number | null;
  maxMembers: number | null;
  myRating: number | null;
  name: string | null;
  ownerUserId: string | null;
  pageActive: boolean | null;
  pageExpiresAt: string | null;
  ratingCount: number | null;
  ratingScore: number | null;
  regionArea: string | null;
  regionCity: string | null;
  regionCityCode?: string | null;
  regionId: string | null;
  regionProvince: string | null;
  regionProvinceCode?: string | null;
  regionStreet: string | null;
  regionWard: string | null;
  regionWardCode?: string | null;
  status: CafePageStatus | null;
  updatedAt: string | null;
};

export type CafePageBlogPageResponse = {
  hasMore: boolean | null;
  items: BlogResponse[] | null;
  nextCursor: string | null;
};

export type PageFollowResponse = {
  cafePageId: string;
  createdAt: string | null;
  id: string;
  userId: string;
};

export type PageLikeResponse = {
  cafePageId: string;
  createdAt: string | null;
  id: string;
  userId: string;
};
