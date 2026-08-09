# S2-01 — Issue log

## S2-01-ISSUE-001 — Map-based tests không còn compile sau typed contract

- Phân loại: `IN_SCOPE_TEST_COMPATIBILITY`.
- Mức độ: `EXPECTED / BUILD_BLOCKING`.
- Phát hiện bởi: `mvn -DskipTests test-compile`.
- Kết quả thực tế: `13` compilation errors tại hai focused test do gọi `Map.get(...)`,
  `containsKeys(...)` hoặc gán `List<Map<...>>` vào typed DTO.
- Nguyên nhân: production contract đã chuyển `policyContext` và `evidence[]` từ raw `Map`
  sang DTO typed theo S2-01, còn fixture/assertion Sprint 1 vẫn dùng shape cũ.
- Hành động trong phạm vi: chuyển test sang typed fixture/assertion; bổ sung lifecycle,
  duplicate Rule/Evidence ID và snapshot-binding cases.
- Auto-fix allowed: `yes`, vì đây là thay đổi test bắt buộc trong acceptance S2-01.
- Fix applied:
  - assertions và fixtures đã chuyển sang typed DTO;
  - thêm lifecycle evaluation-only clamp;
  - thêm duplicate Rule/Evidence ID và snapshot-binding guards;
  - focused Backend test cuối đạt `40/40`.
- Trạng thái: `RESOLVED_VERIFIED`.

## S2-01-ISSUE-002 — Unknown evaluation mode có thể lọt qua Backend lifecycle guard

- Phân loại: `IN_SCOPE_POLICY_GUARD`.
- Mức độ: `HIGH`.
- Phát hiện bởi: review điều kiện `isRuntimePolicyActive`.
- Kết quả trước fix: nếu `policyStatus` và `ruleCatalogStatus` là `ACTIVE`, giá trị
  `evaluationMode = null/unknown` thỏa điều kiện “không phải proposed”.
- Hành động: đổi sang allowlist exact `ACTIVE_RUNTIME`; thêm regression case mode `null`.
- Auto-fix allowed: `yes`.
- Retest: focused Backend `40/40 PASS`; validator đạt `100% line / 90.56% branch`.
- Trạng thái: `RESOLVED_VERIFIED`.

## S2-01-ISSUE-003 — Candidate rule lifecycle và catalog mutability chưa fail-closed đầy đủ

- Phân loại: `IN_SCOPE_POLICY_GUARD`.
- Mức độ: `HIGH`.
- Phát hiện bởi: final source self-review.
- Vấn đề 1: Backend mới kiểm tra lifecycle ở policy/catalog level, chưa bắt buộc từng
  `candidateRule.ruleStatus = ACTIVE`.
- Vấn đề 2: conditional requirement DTO được tái sử dụng từ static catalog definition; caller có thể
  mutate object và ảnh hưởng lần đọc kế tiếp.
- Hành động:
  - yêu cầu exact active cho mọi candidate rule;
  - deep-copy conditional requirement khi tạo runtime DTO;
  - thêm regression cho proposed rule và caller mutation.
- Auto-fix allowed: `yes`.
- Retest:
  - focused Backend `40/40 PASS`;
  - full Backend `628`, failure `0`, error `0`, skipped `1`;
  - catalog đạt `100% line / 100% branch`;
  - validator đạt `100% line / 90.56% branch`.
- Trạng thái: `RESOLVED_VERIFIED`.
