# Sprint 02 — Prompt, Rule Context, Schema và Evaluation

| Thuộc tính | Giá trị |
|---|---|
| Trạng thái | `COMPLETED_VERIFIED_APPROVED` |
| Rebase token | `IMPLEMENT_S2_REBASE_01` |
| Rebase approval | `APPROVE_S2_REBASE_01` |
| Detailed rebase | `s2-rebase-01.vi.md` |
| Detailed Design | `s2-dd-01-runtime-rule-evidence-prompt-evaluation.vi.md` |
| DD approval | `APPROVE_S2_DD_01` |
| S2-01 approval | `APPROVE_S2_01` |
| S2-02 implementation | `IMPLEMENT_S2_02` |
| S2-02 approval | `APPROVE_S2_02` |
| S2-03 implementation | `IMPLEMENT_S2_03` |
| S2-03 approval | `APPROVE_S2_03` |
| S2-04 implementation | `IMPLEMENT_S2_04` |
| S2-04 approval | `APPROVE_S2_04` |
| S2-05 disposition | `DEFERRED_AFTER_PARTIAL_RUN_NO_QUALITY_AUTHORITY` |
| S2-DONE-AUDIT | `10 PASS`, `1 PASS_WITH_SCOPE`, `3 PARTIAL`, `0 BLOCKED` |
| Audit approval | `APPROVE_S2_DONE_AUDIT` |
| Remediation | `S2-DONE-FIX-01`, `S2-DONE-FIX-02` completed/verified |
| Final approval | `APPROVE_S2_DONE` |
| Token kế tiếp | Không có |
| Automation | Giữ `A0_RECOMMEND_ONLY` |
| Production | `NOT_AUTHORIZED` |

## Scope sau rebase

Core theo thứ tự:

1. `S2-DD-01` — Detailed Design trước code;
2. `S2-01` — Runtime Rule Context Contract;
3. `S2-02` — bounded strict schema và Backend semantic burden;
4. `S2-03` — versioned evaluation dataset/harness;
5. `S2-04` — rule-family prompt pilot cho text-only BLOG/COMMENT;
6. `S2-DONE-AUDIT` — re-audit.

`S2-05` provider/model và cost/latency benchmark chỉ mở sau khi dataset/harness và prompt candidates
được approved.

External authoritative-source adapters, numeric calibration, USER/CAFE_PAGE deep evidence và
media OCR/vision được deferred khỏi Sprint 2 core. Không triển khai contract song song và không để
n8n sở hữu policy catalog riêng.

`S2-01` đã completed/verified/approved. `S2-02` đã triển khai bounded schema và Backend semantic
invariants, focused `50/50`, full Backend `638`, schema boundary `7/7`, ADV `12/12`, M07 `18/18`
và cross-review `16/16`. Chưa publish n8n runtime hoặc gọi provider thật.

S2-02 đã được review và chốt bằng:

```text
APPROVE_S2_02
```

S2-03 đã được mở bằng:

```text
IMPLEMENT_S2_03
```

## Kết quả S2-03

- Đã tạo dataset/schema/rubric/manifest `1.0.0-rc.1`; lifecycle `PROPOSED`,
  `runtimeAuthority=false`.
- Dataset có `26` case và `9` evidence bundle:
  - `20` hard-safety oracle;
  - `4` semantic candidate ở trạng thái `PROVISIONAL_NOT_GROUND_TRUTH`;
  - `2` disagreement ở trạng thái `OPEN`.
- Tách rõ evidence đã thu thập (`collectionState`), chất lượng item (`quality`) và
  mức đủ để kết luận rule (`evidenceSufficiency`).
- Manifest khóa SHA-256 của dataset/schema/rubric và dependency contract/catalog.
- Harness kiểm tra strict schema, version/fingerprint, unique ID, slice coverage,
  oracle traceability, ground-truth guard và tự chạy hard gate hiện có.
- Verify: dataset negative guard `6/6`; schema `7/7`; ADV `12/12`; M07
  `18/18`; cross-review `16/16`; Backend focused `50/50`; full Backend `638`,
  fail/error `0`, skip `1`.
- Hard safety đạt `100%`. Provider không được gọi; provider quality
  `NOT_EVALUATED`, denominator `0`; không có tuyên bố AI accuracy.
- Không đổi production source, FE, DB hoặc published n8n runtime trong S2-03.

S2-03 đã được review và chốt bằng:

