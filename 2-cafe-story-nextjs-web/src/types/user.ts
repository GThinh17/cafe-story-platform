export type UserProfile = {
  displayName: string;
  username: string;
  avatarInitials: string;
  avatarImage: string;
  bio: string;
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

export type ActivityNotification = {
  id: string;
  actor: string;
  avatarInitials: string;
  action: string;
  target: string;
  time: string;
  unread?: boolean;
};

export type SuggestedUser = {
  name: string;
  username: string;
  initials: string;
};
