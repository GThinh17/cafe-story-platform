# Fix log G0-12F

## `G012F-DOC-001`

### Trước sửa

Detailed Design tổng kết luận:

- HMAC/replay runtime chưa hoàn thành;
- published n8n/provider chưa chạy;
- Admin UI E2E bị blocked;
- runtime FE → BE → n8n chưa được kiểm chứng.

Các câu này phản ánh snapshot trước G0-12D/E và không còn đúng với evidence hiện tại.

### Cách sửa

- tái tạo DD tổng theo source tại commit `9c155ff`;
- nối từng layer tới evidence G0-12A–E;
- ghi cả source export `active=false` và published runtime active để tránh đánh đồng;
- cập nhật kết quả n8n/provider, UI/E2E, no-mutation và legacy cancel;
- giữ rõ giới hạn local/disposable, approval pending và production not authorized.

### Verify

- không còn claim `published n8n NOT_RUN` hoặc `Admin UI BLOCKED_ENVIRONMENT` trong DD tổng;
- traceability matrix có đủ A, B, C, D, E và F;
- residual risks không bị che giấu.

## Source/runtime

Không sửa source code, database, secret hoặc n8n runtime trong G0-12F.

