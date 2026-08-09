# Issue Register — G0-12-DONE Re-audit

## Kết quả trong phạm vi

- Không phát hiện `CODE_BUG`.
- DOD-14–19 đều có executable evidence và approval.
- Open DOD issue trong phạm vi Sprint 1: `0`.

## REAUDIT-TEST-001

- Classification: `TEST_BUG`.
- Evidence: mandatory evidence-file validator.
- Affected behavior: completeness của re-audit evidence package.
- Actual: lần verify đầu thiếu `bao-cao-danh-gia.md`.
- Expected: package phải có đủ năm file bắt buộc và raw/log/coverage evidence.
- Likely owner: re-audit document assembly.
- Proposed action: bổ sung báo cáo đánh giá tổng hợp và chạy lại validator.
- Auto-fix allowed: yes.
- Status: `FIXED_VERIFIED`.
- Verification: mandatory evidence-file validator tìm thấy đầy đủ file sau fix.

## REAUDIT-CONFIG-001

- Classification: `CONFIG_ENV`.
- Evidence: `raw/production-readiness-snapshot.tsv`.
- Affected behavior: production deployment readiness.
- Actual: còn 5/14 mục mở — owners, target legacy inventory, external DB verification, rollback
  drill, monitoring/alert/retention runtime.
- Likely owner: Policy/Security/Backend/Operations/production environment.
- Proposed action: xử lý trong production-readiness gate riêng.
- Auto-fix allowed: no.
- Status: `DOCUMENTED_OUTSIDE_SPRINT1_CLOSURE`.

## REAUDIT-CONFIG-002

- Classification: `CONFIG_ENV`.
- Evidence: `raw/source-fingerprint.tsv` và Git HEAD `9c155ff`.
- Affected behavior: reproducibility/handoff.
- Actual: 15 file remediation source/test/workflow đang nằm trong dirty worktree thay vì commit mới.
- Likely owner: source-control handoff.
- Proposed action: commit source/test/workflow sau khi user review gate; tiếp tục loại documents khỏi
  commit nếu đó vẫn là yêu cầu.
- Auto-fix allowed: no trong re-audit.
- Status: `DOCUMENTED_RESIDUAL`.

Hai residual trên không phủ định evidence kỹ thuật hiện tại. Bất kỳ thay đổi nào vào 15 file fingerprint
đều làm re-audit stale và yêu cầu kiểm chứng lại.
