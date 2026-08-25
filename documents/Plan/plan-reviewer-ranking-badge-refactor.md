# Feature Plan: Reviewer Ranking & Badge Refactor

## Scope

- Target area: Backend (`1-cafe-story-backend-javaspring`)
- User-facing behavior: Ranking và badge calculation giữ nguyên behavior, nhưng công thức tính score và badge thresholds được lưu trong DB thay vì hardcode.
- Backend/API behavior:
  - Thêm entity `ReviewerScoringFormula` lưu trọng số scoring (like_weight, comment_weight, share_weight).
  - Thêm entity `ReviewerRankingSnapshot` lưu kết quả ranking đã tính (thay vì tính realtime mỗi request).
  - Thêm entity `ReviewerBadgeThreshold` lưu ngưỡng score cho mỗi badge level.
  - Admin API để CRUD formula và badge thresholds.
  - Scheduler job tính ranking snapshot định kỳ.
- Data model impact: 3 bảng mới: `reviewer_scoring_formula`, `reviewer_ranking_snapshot`, `reviewer_badge_threshold`.
- Security/authorization impact: Admin-only cho CRUD formula/thresholds.

## Existing Evidence

- Files inspected:
  - `entity/Reviewer.java` — entity reviewer, không có score/rank field.
  - `entity/ReviewerBadgeHistory.java` — lưu badge theo tháng, có score, like/share/comment count.
  - `entity/ReviewerPayout.java` — payout với hardcoded LIKE_AMOUNT/SHARE_AMOUNT/COMMENT_AMOUNT.
  - `entity/enums/ReviewerBadge.java` — enum IRON/BRONZE/SILVER/GOLD/DIAMOND.
  - `service/serviceImplement/ReviewerServiceImpl.java` — **core file cần refactor**.
  - `controller/ReviewerController.java` — thin controller, ít thay đổi.
  - `cafestory-schema.dbml` — schema hiện tại.
- Patterns reused:
  - UUID primary key, `@GeneratedValue(strategy = GenerationType.UUID)`.
  - `PrePersist`/`PreUpdate` cho timestamps.
  - Service → Repository pattern.
  - Admin validation qua `validateAdmin()`.
- Business rules confirmed:
  - Score hiện tại: `likeCount * 1 + shareCount * 3 + commentCount * 5` (hardcoded line 672-674).
  - Badge thresholds hardcoded: IRON(<100), BRONZE(100-299), SILVER(300-699), GOLD(700-1499), DIAMOND(>=1500) (line 656-670).
  - Payout amounts hardcoded: LIKE=100, SHARE=300, COMMENT=500 (line 64-66).
- Assumptions:
  - Chỉ có 1 formula active tại một thời điểm.
  - Badge thresholds cũng chỉ 1 bộ active.
  - Ranking snapshot tính theo period (day/week/month).

## Hardcoded Values Cần Extract

| Constant | Current Value | Location | Mục đích |
|---|---|---|---|
| `likeCount * 1` | weight = 1 | `calculateScore()` line 672 | Score ranking |
| `shareCount * 3` | weight = 3 | `calculateScore()` line 672 | Score ranking |
| `commentCount * 5` | weight = 5 | `calculateScore()` line 672 | Score ranking |
| `EngagementAccumulator.score()` | same formula | line 1061 | Duplicate logic |
| `LIKE_AMOUNT = 100` | 100 VND/like | line 64 | Payout calculation |
| `SHARE_AMOUNT = 300` | 300 VND/share | line 65 | Payout calculation |
| `COMMENT_AMOUNT = 500` | 500 VND/comment | line 66 | Payout calculation |
| Badge thresholds | <100, 100, 300, 700, 1500 | `badgeForScore()` line 656-670 | Badge assignment |
| Segment thresholds | 0, 100, 300, 700, 1500 | `calculateReviewerSegment()` line 268-285 | Segment classification |

## Implementation Steps

### Phase 1: New Entities & Repository

1. **Tạo entity `ReviewerScoringFormula`** — bảng `reviewer_scoring_formula`
   - Fields: `id` (UUID PK), `like_weight` (int, default 1), `comment_weight` (int, default 5), `share_weight` (int, default 3), `like_payout_amount` (long, default 100), `comment_payout_amount` (long, default 500), `share_payout_amount` (long, default 300), `active` (boolean), `created_by` (UUID FK → users), `created_at`, `updated_at`.
   - Constraint: chỉ 1 row có `active = true` tại một thời điểm.

2. **Tạo entity `ReviewerBadgeThreshold`** — bảng `reviewer_badge_threshold`
   - Fields: `id` (UUID PK), `badge` (ReviewerBadge enum), `min_score` (long), `formula_id` (UUID FK → reviewer_scoring_formula), `created_at`, `updated_at`.
   - Mỗi formula có 5 rows tương ứng 5 badge levels.

3. **Tạo entity `ReviewerRankingSnapshot`** — bảng `reviewer_ranking_snapshot`
   - Fields: `id` (UUID PK), `reviewer_id` (UUID FK), `period` (String, e.g. "2026-06", "2026-W24"), `period_type` (enum: DAILY/WEEKLY/MONTHLY), `rank_position` (int), `score` (long), `like_count`, `share_count`, `comment_count` (long), `badge` (ReviewerBadge), `formula_id` (UUID FK → reviewer_scoring_formula), `created_at`, `updated_at`.
   - Unique constraint: `(reviewer_id, period, period_type)`.

4. **Tạo repositories** cho 3 entity mới.

### Phase 2: Service Layer Refactor

