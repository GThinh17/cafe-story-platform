# Detailed Design G0-12E — Admin UI, E2E và bất biến không mutation

## 1. Trạng thái tài liệu

- Gate: `G0-12E`.
- Trạng thái triển khai: `COMPLETED_VERIFIED_APPROVED`.
- Nhánh: `n8n/Ai-agent/fix-bug-report-admin`.
- Source/test commit: `9c155ff`.
- Contract: `2.0`.
- Automation mode: `A0_RECOMMEND_ONLY`.
- Ngày kiểm chứng: `2026-07-29`.
- Production deployment: `NOT_AUTHORIZED`.

Tài liệu này mô tả hành vi đã được kiểm chứng ở runtime local/disposable. Approval `APPROVE_G0-12E` đã được
ghi nhận; approval này không cấp production authority.

## 2. Bound

### 2.1. Mục tiêu

1. Kiểm tra UI Admin Report bằng account Admin test.
2. Chạy luồng FE → BE → n8n/provider → BE → FE cho BLOG và COMMENT.
3. Kiểm tra USER và CAFE_PAGE trả kết quả manual-only tại Backend.
4. Chứng minh việc xin AI recommendation không thay đổi report hoặc target.
5. Kiểm tra UI không cho tạo auto-apply mới nhưng vẫn đọc/hủy được legacy job.
6. Sửa lỗi thuộc phạm vi nếu có bằng vòng lặp phát hiện → phân loại → fix → test lại.

### 2.2. Được phép thay đổi

- Backend service và regression test trực tiếp liên quan đến Admin Report AI.
- Admin Playwright E2E để bổ sung target snapshot trước/sau.
- Tài liệu và evidence dưới `documents/report-admin/`.
- Dữ liệu synthetic trên PostgreSQL disposable.

### 2.3. Ngoài phạm vi

- Production deployment.
- Migration/repair hoặc ghi dữ liệu vào Supabase đã cấu hình.
- Deep policy/evidence cho USER và CAFE_PAGE.
- Sửa legacy `ReportModerationJobWorker` không thuộc Admin Resolve Report AI V2.
- Thay đổi workflow n8n đã được chốt tại G0-12D nếu không có lỗi V2 mới.

## 3. Môi trường kiểm thử

| Thành phần | Runtime |
|---|---|
| Admin UI | Next.js dev tại `http://localhost:3636` |
| Backend | Spring Boot source runtime tại `http://localhost:8080` |
| Database | PostgreSQL 17 disposable, localhost port `55432` |
| Flyway | 10 migration hợp lệ, current `20260723.01` |
| Hibernate | `spring.jpa.hibernate.ddl-auto=validate` |
| n8n | `2.28.6`, exact webhook đã publish từ G0-12D |
| Browser | Chromium qua Playwright và in-app browser |
| Admin | Account test được nạp từ file local ignored; credential không ghi vào evidence |

Configured Supabase không được dùng vì migration history bị lệch với nhánh hiện tại và chưa có
Contract V2. Không chạy `repair`, không tắt validation và không apply migration lên Supabase.

## 4. Luồng runtime đã kiểm chứng

```text
Admin UI
  |
  | POST /api/admin/reports/{reportId}/ai-resolution
  v
Backend
  |-- USER/CAFE_PAGE --> local manual-only response
  |
  |-- BLOG/COMMENT --> signed Contract V2 request
                         |
                         v
                    n8n -> OpenAI
                         |
                         v
                    signed V2 response
  |
  | validate signature + semantic contract
  | persist sanitized resolution
  | do not persist provider raw response
  | do not create auto-apply job in A0
  v
Admin UI evidence-first recommendation
```

### 4.1. FE trước khi gọi AI

- Admin mở `/reports`, chọn report và mở `Report detail`.
- UI hiển thị rõ “Recommendation only”.
- Dialog Ask AI không có checkbox hoặc delay để tạo auto-apply.
- E2E gọi API snapshot target ngay trước AI bằng endpoint Admin tương ứng.

### 4.2. Backend BLOG/COMMENT

1. Kiểm tra report còn `OPEN` hoặc `REVIEWING`.
2. Tạo evidence snapshot và idempotency key.
3. Nếu chưa có resolution cùng key, gửi signed request sang exact n8n webhook.
4. Kiểm tra signed response và semantic contract.
5. Lưu Contract V2 đã sanitize.
6. Trả recommendation; không thực thi `targetAction`.

