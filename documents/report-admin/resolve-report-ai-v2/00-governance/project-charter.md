# Project Charter — Resolve Report with AI V2

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Dossier | `resolve-report-ai-v2` |
| Loại tài liệu | Governance charter |
| Trạng thái | `APPROVED` cho phạm vi Pre-Sprint Gate 0 |
| Người phê duyệt nghiệp vụ | Product/Business Owner |
| Approval trên chat | `APPROVE_G0-01`, `APPROVE_G0-03` |
| Ngày cập nhật | 2026-07-23 |

> Việc phê duyệt charter không đồng nghĩa với phê duyệt policy production, rule catalog, auto-apply hoặc thay đổi source code.

## 2. Bối cảnh

Chức năng Admin Resolve Report with AI hiện có pipeline tạo recommendation, validate kết quả và hỗ trợ Admin xử lý report. Tuy nhiên, chất lượng đánh giá hiện mới ở mức skeleton:

- report của người dùng đang là nguồn thông tin nổi bật nhưng chưa được tách rõ khỏi fact đã xác minh;
- evidence chưa có tiêu chuẩn chất lượng, provenance và traceability đầy đủ;
- severity, confidence, risk và action chưa có semantics thống nhất;
- rule và policy chưa đủ chi tiết để giải thích vì sao một action được đề xuất;
- chưa có cơ sở đủ mạnh để tin tưởng các hành động tự động mang tính khó hoàn tác.

Vì vậy, trước khi sửa code cần xây dựng một bộ Policy–Evidence–AI System Design Dossier làm nguồn thiết kế và quyết định có kiểm soát.

## 3. Tuyên bố mục tiêu

Thiết kế Resolve Report with AI V2 như một hệ thống hỗ trợ Admin điều tra và ra quyết định dựa trên evidence, trong đó:

```text
Reporter claim
→ Trusted input and target snapshot
→ Evidence and counter-evidence
→ Policy finding
→ AI recommendation
→ Backend validation
→ Admin decision or approved safe automation
```

AI không được tự xem mình là cơ quan xác minh, nguồn evidence độc lập hoặc người có thẩm quyền ra quyết định cuối cùng.

## 4. Mục tiêu cụ thể

1. Phân biệt rõ claim, fact, evidence, observation, inference và policy finding.
2. Chuẩn hóa terminology, severity, confidence, probability, harm và action risk.
3. Xác định evidence tối thiểu và burden of proof tương ứng với từng action.
4. Làm cho recommendation có thể giải thích, kiểm tra và audit.
5. Đồng bộ semantics giữa Admin UI, Backend, n8n và AI model.
6. Xác định ranh giới Human-in-the-loop và automation an toàn.
7. Tạo traceability từ product objective đến policy, rule, prompt, validator, UI và test.
8. Chỉ thiết kế Sprint 1 sau khi các policy decision bắt buộc đã được phê duyệt.

## 5. Kết quả mong muốn của Gate 0

- bộ thuật ngữ không còn mơ hồ;
- cơ sở lý thuyết và evidence standard có thể dùng để phản biện rule;
- current-state inventory dựa trên source/database thực tế;
- policy framework và rule catalog được gắn trạng thái rõ ràng;
- danh sách business decision và open question được quản lý;
- traceability matrix chỉ ra nguồn gốc của từng yêu cầu;
- Detailed Design Sprint 1 không tự phát minh policy trong lúc coding.

## 6. Chủ thể và thẩm quyền

| Chủ thể | Trách nhiệm | Không có thẩm quyền |
|---|---|---|
| Product/Business Owner | Phê duyệt objective, scope, policy, rule và ranh giới automation | Không cần trực tiếp triển khai kỹ thuật |
| Codex/Engineering | Audit, cung cấp evidence, phản biện, đề xuất và triển khai phần đã được duyệt | Không tự phê duyệt business policy |
| Admin | Review recommendation và đưa ra quyết định moderation theo quyền được cấp | Không thay đổi policy bằng một quyết định đơn lẻ |
| AI model/n8n | Phân tích và điều phối recommendation theo contract | Không phải source of truth và không tự mutate dữ liệu CafeStory |
| Backend | Xây input tin cậy, validate contract/policy và kiểm tra điều kiện trước mutation | Không được che giấu thiếu evidence bằng fallback tùy tiện |

## 7. Nguyên tắc bắt buộc

1. Report của user là claim, chưa phải fact.
2. `INSUFFICIENT_EVIDENCE` không đồng nghĩa với “report sai” hoặc “target vô tội”.
3. AI output không được xem là evidence duy nhất cho chính kết luận của AI.
4. Action càng khó hoàn tác thì burden of proof càng cao.
5. Recommendation phải truy vết được về evidence, rule và policy version.
6. Admin phải biết AI đã quan sát gì, suy luận gì và còn thiếu gì.
7. Human override phải có audit trail phù hợp với mức độ rủi ro.
8. Auto-apply phải fail-safe và revalidate ngay trước khi thực thi.
9. Backend giữ vai trò source of truth cho business validation; n8n chỉ orchestration.
10. Không tuyên bố hoàn thành khi tiêu chí bắt buộc chưa được kiểm chứng.

## 8. Ràng buộc thực hiện

- Tuân thủ Bound → Execute → Verify → Done.
- Mỗi mục Gate 0 phải được review trước khi chuyển mục tiếp theo.
- Tài liệu normative mặc định là `PROPOSED` cho đến khi có business approval rõ ràng.
- Không sửa FE, BE, database hoặc n8n trong Gate 0.
- Không biến thiếu fixture hoặc thiếu runtime thành kết luận hệ thống đã pass.
- Không mở rộng sang deep evidence theo từng target type trong Sprint 1 nếu chưa được phê duyệt.

## 9. Definition of Done của Gate 0

Gate 0 chỉ hoàn thành khi:

- toàn bộ checklist G0 bắt buộc đã được duyệt;
- mỗi thuật ngữ, policy và rule quan trọng có trạng thái rõ ràng;
- blocker, assumption và open question được ghi nhận;
- business decision bắt buộc đã được chốt hoặc đánh dấu blocker;
- Sprint 1 có traceability về policy đã duyệt;
- không có thay đổi source code ngoài phạm vi;
- Product/Business Owner cấp approval cuối trước khi bước sang coding.

## 10. Approval record

| Gate | Kết quả | Ý nghĩa |
|---|---|---|
| `G0-01` | `APPROVED` | Duyệt mục tiêu, nguyên tắc và ranh giới charter trên chat |
| `G0-03` | `APPROVED` | Cho phép ghi charter và scope đã duyệt vào dossier |

