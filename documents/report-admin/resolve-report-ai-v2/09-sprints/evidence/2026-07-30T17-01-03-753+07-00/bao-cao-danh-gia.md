# Báo cáo đánh giá S2-DONE-FIX-02-DB-HASH-LENGTH

## Kết luận

Gate `S2-DONE-FIX-02-DB-HASH-LENGTH` đạt `COMPLETED_VERIFIED`.

Mismatch làm PostgreSQL từ chối canonical hash đã được sửa đúng ở schema
boundary bằng migration mới. Không truncate hash, không bỏ prefix `sha256:` và
không sửa migration cũ.

## Kết quả

- Entity/DB cùng length `71`.
- Flyway áp `20260730.01` thành công trên PostgreSQL disposable.
- JPA `validate` và Backend API startup pass.
- Focused Backend `54/54`.
- Full Backend `642`, failure/error `0`, skipped `1`.
- Security harness pass.
- `E2E-S1-13` provider outage/recovery pass.
- `E2E-S1-04` critical-evidence manual/no-action pass.
- Report/target không mutation; auto-apply job `0`.
- Synthetic data cleanup hoàn toàn.
- Screenshot UI được kiểm tra trực quan, không còn persistence error.

## Residual

- Local Backend có Redis warning không chặn gate; không sửa source để che lỗi
  môi trường optional.
- Background Report Moderation worker có lỗi lazy-loading image collection trên
  fixture. Đây là `CODE_BUG` ngoài phạm vi Admin AI; job/fixture đã cleanup và
  không làm hai E2E fail.
- Chưa deploy hoặc migrate external/production database.
- S2-05, Sprint 3 và Sprint 4 vẫn là backlog không chặn core.

## Evidence

- [`test-log.md`](test-log.md)
- [`raw/flyway-schema-probe.txt`](raw/flyway-schema-probe.txt)
- [`issue.md`](issue.md)
- [`fix-log.md`](fix-log.md)
- [`e2e-provider-recovery/raw/E2E-S1-13-retry-recovery.json`](e2e-provider-recovery/raw/E2E-S1-13-retry-recovery.json)
- [`e2e-manual-failsafe/raw/E2E-S1-04-runtime-result.json`](e2e-manual-failsafe/raw/E2E-S1-04-runtime-result.json)

## Handoff

Functional gate đã pass nhưng core chưa được đánh dấu approved tự động. Token
review tiếp theo là:

```text
APPROVE_S2_DONE
```
