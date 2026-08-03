# Issue register — DOD-FIX-02

## FIX02-CODE-001

- Classification: `CODE_BUG`
- Evidence: focused baseline
  `AdminReportAiResolutionServiceImplTest#createResolution_changedSnapshotDoesNotReusePreviouslyMatchedCache_CT010_SAF010`.
- Actual: cached resolution ID của snapshot V1 vẫn được trả về sau khi target đã đổi sang snapshot V2.
- Expected: recompute freshness trước khi trả cache; key/hash mới không được reuse record cũ.
- Affected behavior: `CT-010`, `SAF-010`.
- Likely owner: Backend `AdminReportAiResolutionServiceImpl`.
- Proposed action: refresh target authoritative state và rebuild request trước cached return.
- Auto-fix allowed: yes.
- Status: `FIXED_VERIFIED`.
- Verification: explicit test đỏ trước sửa; sau sửa test pass và kết quả cache cũ không còn được trả về.

## FIX02-CODE-002

- Classification: `CODE_BUG`
- Evidence: focused baseline
  `AdminReportAiResolutionServiceImplTest#createResolution_targetChangesDuringProviderClampsStaleResult_CT010_1_SAF010`.
- Actual: target đổi trong provider window nhưng kết quả vẫn là `RESOLVE + HIDE`.
- Expected: output stale phải thành `NEEDS_MANUAL_REVIEW + NO_ACTION`, findings rỗng,
  blocked reason `TARGET_SNAPSHOT_CHANGED_DURING_EVALUATION`.
- Affected behavior: `CT-010`, `SAF-010`.
- Likely owner: Backend `AdminReportAiResolutionServiceImpl`.
- Proposed action: refresh/recompute target snapshot ngay trước semantic validation/persistence và clamp stale result.
- Auto-fix allowed: yes.
- Status: `FIXED_VERIFIED`.
- Verification: explicit test đỏ trước sửa; sau sửa response stale được clamp về manual/no-action và không mutate report/target.

## FIX02-TEST-001

- Classification: `TEST_BUG`
- Evidence: focused rerun sau source fix dừng tại test compile:
  `String cannot be converted to RestClient`.
- Actual: test constructor coverage vẫn dùng public constructor cũ không có `EntityManager`.
- Expected: giữ overload tương thích và để Spring dùng constructor có `EntityManager`.
- Affected behavior: validation harness; chưa phản ánh behavioral regression.
- Likely owner: test/constructor compatibility.
- Proposed action: bổ sung overload cũ delegate với `entityManager = null`.
- Auto-fix allowed: yes.
- Status: `FIXED_VERIFIED`.

## FIX02-TEST-002

- Classification: `TEST_BUG`
- Evidence: JaCoCo sau full service test: line `455/458 = 99.34%`, branch
  `171/196 = 87.24%`.
- Actual: chưa chạy nhánh constructor không có `EntityManager` và
  `EntityNotFoundException` khi authoritative refresh.
- Expected: changed production class line `100%`, branch `>=85%`.
- Affected behavior: compatibility/fail-closed freshness paths.
- Likely owner: tests.
- Proposed action: thêm focused tests cho no-op compatibility và deleted target conflict.
- Auto-fix allowed: yes.
- Status: `FIXED_VERIFIED`.
- Verification: service suite `25/25 PASS`; changed class line `458/458 = 100%`,
  branch `172/196 = 87.76%`.

## Ngoài phạm vi

- Không kiểm thử multi-node distributed race hoặc DB unique-conflict trong package này.
- Không sửa UI, n8n, database hoặc provider runtime.
