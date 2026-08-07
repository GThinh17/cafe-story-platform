import type { BlogResponse } from "@/types/blog";

export type CafeSummary = {
  id: string;
  ownerUserId?: string | null;
  name: string;
  location: string;
  address: string;
  type: string;
  rating: string;
  reviewCount: string;
  distance: string;
  priceLevel: string;
  hours: string;
  status?: string;
  photoCount?: string;
  likeCount?: number;
  isLiked?: boolean;
  isFollowing?: boolean | null;
  pageActive?: boolean | null;
  image: string;
  avatarImage?: string;
  avatarImageAlt?: string;
  coverImage?: string;
  coverImageAlt?: string;
  gallery: string[];
  tags: string[];
  amenities: string[];
  popularDrinks: string[];
  description: string;
  regionCity?: string | null;
  regionCityCode?: string | null;
  regionProvince?: string | null;
  regionProvinceCode?: string | null;
  regionWard?: string | null;
  regionWardCode?: string | null;
  regionArea?: string | null;
  regionStreet?: string | null;
  featureSummary?: string;
  peakHours?: string;
  openingHours?: {
    day: string;
    time: string;
    highlight?: boolean;
  }[];
  communityPhotos?: {
    image: string;
    alt: string;
  }[];
};

export type CafePageResponse = {
  id: string;
  ownerUserId: string | null;
  regionId: string | null;
  regionCityCode?: string | null;
  regionCity: string | null;
  regionProvinceCode?: string | null;
  regionProvince: string | null;
  regionWardCode?: string | null;
  regionWard: string | null;
  regionArea: string | null;
  regionStreet: string | null;
  name: string;
  address: string;
  description: string | null;
  avatarUrl: string | null;
  coverUrl: string | null;
  status: "ACTIVE" | "DRAFT" | "SUSPENDED" | string;
  likeCount: number | null;
  followerCount: number | null;
  isFollowing: boolean | null;
  isLiked: boolean | null;
  isRating: boolean | null;
  myRating: number | null;
  ratingScore: number | null;
  ratingCount: number | null;
  maxMembers: number | null;
  pageActive: boolean | null;
  pageExpiresAt: string | null;
  createdAt: string | null;
  updatedAt: string | null;
};

export type CafePageFollowResponse = {
  id: string;
  userId: string;
  cafePageId: string;
  createdAt: string | null;
};

export type CafePageCreateRequest = {
  name: string;
  address: string;
  description?: string;
  avatarUrl?: string;
  coverUrl?: string;
  regionId?: string;
  coOwnerUserIds?: string[];
};

export type CafePageUpdateRequest = {
  name?: string;
  address?: string;
  description?: string;
  avatarUrl?: string;
  coverUrl?: string;
  regionId?: string;
  status?: "ACTIVE" | "DRAFT" | "SUSPENDED" | string;
};

export type PageMemberStatus = "PENDING" | "ACTIVE" | "REJECTED";

export type PageMemberRoleName = "OWNER" | "CO_OWNER" | "MEMBER";

export type PageMemberResponse = {
  cafePageId: string;
  userId: string;
  roleName: string;
  status: PageMemberStatus;
  createdAt: string | null;
  updatedAt: string | null;
};

export type PageMemberAddRequest = {
  userId: string;
  roleName: PageMemberRoleName;
};

export type PageMemberStatusUpdateRequest = {
  status: PageMemberStatus;
};

export type CafePageBlogCursorResponse = {
  items: BlogResponse[];
  nextCursor: string | null;
  hasMore: boolean;
};

export type CafePageLikeResponse = {
  id: string;
  userId: string;
  cafePageId: string;
  createdAt: string | null;
};

export type CafePageRankingResponse = {
  id: string;
  ownerUserId: string | null;
  regionId: string | null;
  regionCity: string | null;
  regionProvince: string | null;
  regionArea: string | null;
  name: string;
  address: string;
  description: string | null;
  avatarUrl: string | null;
  coverUrl: string | null;
  status: "ACTIVE" | "DRAFT" | "SUSPENDED" | string;
  likeCount: number | null;
  followerCount: number | null;
  pageActive: boolean | null;
  rankingScore: number | null;
  rankPosition: number | null;
  isFollowing: boolean | null;
  createdAt: string | null;
};

export type CafeTopParams = {
  city?: string;
  area?: string;
  province?: string;
  regionId?: string;
  size?: number;
};

export type CafeCategory = {
  id: string;
  label: string;
  icon: "sparkle" | "laptop" | "coffee" | "gem" | "leaf";
};

export type CafeMenu = {
  signature: CafeMenuItem[];
  seasonal: CafeMenuItem[];
  classics: CafeMenuItem[];
  pastries: CafeMenuItem[];
};

export type CafeMenuItem = {
  id: string;
  name: string;
  description: string;
  price: number;
  image?: string;
  images?: {
    src: string;
    alt: string;
    label?: string;
  }[];
  badge?: string;
};
