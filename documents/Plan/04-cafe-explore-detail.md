# Phase 04 - Cafe Explore And Detail

## Mục tiêu

Gắn các màn cafe với API `CafePage`: explore danh sách cafe, chi tiết cafe, và blog thuộc cafe.

## Phạm vi frontend

- `2-cafe-story-nextjs-web/src/app/(main)/explore/page.tsx`
- `2-cafe-story-nextjs-web/src/app/(main)/cafes/[id]/page.tsx`
- `2-cafe-story-nextjs-web/src/components/cafe/explore-cafes.tsx`
- `2-cafe-story-nextjs-web/src/components/cafe/cafe-card.tsx`
- `2-cafe-story-nextjs-web/src/components/cafe/cafe-page.tsx`
- `2-cafe-story-nextjs-web/src/components/cafe/cafe-suggestion-list.tsx`
- `2-cafe-story-nextjs-web/src/lib/api/cafes.ts`
- `2-cafe-story-nextjs-web/src/types/cafe.ts`
- `2-cafe-story-nextjs-web/src/features/cafes/`

## Backend liên quan

- `GET /api/cafe-pages`
- `GET /api/cafe-pages/{cafePageId}`
- `GET /api/cafe-pages/{cafePageId}/blogs`
- `POST /api/cafe-pages/{cafePageId}/likes`
- `DELETE /api/cafe-pages/{cafePageId}/likes`
- `POST /api/cafe-pages/{cafePageId}/follows`
- `DELETE /api/cafe-pages/{cafePageId}/follows`

## Việc cần làm

1. Tạo `src/lib/api/cafes.ts`:
   - `getCafePages()`;
   - `getCafePageById(cafePageId)`;
   - `getCafePageBlogs(cafePageId)`;
   - `likeCafePage(cafePageId)`;
   - `unlikeCafePage(cafePageId)`;
   - `followCafePage(cafePageId)`;
   - `unfollowCafePage(cafePageId)`.
2. Chuẩn hóa `CafePageResponseDTO` sang `CafeSummary` hoặc model UI mới.
3. Explore dùng API list thay vì `mockCafeSummaries`.
4. Cafe detail dùng API detail và API blog theo cafe.
5. `CafeSuggestionList` nên nhận data từ route/layout thay vì tự import mock.

## Rủi ro cần kiểm tra

- Mock UI có gallery nhiều ảnh, backend có thể chỉ có một ảnh.
- Review count/rating có thể chưa có sẵn trong cafe response.
- Like/follow status của current user có thể cần endpoint riêng nếu backend chưa trả.

## Tiêu chí hoàn thành

- `/explore` hiển thị cafe thật.
- `/cafes/[id]` hiển thị chi tiết cafe thật.
- Blog trong cafe detail lấy từ `GET /api/cafe-pages/{id}/blogs`.
- Không crash nếu cafe không có ảnh.
- `npm run typecheck` pass.

