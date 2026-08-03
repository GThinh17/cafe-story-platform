# Prompt: Refactor Reviewer Ranking & Badge System

## Task

Refactor hệ thống reviewer ranking và badge để lưu scoring formula và badge thresholds vào database thay vì hardcode. Thêm ranking snapshot table để cache kết quả ranking.

## Target area
- Backend (`1-cafe-story-backend-javaspring`)

## Bối cảnh hiện tại

File chính cần refactor: `service/serviceImplement/ReviewerServiceImpl.java`

Hardcoded values cần extract:
- `calculateScore()` (line 672): `likeCount * 1 + shareCount * 3 + commentCount * 5`
- `EngagementAccumulator.score()` (line 1061): duplicate formula
- `LIKE_AMOUNT = 100`, `SHARE_AMOUNT = 300`, `COMMENT_AMOUNT = 500` (line 64-66): payout amounts
- `badgeForScore()` (line 656-670): badge thresholds IRON(<100), BRONZE(100), SILVER(300), GOLD(700), DIAMOND(1500)
- `calculateReviewerSegment()` (line 268-285): segment thresholds (giống badge thresholds)

## Yêu cầu chi tiết

### 1. Entity `ReviewerScoringFormula` — bảng `reviewer_scoring_formula`

```
Fields:
- id: UUID PK (@GeneratedValue UUID)
- like_weight: int (default 1) — trọng số like trong công thức score
- comment_weight: int (default 5) — trọng số comment
- share_weight: int (default 3) — trọng số share
- like_payout_amount: long (default 100) — VND per like cho payout
- comment_payout_amount: long (default 500)
- share_payout_amount: long (default 300)
- active: boolean (default false) — chỉ 1 row active tại 1 thời điểm
- description: String (nullable) — mô tả formula
- created_by: UUID FK → users (nullable)
- created_at, updated_at: LocalDateTime (PrePersist/PreUpdate pattern)
```

### 2. Entity `ReviewerBadgeThreshold` — bảng `reviewer_badge_threshold`

```
Fields:
- id: UUID PK
- badge: ReviewerBadge enum (IRON/BRONZE/SILVER/GOLD/DIAMOND)
- min_score: long — ngưỡng tối thiểu để đạt badge này
- formula_id: UUID FK → reviewer_scoring_formula
- created_at, updated_at

Unique constraint: (formula_id, badge)
```

Default thresholds cho formula mặc định:
- IRON: min_score = 0
- BRONZE: min_score = 100
- SILVER: min_score = 300
- GOLD: min_score = 700
- DIAMOND: min_score = 1500

### 3. Entity `ReviewerRankingSnapshot` — bảng `reviewer_ranking_snapshot`

```
Fields:
- id: UUID PK
- reviewer_id: UUID FK → reviewers
- period: String (e.g. "2026-06", "2026-W24", "2026-06-16")
- period_type: RankingPeriodType enum (DAILY, WEEKLY, MONTHLY)
- rank_position: int
- score: long
- like_count, share_count, comment_count: long
- badge: ReviewerBadge enum
- formula_id: UUID FK → reviewer_scoring_formula
- created_at, updated_at

Unique constraint: (reviewer_id, period, period_type)
```

### 4. Enum `RankingPeriodType`
```
DAILY, WEEKLY, MONTHLY
```
Đặt trong `entity/enums/RankingPeriodType.java`.

### 5. Repositories

Tạo 3 repository:
- `ReviewerScoringFormulaRepository`
  - `Optional<ReviewerScoringFormula> findByActiveTrue()`
  - `List<ReviewerScoringFormula> findAllByOrderByCreatedAtDesc()`
- `ReviewerBadgeThresholdRepository`
  - `List<ReviewerBadgeThreshold> findByFormulaIdOrderByMinScoreAsc(UUID formulaId)`
  - `void deleteByFormulaId(UUID formulaId)`
