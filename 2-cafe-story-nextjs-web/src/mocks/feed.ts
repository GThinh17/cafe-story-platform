import type { FeedPost, StoryItem, TopCafe } from "@/types/feed";

export const mockStories: StoryItem[] = [
  {
    name: "Curated",
    image:
      "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=160&q=80",
  },
  {
    name: "Roasters",
    image:
      "https://images.unsplash.com/photo-1442512595331-e89e73853f31?auto=format&fit=crop&w=160&q=80",
  },
  {
    name: "Vibe",
    image:
      "https://images.unsplash.com/photo-1511920170033-f8396924c348?auto=format&fit=crop&w=160&q=80",
  },
  {
    name: "Quiet",
    image:
      "https://images.unsplash.com/photo-1514066558159-fc8c737ef259?auto=format&fit=crop&w=160&q=80",
  },
  {
    name: "Work",
    image:
      "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?auto=format&fit=crop&w=160&q=80",
  },
];

export const mockFeedPosts: FeedPost[] = [
  {
    cafe: "The Monolith",
    location: "Shoreditch, London",
    author: "jessica_brew",
    time: "12 min",
    rating: "4.2",
    caption:
      "The perfect spot for a rainy Tuesday. Their oat flat white is consistently smooth with a rich, nutty finish.",
    image:
      "https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&w=1200&q=85",
    likes: "1,284",
    comments: "86",
    tags: ["Oat flat white", "Window seat"],
  },
  {
    cafe: "Velvet Roast",
    location: "Le Marais, Paris",
    author: "marco_explorer",
    time: "48 min",
    rating: "5.0",
    caption:
      "Found a hidden gem. The service is as refined as the beans they roast on-site.",
    image:
      "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=1200&q=85",
    likes: "2,018",
    comments: "143",
    tags: ["Roastery", "Minimal"],
  },
];

export const mockTopCafes: TopCafe[] = [
  { name: "The Grounds", rating: "4.9", type: "Roastery" },
  { name: "Batch Baby", rating: "4.7", type: "Filter Only" },
  { name: "Prufrock", rating: "4.8", type: "Training" },
  { name: "Origin Coffee", rating: "4.6", type: "Workspace" },
];
