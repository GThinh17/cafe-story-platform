export type UserProfile = {
  displayName: string;
  username: string;
  avatarInitials: string;
  avatarImage: string;
  bio: string;
  email?: string;
  location: string;
  website: string;
  stats: {
    posts: string;
    cafes: string;
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
