# Báo cáo kiểm chứng G0-12

## Trạng thái

`PARTIAL`

## Đã kiểm chứng

- Backend A0 và Contract V2 focused tests: pass.
- Controller contract tests: pass.
- Backend compile: pass.
- Admin typecheck: pass.
- Admin production build: pass.
- Playwright test suite parse/list: pass.
- n8n JSON và Code-node JavaScript syntax: pass.
- n8n export giữ đúng workflow ID/path và `active=false`.
- V2 response không còn field `rawResponse`.
- Source auto-apply không còn target/report mutation path.

## Evidence định lượng

```text
Focused BE run: 27 tests, 0 failure, 0 error
Contract/A0 run: 19 tests, 0 failure, 0 error
Admin routes built: 21
n8n nodes: 5
Playwright suites discovered: 1
```

## Chưa đạt

- Coverage changed-file chưa đạt 100% line và 85% branch.
- Chưa implement HMAC, timestamp freshness và nonce replay guard.
- Chưa apply migration trên PostgreSQL thật.
- Chưa chạy account admin trên browser vì port 3636/8080/5678 đều đóng.
- Docker Desktop engine chưa chạy.
- Chưa publish n8n export và chưa probe exact published webhook.

## Lỗi đã highlight

1. `G012-AUTO-001`: score-based destructive auto apply.
2. `G012-BE-002`: USER timestamp accessor không tồn tại.
3. `G012-TEST-003`: canonical hash phụ thuộc Jackson date module.
4. `G012-SEC-003`: raw provider response preview trong log parse error.

## Bước tiếp theo

1. HMAC/replay implementation và negative tests.
2. Bổ sung coverage.
3. Mở Docker/PostgreSQL/n8n, import workflow, start BE/FE.
4. Chạy E2E bằng account admin test và thu screenshot.
