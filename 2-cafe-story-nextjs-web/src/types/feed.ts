export type StoryItem = {
  name: string;
  image: string;
};

export type FeedPostComment = {
  id: string;
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
  author: string;
  authorUserId?: string;
  authorUsername?: string;
  authorAvatar?: string;
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
