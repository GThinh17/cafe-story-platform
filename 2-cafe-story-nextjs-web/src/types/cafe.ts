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
  coverImage?: string;
  coverImageAlt?: string;
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

export type CafePageRankingResponse = {
  id: string;
  ownerUserId: string | null;
  regionId: string | null;
  regionCity: string | null;
  regionProvince: string | null;
  regionArea: string | null;
  name: string;
  address: string;
  description: string | null;
  avatarUrl: string | null;
  coverUrl: string | null;
  status: "ACTIVE" | "DRAFT" | "SUSPENDED" | string;
  likeCount: number | null;
  followerCount: number | null;
  pageActive: boolean | null;
  rankingScore: number | null;
  rankPosition: number | null;
  createdAt: string | null;
};

export type CafeTopParams = {
  city?: string;
  regionId?: string;
  size?: number;
};

export type CafeEditorialCollection = {
  id: string;
  eyebrow: string;
  title: string;
  ctaLabel: string;
  image: string;
  alt: string;
};

export type CafeCategory = {
  id: string;
  label: string;
  icon: "sparkle" | "laptop" | "coffee" | "gem" | "leaf";
};

export type CafeMenu = {
  signature: CafeMenuItem[];
  seasonal: CafeMenuItem[];
  classics: CafeMenuItem[];
  pastries: CafeMenuItem[];
};

export type CafeMenuItem = {
  id: string;
  name: string;
  description: string;
  price: number;
  image?: string;
  images?: {
    src: string;
    alt: string;
    label?: string;
  }[];
  badge?: string;
};
