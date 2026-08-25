/** Khớp com.cafestory.entity.enums.RecommendationTargetType. */
export type RecommendationTargetType = "USER" | "REVIEWER" | "CAFE_PAGE";

/**
 * Khớp RecommendationCardResponseDTO.
 *
 * `badge` và `isFollowing` được backend gắn bằng query batch trên đúng trang
 * kết quả (tối đa 3 query cho cả trang), không phải query cho từng bản ghi như
 * các endpoint explore cũ.
 */
export type RecommendationCardResponse = {
  avatar: string | null;
  /** Chỉ có với targetType REVIEWER. */
  badge: string | null;
  city: string | null;
  fullName: string | null;
  isFollowing: boolean;
  reason: string | null;
  targetId: string;
  targetType: RecommendationTargetType;
  userId: string | null;
  username: string | null;
};
