export type RecommendationTargetType = "USER" | "REVIEWER" | "CAFE_PAGE" | string;

export type RecommendationCardResponse = {
  avatar: string | null;
  city: string | null;
  fullName: string | null;
  reason: string | null;
  targetId: string;
  targetType: RecommendationTargetType;
  userId?: string | null;
  username: string | null;
};
