# Database Inventory — Read-only Runtime Snapshot

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-06C` |
| Approval | `APPROVE_G0-06C` |
| Trạng thái | `AUDITED_READ_ONLY` |
| Thời điểm | 2026-07-23 |
| Engine | PostgreSQL `17.6` |
| Schema | `public` |
| Guard | `transaction_read_only=on`, statement timeout 15 giây, lock timeout 3 giây |
| Kết thúc | `ROLLBACK_COMPLETE` |

Database host, database name, username và credential không được ghi vào evidence.

## 2. Phạm vi query

Đã đọc:

- schema/column/constraint/index metadata;
- Flyway state của hai migration AI report;
- report reason catalog;
- aggregate report distribution và integrity;
- aggregate AI decision/action/score/model/rule;
- aggregate auto-apply status/action/integrity.

Không đọc hoặc lưu:

- reporter ID/email/phone;
- report description hoặc target content;
- explanation/raw AI response;
- row ID;
- credential;
- dữ liệu mutation.

Query runner: `evidence/2026-07-23T16-15-27-676+07-00/raw/G006CDbAudit.java`.

## 3. Schema hiện tại

Tất cả bảng liên quan đều tồn tại:

- `report_reasons`;
- `content_reports`;
- `ai_moderation_results`;
- `admin_report_ai_resolutions`;
- `admin_report_ai_auto_apply_jobs`;
- `flyway_schema_history`.

Hai migration được DB ghi nhận thành công:

| Version | Description | Success |
|---|---|---|
| `20260705.01` | admin report ai resolutions | `true` |
| `20260706.01` | admin report ai auto apply jobs | `true` |

### Constraint/index đáng chú ý

- `content_reports.status` và `target_type` có check constraint.
- `report_reasons.target_type` có check constraint; `code` unique.
- AI resolution/job có PK và FK.
- Chỉ có unique partial index ngăn nhiều job `SCHEDULED/APPLYING` trên cùng report.
- Không có DB check constraint cho decision enum, action enum, decision/action combination, score `0..100`, score semantics, evidence sufficiency hoặc policy/rule/version.

Metadata xác nhận `content_reports` còn ba cột legacy không được `ContentReport` entity hiện tại map:

- `admin_decision`;
- `admin_note`;
- `reason_type`.

## 4. Report reason catalog runtime

Database có `22/22` reason active, severity từ `1` đến `5`, trong đó `17` reason bắt buộc description.

| Code | Severity | Requires description | Target | Quan hệ với initializer |
|---|---:|---|---|---|
| `DISLIKE_CONTENT` | 1 | Không | ALL | SOURCE |
| `SPAM` | 2 | Không | ALL | DB_ONLY |
| `BULLYING_OR_UNWANTED_CONTACT` | 3 | Không | ALL | SOURCE |
| `SCAM_OR_FRAUD` | 5 | Có | ALL | DB_ONLY |
| `HARASSMENT` | 4 | Có | ALL | DB_ONLY |
| `SELF_HARM_OR_ABNORMAL_EATING` | 5 | Có | ALL | SOURCE |
| `HATE_SPEECH` | 5 | Có | ALL | DB_ONLY |
| `VIOLENCE_HATE_OR_EXPLOITATION` | 5 | Có | ALL | SOURCE |
| `VIOLENCE_THREAT` | 5 | Có | ALL | DB_ONLY |
| `NUDITY_OR_SEXUAL_ACTIVITY` | 5 | Có | ALL | SOURCE |
| `SEXUAL_CONTENT` | 4 | Có | ALL | DB_ONLY |
| `PRIVACY_VIOLATION` | 5 | Có | ALL | DB_ONLY |
| `SCAM_FRAUD_OR_SPAM` | 4 | Không | ALL | SOURCE |
| `COPYRIGHT_VIOLATION` | 3 | Có | ALL | DB_ONLY |
| `FALSE_INFORMATION` | 3 | Có | ALL | SOURCE nhưng field drift |
| `INTELLECTUAL_PROPERTY` | 3 | Có | ALL | SOURCE |
| `IMPERSONATION` | 5 | Có | ALL | DB_ONLY |
| `FAKE_OR_MISLEADING` | 4 | Có | ALL | DB_ONLY |
| `INAPPROPRIATE_IMAGE` | 3 | Có | ALL | DB_ONLY |
| `RESTRICTED_GOODS` | 4 | Có | ALL | SOURCE nhưng field drift |
| `OFF_TOPIC_OR_IRRELEVANT` | 1 | Không | ALL | DB_ONLY |
| `OTHER` | 2 | Có | ALL | DB_ONLY |

### Source ↔ DB drift

Initializer source chỉ khai báo 9 code. DB có đủ 9 code đó và thêm 13 code `DB_ONLY`.

| Code | Source | Database |
|---|---|---|
| `FALSE_INFORMATION` | Target BLOG, không bắt buộc description, sort 80 | Target ALL, bắt buộc description, sort 90 |
| `RESTRICTED_GOODS` | Không bắt buộc description, sort 50 | Bắt buộc description, sort 130 |

Bảy label thuộc nhóm source-origin đang lưu dạng tiếng Việt không dấu, trong khi các reason thêm sau phần lớn có Unicode tiếng Việt đầy đủ. Đây là inconsistency dữ liệu, không phải lỗi decode terminal.

Nguyên nhân có khả năng cao là initializer chỉ `create if missing`, nên không reconcile record đã tồn tại. Đây là inference từ source + DB, chưa phải audit lịch sử thay đổi.

## 5. Content report runtime snapshot

| Status | Target | Count |
|---|---|---:|
| OPEN | BLOG | 1 |
| OPEN | USER | 1 |
| REVIEWING | BLOG | 1 |
| REJECTED | BLOG | 5 |
| REJECTED | CAFE_PAGE | 3 |
| REJECTED | USER | 8 |

Tổng cộng `19` reports:

- không có COMMENT report;
- không có RESOLVED report;
- `18/19` dùng reason `SCAM_FRAUD_OR_SPAM`;
- `1/19` dùng `SCAM_OR_FRAUD`;
- 20 reason còn lại không xuất hiện trong snapshot report hiện tại.

### Integrity aggregate

| Check | Count lỗi |
|---|---:|
| Null reason ID | 0 |
| Blank reason snapshot | 0 |
| Target cardinality khác 1 | 0 |
| Target type/link mismatch | 0 |
| Description dài hơn 2000 | 0 |
| Missing reason reference | 0 |
| Inactive reason reference | 0 |
| Reason target mismatch | 0 |

Data integrity kỹ thuật của 19 rows đang tốt, nhưng dataset quá hẹp để dùng làm policy/evaluation evidence cho toàn bộ target/reason.

## 6. AI resolution runtime snapshot

Tổng cộng `23` recommendation:

| Decision | Target | Action | Count |
|---|---|---|---:|
| `NEEDS_MANUAL_REVIEW` | BLOG | `NONE` | 6 |
| `RESOLVE` | BLOG | `HIDE` | 5 |
| `RESOLVE` | CAFE_PAGE | `SUSPEND_PAGE` | 6 |
| `RESOLVE` | USER | `SUSPEND_USER` | 6 |

Không có `REJECT`, COMMENT recommendation, `REMOVE` hoặc invalid decision/action combination.

Tất cả 23 rows có confidence, risk, explanation, model, labels và raw response khác null. Điều này chỉ chứng minh field presence, không chứng minh correctness hoặc evidence sufficiency.

### Score profile theo outcome

| Decision/action | Count | Confidence min–max / avg | Risk min–max / avg |
|---|---:|---|---|
| Manual review / NONE | 6 | 50–75 / 60.00 | 3–4 / 3.58 |
| Resolve BLOG / HIDE | 5 | 85–85 / 85.00 | 4.5–75 / 20.28 |
| Resolve CAFE_PAGE / SUSPEND_PAGE | 6 | 85–85 / 85.00 | 7.5–8.5 / 7.83 |
| Resolve USER / SUSPEND_USER | 6 | 85–85 / 85.00 | 7–8 / 7.33 |

`16/17` recommendation `RESOLVE` có `riskScore < 70`, trong khi BE dùng `70` làm legacy auto-resolve threshold.

Đây là bằng chứng mạnh rằng `riskScore` không có semantics nhất quán giữa model output và backend automation. Không thể biết score đang đại diện violation likelihood, harm hay action risk chỉ từ dữ liệu.

### Model và rule code

- Cả 23 rows ghi model `gpt-4o-mini-2024-07-18`.
- Có 14 `ruleCode` khác nhau trên 23 rows.
- Các code có pattern không đồng nhất như `SCAM_FRAUD_OR_SPAM`, `SCAM_FRAUD`, `BLOG_SCAM`, `BLOG_VIOLATION`, `CAFE_PAGE_POLICY`, `USER_SUSPENSION`.
- DB không có rule catalog/version để kiểm tra các code này.

## 7. Auto-apply runtime snapshot

Chỉ có một job:

- status `CANCELLED`;
- decision `RESOLVE`;
- target BLOG;
- action `HIDE`.

Không có active job, applied/skipped/failed job, persisted job `REMOVE`/`SUSPEND_USER`/`SUSPEND_PAGE`, duplicate active job hoặc score/timestamp inconsistency.

Điều này chứng minh snapshot hiện tại chưa có destructive auto-apply job được persist/applied. Nó không loại bỏ risk trong code vì source vẫn allow các action đó.

## 8. Finding register

| ID | Mức | Class | Finding | Evidence/relation |
|---|---|---|---|---|
| `DB-001` | Thông tin | `CURRENT` | Các bảng AI/report và hai Flyway migration tồn tại, migration success | Metadata + Flyway query |
| `DB-002` | Cao | `CONFLICT` | Runtime có 22 reasons, initializer chỉ có 9 | Catalog query vs `ReportReasonDataInitializer.java:22-30` |
| `DB-003` | Cao | `CONFLICT` | `FALSE_INFORMATION`, `RESTRICTED_GOODS` drift field so với source | Catalog query |
| `DB-004` | Trung bình | `LEGACY` | Bảy label source-origin lưu tiếng Việt không dấu, catalog trộn hai chuẩn text | Unicode-escaped catalog output |
| `DB-005` | Cao | `MISSING` | Tất cả reasons runtime áp dụng ALL target; không có taxonomy/rule target-specific thực tế | Thiếu `E5`, `H7` |
| `DB-006` | Thông tin | `CURRENT` | 19 reports không có lỗi FK/cardinality/snapshot aggregate được kiểm tra | Data-quality queries |
| `DB-007` | Cao | `UNKNOWN` | Dataset không có COMMENT/RESOLVED và chỉ dùng 2/22 reasons | Không đủ representative evidence |
| `DB-008` | Cao | `CONFLICT` | 16/17 RESOLVE có risk dưới legacy threshold 70 | Score-by-outcome query; xác nhận `R8` |
| `DB-009` | Cao | `CONFLICT` | 12 recommendation SUSPEND_USER/PAGE có confidence 85 nhưng risk khoảng 7–8.5 | Score semantics không nhất quán |
| `DB-010` | Cao | `MISSING` | AI tables không có evidence/counter/missing/sufficiency/provenance fields | Thiếu `HI6`, `XA3`, `XA4` |
| `DB-011` | Cao | `MISSING` | Không có policy/schema/prompt/workflow version, correlation/idempotency/snapshot hash columns | Thiếu `AR6`, `AR9`, `XA6`, `XA9` |
| `DB-012` | Trung bình | `MISSING` | DB không enforce score range hoặc decision/action matrix | Constraint metadata |
| `DB-013` | Cao | `CONFLICT` | 14 free-form rule codes/23 rows và không có rule catalog/version | Trái `E5`, `XA12` |
| `DB-014` | Thông tin | `CURRENT` | Một auto job đã cancel; không có destructive/applied job trong snapshot | Auto-job aggregate |
| `DB-015` | Trung bình | `LEGACY` | `content_reports` còn `admin_decision`, `admin_note`, `reason_type` ngoài entity current | Metadata vs `ContentReport.java:35-86` |
| `DB-016` | Trung bình | `UNKNOWN` | Snapshot không chứng minh production, historical completeness hoặc runtime n8n state | Scope limitation |

## 9. Kết luận

Database hiện khỏe về referential/cardinality integrity trong dataset nhỏ, nhưng chưa phải canonical Policy–Evidence store:

- catalog reason drift khỏi source;
- taxonomy chồng lấn và không target-specific;
- score semantics mâu thuẫn với backend threshold;
- rule code free-form;
- thiếu evidence/version/correlation;
- dữ liệu không đủ đại diện để hiệu chỉnh policy hoặc confidence.

Không mutation nào được thực hiện. Consolidated gap register chỉ được viết tại `G0-06D` sau khi người dùng review inventory này.
