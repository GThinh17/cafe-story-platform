export type StoryItem = {
  name: string;
  image: string;
};

export type FeedPostComment = {
  id: string;
  localStatus?: "sending" | "sent" | "error";
  serverId?: string;
  author: string;
  authorUsername?: string;
  authorAvatar?: string;
  body: string;
  time?: string;
  likes?: string;
  likeCount?: number;
  isLiked?: boolean;
  replies?: string;
  replyToAuthor?: string;
  replyToUsername?: string;
  replyItems?: FeedPostComment[];
};

export type FeedPostMedia = {
  id?: string;
  src: string;
  alt?: string;
  type?: "image" | "video";
};

export type FeedPost = {
  id?: string;
  cafe: string;
  location: string;
  locationLabel?: string;
  author: string;
  authorUserId?: string;
  authorUsername?: string;
  authorAvatar?: string;
  displayAuthorType?: "USER" | "CAFE_PAGE" | null;
  displayName?: string | null;
  displayAvatarUrl?: string | null;
  pageId?: string | null;
  pageName?: string | null;
  pageAvatarUrl?: string | null;
  time: string;
  rating: string;
  caption: string;
  image: string;
  media?: FeedPostMedia[];
  likes: string;
  likeCount?: number;
  isLiked?: boolean;
  allowComment?: boolean;
  comments: string | FeedPostComment[];
  commentCount?: number;
  commentItems?: FeedPostComment[];
  shares?: string;
  tags: string[];
};

export type TopCafe = {
  name: string;
  rating: string;
  type: string;
};
