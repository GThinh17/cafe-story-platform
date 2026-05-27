export type StoryItem = {
  name: string;
  image: string;
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
  likes: string;
  comments: string;
  tags: string[];
};

export type TopCafe = {
  name: string;
  rating: string;
  type: string;
};
