# Báo cáo đánh giá Resolve Report with AI

## Kết luận

Trạng thái kiểm chứng: **PARTIAL**.

Phần backend nội bộ và UI quản trị cơ bản được triển khai khá đầy đủ: có recommendation history, optional delayed auto-apply, safety gate, countdown/cancel, bulk mode và manual status workflow. Focused test pass 26/26; full backend regression pass 580 test, 0 failure, 0 error, 1 skipped; admin typecheck/build pass.

Tuy nhiên chức năng AI thật hiện **không chạy end-to-end**. n8n health trả 200 nhưng production webhook `cafestory-admin-report-ai-resolution` trả 404. Backend vì vậy trả 502 cho Ask AI. AI response contract, OpenAI latency thực, safety gate và auto-apply runtime chưa thể xác nhận.

## Điểm tốt hiện có

- Backend tự dựng report context từ dữ liệu tin cậy.
- Hỗ trợ BLOG, COMMENT, USER, CAFE_PAGE.
- AI chỉ recommendation mặc định; auto-apply phải bật riêng.
- Auto-apply có delay, safety thresholds, partial unique index và cancel.
- UI phân tách recommendation với final action và có bulk confirmation.
- Test backend hiện tại không có regression.
- E2E cleanup đã đóng toàn bộ report test; `activeLeft` rỗng.

## Vấn đề chính

1. P0: n8n production webhook không được đăng ký/publish đúng; runtime có hai workflow trùng path/name.
2. P1: webhook không có authentication/signature.
3. P1: auto-apply không revalidate content snapshot/version trước khi mutate.
4. P1: backend thiếu semantic decision-action matrix và audit metadata đầy đủ.
5. P1/P2: timeout backend 15 giây nhưng OpenAI node 30 giây; thiếu retry/error workflow/idempotency.
6. P2: image URL chỉ nằm trong JSON text, chưa được phân tích bằng vision.
7. P2: lỗi Ask AI hiển thị phía sau dialog; bulk chỉ có số đếm, không có per-item error/retry.
8. P2: E2E đang có false positive, khiến tổng 86% không phản ánh trạng thái chức năng thật.

## UI critique

- Heuristic score: 25/40.
- Strengths: bố cục nhất quán, auto-apply opt-in, confirmation rõ, có history/countdown/cancel.
- Weaknesses: modal dày UUID/enum kỹ thuật, lỗi bị che, tên “AI resolve all” gây hiểu nhầm, score thiếu diễn giải và bulk error thiếu khả năng phục hồi.
- Automated detector: không phát hiện anti-pattern theo rule set.
- Assessment independence: degraded vì session policy không cho phép sub-agent.
- Browser overlay: không inject được do evaluation surface read-only; dùng screenshot và DOM inspection thay thế.

## Verdict theo môi trường

| Lớp | Verdict |
|---|---|
| Source architecture | Có nền tảng tốt nhưng cần hardening. |
| Unit/controller test | PASS. |
| Backend regression | PASS. |
| Admin typecheck/build | PASS. |
| n8n health | PASS. |
| n8n production webhook | FAIL 404. |
| Backend to n8n to OpenAI | FAIL/BLOCKED. |
| Admin UI non-AI operations | PASS. |
| Ask AI UI | Render được nhưng request FAIL 502 và error recovery kém. |
| Auto-apply runtime | BLOCKED vì không tạo được recommendation/job. |

## Lệnh và kết quả kiểm chứng

| Tiêu chí | Lệnh/thao tác | Mong đợi | Thực tế |
|---|---|---|---|
| Focused backend | `mvn '-Dtest=AdminReportAiResolutionServiceImplTest,AdminReportAiAutoApplyJobServiceImplTest,AdminContentReportControllerTest' test` | Ba suite pass | 26 test pass, 0 fail/error. |
| Backend regression | `mvn test` | Không có regression | 580 test, 0 failure, 0 error, 1 skipped; `BUILD SUCCESS`. |
| Cấu trúc repo | `python .agents/skills/cafestory-engineering-workflows/scripts/check_project.py .` | Không warning | `warnings: []`. |
| Admin type safety | `npm run typecheck` | Exit 0 | Exit 0. |
| Admin build | `npm run build` | Build production thành công | Build thành công, 21 static pages, có `/reports`. |
| n8n health | `GET http://localhost:5678/healthz` | HTTP 200 | HTTP 200, `{"status":"ok"}`. |
| n8n workflow registration | POST `/webhook/cafestory-admin-report-ai-resolution` | Webhook được đăng ký | HTTP 404, `not registered`. |
| Workflow inventory | `n8n list:workflow --active=true/false` trong container | Một workflow production duy nhất | Cùng tên tồn tại ở active ID `tHNqyfXwIY4uy5t2` và inactive ID `cafestory-admin-report-ai-resolution`. |
| UI/E2E | `npm run test:e2e:admin-report-ai` | Tất cả luồng AI hợp lệ | Playwright run `failed`; Ask AI trả 502; manual status/cleanup pass. |
| Cleanup | Đọc `raw/RAI-30-cleanup-verification.json` | Không còn report test active | `activeLeft: []`. |

Evidence chi tiết của lần E2E nằm tại `documents/report-admin/ai-report-e2e/evidence/2026-07-21T15-05-30-069Z/`. Playwright `.last-run.json` ghi nhận `status: failed`.

Chi tiết thiết kế và backlog nằm tại `documents/report-admin/resolve-report-with-ai-detail-design.md`.
