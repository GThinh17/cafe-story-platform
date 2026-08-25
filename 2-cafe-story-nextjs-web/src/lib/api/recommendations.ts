import { apiCacheTtl, cachedApiCall } from "@/lib/api/api-cache";
import { apiFetch } from "@/lib/api/client";
import { apiEndpoints } from "@/lib/api/endpoints";
import type { RecommendationCardResponse } from "@/types/recommendation";

/**
 * Gợi ý reviewer/cafe page cho trang explore — cùng endpoint mà app mobile dùng.
 *
 * <p>Backend đã lọc, xếp hạng theo vùng và cắt trang ngay trong SQL, kèm cache
 * Redis theo (user, page, size). Nhờ vậy client không cần chuỗi fallback theo
 * khu vực như các endpoint explore cũ: thứ tự ưu tiên cùng vùng → cùng tỉnh →
 * còn lại nằm sẵn trong mệnh đề ORDER BY.
 *
 * <p>Cả hai endpoint yêu cầu đăng nhập (`requireUserId`), nên nhánh khách vãng
 * lai phải dùng đường khác.
 */
export function getReviewerRecommendations(page = 0, size = 20) {
  return cachedApiCall(
    `recommendations:reviewers:${page}:${size}`,
    apiCacheTtl.dynamic,
    () =>
      apiFetch<RecommendationCardResponse[]>(
        apiEndpoints.recommendations.reviewers(page, size),
        { method: "GET" },
      ),
  );
}

export function getCafePageRecommendations(page = 0, size = 20) {
  return cachedApiCall(
    `recommendations:cafe-pages:${page}:${size}`,
    apiCacheTtl.dynamic,
    () =>
      apiFetch<RecommendationCardResponse[]>(
        apiEndpoints.recommendations.cafePages(page, size),
        { method: "GET" },
      ),
  );
}
