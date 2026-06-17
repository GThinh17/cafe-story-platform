import type { ImageSourcePropType } from "react-native";

export type UserResponse = {
  accountStatus: boolean | null;
  cafePageId?: string | null;
  followingCount: number | null;
  isFollowing: boolean | null;
  pageId?: string | null;
  regionArea: string | null;
  regionCityCode?: string | null;
  regionCity: string | null;
  regionId: string | null;
  regionProvinceCode?: string | null;
  regionProvince: string | null;
  regionStreet: string | null;
  regionWardCode?: string | null;
  regionWard: string | null;
  userAvatar: string | null;
  userDescription: string | null;
  userEmail: string | null;
  userFollower: number | null;
  userFullName: string | null;
  userId: string;
  userLike: number | null;
  userName: string;
  userPhone: number | null;
};

export type UserUpdateRequest = {
  accountStatus?: boolean;
  regionId?: string;
  userAvatar?: string;
  userDescription?: string;
  userEmail?: string;
  userFullName?: string;
  userName?: string;
  userPassword?: string;
  userPhone?: number;
};

export type UserRegionUpdateRequest = {
  city?: string;
  cityCode?: string;
  province?: string;
  provinceCode?: string;
  street?: string;
  ward?: string;
  wardCode?: string;
};

export type UserPostPreview = {
  caption: string;
  commentCount: number;
  id: string;
  image?: ImageSourcePropType;
  imageUri?: string | null;
  likeCount: number;
};

export type ProfileContentTab = "posts" | "saved" | "shared" | "tagged";

export type ReviewerBadge =
  | "IRON"
  | "BRONZE"
  | "SILVER"
  | "GOLD"
  | "PLATINUM"
  | "DIAMOND"
  | string;

export type ReviewerResponse = {
  avatar: string | null;
  badge: ReviewerBadge | null;
  expireDate: string | null;
  follow: number;
  follower: number;
  like: number;
  name: string | null;
  reviewerId: string;
  role: string | null;
  score: number;
  userId: string;
};

export type RecommendationTargetType = "USER" | "REVIEWER" | "CAFE_PAGE" | string;

export type RecommendationCardResponse = {
  avatar: string | null;
  city: string | null;
  fullName: string | null;
  reason: string | null;
  targetId: string;
  targetType: RecommendationTargetType;
  userId?: string | null;
  username: string | null;
};
