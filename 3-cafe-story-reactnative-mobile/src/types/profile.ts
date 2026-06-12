import type { ImageSourcePropType } from "react-native";

export type UserResponse = {
  accountStatus: boolean | null;
  followingCount: number | null;
  isFollowing: boolean | null;
  regionArea: string | null;
  regionCity: string | null;
  regionId: string | null;
  regionProvince: string | null;
  regionStreet: string | null;
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

export type UserPostPreview = {
  caption: string;
  commentCount: number;
  id: string;
  image?: ImageSourcePropType;
  imageUri?: string | null;
  likeCount: number;
};
