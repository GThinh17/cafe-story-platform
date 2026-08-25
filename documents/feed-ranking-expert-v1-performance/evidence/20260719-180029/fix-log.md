# Nhật ký sửa lỗi

## Đã sửa

1. Thay cold/stale personalized Feed từ rebuild đồng bộ sang stale-while-revalidate.
   - GET trả snapshot cũ hoặc organic fallback ngay.
   - Dùng tập `in-flight` để cùng user/window/region chỉ schedule một rebuild nền.
   - Dùng executor bounded riêng và transaction mới cho background task.

2. Thay N lần native upsert bằng một JDBC batch upsert.
   - Lần thử đầu phát hiện ép kiểu `trend_window_type` không tồn tại.
   - Đã truyền enum name dạng chuỗi, đồng nhất `@Enumerated(EnumType.STRING)`.

3. Sửa Redis personalized Feed cache.
   - Trước sửa: Redis có key nhưng deserialize `List<BlogFeedResponse>` lỗi, mỗi request vẫn chạy 10 SQL.
   - Sau sửa: cache lưu JSON string có kiểu đích rõ ràng; cache-hit còn 1 SQL và 253–337 ms ở phép kiểm tra 4 lượt đầu.

4. Giữ batch follow và batch ad-frequency từ đợt triển khai `EXPERT_V1`; không thêm query per item.

## Xác minh

- Rebuild PostgreSQL thật: HTTP 200, 434 score, 5,635.75 ms.
- Focused test: 24 pass.
- Regression: 571 test, 0 fail, 0 error, 1 skip.
- Web typecheck: pass.
- Personalized benchmark: 50 request, p95 267.95 ms, error rate 0%.
- Web Home HTTP: 10 request, p95 224.36 ms, error rate 0%.

### Xác minh lại trước commit ngày 20/07/2026

- Focused backend mở rộng: 34 test pass, gồm ranking, batch writer, công thức score và sponsored candidate.
- Web typecheck lần đầu fail do `.next/dev/types/routes.d.ts` hỏng; sau cleanup generated cache, typecheck pass.
- `git diff --check`: không có whitespace error; chỉ có cảnh báo LF/CRLF của worktree Windows.

## Không sửa ngoài phạm vi

- Không cho Web/Mobile gọi trực tiếp endpoint rebuild.
- Không đổi cursor/session contract.
- Không tái kiến trúc sponsored Feed dù Mixed Feed còn vượt ngưỡng.
- Không sửa `PRODUCT.md` hoặc `seed-data-generator/`.