5. **Tạo `ReviewerScoringFormulaService`** (interface + impl)
   - `getActiveFormula()` — trả formula active, fallback default nếu chưa có.
   - `createFormula(request)` — admin tạo formula mới.
   - `activateFormula(formulaId)` — deactivate formula cũ, activate formula mới.
   - `getFormulaHistory()` — list all formulas.

6. **Tạo `ReviewerBadgeThresholdService`** (interface + impl)
   - `getThresholdsForFormula(formulaId)` — list thresholds.
   - `updateThresholds(formulaId, request)` — admin cập nhật thresholds.
   - `badgeForScore(score)` — lookup badge dựa trên active thresholds thay vì hardcode.

7. **Refactor `ReviewerServiceImpl`**
   - Inject `ReviewerScoringFormulaService` và `ReviewerBadgeThresholdService`.
   - Thay `calculateScore()` hardcoded → dùng `getActiveFormula()`.
   - Thay `badgeForScore()` hardcoded → dùng `badgeThresholdService.badgeForScore()`.
   - Thay `LIKE_AMOUNT/SHARE_AMOUNT/COMMENT_AMOUNT` → lấy từ active formula.
   - Thay `EngagementAccumulator.score()` → nhận weights từ formula.
   - `getReviewerRanking()` → đọc từ `ReviewerRankingSnapshot` nếu có, fallback tính realtime.

8. **Tạo `ReviewerRankingSnapshotService`** (interface + impl)
   - `generateSnapshot(periodType)` — tính ranking cho tất cả reviewer, lưu vào snapshot table.
   - `getRankingFromSnapshot(period, periodType, page, limit, filters)` — đọc ranking đã tính.

### Phase 3: Admin API

9. **Tạo `AdminReviewerConfigController`** — `/api/admin/reviewer-config`
   - `GET /formulas` — list all formulas.
   - `POST /formulas` — tạo formula mới.
   - `PUT /formulas/{id}/activate` — activate formula.
   - `GET /formulas/{id}/thresholds` — list badge thresholds cho formula.
   - `PUT /formulas/{id}/thresholds` — update badge thresholds.

### Phase 4: Scheduler & Data Init

10. **Tạo `ReviewerRankingSnapshotJob`** (scheduled job)
    - Chạy daily/weekly/monthly tính ranking snapshot.
    - Đặt trong `until/schedule/` theo pattern hiện có (`BlogRecommendationCalculationJob`).

11. **Data initialization**
    - Seed default formula (1/3/5, payout 100/300/500) và default badge thresholds vào DB.
    - Có thể dùng `DataInitializer` hoặc migration SQL.

### Phase 5: Tests

12. **Unit tests**
    - `ReviewerScoringFormulaServiceImplTest`
    - `ReviewerBadgeThresholdServiceImplTest`
    - `ReviewerRankingSnapshotServiceImplTest`
    - Update `ReviewerServiceImplTest` cho logic mới.
    - `AdminReviewerConfigControllerTest`

## Files Expected To Change

### New files:
- `entity/ReviewerScoringFormula.java`
- `entity/ReviewerBadgeThreshold.java`
- `entity/ReviewerRankingSnapshot.java`
- `entity/enums/RankingPeriodType.java`
- `repository/ReviewerScoringFormulaRepository.java`
- `repository/ReviewerBadgeThresholdRepository.java`
- `repository/ReviewerRankingSnapshotRepository.java`
- `service/serviceInterface/ReviewerScoringFormulaService.java`
- `service/serviceImplement/ReviewerScoringFormulaServiceImpl.java`
- `service/serviceInterface/ReviewerBadgeThresholdService.java`
- `service/serviceImplement/ReviewerBadgeThresholdServiceImpl.java`
- `service/serviceInterface/ReviewerRankingSnapshotService.java`
- `service/serviceImplement/ReviewerRankingSnapshotServiceImpl.java`
- `controller/AdminReviewerConfigController.java`
- `dto/requestDTO/ReviewerScoringFormulaRequest.java`
- `dto/requestDTO/ReviewerBadgeThresholdRequest.java`
- `dto/responseDTO/reviewer/ReviewerScoringFormulaResponseDTO.java`
- `dto/responseDTO/reviewer/ReviewerBadgeThresholdResponseDTO.java`
- `dto/responseDTO/reviewer/ReviewerRankingSnapshotResponseDTO.java`
- `until/schedule/ReviewerRankingSnapshotJob.java`
- Tests cho các file trên.

### Modified files:
- `service/serviceImplement/ReviewerServiceImpl.java` — refactor scoring/badge logic.
- `service/serviceInterface/ReviewerService.java` — có thể thêm method mới.
- Schema DBML — thêm 3 bảng mới.

## Validation

- `mvn test` — chạy full test suite.
- Test class cụ thể cho từng service mới.
- Verify admin endpoints với test.
- Verify default formula seed đúng giá trị hiện tại (1/3/5).

## Risks

- **Data migration**: Formula active mặc định phải match giá trị hardcode hiện tại để không break behavior.
- **Performance**: `getActiveFormula()` gọi mỗi request → cần cache (Spring `@Cacheable` hoặc in-memory).
- **Concurrent formula switch**: Cần đảm bảo chỉ 1 formula active → dùng `@Transactional` khi activate.
- **Snapshot staleness**: Ranking snapshot có thể outdated → cần hiển thị "last updated" timestamp.
- **Backward compatibility**: Existing badge history records dùng formula cũ → lưu `formula_id` trong snapshot để truy vết.
