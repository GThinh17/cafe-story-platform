# Community Policy Principles

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-07` |
| Trạng thái | `PROPOSED` |
| Loại | Normative Policy Framework |
| Approval chat | `APPROVE_G0-07` chỉ cho phép soạn thảo, không phải business adoption |
| Input | Approved theory G0-05; current-state gaps `CG-001`–`CG-019` |
| Version draft | `PF-2.0.0-proposed.1` |

## 2. Mục tiêu

Thiết lập nguyên tắc chung để mọi report được đánh giá:

- dựa trên evidence thay vì chỉ dựa trên reporter claim;
- nhất quán giữa AI, n8n, Backend và Admin UI;
- giảm false positive khi action khó hoàn tác;
- vẫn ưu tiên human escalation khi harm tiềm năng cao;
- có thể audit, contest và cải tiến.

## 3. Từ khóa normative

| Từ khóa | Ý nghĩa |
|---|---|
| `MUST` | Bắt buộc để đạt policy compliance |
| `MUST NOT` | Bị cấm |
| `SHOULD` | Khuyến nghị mạnh; deviation phải có lý do |
| `MAY` | Tùy chọn trong phạm vi policy cho phép |

## 4. Authority model

| Actor/component | Vai trò | Không có quyền |
|---|---|---|
| Reporter | Gửi claim và context ban đầu | Không chứng minh violation chỉ bằng report |
| AI model | Phân tích và đưa recommendation có cấu trúc | Không quyết định cuối, không mutation |
| n8n | Orchestrate, validate shape, gọi provider | Không sở hữu policy, không ghi CafeStory DB |
| Backend | Source of truth cho policy/action eligibility, validation, persistence, execution guard | Không được tin model output mà bỏ semantic validation |
| Admin | Human review/decision trong phạm vi quyền | Không override action policy-invalid hoặc bỏ qua revalidation |
| Policy owner | Duyệt policy/rule/version | Chưa chốt danh tính/role tại G0-07 |

## 5. Normative principles

| ID | Nguyên tắc đề xuất |
|---|---|
| `CP-001` | Report reason và reporter description MUST được xem là claim, không phải fact hoặc violation evidence. |
| `CP-002` | Mỗi policy finding MUST tham chiếu rule version và evidence ID cụ thể. |
| `CP-003` | AI explanation MUST NOT được dùng như evidence; chỉ là structured rationale. |
| `CP-004` | Critical missing evidence hoặc unresolved material conflict MUST dẫn tới `NEEDS_MANUAL_REVIEW + NO_ACTION`. |
| `CP-005` | Harm tiềm năng cao nhưng evidence chưa đủ MUST tạo urgent human escalation, không tự động trừng phạt. |
| `CP-006` | Report decision và target action MUST được đánh giá riêng; action burden tăng theo irreversibility và blast radius. |
| `CP-007` | Backend MUST là authority cuối cho action eligibility và MUST revalidate ngay trước mutation. |
| `CP-008` | Automation MUST là capability được allowlist riêng; baseline V2 là `A0 RECOMMEND_ONLY`. |
| `CP-009` | Admin UI MUST hiển thị evidence, counter-evidence, missing evidence, uncertainty và version context trước action. |
| `CP-010` | Mọi recommendation/decision/execution MUST có audit record phân biệt rõ ba giai đoạn. |
| `CP-011` | Raw input/output MUST được sanitize, kiểm soát quyền truy cập và retention; secret MUST NOT được lưu. |
| `CP-012` | Policy, rule, schema, prompt, workflow và model context MUST được version/pin cho mỗi recommendation. |
| `CP-013` | Operational failure MUST tách khỏi content uncertainty và MUST NOT tự chuyển thành `REJECT` hoặc `RESOLVE`. |
| `CP-014` | Không dùng một global preference giữa false positive và false negative; trade-off MUST theo rule và action. |
| `CP-015` | Không dùng threshold/công thức số để cấp automation trước khi có evaluation/calibration evidence được duyệt. |
| `CP-016` | User/Admin MUST có đường contest/appeal phù hợp với action; override MUST lưu structured reason. |
| `CP-017` | Giao tiếp Backend–n8n MUST có authentication, integrity protection, freshness/timestamp và replay protection. |
| `CP-018` | Reporter description, target content và external text MUST được coi là untrusted data; chúng MUST NOT thay đổi system instruction, policy authority hoặc tool/action scope. |

## 6. Decision quality order

Khi các mục tiêu xung đột, hệ thống ưu tiên:

1. Không thực hiện action ngoài authority/policy.
2. Không thực hiện destructive action khi burden chưa đạt.
3. Escalate harm cao khi evidence chưa đủ.
4. Bảo toàn evidence/audit trail.
5. Chọn action ít xâm lấn nhất vẫn đáp ứng policy.
6. Tối ưu tốc độ/automation sau khi các điều kiện trên đạt.

## 7. Không phải policy evidence

Các nguồn sau không tự chứng minh violation:

- số lượng report;
- reason severity do catalog gán;
- reporter description;
- AI moderation output cũ;
- AI explanation hiện tại;
- confidence cao;
- rule code free-form;
- label hoặc anomaly không có evidence độc lập.

Chúng MAY dùng để ưu tiên điều tra hoặc định tuyến review.

## 8. Baseline safety

Cho tới khi G0-10 duyệt khác:

- automation mode được xem là `A0 RECOMMEND_ONLY`;
- `REMOVE`, `SUSPEND_USER`, `SUSPEND_PAGE` luôn Human Review;
- evidence thiếu không được hiểu là report sai;
- image là critical evidence nhưng không đọc được thì manual review;
- policy/rule/version/citation invalid thì block automation.

## 9. Quan hệ với gap

Tài liệu này trực tiếp đặt policy control cho:

- `CG-001`, `CG-003`, `CG-006`;
- `CG-008`–`CG-013`;
- `CG-015`.

Chi tiết taxonomy nằm ở `policy-taxonomy.md`; decision/action nằm ở hai framework tương ứng.

## 10. Nội dung chưa được chốt

- policy owner cụ thể;
- role matrix;
- two-person approval;
- auto-apply allowlist;
- retention period;
- SLA review/appeal;
- threshold/calibration acceptance.

Các mục này chuyển G0-10.
