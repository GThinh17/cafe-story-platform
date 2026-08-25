# S2-04 — Bound

## Mục tiêu

Tạo bộ prompt candidate theo rule family có version, assembler deterministic và
executable harness cho text-only BLOG/COMMENT, dựa trên Runtime Rule Context và
dataset S2-03 đã được approve.

## Pilot được chọn

| Rule | Nhánh | Lý do |
|---|---|---|
| `CSR.HAR.001` | BLOG text trực tiếp và COMMENT phụ thuộc context | Có thể kiểm tra cả hai burden branch mà không mở thêm rule family: direct khi target/conduct quan sát được; context-dependent khi COMMENT cần parent BLOG context |

Loại khỏi pilot:

- `CSR.HAR.002`, `CSR.SPAM.001`: cần pattern/history chưa có;
- `CSR.HATE.001`: Runtime Rule Context chưa pin protected-list version;
- `CSR.REL.001`: runtime catalog hiện phát hành sai semantic thành `VIOLATION`
  có punitive candidate actions, không khớp policy `NON_VIOLATION_SIGNAL`;
  xem `S2-04-ISSUE-001`;
- legal/privacy/identity/authoritative/media rule: cần adapter hoặc policy context
  ngoài scope.

## Phạm vi được thay đổi

- Thêm prompt-candidate specification/schema dưới `docker/contracts/`.
- Thêm pure prompt assembler candidate dưới
  `docker/n8n-code/admin-report-ai-resolution/`.
- Thêm deterministic prompt-pilot harness/fixtures dưới `docker/tests/`.
- Cập nhật DD/governance/evidence dưới `documents/`.

## Ngoài phạm vi và không được tác động

- Không đổi Backend production source/public API/database.
- Không sửa dataset/schema/rubric/manifest S2-03.
- Không thay canonical `openaiRequest` đang dùng hoặc published n8n runtime.
- Không gọi provider; không benchmark quality/cost/latency.
- Không activate policy/rule catalog; giữ `A0_RECOMMEND_ONLY`.
- Không thay FE/mobile, không tạo report/target mutation.
- Không mở `S2-05` hay `S2-DONE-AUDIT`.

## Acceptance

1. Prompt candidate versioned, `PROPOSED`, `runtimeAuthority=false`.
2. Base safety, lifecycle, rule/requirement/exception/ceiling và output clauses
   được tách rõ.
3. Untrusted report/target/evidence/prior-AI text không xuất hiện trong system
   prompt.
4. Assembler không tạo/mở rộng rule ID và chỉ gắn profile đã allowlist.
5. HAR direct BLOG và HAR context-dependent COMMENT có executable test.
6. Missing target hoặc parent context luôn route manual/no-action.
7. Injection/adversarial và S2-03 hard gate vẫn pass 100%.
8. Provider call `false`; runtime publish `false`; production source change `0`.

## Môi trường

- Windows PowerShell, Node.js hiện hành của workspace.
- Maven chạy trong `1-cafe-story-backend-javaspring`.
- Dùng synthetic fixtures; không dùng production data hoặc secret.
