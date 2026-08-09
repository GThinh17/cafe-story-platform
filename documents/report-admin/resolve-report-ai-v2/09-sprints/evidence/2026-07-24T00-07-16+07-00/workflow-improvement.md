# Gợi ý cải tiến workflow sau G0-12

1. Không publish n8n trước khi HMAC/replay tests pass.
2. Thêm một preflight endpoint không gọi OpenAI để phân biệt liveness và workflow readiness.
3. Chạy contract fixtures trong CI cho BLOG sufficient, COMMENT insufficient và USER/PAGE local manual.
4. Tách snapshot/evidence factory ra khỏi resolution service để tăng testability và coverage.
5. Dùng stage-specific bulk outcome xuyên suốt BE/FE/E2E.
6. Dùng DB unique constraint làm final idempotency guard và map race conflict thành reuse response.
7. Chỉ mở target-deep policy sau khi có evidence model và business approval riêng.
