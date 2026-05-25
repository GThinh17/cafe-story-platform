import type {
  CafeReviewPost,
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

export const mockCafeReviewPosts: CafeReviewPost[] = [
  {
    id: "cafe-review-1",
    cafe: "The Arch Coffee",
    neighborhood: "DUMBO, Brooklyn",
    image: "/images/cafes/velvet-roast/minimal-interior.jpg",
    rating: "4.9",
    excerpt:
      "The sunlight hits the marble counters just right at 10 AM. Best for a quiet reset before work.",
  },
  {
    id: "cafe-review-2",
    cafe: "Dark Roast Collective",
    neighborhood: "Lower East Side",
    image: "/images/cafes/velvet-roast/espresso-machine.jpg",
    rating: "5.0",
    excerpt:
      "Pure coffee science. The Ethiopia single-origin pour over is bright, floral, and worth the wait.",
  },
  {
    id: "cafe-review-3",
    cafe: "L'Avenue Patisserie",
    neighborhood: "Upper West Side",
    image: "/images/cafes/velvet-roast/croissant-flatlay.jpg",
    rating: "4.7",
    excerpt:
      "Transported to Paris. The laminations on this croissant are delicate, crisp, and buttery.",
  },
];

export const mockReviewerRecentReviews: CafeReviewPost[] = [
  {
    id: "reviewer-review-1",
    cafe: "The Arch Coffee",
    neighborhood: "DUMBO, Brooklyn",
    image: "/images/reviewers/julianne/review-arch.jpg",
    rating: "4.9",
    excerpt:
      "The sunlight hits the marble counters just right at 10 AM. Best oat flat white in the borough.",
  },
  {
    id: "reviewer-review-2",
    cafe: "Dark Roast Collective",
    neighborhood: "Lower East Side",
    image: "/images/reviewers/julianne/review-dark-roast.jpg",
    rating: "5.0",
    excerpt:
      "Pure coffee science. The Ethiopia single-origin pour over is life-changing.",
  },
  {
    id: "reviewer-review-3",
    cafe: "L'Avenue Patisserie",
    neighborhood: "Upper West Side",
    image: "/images/reviewers/julianne/review-croissant.jpg",
    rating: "4.7",
    excerpt:
      "Transported to Paris. The laminations on this croissant are actually illegal.",
  },
];

export const mockReviewerProfileReviews: ProfileReview[] = [
  {
    id: "reviewer-post-1",
    cafe: "The Arch Coffee",
    image: "/images/reviewers/julianne/review-arch.jpg",
    rating: "4.9",
    caption: "Best oat flat white in the borough.",
  },
  {
    id: "reviewer-post-2",
    cafe: "Dark Roast Collective",
    image: "/images/reviewers/julianne/review-dark-roast.jpg",
    rating: "5.0",
    caption: "Precise pour overs and a quiet morning room.",
  },
  {
    id: "reviewer-post-3",
    cafe: "L'Avenue Patisserie",
    image: "/images/reviewers/julianne/review-croissant.jpg",
    rating: "4.7",
    caption: "A pastry stop worth saving.",
  },
  {
    id: "reviewer-post-4",
    cafe: "Minimal Cup",
    image: "/images/reviewers/julianne/diary-white-cup.jpg",
    rating: "4.6",
    caption: "Soft light, clean cups, and a slower pace.",
  },
  {
    id: "reviewer-post-5",
    cafe: "Typewriter Table",
    image: "/images/reviewers/julianne/diary-typewriter.jpg",
    rating: "4.8",
    caption: "A corner made for writing reviews.",
  },
  {
    id: "reviewer-post-6",
    cafe: "Blue Saucer",
    image: "/images/reviewers/julianne/diary-latte.jpg",
    rating: "4.8",
    caption: "Balanced milk texture and a bright espresso finish.",
  },
];