```text
APPROVE_S2_03
```

Approval này chỉ khóa dataset/harness S2-03; không gọi provider, không activate
policy và không publish n8n runtime. S2-04 đã được mở bằng:

```text
IMPLEMENT_S2_04
```

## Kết quả S2-04

- Candidate `report-ai-v2-sprint2.4-candidate.1` có lifecycle `PROPOSED`,
  `runtimeAuthority=false`, `A0_RECOMMEND_ONLY`.
- Pilot chỉ chọn `CSR.HAR.001` cho BLOG direct và COMMENT context-dependent.
- Tạo strict schema/spec, deterministic candidate assembler, hai fixture,
  SHA-256 manifest và focused harness.
- Untrusted claim/target/evidence/prior-AI mutation không đổi system prompt
  `4/4`; assembler negative `5/5`; spec negative `6/6`.
- Output instruction khớp canonical provider field `evidenceIds`.
- `REL.001` bị loại vì Runtime Rule Context semantic không khớp policy; issue
  được route, không prompt-override.
- S2-03 hard safety vẫn `100%`; full Backend `638`, fail/error `0`, skip `1`.
- Provider call/runtime publish/production authority đều `false`.
- DD: `../10-verification/prompt-pilot-design.md`.

S2-04 đã được review và khóa bằng:

```text
APPROVE_S2_04
```

Approval không publish/import n8n, không gọi provider, không activate policy và
không cấp production authority. `S2-05` hiện đủ prerequisite nhưng vẫn là
conditional package. Người dùng cần chọn `IMPLEMENT_S2_05` nếu muốn benchmark
có external call/cost, hoặc `IMPLEMENT_S2_DONE_AUDIT` nếu muốn defer benchmark
và audit core để đóng Sprint 2.

## Kết quả S2-DONE-AUDIT

`IMPLEMENT_S2_DONE_AUDIT` đã được thực hiện theo lựa chọn defer S2-05 khỏi
Sprint 2 core. Lựa chọn này chỉ chốt disposition của package conditional; không
biến benchmark thất bại thành pass, không cấp quality authority và không chọn model.

- DoD: `10 PASS`, `1 PASS_WITH_SCOPE`, `3 PARTIAL`, `0 BLOCKED`.
- Backend focused: `50/50 PASS`; full regression: `638`, failure/error `0`,
  skipped `1`.
- Schema/parity, adversarial, prompt pilot, dataset/hard-safety và benchmark
  harness static gate đều pass.
- S2-05 vẫn có quality denominator `0`, selected model `null`, recorded cost
  `0 USD`; provider inference thành công `0/36`.
- Ba remediation bắt buộc:
  - tạo lại `summary.json` S2-01 từ evidence hiện có;
  - bổ sung test để coverage dòng của
    `AdminReportAiResolutionServiceImpl` đạt `100%`;
  - cập nhật security harness fixture theo canonical strict request hiện tại.
- Báo cáo: `s2-done-audit.vi.md`.
- Evidence: `evidence/2026-07-30T16-09-39-334+07-00/`.
- Production readiness giữ độc lập ở `9/14 NOT_READY`;
  production authority vẫn `NOT_AUTHORIZED`.

Trạng thái audit là `PARTIAL — AUDIT_FINDINGS_REMEDIATION_REQUIRED`. Findings
đã được review bằng:

```text
APPROVE_S2_DONE_AUDIT
```

Remediation đầu tiên được mở bằng:

```text
IMPLEMENT_S2_DONE_FIX_01
```

## Kết quả đóng Sprint 2

- `S2-DONE-FIX-01` đã xử lý ba finding bắt buộc của audit.
- Full-path Admin E2E sau đó phát hiện schema mismatch
  `targetSnapshotHash 71 > VARCHAR(64)`.
- `S2-DONE-FIX-02` thêm immutable Flyway migration `20260730.01`, đồng bộ
  entity/DB length `71` và khóa bằng schema-contract test.
- Final verification: focused `54/54`, full Backend `642`, failure/error `0`,
  skipped `1`; security harness và hai E2E pass.
- Không report/target mutation; auto-apply job `0`; fixture cleanup hoàn toàn.
- Final approval:

```text
APPROVE_S2_DONE
```

Sprint 2 core đã đóng. S2-05 vẫn deferred không có quality/model-selection
authority; production deployment vẫn `NOT_AUTHORIZED`.
