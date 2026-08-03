# Workflow Improvement — G0-12A

1. G0-12B nên tính diff/changed-file coverage tách khỏi aggregate class cũ để tránh che phần code mới.
2. G0-12D phải có exact-webhook test matrix gồm valid, missing header, invalid HMAC, stale, future, replay,
   tampered body và signed response.
3. Không dùng `/healthz` để suy ra published webhook readiness.
4. Trước G0-12D phải rotate provider key đã lộ và chỉ báo boolean configured/not-configured trong evidence.
5. Thêm secret readiness preflight nhưng không in giá trị hoặc chiều dài secret.
6. Khi có nhiều n8n/Backend instance, thay workflow static data và in-process map bằng shared atomic TTL store.
7. Thiết kế secret rotation về sau nên có `keyId`, primary/secondary overlap ngắn và audit không chứa secret.
8. N8n test nên tiếp tục chạy trực tiếp code được embed trong export để phát hiện drift giữa test và workflow.
