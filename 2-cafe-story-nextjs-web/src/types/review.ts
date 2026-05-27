export type ReviewDraftHint = {
  label: string;
  value: string;
};

export type ReviewComposerModel = {
  title: string;
  subtitle: string;
  selectedPhotos: {
    id: string;
    image?: string;
    alt: string;
  }[];
  selectedCafe: string;
  location: string;
  previewImage: string;
  rating: number;
  visitType: string;
  spend: string;
  caption: string;
  taggedPeople: string[];
  suggestedTags: {
    label: string;
    selected: boolean;
  }[];
  aiTags: string[];
  moderation: {
    label: string;
    status: string;
  };
};

export type ProfileReview = {
  id: string;
  cafe: string;
  image: string;
  rating: string;
  caption: string;
};

export type CafeReviewPost = {
  id: string;
  cafe: string;
  neighborhood: string;
  image: string;
  rating: string;
  excerpt: string;
};

export type BlogDetailComment = {
  id: string;
  author: string;
  avatar: string;
  body: string;
  time: string;
};

export type BlogDetailReview = {
  id: string;
  cafe: string;
  reviewerName: string;
  reviewerAvatar: string;
  date: string;
  rating: string;
  ratingMax: string;
  images: {
    id: string;
    src: string;
    alt: string;
  }[];
  paragraphs: string[];
  tags: string[];
  likes: string;
  commentsCount: string;
  viewerAvatar: string;
  comments: BlogDetailComment[];
};
