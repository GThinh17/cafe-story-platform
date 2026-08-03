# Đề xuất cải tiến workflow — G0-12D

## Ưu tiên P0/P1 trước khi production

1. Thay nhánh `throw` ở bước kiểm tra request bằng một response lỗi được kiểm soát:
   - HTTP `400` cho thiếu/sai contract;
   - HTTP `401` hoặc `403` cho chữ ký sai;
   - HTTP `409` cho nonce replay;
   - body lỗi đã sanitize, tuyệt đối không chứa secret hay nội dung provider.
2. Giữ Backend ở chế độ fail-closed: response n8n rỗng, thiếu chữ ký hoặc chữ ký sai không được persistence và không được tạo target action.
3. Thêm runtime regression test kiểm tra cả HTTP status, response body và execution audit. Hiện n8n `2.28.6` đánh dấu execution là `error`, nhưng transport vẫn trả `200` với body rỗng khi Code node `throw`.
4. Cấu hình rõ lifecycle của `N8N_ENCRYPTION_KEY` trước production. Không thay key của volume hiện hữu nếu chưa có backup/restore drill vì có thể làm mất khả năng giải mã credential.

## Ưu tiên P2

5. Chuyển nonce store từ file atomic sang Redis/PostgreSQL có atomic create-if-absent và TTL trước khi chạy nhiều n8n instance.
6. Thêm deploy preflight bắt buộc:
   - chỉ một canonical workflow ID;
   - node parameter hash khớp repo;
   - đúng published version;
   - readiness secret chỉ xuất boolean;
   - exact webhook probe pass, không dùng `/healthz` làm bằng chứng thay thế.
7. Thêm provider spy/counter trong môi trường test để chứng minh request invalid, stale, replay và tampered bị chặn trước provider.
8. Chuẩn hóa key rotation bằng `keyId`, thời gian overlap primary/secondary ngắn và audit không chứa secret.
9. Cảnh báo drift nếu workflow đang publish khác export canonical trong repo.

## Bài học từ lần verify này

- `$getWorkflowStaticData('global')` phù hợp với static simulation nhưng không chứng minh được replay protection ở runtime này. Hai execution liên tiếp từng cùng `success` với một nonce.
- Nonce store mới dùng file tên là SHA-256 của nonce và `fs.openSync(..., 'wx')`; thao tác tạo file độc quyền giúp claim atomic trên một volume dùng chung.
- File nonce tồn tại sau restart n8n, nhưng đây vẫn chỉ là baseline một instance, chưa phải thiết kế phân tán.
- `/healthz = 200` chỉ chứng minh process sẵn sàng; exact webhook, response signature và execution audit mới chứng minh workflow thực sự sẵn sàng.