- `ReviewerRankingSnapshotRepository`
  - `List<ReviewerRankingSnapshot> findByPeriodAndPeriodTypeOrderByRankPositionAsc(String period, RankingPeriodType periodType)`
  - `Optional<ReviewerRankingSnapshot> findByReviewerReviewerIdAndPeriodAndPeriodType(UUID reviewerId, String period, RankingPeriodType periodType)`
  - `void deleteByPeriodAndPeriodType(String period, RankingPeriodType periodType)`

### 6. Service `ReviewerScoringFormulaService` + Impl

```java
public interface ReviewerScoringFormulaService {
    ReviewerScoringFormula getActiveFormula();
    ReviewerScoringFormulaResponseDTO createFormula(UUID adminUserId, ReviewerScoringFormulaRequest request);
    ReviewerScoringFormulaResponseDTO activateFormula(UUID adminUserId, UUID formulaId);
    List<ReviewerScoringFormulaResponseDTO> getAllFormulas();
    long calculateScore(long likeCount, long shareCount, long commentCount);
    long calculatePayout(long likeCount, long shareCount, long commentCount);
}
```

Impl notes:
- `getActiveFormula()`: query `findByActiveTrue()`. Nếu không có, tạo default formula (1/3/5, payout 100/300/500) và activate nó. Cache result với `@Cacheable("activeFormula")`.
- `activateFormula()`: trong `@Transactional`, set `active=false` cho formula cũ, set `active=true` cho formula mới. Evict cache.
- `calculateScore()`: `likeCount * formula.likeWeight + shareCount * formula.shareWeight + commentCount * formula.commentWeight`.
- `calculatePayout()`: `likeCount * formula.likePayout + shareCount * formula.sharePayout + commentCount * formula.commentPayout`.

### 7. Service `ReviewerBadgeThresholdService` + Impl

```java
public interface ReviewerBadgeThresholdService {
    ReviewerBadge badgeForScore(long score);
    List<ReviewerBadgeThresholdResponseDTO> getThresholds(UUID formulaId);
    List<ReviewerBadgeThresholdResponseDTO> updateThresholds(UUID adminUserId, UUID formulaId, List<ReviewerBadgeThresholdRequest> thresholds);
}
```

Impl notes:
- `badgeForScore()`: lấy thresholds của active formula, sort by min_score DESC, return badge đầu tiên mà `score >= min_score`. Cache.
- `updateThresholds()`: delete existing, insert new. Validate đủ 5 badges. Evict cache.

### 8. Service `ReviewerRankingSnapshotService` + Impl

```java
public interface ReviewerRankingSnapshotService {
    void generateSnapshot(RankingPeriodType periodType);
    List<ReviewerRankingSnapshotResponseDTO> getRanking(String period, RankingPeriodType periodType, int page, int limit);
}
```

Impl notes:
- `generateSnapshot()`: tính period string từ current date + periodType, aggregate engagement cho tất cả reviewer (reuse logic từ `ReviewerServiceImpl.aggregateEngagementForAllUsers`), tính score bằng active formula, sort, assign rank, delete old snapshot cho period này, save mới.
- `getRanking()`: query snapshot table, paginate.

### 9. Refactor `ReviewerServiceImpl`

- Inject `ReviewerScoringFormulaService` và `ReviewerBadgeThresholdService`.
- Xóa constants `LIKE_AMOUNT`, `SHARE_AMOUNT`, `COMMENT_AMOUNT`.
- Thay `calculateScore(like, share, comment)` → `formulaService.calculateScore(like, share, comment)`.
- Thay `badgeForScore(score)` → `badgeThresholdService.badgeForScore(score)`.
- Thay payout calculation → `formulaService.calculatePayout(like, share, comment)`.
- Cập nhật `EngagementAccumulator.score()` → nhận weights từ formula (truyền qua constructor hoặc method parameter).
- `calculateReviewerPayout()` → dùng formula service.
- `calculateReviewerBadge()` → dùng badge threshold service.

