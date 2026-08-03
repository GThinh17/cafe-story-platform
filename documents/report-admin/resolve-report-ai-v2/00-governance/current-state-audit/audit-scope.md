# Current-state Audit Scope

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Dossier | `resolve-report-ai-v2` |
| Trạng thái | `APPROVED` |
| Approval | `APPROVE_G0-06A` |
| Chế độ | Read-only audit |
| Ngày cập nhật | 2026-07-23 |

## 2. Mục tiêu

Xác định bằng evidence chức năng hiện tại thực sự đang có:

- report reasons, severity, status và target type;
- decision/action và score semantics;
- policy/rule representation;
- prompt/input/output contract;
- Backend validation và auto-apply behavior;
- Admin UI behavior;
- n8n orchestration;
- database persistence;
- phần nào là current, legacy, missing, conflict hoặc unknown.

## 3. Trong phạm vi

### G0-06B — Source audit

- Backend entity/enum/DTO/controller/service/repository.
- Validation, persistence, scheduling, cancel và execution code.
- Migration, schema artifact và seed liên quan.
- Admin FE types, API integration, labels, filters, bulk và action mapping.
- n8n workflow export, prompt, normalization và response contract.
- Existing unit/integration/E2E test và evidence liên quan.

### G0-06C — Database audit

- schema/table/column liên quan;
- distinct report reason/status/severity/target type;
- AI recommendation/action fields;
- policy/rule/version fields;
- aggregate/null/legacy observations cần thiết;
- chỉ truy vấn read-only và sanitize output.

### G0-06D — Consolidation

- inventory có source/query reference;
- gap register;
- conflict với approved theoretical foundation;
- blocker/unknown;
- input cho Policy Framework `PROPOSED`.

## 4. Ngoài phạm vi

- sửa source/test/config;
- `INSERT`, `UPDATE`, `DELETE`, DDL hoặc migration;
- seed hoặc tạo fixture;
- Flyway metadata repair;
- import/publish/activate workflow n8n;
- runtime mutation qua API/UI;
- đọc hoặc lưu secret;
- kết luận runtime pass khi chỉ audit tĩnh;
- viết policy framework trước khi current-state inventory được review.

## 5. Protected assets

- source và test hiện có;
- database schema/data;
- workflow n8n active/published;
- credential/API key;
- evidence cũ;
- unrelated worktree changes;
- Git history.

## 6. Evidence standard

Mỗi finding phải có:

- finding ID;
- classification: `CURRENT`, `LEGACY`, `MISSING`, `CONFLICT`, `UNKNOWN`;
- component;
- file:line, artifact reference hoặc read-only SQL query;
- observed behavior/data;
- confidence/limitation;
- relation với approved foundation;
- không chứa secret hoặc direct PII không cần thiết.

## 7. Môi trường

- G0-06B không yêu cầu runtime; dùng active checkout.
- G0-06C cần database local chạy và connection được lấy từ cấu hình hiện có mà không in secret.
- n8n runtime chưa cần cho static export audit; runtime probe cần approval riêng nếu phát sinh.

## 8. Acceptance criteria

1. Current và proposed được tách rõ.
2. Mỗi kết luận có evidence reference.
3. Không suy diễn từ tên field hoặc report data.
4. Không mutation source/database/n8n.
5. Thiếu môi trường được ghi `PARTIAL` hoặc `BLOCKED`.
6. Gap được phân loại thống nhất.
7. Evidence được sanitize.
8. Không tuyên bố full runtime compliance từ static audit.

