# Detailed Design — G0-12-DONE-FIX-03

## 1. Mục tiêu và phạm vi

`DOD-FIX-03` hiện thực hóa `DOD-16`, `E2E-S1-04` và `SEM-009`: khi một COMMENT cần parent
context để đánh giá nhưng parent context không đọc được hoặc rỗng, hệ thống không được suy diễn
vi phạm từ nội dung comment đơn lẻ.

Kết quả bắt buộc:

```text
reportDecision      = NEEDS_MANUAL_REVIEW
targetAction        = NO_ACTION
evidenceSufficiency = UNASSESSABLE
findings            = []
blockedReasons      includes CRITICAL_EVIDENCE_MISSING
```

Trong phạm vi: Backend evidence packet/semantic trust boundary, unit regression, Playwright
full-path fixture, Admin UI assertion và evidence. Ngoài phạm vi: thay prompt/n8n canonical
workflow, DOD-FIX-04–06, production data và production deploy.

## 2. Lỗi được xác thực

### 2.1 `FIX03-TEST-001` — cleanup sai thứ tự dependency

Fixture lần đầu xóa `content_reports` trước `admin_report_ai_resolutions`, gây lỗi foreign key.
Đây là `TEST_BUG`; production behavior chưa được kết luận từ attempt đó.

Harness được sửa để:

1. ghi sanitized runtime result trước UI assertion;
2. xóa auto jobs, resolutions, moderation jobs/results trước report;
3. xóa COMMENT rồi BLOG;
4. query lại đúng ID và yêu cầu số dòng còn lại là `0/0/0`.

### 2.2 `FIX03-CODE-001` — whitespace parent context bị coi là evidence usable

Backend cũ chỉ xem parent context thiếu khi object BLOG là `null`. BLOG tồn tại nhưng content
`null`, rỗng hoặc chỉ whitespace vẫn được gắn `HIGH/AVAILABLE`; do đó execution constraints không
đặt `criticalEvidenceMissing=true`.

Baseline unit đỏ:

```text
expected NEEDS_MANUAL_REVIEW
actual   RESOLVE
```

### 2.3 `FIX03-CODE-002` — critical missing vẫn giữ `INSUFFICIENT`

Sau FIX03-CODE-001, Backend đã thêm `CRITICAL_EVIDENCE_MISSING`, nhưng semantic validator chỉ đổi
`SUFFICIENT -> UNASSESSABLE`. Nếu provider trả `INSUFFICIENT`, giá trị đó bị giữ lại.

Baseline unit đỏ:

```text
expected UNASSESSABLE
actual   INSUFFICIENT
```

## 3. Thiết kế Backend

### 3.1 Usable parent-context predicate

`hasUsableParentContext(parentBlog)` chỉ trả `true` khi:

- parent BLOG tồn tại;
- content khác `null`;
- content không blank theo Java `String.isBlank()`.

Predicate này được dùng thống nhất tại ba nơi:

1. target snapshot: không đưa excerpt giả vào snapshot;
2. evidence packet: `EV-PARENT-CONTEXT` thành `UNUSABLE/MISSING`, observation `null`;
3. execution constraints: đặt `criticalEvidenceMissing=true`.

Nhờ đó snapshot, evidence envelope và semantic constraint không thể mâu thuẫn nhau.

### 3.2 Semantic trust boundary

`AdminReportAiSemanticValidator` đọc `criticalEvidenceMissing` một lần và dùng cùng cờ để:

1. thêm `CRITICAL_EVIDENCE_MISSING`;
2. clamp decision về manual;
3. clamp action về no action;
4. xóa findings;
5. bắt buộc evidence sufficiency thành `UNASSESSABLE`.

Chỉ thiếu critical evidence mới bắt buộc `UNASSESSABLE`. Evidence không đủ nhưng vẫn assessable
tiếp tục giữ `INSUFFICIENT`; hai khái niệm không bị nhập làm một.

## 4. Luồng FE → BE → n8n/OpenAI → BE → FE

### 4.1 Admin UI

Playwright đăng nhập bằng account Admin test từ local env, tạo report cho synthetic COMMENT và
gọi Ask AI trên report detail. Credential không được ghi vào evidence.

### 4.2 Backend trước n8n

Backend load COMMENT và parent BLOG. Vì BLOG content chỉ có whitespace:

- snapshot không có usable parent excerpt;
- evidence `EV-PARENT-CONTEXT` là `UNUSABLE/MISSING`;
- `criticalEvidenceMissing=true`.

Backend ký request Contract V2 và gửi sang exact n8n webhook.

### 4.3 n8n/OpenAI

n8n canonical workflow gọi OpenAI bằng cấu hình runtime hiện có và trả signed Contract V2.
FIX-03 không đổi hoặc publish workflow. Output provider là recommendation, không phải evidence và
không có quyền vượt Backend trust boundary.

### 4.4 Backend sau n8n

Backend xác minh signed response, chạy semantic validator và persist kết quả đã clamp:
`NEEDS_MANUAL_REVIEW + NO_ACTION + UNASSESSABLE`. Findings rỗng và không tạo auto-apply job.

### 4.5 Admin UI nhận kết quả

UI render rõ `CRITICAL_EVIDENCE_MISSING` và `UNASSESSABLE`. Playwright chụp screenshot, kiểm tra
target snapshot trước/sau không đổi, rồi cleanup toàn bộ synthetic fixture.

## 5. Test design và traceability

| Requirement | Test/evidence | Chứng minh |
|---|---|---|
| `SEM-009` | `TC015` trong `AdminReportAiResolutionServiceImplTest` | blank parent thành unusable/missing và critical constraint |
| `SEM-009` | `TC005` trong `AdminReportAiSemanticValidatorTest` | critical missing ghi đè provider insufficient thành unassessable |
| `DOD-16`, `E2E-S1-04` | Playwright `E2E-S1-04` | full path thật, UI state và no mutation |
| Safety A0 | runtime result + job query | `targetAction=NO_ACTION`, auto job `0` |
| Test hygiene | cleanup verification | report/comment/blog còn `0/0/0` |

Kết quả kiểm chứng:

- Backend service + semantic: `37/37 PASS`;
- Admin Report AI focused: `63/63 PASS`;
- full Backend: `624`, failure/error `0`, skipped `1`;
- Admin typecheck/build: `PASS`;
- full-path Playwright: `1/1 PASS`;
- `AdminReportAiResolutionServiceImpl`: line `100%`, branch `87.75%`;
- `AdminReportAiSemanticValidator`: line `100%`, branch `96.72%`.

## 6. Trạng thái và giới hạn kết luận

- Implementation/test/evidence: `COMPLETED_VERIFIED`.
- User approval: `APPROVED`.
- Token đã nhận: `APPROVE_G0-12-DONE-FIX-03`.
- n8n runtime: dùng workflow active hiện có để test; không publish thay đổi trong FIX-03.
- Production deployment: `NOT_AUTHORIZED`.
- `G0-12-DONE`: chưa đóng; còn DOD-FIX-04–06.