### 4.3. n8n/provider

- BLOG và COMMENT đi qua canonical workflow G0-12D.
- Response có `contractVersion=2.0`, categorical risk, versions, findings/evidence references.
- Missing/insufficient evidence đi về `NEEDS_MANUAL_REVIEW + NO_ACTION`.
- Không dùng explanation làm evidence.
- Không trả provider raw payload về Admin UI.

### 4.4. Backend USER/CAFE_PAGE

- Không gọi webhook/provider từ nhánh service.
- Backend tự tạo kết quả `NEEDS_MANUAL_REVIEW + NO_ACTION`.
- `modelName=backend-policy-guard`.
- `blockedReasons` chứa `TARGET_DEEP_POLICY_NOT_IN_SPRINT1`.
- Runtime latency đo được lần lượt khoảng `59 ms` và `94 ms`; unit test xác nhận
  `providerCalls=0`.

### 4.5. FE sau khi nhận kết quả

UI hiển thị:

- `NEEDS_MANUAL_REVIEW` và `NO_ACTION`;
- badge `CONTRACT V2`;
- evidence sufficiency và evidence quality tách riêng;
- likelihood, harm và action risk dạng categorical;
- blocked reasons;
- “AI rationale — not evidence”;
- policy, rule, prompt và workflow version;
- correlation ID;
- legacy auto-apply history tách riêng.

Không hiển thị:

- provider raw response;
- secret/token/API key;
- nút tạo auto-apply mới;
- numeric score như action authority cho V2.

## 5. Lỗi phát hiện và sửa

### 5.1. `G012E-CODE-004` — mất cảnh báo A0 ở nhánh idempotent

Triệu chứng:

- Request compatibility có `autoApplyEnabled=true`.
- Resolution cùng idempotency key đã tồn tại.
- Backend trả HTTP `200`, không tạo job, nhưng `autoApplyWarning=null`.

Nguyên nhân:

- `createResolution` trả `toResponse(existing)` ngay khi tìm thấy resolution cũ.
- Nhánh return sớm bỏ qua `scheduleIfRequested`, nên không gắn warning A0.

Sửa:

- Tách helper `withAutoApplyOutcome`.
- Cả resolution mới và resolution tái sử dụng đều đi qua helper.
- A0 service vẫn trả `job=null`, chỉ trả warning, không tạo mutation.

Regression:

- Thêm `createResolution_duplicateAutoApplyRequestStillReturnsA0Warning_TC006_1`.
- Kiểm tra provider không bị gọi, resolution không lưu lặp, job null và warning có
  `A0_RECOMMEND_ONLY`.

### 5.2. `G012E-TEST-003` — no-mutation test chưa chứng minh target

Triệu chứng:

- Test cũ chỉ kiểm tra report status và auto-apply job.
- Không có ảnh chụp trạng thái BLOG/COMMENT/USER/CAFE_PAGE trước/sau.

Sửa:

- Thêm `fetchTargetSnapshot`.
- Normalize đúng các field có ý nghĩa moderation:
  - BLOG: owner, content, status;
  - COMMENT: blog, owner, content, status;
  - USER: account status, username;
  - CAFE_PAGE: owner, name, status.
- So sánh snapshot trước/sau từng AI call.
- Lưu raw evidence riêng cho `RAI-07`–`RAI-10`.
- `RAI-17` kiểm tra đồng thời report status, target snapshot và job.

## 6. Bất biến không mutation

```text
Ask AI
  => targetBefore == targetAfter
  => report status unchanged
  => no SCHEDULED/APPLYING job created
  => candidate targetAction is recommendation only
```

Kết quả runtime:

| Target | Trước | Sau | Kết luận |
|---|---|---|---|
| BLOG | `PUBLISHED`, content/owner cố định | giống trước | PASS |
| COMMENT | `PUBLISHED`, content/blog/owner cố định | giống trước | PASS |
| USER | `accountStatus=true`, username cố định | giống trước | PASS |
| CAFE_PAGE | `ACTIVE`, name/owner cố định | giống trước | PASS |

Raw evidence:

