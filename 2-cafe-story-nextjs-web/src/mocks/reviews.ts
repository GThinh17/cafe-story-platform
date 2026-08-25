import type { ReviewComposerModel, ReviewDraftHint } from "@/types/review";

export const mockReviewDraftHints: ReviewDraftHint[] = [
  { label: "Mood", value: "Quiet morning" },
  { label: "Drink", value: "Oat flat white" },
  { label: "Best for", value: "Work session" },
  { label: "Price", value: "$$" },
  { label: "Noise", value: "Soft" },
];

export const mockReviewComposer: ReviewComposerModel = {
  title: "New Review",
  subtitle: "Share the useful details people need before choosing a table.",
  selectedPhotos: [
    {
      id: "placeholder",
      alt: "Empty selected photo slot",
    },
    {
      id: "croissant",
      image: "/images/reviews/new-review-croissant.jpg",
      alt: "Close-up of a flaky croissant on a ceramic plate",
    },
    {
      id: "roastery-counter",
      image: "/images/reviews/new-review-roastery-counter.jpg",
      alt: "Boutique coffee roastery counter with soft light",
    },
  ],
  selectedCafe: "The Monolith",
  location: "Shoreditch, London",
  previewImage: "/images/reviews/new-review-cafe-interior.jpg",
  rating: 4,
  visitType: "Work session",
  spend: "$$",
  caption:
    "The oat flat white was smooth, the room stayed quiet, and the window seat had enough space for notes.",
  taggedPeople: ["@coffeelover"],
  suggestedTags: [
    { label: "Vintage", selected: true },
    { label: "Workspace", selected: true },
    { label: "Photo", selected: true },
    { label: "Quiet", selected: false },
    { label: "Minimalist", selected: false },
  ],
  aiTags: ["Quiet corner", "Oat flat white", "Laptop friendly", "Soft music"],
  moderation: {
    label: "Moderation check",
    status: "Looks ready to publish",
  },
};
