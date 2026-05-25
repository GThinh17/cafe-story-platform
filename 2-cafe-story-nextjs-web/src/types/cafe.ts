export type CafeSummary = {
  id: string;
  name: string;
  location: string;
  address: string;
  type: string;
  rating: string;
  reviewCount: string;
  distance: string;
  priceLevel: string;
  hours: string;
  status?: string;
  photoCount?: string;
  image: string;
  gallery: string[];
  tags: string[];
  amenities: string[];
  popularDrinks: string[];
  description: string;
  featureSummary?: string;
  peakHours?: string;
  openingHours?: {
    day: string;
    time: string;
    highlight?: boolean;
  }[];
  communityPhotos?: {
    image: string;
    alt: string;
  }[];
};

export type CafeEditorialCollection = {
  id: string;
  eyebrow: string;
  title: string;
  ctaLabel: string;
  image: string;
  alt: string;
};
