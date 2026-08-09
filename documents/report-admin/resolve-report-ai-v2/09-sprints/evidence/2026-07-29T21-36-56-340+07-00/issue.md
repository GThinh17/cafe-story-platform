# S2-02 — Issue log

## S2-02-ISSUE-001 — Null outcome gây exception thay vì fail-closed

- Classification: `CODE_BUG`.
- Mức độ: `HIGH`.
- Phát hiện bởi:
  `mvn '-Dtest=AdminReportAiResolutionServiceImplTest,AdminReportAiSemanticValidatorTest' test`.
- Evidence thực tế: `NullPointerException` tại `hasCompleteMaterialScope` khi finding không tạo được
  outcome hợp lệ nhưng code gọi `Set.of(...).contains(null)`.
- Hành vi bị ảnh hưởng: provider output sai shape có thể đi vào operational exception thay vì được
  normalize thành manual/no-action với blocked reason.
- Owner: Backend semantic validator.
- Hành động đề xuất: kiểm tra null trước membership test; thêm regression cho missing/invalid outcome.
- Auto-fix allowed: `yes`.
- Fix: `isCompleteOutcome` kiểm tra null trước membership; malformed outcome bị clamp.
- Verify: focused `50/50 PASS`, full Backend `638` không failure/error.
- Trạng thái: `FIXED_VERIFIED`.

## S2-02-ISSUE-003 — Categorical value sai vẫn có thể được giữ lại trong DTO đã clamp

- Classification: `CODE_BUG`.
- Mức độ: `MEDIUM`.
- Phát hiện bởi: self-review sau focused test và kiểm tra nhánh normalize trước persistence.
- Evidence thực tế: validator đã thêm `INVALID_CATEGORICAL_SEMANTICS` và clamp quyết định về
  `NEEDS_MANUAL_REVIEW`, nhưng các giá trị không thuộc enum như `UNKNOWN_VALUE` vẫn còn trên
  `evidenceQuality`, `evidenceSufficiency`, `violationLikelihood` hoặc `harmSeverity`.
- Hành vi bị ảnh hưởng: kết quả fail-closed đúng về action nhưng chưa bảo đảm “persist only normalized result”.
- Owner: Backend semantic validator.
- Hành động đề xuất: normalize categorical value không hợp lệ về fallback canonical và thêm regression assertions.
- Auto-fix allowed: `yes`.
- Fix: fallback `UNUSABLE` / `UNASSESSABLE` / `UNKNOWN`; regression assert DTO đã normalize.
- Verify: focused `50/50 PASS`.
- Trạng thái: `FIXED_VERIFIED`.

## S2-02-ISSUE-004 — Null rule version gây exception thay vì fail-closed

- Classification: `CODE_BUG`.
- Mức độ: `HIGH`.
- Phát hiện bởi:
  `mvn '-Dtest=AdminReportAiResolutionServiceImplTest,AdminReportAiSemanticValidatorTest' test`.
- Evidence thực tế:
  - candidate rule có `ruleVersion = null`; `analyzeFindings` gọi `.equals(...)` trên null;
  - sau lần sửa hẹp đầu tiên, candidate rule có `requiredEvidenceKinds = null` tiếp tục gây NPE tại
    `hasMissingEvidenceKindBurden`.
- Root cause: validator nhận biết context sai schema nhưng vẫn chuyển candidate object sai xuống semantic evaluation.
- Hành vi bị ảnh hưởng: rule context sai schema có thể gây operational error thay vì trả
  `RULE_CONTEXT_SCHEMA_MISMATCH` và manual/no-action.
- Owner: Backend semantic validator.
- Hành động đề xuất: chặn context sai ngay tại ranh giới semantic evaluation, giữ so sánh version null-safe và
  giữ toàn bộ field-variant test làm regression.
- Auto-fix allowed: `yes`.
- Fix: invalid Rule Context không đi xuống semantic evaluation; version compare null-safe.
- Verify: 24 field-variant probes fail-closed; focused `50/50 PASS`.
- Trạng thái: `FIXED_VERIFIED`.

## S2-02-ISSUE-005 — Nested runtime request chưa thực thi đầy đủ strict schema tại n8n boundary

- Classification: `CONTRACT_GAP`.
- Mức độ: `HIGH`.
- Phát hiện bởi: source-to-schema parity review.
- Evidence thực tế: canonical request schema đặt `additionalProperties: false` cho các object lồng nhau, nhưng
  Code node mới kiểm tra exact keys tại outer request, policy context, candidate rule và evidence envelope.
  `reportClaim`, `targetSnapshot`, `observableFields`, evidence metadata/payload và `executionConstraints`
  vẫn có thể nhận unknown property.
- Hành vi bị ảnh hưởng: schema artifact và boundary behavior chưa tương đương; field ngoài allowlist có thể lọt
  qua trước provider projection.
- Owner: n8n request boundary.
- Hành động đề xuất: thêm exact-required-key checks cho toàn bộ nested bounded object và negative runtime probe.
- Auto-fix allowed: `yes`.
- Fix: exact-required-key checks cho nested authority objects và signed negative Code-node probe.
- Verify: schema boundary `7/7 PASS`; n8n nested boundary `PASS`.
- Trạng thái: `FIXED_VERIFIED`.

## S2-02-ISSUE-002 — Fixture cũ không còn khớp canonical S2-02 contract

- Classification: `TEST_BUG`.
- Mức độ: `EXPECTED / BUILD_BLOCKING`.
- Phát hiện bởi cùng focused command.
- Evidence thực tế:
  - fixture còn dùng `SUPPORTED`, `NOT_SUPPORTED`, `INCONCLUSIVE`;
  - finding thiếu `ruleVersion`, `violationLikelihood`, `rationale`;
  - assertion còn yêu cầu `allowedCandidateActions = [NO_ACTION]`;
  - fixture valid chỉ tạo một Evidence Kind và một material evaluation dù catalog yêu cầu đầy đủ.
- Hành vi bị ảnh hưởng: test không còn đại diện cho bounded schema và complete-scope S2-02.
- Owner: focused tests.
- Hành động đề xuất: chuyển fixture sang outcome canonical, đủ required evidence/rule scope và giữ
  các negative case cũ dưới expectation fail-closed mới.
- Auto-fix allowed: `yes`.
- Fix: canonical outcomes, exact finding shape, đủ evidence và material scope.
- Verify: focused `50/50 PASS`.
- Trạng thái: `FIXED_VERIFIED`.
