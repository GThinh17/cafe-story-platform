export type UserProfile = {
  displayName: string;
  username: string;
  avatarInitials: string;
  avatarImage: string;
  bio: string;
  email?: string;
  location: string;
  website: string;
  badge?: string | null;
  stats: {
    posts: string;
    following: string;
    followers: string;
  };
};

export type ProfileHighlight = {
  id: string;
  label: string;
  image: string;
};

export type ReviewerProfile = {
  displayName: string;
  username: string;
  title: string;
  badge: string;
  avatarImage: string;
  bio: string;
  location: string;
  stats: {
    reviews: string;
    followers: string;
    following: string;
  };
  visualDiary: {
    alt: string;
    image: string;
    label: string;
  }[];
};

export type ActivityNotification = {
  id: string;
  section: "thisMonth" | "earlier";
  actors: string[];
  avatarImages: string[];
  message: string;
  date: string;
  thumbnailImage?: string;
  iconLabel?: string;
  unread?: boolean;
};

export type SuggestedUser = {
  name: string;
  username: string;
  initials: string;
};

export type UserResponse = {
  avatar?: string | null;
  imageUrl?: string | null;
  profileImage?: string | null;
  userId: string;
  userName: string;
  userFullName: string | null;
  userEmail: string | null;
  userPhone: number | null;
  userAvatar: string | null;
  userDescription?: string | null;
  userLike: number | null;
  userFollower: number | null;
  followingCount: number | null;
  accountStatus: boolean | null;
  regionId: string | null;
  regionCityCode?: string | null;
  regionCity: string | null;
  regionProvinceCode?: string | null;
  regionProvince: string | null;
  regionWardCode?: string | null;
  regionWard: string | null;
  regionArea: string | null;
  regionStreet: string | null;
  isFollowing: boolean | null;
};

export type UserFollowResponse = {
  id: string;
  followerUserId: string;
  followingUserId: string;
  createdAt: string | null;
};
