# Issues — G0-12E

## Bound

- Gate: `G0-12E`.
- Scope: Admin UI/E2E, Backend → n8n/OpenAI → Backend → UI và no-mutation invariant.
- Runtime: PostgreSQL disposable localhost, Backend `8080`, Admin `3636`, n8n `5678`.
- Production deployment và external database mutation: không được phép.
- Evidence không lưu password, cookie, token, API key hoặc email đầy đủ.

## Issue register

| ID | Classification | Severity | Trạng thái | Ảnh hưởng | Xử lý |
|---|---|---:|---|---|---|
| `G012E-ENV-001` | `CONFIG_ENV` | HIGH | ROUTED | Configured Supabase không thể chạy Contract V2 | Không repair/apply; dùng PostgreSQL disposable |
| `G012E-DATA-002` | `TEST_DATA` | MEDIUM | RESOLVED | BLOG payload đầu thiếu `regionId`, trả `400` | Sửa synthetic fixture theo contract, không sửa source |
| `G012E-TEST-003` | `TEST_BUG` | HIGH | FIXED | Test cũ không chứng minh target không bị mutation | Thêm snapshot trước/sau cho bốn target |
| `G012E-CODE-004` | `CODE_BUG` | HIGH | FIXED | Cached/idempotent resolution làm mất A0 warning | Dùng chung `withAutoApplyOutcome` cho new/cached result |
| `G012E-ENV-005` | `CONFIG_ENV` | HIGH | RESOLVED | Stale Backend JAR thiếu HMAC hiện tại làm AI call `502` | Dừng JAR cũ, chạy current source/classes |
| `G012E-RELATED-006` | `CODE_BUG` | HIGH | OUT_OF_SCOPE | Legacy moderation worker serialize lazy `imageUrls` lỗi | Tạo bug riêng cho legacy worker |
| `G012E-TEST-007` | `TEST_DATA` | MEDIUM | RESOLVED_SUPPLEMENT | Automated RAI-16 thiếu legacy scheduled fixture | Seed an toàn trên DB disposable và test UI cancel riêng |

## Highlight lỗi đã sửa — `G012E-CODE-004`

Backend tìm thấy resolution theo idempotency key rồi return sớm. Vì vậy request compatibility có
`autoApplyEnabled=true` không đi qua A0 blocker và response thiếu `autoApplyWarning`, dù không tạo job.

Sửa ở `AdminReportAiResolutionServiceImpl`:

- cả resolution mới và cached resolution đi qua `withAutoApplyOutcome`;
- A0 trả warning nhưng luôn giữ `autoApplyJob=null`;
- không gọi provider lại cho cached result;
- không persist resolution hoặc job lặp.

Regression test mới: `createResolution_duplicateAutoApplyRequestStillReturnsA0Warning_TC006_1`.

## Highlight test gap đã sửa — `G012E-TEST-003`

E2E hiện chụp và normalize target state trước/sau:

- BLOG: owner, content, status;
- COMMENT: blog, owner, content, status;
- USER: account status, username;
- CAFE_PAGE: owner, name, status.

Tất cả snapshot trước/sau bằng nhau trong lượt E2E cuối.

## Ngoài phạm vi — `G012E-RELATED-006`

Legacy `ReportModerationJobWorker` có lần fail khi serialize lazy `imageUrls` ngoài Hibernate session.
Đây không phải luồng Admin Resolve Report AI V2. Không sửa trong gate này để tránh mở rộng scope,
nhưng phải tạo bug riêng vì có thể làm legacy moderation job thất bại.
