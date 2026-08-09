# Sprint 1 Production Readiness Checklist

- [x] `APPROVE_G0-12` recorded.
- [ ] Policy, Security/Privacy, Backend and Operations owners named — `NOT_EVIDENCED`.
- [x] A0 default/kill-switch tests pass.
- [ ] Active legacy jobs inventoried and safely handled — supplemental disposable job pass; target-environment inventory chưa có.
- [ ] Flyway history/schema verified before additive migration — disposable PostgreSQL pass; target external/production database chưa verify.
- [x] Changed-file coverage gate passes.
- [x] HMAC positive/negative/replay tests pass.
- [x] Exact published webhook readiness passes trên local n8n runtime.
- [x] BLOG và COMMENT E2E pass với safe fixtures.
- [x] USER/PAGE provider suppression pass bằng Backend mock verification; runtime E2E trả local manual-only.
- [x] Raw/secret scan passes.
- [x] Target/report before-after proves zero AI mutation cho BLOG/COMMENT/USER/CAFE_PAGE.
- [ ] Rollback drill passes — plan đã có, drill chưa chạy.
- [ ] Monitoring/alert and retention jobs are ready — mới có design, chưa có runtime evidence.

Unchecked mandatory item means `NOT_READY`, not “mostly ready”.

Trạng thái audit `2026-07-29`: `9/14 PASS`, `5/14 OPEN`; production deployment vẫn `NOT_AUTHORIZED`.

Re-audit `G0-12-DONE` sau sáu remediation xác nhận technical Sprint 1 DoD đạt `21/21`, nhưng không
có production authority hoặc evidence mới để đổi năm mục unchecked ở trên. Production readiness vì
vậy vẫn là `9/14 PASS`, `5/14 OPEN`, `NOT_READY`.
