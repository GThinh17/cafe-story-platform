# Scope and Non-goals — Resolve Report with AI V2

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Dossier | `resolve-report-ai-v2` |
| Loại tài liệu | Governance scope boundary |
| Trạng thái | `APPROVED` cho Pre-Sprint Gate 0 |
| Approval trên chat | `APPROVE_G0-01`, `APPROVE_G0-03` |
| Ngày cập nhật | 2026-07-23 |

## 2. System of interest

Phạm vi chính là luồng **Admin Resolve Report with AI**:

```text
Admin UI
→ Admin Report API
→ Backend AI-resolution service
→ n8n/model recommendation
→ Backend semantic and safety validation
→ Recommendation/history
→ Admin decision or approved delayed auto-apply
```

Luồng người dùng tạo report chỉ được xem là nguồn input liền kề. Gate 0 không tái thiết kế toàn bộ quy trình tạo report của người dùng.

## 3. Trong phạm vi Gate 0

### 3.1. Governance và nền tảng

- Project Charter, scope, roles, approval gate và Definition of Done.
- Glossary dùng chung giữa business, FE, BE, n8n và AI.
- Moderation model, evidence theory, uncertainty, risk và Human-in-the-loop.

### 3.2. Current-state audit

- Khảo sát report reasons, severity và state hiện có.
- Khảo sát policy/rule đang được biểu diễn trong source và database.
- Khảo sát prompt, input/output contract, validator, auto-apply và UI hiện tại.
- Ghi rõ bằng chứng, giới hạn kiểm chứng và phần chưa xác định.

Việc khảo sát database mặc định là read-only. Mọi mutation, seed hoặc metadata repair cần approval riêng.

### 3.3. Thiết kế policy và evidence

- Policy taxonomy và policy principles.
- Severity, decision và enforcement-action framework.
- Evidence hierarchy, provenance, quality và minimum evidence.
- Counter-evidence, missing evidence và snapshot integrity.
- Rule catalog bản đầu với trạng thái `PROPOSED`.
- Traceability matrix và business decision log.

### 3.4. Phạm vi kỹ thuật ở mức thiết kế

- Contract dự kiến cho FE, BE, n8n và model.
- Prompt design principles và semantic validation.
- Audit, security, idempotency, error và auto-apply design.
- Verification strategy và Detailed Design Sprint 1.

Các nội dung này là thiết kế; chưa cho phép thay đổi runtime hoặc source.

### 3.5. Target types

Gate 0 xem xét phần General dùng chung cho các target đang có:

- `BLOG`;
- `COMMENT`;
- `USER`;
- `CAFE_PAGE`.

Target-specific deep evidence chỉ được ghi nhận ở mức yêu cầu tối thiểu hoặc roadmap, chưa triển khai collector chuyên sâu.

## 4. Ngoài phạm vi Gate 0

- Sửa source code FE, BE hoặc mobile.
- Tạo hoặc sửa database migration.
- Mutation dữ liệu development, staging hoặc production.
- Sửa, import, publish hoặc activate workflow n8n.
- Thay đổi prompt/model đang chạy thực tế.
- Bật hoặc nới điều kiện auto-apply.
- Tự động thực thi `REMOVE`, `SUSPEND_USER` hoặc `SUSPEND_PAGE`.
- Triển khai OCR, computer vision, crawler hoặc external fact-checking.
- Huấn luyện/fine-tune model hoặc xây production evaluation dataset hoàn chỉnh.
- Thiết kế lại toàn bộ user-created report submission pipeline.
- Thay đổi mobile application khi không có contract impact được duyệt.
- Tự tạo fixture có tác động dữ liệu chỉ để biến test bị block thành pass.
- Đọc, ghi hoặc công bố secret/API key.
- Commit tài liệu khi chưa có yêu cầu commit tài liệu rõ ràng.

## 5. Phạm vi Sprint 1 chưa được mặc định phê duyệt

Gate 0 không mặc định đưa toàn bộ dossier vào Sprint 1. Sprint 1 chỉ được xác định sau khi policy framework, rule catalog, traceability và business decisions bắt buộc được chốt.

Các phần sau được xem là ứng viên cải tiến tương lai, không tự động trở thành phạm vi Sprint 1:

- evidence collector sâu riêng cho từng target type;
- media understanding/OCR;
- model calibration bằng dataset lớn;
- autonomous destructive moderation;
- appeal workflow đầy đủ;
- cross-target behavioral investigation;
- long-term model drift automation.

## 6. Tài sản được bảo vệ

Trong Gate 0 không được tác động ngoài ý muốn đến:

- source và test hiện có của FE/BE/mobile;
- schema và dữ liệu database;
- workflow n8n đã publish/activate;
- evidence runtime đã thu thập trước đó;
- secret, credential và thông tin cá nhân;
- thay đổi không liên quan đang có trong worktree;
- lịch sử Git và commit hiện tại.

## 7. Hành động được phép

- Đọc source/config/test liên quan.
- Thực hiện truy vấn database read-only sau khi môi trường được xác định.
- Kiểm tra workflow/export n8n theo phương thức không mutation.
- Viết và cập nhật hồ sơ trong `documents/report-admin/resolve-report-ai-v2/`.
- Chạy validation tĩnh cho tài liệu, schema hoặc artifact đã tạo.
- Ghi blocker, open question và evidence thiếu thay vì suy đoán.
- Cập nhật checklist G0 sau approval của người dùng.

## 8. Tiêu chí chấp nhận phạm vi

Scope được xem là đạt khi:

1. Phân biệt rõ system of interest và adjacent systems.
2. Phân biệt rõ design work và implementation work.
3. Không trao quyền ngầm cho mutation source/database/n8n.
4. General scope và target-specific future scope không bị trộn lẫn.
5. Mọi phát hiện ngoài phạm vi được ghi nhận nhưng không tự sửa.
6. Mọi bước tiếp theo tiếp tục chịu approval gate trên chat.

