# Fix log — DOD-FIX-01

## FIX01-TEST-001

- Thay đổi: assertion tìm JSON-encoded payload thay vì chuỗi thô.
- Production source: không thay đổi.
- Trạng thái: `FIXED_VERIFIED`; suite sau sửa `12/12 PASS`.

## FIX01-CODE-001 / FIX01-CODE-002

- Thay đổi:
  - n8n kiểm tra allowlist cho findings và `evidenceSummary`;
  - n8n clamp finding `SUPPORTED` chỉ dựa vào claim/derived/unusable evidence;
  - Backend lặp lại hai semantic guard trên;
  - bổ sung regression test cho reference fields và independent evidence basis.
- Kết quả:
  - ADV suite `12/12 PASS`;
  - n8n security suite `PASS`;
  - focused Backend `60/60 PASS`;
  - full Backend `621 tests`, `0 failures`, `0 errors`, `1 skipped`;
  - changed class line `100%`, branch `96.61%`.
- Trạng thái: `FIXED_VERIFIED`.

## FIX01-TEST-002

- Thay đổi: sửa đường dẫn JaCoCo sang module Backend.
- Production source: không thay đổi.
- Trạng thái: `FIXED_VERIFIED`; XML đọc thành công.

## Runtime/deployment

- Không publish workflow.
- Không gọi provider thật.
- Không thay đổi database/secret/UI.
- Không production deploy.