- `documents/report-admin/ai-report-e2e/evidence/2026-07-29T04-22-41-677Z/raw/RAI-07-target-no-mutation-blog.json`
- `documents/report-admin/ai-report-e2e/evidence/2026-07-29T04-22-41-677Z/raw/RAI-08-target-no-mutation-comment.json`
- `documents/report-admin/ai-report-e2e/evidence/2026-07-29T04-22-41-677Z/raw/RAI-09-target-no-mutation-user.json`
- `documents/report-admin/ai-report-e2e/evidence/2026-07-29T04-22-41-677Z/raw/RAI-10-target-no-mutation-cafe-page.json`
- `documents/report-admin/ai-report-e2e/evidence/2026-07-29T04-22-41-677Z/raw/RAI-17-a0-safety-invariant.json`

## 7. Legacy auto-apply compatibility

Automated runner không tự tạo legacy job vì A0 cấm creation. Vì vậy `RAI-16` trong báo cáo tự động
được ghi đúng là `BLOCKED`, không giả lập thành pass.

Supplemental test dùng một row synthetic trên database disposable:

1. Job gắn với report/resolution synthetic.
2. Status ban đầu `SCHEDULED`.
3. `scheduled_at=2099-01-01` để background worker không claim.
4. Admin UI hiển thị job và nút `Cancel auto apply`.
5. Admin bấm cancel.
6. UI chuyển sang `Cancelled`.
7. DB xác nhận:
   - status `CANCELLED`;
   - có `cancelled_at`;
   - có `cancelled_by_admin_user_id`;
   - reason `ADMIN_CANCELLED`;
   - `applied_at` vẫn null.

Evidence ảnh:

- `evidence/2026-07-29T11-10-13-870+07-00/rai-16-legacy-job-before-cancel.jpg`
- `evidence/2026-07-29T11-10-13-870+07-00/rai-16-legacy-job-after-cancel.jpg`

## 8. Verification

| Tiêu chí | Phương pháp | Kết quả |
|---|---|---|
| BE focused | 2 service test classes | `32/32 PASS` |
| Changed BE coverage | JaCoCo | `100% line`, `87.85% branch` |
| BE regression | `mvn test` | `618`, 0 failure, 0 error, 1 PostgreSQL integration skip |
| Admin type safety | `npm run typecheck` | PASS |
| Admin production build | `npm run build` | PASS |
| Automated UI E2E | `npm run test:e2e:admin-report-ai` | command PASS; 29/30 automated scenarios PASS |
| Legacy cancel supplement | browser + disposable DB | PASS |
| Effective gate scenarios | automated + supplement | `30/30 PASS` |
| BLOG/COMMENT provider path | exact n8n/provider | PASS |
| USER/PAGE local path | runtime result + unit provider counter | PASS |
| Cleanup | report close, browser/backend/admin stop, DB auth restore/stop | PASS |

## 9. Vấn đề ngoài phạm vi được highlight

### `G012E-RELATED-006` — legacy moderation worker lazy serialization

Trong lúc tạo report synthetic, legacy `ReportModerationJobWorker` có lần thất bại với BLOG/COMMENT do
serialize lazy `imageUrls` ngoài Hibernate session.

- Đây là luồng moderation job cũ, không phải Admin Resolve Report AI V2.
- Admin AI V2 vẫn trả Contract V2 đúng và E2E qua.
- Không sửa trong G0-12E để tránh mở rộng scope.
- Khuyến nghị tạo bug riêng và bổ sung integration test cho worker với target có lazy image collection.

## 10. Cleanup và tác dụng phụ

- Không mutate Supabase.
- Không production deploy.
- Không lưu credential/secret vào source, commit hoặc evidence.
- Report E2E được chuyển khỏi `OPEN/REVIEWING`.
- Legacy supplemental job kết thúc ở `CANCELLED`, không apply.
- Backend và Admin dev runtime đã dừng.
- PostgreSQL disposable đã khôi phục password/`pg_hba.conf` và dừng container.
- n8n và Redis có sẵn trước gate được giữ nguyên.

## 11. Kết luận gate

G0-12E đã có deliverable, evidence kỹ thuật và approval của người dùng. Trạng thái đúng là
`COMPLETED_VERIFIED_APPROVED`.

Token tiếp theo:

```text
IMPLEMENT_G0_12_DONE_AUDIT
```

G0-12F đã regenerate Detailed Design tổng hợp tiếng Việt theo BE → n8n/OpenAI → BE → FE, có evidence thật và đã
được approve. Sprint 1 vẫn cần audit `G0-12-DONE` riêng.
