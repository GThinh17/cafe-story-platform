# Fix Log — G0-12D

| Thứ tự | Issue | Hành động | Kết quả kiểm chứng |
|---:|---|---|---|
| 1 | `G012D-RUNTIME-001` | Xác nhận Docker/n8n và liệt kê workflow | n8n `2.28.6` chạy; phát hiện 3 workflow cùng tên |
| 2 | `G012D-RUNTIME-001` | Export backup toàn bộ workflow trước mutation | `5` workflow được lưu tại `raw/workflows-before/` |
| 3 | `G012D-RUNTIME-001` | So sánh active V2 với canonical export | Runtime cũ thiếu toàn bộ HMAC/freshness/replay markers và còn numeric scores |
| 4 | `G012D-RUNTIME-001` | Unpublish đúng ID V2 rồi restart container | CLI thành công; `/healthz` `200`; exact webhook `404` |
| 5 | `G012D-RUNTIME-001` | Import canonical export với `--activeState=false` | Import thành công; workflow `active=false`, không archived |
| 6 | `G012D-RUNTIME-001` | Export lại và so sánh từng node | `5/5` node có parameter hash khớp canonical repo export |
| 7 | Security static | Chạy executable validator trên canonical export | valid/replay/stale/tamper/invalid signature/signed response đều `PASS` |
| 8 | `G012D-SECRET-002` | Không dùng provider key cũ trong pha safety ban đầu | Provider chỉ được gọi sau khi người dùng xác nhận rotate |
| 9 | `G012D-CONFIG-003` | Không tạo hoặc ghi secret giả vào source/evidence khi chưa được người dùng tiếp tục | Gate từng giữ `BLOCKED`, sau đó được mở bằng shared HMAC local |
| 10 | `G012D-SECRET-002` | Người dùng rotate key; sync từ `docker/.env` chỉ sang consumer hợp lệ | OpenAI `/v1/models` `200`; n8n key khớp source; AI Python client init pass |
| 11 | `G012D-CONFIG-003` | Sinh shared HMAC 256-bit local; sync sang Backend, recreate n8n | Backend/n8n equality boolean `true`; không in secret |
| 12 | `G012D-CONFIG-005` | Xóa `OPENAI_API_KEY` thừa khỏi Backend; không thêm vào FE/mobile | n8n + AI Python configured; Backend/FE/mobile absent |
| 13 | `G012D-CODE-006` | Thay static `RandomNumberGenerator.Fill` bằng `Create().GetBytes()` | Windows PowerShell fixture pass |
| 14 | `G012D-TEST-007` | Cho assertion chấp nhận CRLF | Fixture pass |
| 15 | `G012D-CODE-008` | Resolve default paths trong thân script | Real local sync pass |
| 16 | `G012D-TEST-009` | Gọi AI Python client với required model name | `PYTHON_OPENAI_CLIENT_INIT=PASS` |
| 17 | `G012D-CODE-010` | Thay workflow static data bằng atomic nonce file trên persisted n8n volume | Static replay/expiry/atomic pass; runtime execution `215` success, `216` replay error |
| 18 | G0-12D runtime | Import/publish exact version `4e221ccf-12a3-4583-8336-c4fda22b5b70` | Published export active; `5/5` node parity |
| 19 | G0-12D runtime | Chạy valid + replay/stale/future/invalid-signature/tamper/missing-header | Provider signed response pass; six negative executions error trước provider |
| 20 | Backend regression | Rerun signer/service focused tests | `37/37 PASS` |

## Rollback

Backup trước thay đổi nằm trong `raw/workflows-before/`. Nếu cần rollback local, chỉ restore đúng workflow ID đã chọn; không import toàn bộ backup vì có thể ghi đè Admin Assistant và các workflow ngoài scope.

## Failed-test repair loop

1. Windows PowerShell 5.1 không có `RandomNumberGenerator.Fill` → `CODE_BUG` → sửa API tương thích.
2. CRLF làm fixture báo sai → `TEST_BUG` → sửa regex test.
3. Default path dùng `$PSScriptRoot` quá sớm → `CODE_BUG` → resolve trong thân script.
4. Python smoke thiếu constructor argument → `TEST_BUG` → rerun đúng signature.
5. Runtime chứng minh `$getWorkflowStaticData()` không persist: version cũ có hai success cho cùng nonce →
   `CODE_BUG/CRITICAL` → atomic persisted nonce file → version mới success/error đúng.
6. Negative webhook trả transport `200` body rỗng. Execution audit vẫn chứng minh error tại validator và Backend
   fail closed; ghi thành hardening riêng, không nới test security.
