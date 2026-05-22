import type {
  ProfileReview,
  ReviewComposerModel,
  ReviewDraftHint,
} from "@/types/review";

export const mockReviewDraftHints: ReviewDraftHint[] = [
  { label: "Mood", value: "Quiet morning" },
  { label: "Drink", value: "Oat flat white" },
  { label: "Best for", value: "Work session" },
  { label: "Price", value: "$$" },
  { label: "Noise", value: "Soft" },
];

export const mockReviewComposer: ReviewComposerModel = {
  title: "Create cafe review",
  subtitle: "Share the useful details people need before choosing a table.",
  selectedCafe: "The Monolith",
  location: "Shoreditch, London",
  previewImage:
    "https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&w=1200&q=85",
  rating: 4,
  visitType: "Work session",
  spend: "$$",
  caption:
    "The oat flat white was smooth, the room stayed quiet, and the window seat had enough space for notes.",
  aiTags: ["Quiet corner", "Oat flat white", "Laptop friendly", "Soft music"],
  moderation: {
    label: "Moderation check",
    status: "Looks ready to publish",
  },
};

export const mockProfileReviews: ProfileReview[] = [
  {
    id: "review-1",
    cafe: "The Monolith",
    image:
      "https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&w=900&q=85",
    rating: "4.2",
    caption: "Balanced espresso and the best window seat.",
  },
  {
    id: "review-2",
    cafe: "Velvet Roast",
    image:
      "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=900&q=85",
    rating: "5.0",
    caption: "Warm service, quiet corners, beautiful ceramic cups.",
  },
  {
    id: "review-3",
    cafe: "Batch Baby",
    image:
      "https://images.unsplash.com/photo-1521017432531-fbd92d768814?auto=format&fit=crop&w=900&q=85",
    rating: "4.7",
    caption: "Their filter menu is tiny in the best way.",
  },
  {
    id: "review-4",
    cafe: "Origin Coffee",
    image:
      "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?auto=format&fit=crop&w=900&q=85",
    rating: "4.6",
    caption: "A reliable desk, good Wi-Fi, and clean milk texture.",
  },
];
