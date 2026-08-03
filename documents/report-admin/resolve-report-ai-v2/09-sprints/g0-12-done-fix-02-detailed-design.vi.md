# Detailed Design — G0-12-DONE-FIX-02

## 1. Mục tiêu

`DOD-FIX-02` hiện thực hóa `CT-010` và `SAF-010`: một AI resolution chỉ có giá trị đối với đúng
target snapshot đã được đánh giá. Kết quả hoặc cache của snapshot cũ không được reuse khi target
đã thay đổi.

Gate này sửa freshness/idempotency ở Backend vì Backend sở hữu authorization, persistence và
state transition. n8n/OpenAI không được cấp quyền xác nhận target còn mới trong database.

Ngoài phạm vi: FE, n8n workflow, database migration, provider runtime, production deploy,
distributed multi-instance locking và `DOD-FIX-03`–`DOD-FIX-06`.

## 2. Lỗi được xác thực

### 2.1 Reuse cache cũ

Luồng cũ:

1. Backend build snapshot V1 và idempotency key V1.
2. Repository tìm thấy resolution V1.
3. Target đã đổi thành V2.
4. Backend trả resolution V1 ngay, không refresh authoritative state.

Baseline test thất bại vì ID của record V1 vẫn được trả về.

### 2.2 Persist output stale sau provider

Luồng cũ:

1. Backend gửi snapshot V1 cho provider.
2. Trong provider window, target đổi thành V2.
3. Provider trả `RESOLVE + HIDE` cho V1.
4. Backend persist response đó mà không recheck snapshot.

Baseline test thất bại vì kết quả vẫn là `RESOLVE`, không phải manual review.

## 3. Thiết kế Backend

### 3.1 Authoritative refresh

Backend inject `EntityManager` vào service production. Trong transaction hiện tại,
`refreshSnapshotSources(report)` refresh:

| Target type | Entity được refresh |
|---|---|
| `BLOG` | report và blog |
| `COMMENT` | report, comment và parent blog nếu có |
| `USER` | report và reported user |
| `CAFE_PAGE` | report và cafe page |

Nếu entity biến mất trong lúc refresh, Backend trả `409 CONFLICT` với message không chứa dữ liệu
nhạy cảm và không persist resolution.

Constructor cũ không có `EntityManager` được giữ để tương thích unit harness; production Spring
constructor luôn nhận `EntityManager`.

### 3.2 Guard trước cache return

Sau khi tìm thấy record theo key ban đầu:

1. refresh authoritative report/target;
2. validate report vẫn đủ điều kiện Ask AI;
3. rebuild target snapshot và idempotency key;
4. chỉ reuse cache khi key mới bằng key ban đầu;
5. nếu khác, bỏ cache cũ và tiếp tục với request của snapshot mới.

Vì key chứa snapshot hash cùng các version contract/policy/rule/prompt/workflow, thay đổi target
làm thay đổi key và chặn reuse.

### 3.3 Guard sau provider

Sau provider/local response, trước semantic validation và persistence:

1. refresh authoritative report/target lần nữa;
2. rebuild snapshot/key hiện tại;
3. so sánh với key đã gửi provider.

Nếu khác, Backend không dùng finding/action của provider và tạo response fail-closed:

```text
recommendationState = NEEDS_MANUAL_REVIEW
reportDecision      = NEEDS_MANUAL_REVIEW
targetAction        = NO_ACTION
findings            = []
blockedReasons      = [TARGET_SNAPSHOT_CHANGED_DURING_EVALUATION]
```

Resolution audit vẫn gắn với request snapshot đã gửi provider; target/report không bị mutate.
Admin phải chạy lại AI để đánh giá snapshot mới.

## 4. Luồng FE → BE → n8n/OpenAI → BE → FE

### 4.1 FE

FE gọi Ask AI như cũ. Gate này không đổi API request/response. Khi stale, FE nhận resolution
manual/no-action và blocked reason máy đọc được. UI chuyên biệt cho stale được đề xuất để sau.

### 4.2 Backend trước provider

Backend load report, validate trạng thái, build Contract V2 và key. Nếu cache hit, Backend refresh
và chỉ reuse khi snapshot không đổi.

### 4.3 n8n/OpenAI

n8n/OpenAI xử lý đúng packet đã nhận. Gate này không sửa prompt hoặc workflow; provider không có
quyền đọc authoritative database và không quyết định freshness.

### 4.4 Backend sau provider

Backend refresh/rebuild lần hai. Snapshot khác thì clamp stale; snapshot giống thì chạy semantic
validator và persistence như hiện có.

### 4.5 FE nhận kết quả

FE không nhận action tự động vì Sprint 1 là A0. Với stale, kết quả chỉ yêu cầu manual review và
không làm thay đổi target/report.

## 5. Test design và traceability

| Requirement | Test | Chứng minh |
|---|---|---|
| `CT-010`, `SAF-010` | `createResolution_changedSnapshotDoesNotReusePreviouslyMatchedCache_CT010_SAF010` | cache V1 không reuse; provider nhận key/hash V2 |
| `CT-010`, `SAF-010` | `createResolution_targetChangesDuringProviderClampsStaleResult_CT010_1_SAF010` | provider output V1 bị clamp; không mutate |
| `CT-010` | `refreshSnapshotSources_supportsNoEntityManagerAndMissingTargetConflict_CT010_2` | compatibility path và target-deleted conflict fail-closed |

Kết quả:

- service suite: `25/25 PASS`;
- Admin Report AI focused: `63/63 PASS`;
- full Backend: `624`, failure/error `0`, skipped `1`;
- changed class coverage: line `100%`, branch `87.76%`;
- Spring context: `PASS`.

## 6. Trạng thái

- Source/test implementation: `COMPLETED_VERIFIED`.
- Evidence/DD: `COMPLETED`.
- User approval: `APPROVED`.
- Token đã nhận: `APPROVE_G0-12-DONE-FIX-02`.
- Published n8n runtime: không thay đổi.
- Production deployment: `NOT_AUTHORIZED`.