### 10. Admin Controller `AdminReviewerConfigController`

Path: `/api/admin/reviewer-config`

Endpoints:
- `GET /formulas` → list all formulas
- `POST /formulas` → create new formula (admin only)
- `PUT /formulas/{id}/activate` → activate formula (admin only)
- `GET /formulas/{id}/thresholds` → get badge thresholds for formula
- `PUT /formulas/{id}/thresholds` → update badge thresholds (admin only)
- `POST /ranking/generate` → trigger manual ranking snapshot generation (admin only)

Tất cả admin endpoints cần `@AuthenticationPrincipal` + validate admin role.

### 11. DTOs

Request DTOs (đặt trong `dto/requestDTO/`):
- `ReviewerScoringFormulaRequest`: likeWeight, commentWeight, shareWeight, likePayoutAmount, commentPayoutAmount, sharePayoutAmount, description
- `ReviewerBadgeThresholdRequest`: badge (ReviewerBadge), minScore (long)

Response DTOs (đặt trong `dto/responseDTO/reviewer/`):
- `ReviewerScoringFormulaResponseDTO`: id, likeWeight, commentWeight, shareWeight, likePayoutAmount, commentPayoutAmount, sharePayoutAmount, active, description, createdAt
- `ReviewerBadgeThresholdResponseDTO`: id, badge, minScore, formulaId
- `ReviewerRankingSnapshotResponseDTO`: id, reviewerId, period, periodType, rankPosition, score, likeCount, shareCount, commentCount, badge, formulaId

### 12. Scheduled Job `ReviewerRankingSnapshotJob`

Đặt trong `until/schedule/` theo pattern của `BlogRecommendationCalculationJob`.
- Chạy daily lúc 2:00 AM: generate DAILY snapshot.
- Chạy weekly Monday 3:00 AM: generate WEEKLY snapshot.
- Chạy monthly ngày 1 lúc 4:00 AM: generate MONTHLY snapshot.

### 13. Data Initialization

Tạo default data khi app startup (dùng `@PostConstruct` hoặc `ApplicationRunner`):
- Nếu chưa có formula nào: tạo default formula (1/3/5, payout 100/300/500), set active = true.
- Nếu chưa có thresholds cho formula active: tạo default thresholds (IRON=0, BRONZE=100, SILVER=300, GOLD=700, DIAMOND=1500).

### 14. Security Config

Thêm `/api/admin/reviewer-config/**` vào SecurityConfig, yêu cầu ADMIN role.

## Acceptance criteria

- Score formula đọc từ DB, không hardcode.
- Badge thresholds đọc từ DB, không hardcode.
- Payout amounts đọc từ DB, không hardcode.
- Admin có thể tạo formula mới, activate formula, update thresholds qua API.
- Ranking snapshot được tính và lưu vào DB.
- Default values khi init phải match chính xác giá trị hiện tại (1×like + 3×share + 5×comment, badge thresholds 0/100/300/700/1500, payout 100/300/500).
- Existing endpoints (`/api/reviewers/ranking`, stats, badges, payouts) vẫn hoạt động đúng.
- Unit tests cover service mới và logic đã refactor.

## Constraints

- Follow CafeStory conventions (xem CLAUDE.md).
- **Bắt buộc inspect và follow skills**: `cafestory-naming-layout`, `cafestory-engineering-workflows`, `spring-unit-api-testing`.
- Entity dùng singular PascalCase, table dùng snake_case.
- DTO request kết thúc `Request`, response kết thúc `ResponseDTO`.
- Service impl kết thúc `Impl`.
- Không expose Entity trực tiếp trong API response.
- UUID primary key.
- Controller mỏng, business logic trong Service.
- Không refactor ngoài scope.
- Reuse pattern/component/helper hiện có.

## Output expected

- **Skills used**: liệt kê skills đã dùng.
- Files changed/created.
- Behavior changed.
- Validation commands đã chạy.
- Risks/TODO nếu còn.
