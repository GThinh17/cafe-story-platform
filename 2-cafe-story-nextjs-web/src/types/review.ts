export type ReviewDraftHint = {
  label: string;
  value: string;
};

export type ReviewComposerModel = {
  title: string;
  subtitle: string;
  selectedCafe: string;
  location: string;
  previewImage: string;
  rating: number;
  visitType: string;
  spend: string;
  caption: string;
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
