export type StoryItem = {
  name: string;
  image: string;
};

export type FeedPostComment = {
  id: string;
  author: string;
  authorAvatar?: string;
  body: string;
  time?: string;
  likes?: string;
  replies?: string;
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
  authorAvatar?: string;
  time: string;
  rating: string;
  caption: string;
  image: string;
  media?: FeedPostMedia[];
  likes: string;
  comments: string | FeedPostComment[];
  commentItems?: FeedPostComment[];
  shares?: string;
  tags: string[];
};

export type TopCafe = {
  name: string;
  rating: string;
  type: string;
};
