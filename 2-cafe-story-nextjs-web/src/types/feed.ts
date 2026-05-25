export type StoryItem = {
  name: string;
  image: string;
};

export type FeedPost = {
  cafe: string;
  location: string;
  author: string;
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
