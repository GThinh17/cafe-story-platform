# Báo cáo đánh giá G0-12C — PostgreSQL/Flyway và Backend runtime

## 1. Trạng thái

`IMPLEMENTED_RUNTIME_VERIFIED_AWAITING_APPROVAL`

G0-12C đã đạt acceptance criteria trên PostgreSQL disposable cục bộ. Gate chưa được tự động đánh dấu
completed vì còn chờ người dùng gửi `APPROVE_G0-12C`.

## 2. Bound

- Chỉ verify migration `V20260723_01__admin_report_ai_contract_v2.sql` và Backend runtime.
- Không kết nối hoặc mutate Supabase/production database.
- Không publish/activate n8n.
- Không gọi OpenAI/provider.
- Không đăng nhập Admin UI và không chạy UI E2E.
- Không thay đổi source, test hoặc migration.

## 3. Execute

1. Khởi động Docker Desktop.
2. Tạo PostgreSQL 17 disposable, chỉ bind `127.0.0.1:55432`.
3. Bootstrap schema hiện tại bằng JPA `create`, Flyway tắt, data initializer tắt.
4. Xóa riêng 17 cột V2 để tạo synthetic pre-V2 snapshot; giữ 62 bảng nền.
5. Baseline Flyway tại `20260719.01`.
6. Khởi động Backend với Flyway bật và JPA `validate`.
7. Flyway áp dụng `20260723.01`.
8. Restart Backend để xác nhận validate/up-to-date.
9. Chạy API smoke, schema query, negative constraint probes và regression.
10. Rollback probe data, dừng Backend và container disposable.

## 4. Verify

| Tiêu chí | Mong đợi | Thực tế | Kết quả |
|---|---|---|---|
| Migration apply | `20260723.01` success | Flyway applied 1 migration | PASS |
| Restart | Không apply lại, schema up-to-date | Validated 10 migrations; no migration necessary | PASS |
| Entity/schema | JPA mapping hợp lệ | `JPA_DDL_AUTO=validate` start success | PASS |
| V2 columns | 17 cột đúng type/length | 17/17 | PASS |
| CHECK constraints | 8 constraint valid | 8/8, `convalidated=true` | PASS |
| Unique indexes | correlation/idempotency unique + valid | 2/2 | PASS |
| Negative probes | Chặn invalid automation/manual action/duplicate keys | 4/4 bị chặn đúng | PASS |
| Probe cleanup | Không còn test row | resolution `0`, job `0` | PASS |
| Runtime public docs | API docs reachable | `200`; Swagger redirect `302` | PASS |
| Admin security | Unauthenticated bị chặn | list `401`; AI POST `401` | PASS |
| Focused regression | Không failure/error | `63/63`, skip `0` | PASS |
| Full regression | Không failure/error | `617`, failure `0`, error `0`, skip `1` | PASS |
| Package | Build artifact thành công | `BUILD SUCCESS` | PASS |

Skip duy nhất là `FeedQueryCountPostgresIntegrationTest`, một conditional test của Feed và không thuộc
Admin Report AI G0-12C. Runtime PostgreSQL của gate này được chứng minh bằng Flyway/JPA/API/schema probes riêng.

## 5. Issue và residual risk

- `G012C-TEST-001 — TEST_BUG`: probe đầu dùng decision không canonical; đã sửa probe và chạy lại pass.
- `G012C-ENV-002 — CONFIG_ENV`: background scheduler/custom Redis cache vẫn tạo log/query trong runtime test.
  Không làm gate fail, nhưng nên có integration profile tắt nền.
- `G012C-DATA-003 — TEST_DATA`: synthetic snapshot không có legacy resolution rows. Không được suy rộng
  thành bằng chứng tương thích với toàn bộ dữ liệu production.

## 6. Cleanup và tác dụng phụ

- Backend port `18080`: đã đóng.
- PostgreSQL localhost port `55432`: đã đóng.
- Container disposable: đã dừng, chưa xóa để có thể inspect.
- Existing n8n container: giữ nguyên trạng thái running, không publish hoặc sửa workflow.
- Source/migration/test: không thay đổi.
- Secret local dùng cho runtime không được ghi vào evidence.

## 7. Done boundary

G0-12C đủ evidence để review nhưng chưa tự đánh dấu completed:

```text
G0-12C STATUS: IMPLEMENTED_RUNTIME_VERIFIED_AWAITING_APPROVAL
NEXT TOKEN: APPROVE_G0-12C
```

Approval này không xác nhận production database, n8n/provider, Admin UI/E2E hoặc production deployment.

