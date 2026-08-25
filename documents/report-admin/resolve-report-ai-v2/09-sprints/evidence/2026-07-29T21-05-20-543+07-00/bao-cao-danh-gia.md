# Báo cáo đánh giá S2-01

## Kết luận

`S2-01 — Runtime Rule Context Contract` đã hoàn thành về source, verification và được user review
bằng `APPROVE_S2_01`.

## Đã thực hiện

1. Chuyển `policyContext` và `evidence[]` từ raw map sang typed DTO.
2. Materialize version, lifecycle, rule metadata, evidence availability và missing requirements.
3. Dùng Common Evidence Envelope tương thích M07.
4. Loại reporter claim và prior-AI signal khỏi evidence.
5. Bind Evidence subject với target type/version/hash.
6. Guard duplicate Rule ID, duplicate Evidence ID và snapshot mismatch.
7. Clamp policy/rule `PROPOSED` về `NEEDS_MANUAL_REVIEW + NO_ACTION`.
8. Tách n8n internal `requestContext` khỏi allowlisted `providerInput`.
9. Không gửi report claim, raw target ID, internal request metadata, raw prior-AI hoặc raw media URL
   sang provider.
10. Bump prompt/workflow behavior version sang Sprint 2.1.

## Lỗi đã phát hiện và sửa

| ID | Lỗi | Kết quả |
|---|---|---|
| `S2-01-ISSUE-001` | Test cũ còn phụ thuộc raw map nên không compile sau typed contract | Đổi fixture/assertion sang typed; verified |
| `S2-01-ISSUE-002` | Backend từng coi null/unknown evaluation mode là active trong một tổ hợp | Chỉ allow exact `ACTIVE_RUNTIME`; verified |
| `S2-01-ISSUE-003` | Lifecycle từng candidate rule chưa bị chặn và conditional requirement có thể bị caller mutate | Bắt buộc exact `ACTIVE`, deep-copy catalog DTO; verified |

## Verification

- Focused Backend: `40/40 PASS`.
- Full Backend: `628`, failure `0`, error `0`, skipped `1`.
- Changed production coverage: line `100%`; branch thấp nhất `88.84%`.
- n8n ADV: `12/12 PASS`.
- n8n contract guards: `3/3 PASS`.
- M07 fixtures: `18/18 PASS`.
- M07 cross-review: `16/16 PASS`.
- `git diff --check`: pass; chỉ có cảnh báo LF/CRLF.

## Giới hạn kết luận

- Workflow source đã verify bằng executable VM test nhưng chưa import/publish lên n8n runtime.
- Không gọi OpenAI provider thật trong S2-01.
- Không chạy full-path Admin UI với workflow mới.
- Không activate policy; runtime authority vẫn false.
- Không thay database hoặc frontend trong S2-01.

## Trạng thái

`COMPLETED_VERIFIED_APPROVED`

Bước tiếp theo chưa triển khai: chờ `IMPLEMENT_S2_02`.
