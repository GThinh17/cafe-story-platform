# Detailed Design — Evaluation Dataset/Harness V1 (S2-03)

## 1. Trạng thái và phạm vi

| Thuộc tính | Giá trị |
|---|---|
| Package | `S2-03` |
| Implementation token | `IMPLEMENT_S2_03` |
| Trạng thái | `COMPLETED_VERIFIED_APPROVED` |
| Approval | `APPROVE_S2_03` |
| Dataset/rubric | `1.0.0-rc.1` |
| Lifecycle | `PROPOSED` |
| Runtime authority | `false` |
| Automation | giữ `A0_RECOMMEND_ONLY` |

Mục tiêu của S2-03 là tạo một bộ dữ liệu đánh giá versioned và harness
deterministic để chứng minh các invariant an toàn đã có của S2-02. Package này
không đánh giá chất lượng model thật, không chốt threshold, không activate
policy và không cấp quyền xử lý report tự động.

Trong phạm vi:

- synthetic/sanitized evaluation record cho BLOG và COMMENT;
- Evidence Bundle metadata, expected invariant, allowed/prohibited outcome;
- schema, rubric, manifest và SHA-256 fingerprint;
- harness tự kiểm tra dataset và gọi lại các hard gate hiện có;
- evidence và governance handoff.

Ngoài phạm vi:

- prompt pilot theo rule family (`S2-04`);
- provider/model/cost/latency benchmark (`S2-05`, conditional);
- gọi OpenAI/provider thật;
- import/publish n8n runtime;
- UI/E2E, database migration, production data và deployment;
- deep evidence cho USER/CAFE_PAGE, media/OCR hoặc authoritative adapter;
- numeric calibration hoặc tuyên bố “AI accuracy”.

## 2. Artifact

| Artifact | Vai trò |
|---|---|
| `docker/tests/fixtures/admin-report-ai-evaluation-v1/dataset.schema.json` | Strict Draft 2020-12 schema |
| `docker/tests/fixtures/admin-report-ai-evaluation-v1/dataset.json` | Dataset synthetic/sanitized |
| `docker/tests/fixtures/admin-report-ai-evaluation-v1/rubric.json` | Coverage, oracle và scoring rule |
| `docker/tests/fixtures/admin-report-ai-evaluation-v1/manifest.json` | Version/count/SHA-256 lock |
| `docker/tests/validate-admin-report-ai-evaluation-dataset.mjs` | Executable deterministic harness |

Manifest fingerprint ba artifact dataset và năm dependency:

- runtime request schema;
- provider output schema;
- adversarial vector;
- Backend policy catalog;
- M07 evidence contract manifest.

Nếu bất kỳ file được pin nào thay đổi mà chưa cập nhật/review manifest, harness
fail với SHA-256 drift. Fingerprint bảo đảm reproducibility, không chứng minh nội
dung policy đúng về mặt business.

## 3. Mô hình dữ liệu

### 3.1. Dataset

```text
dataset
├── datasetVersion/lifecycle/runtimeAuthority
├── versions
├── evidenceBundles[]
└── records[]
```

`versions` được pin đồng thời ở dataset và từng record:

- outer contract `2.0`;
- Runtime Rule Context `RRC-1.0.0-rc.1`;
- Evidence Envelope `1.0.0-rc.1`;
- policy `PF-2.0.0-proposed.1`;
- rule catalog `RC-2.0.0-proposed.2`;
- rule `1.0.0-proposed.2`;
- prompt `report-ai-v2-sprint2.2`;
- canonical runtime/provider schema ID;
- rubric `1.0.0-rc.1`.

Harness yêu cầu exact parity; không cho phép một record âm thầm dùng version
khác.

### 3.2. Evidence Bundle

Mỗi bundle có:

- `bundleId`, target type được phép;
- `collectionState`;
- envelope version và snapshot binding;
- cờ required kind;
- các evidence item có ID/kind/availability/quality/source/intended use.

Ba chiều sau độc lập:

| Chiều | Câu hỏi |
|---|---|
| `collectionState` | Bundle đã thu được gì, thiếu hay xung đột ở mức thu thập? |
| item `quality` | Evidence item đọc/đối chiếu được ở chất lượng nào? |
| record `evidenceSufficiency` | Với rule cụ thể, evidence hiện có đã đủ gánh nặng kết luận chưa? |

Vì vậy `AVAILABLE_COMPLETE` không tự động đồng nghĩa `SUFFICIENT`. Ví dụ target
text có thể đầy đủ và chất lượng cao nhưng không đủ chứng minh một factual claim
cần nguồn authoritative.

### 3.3. Evaluation record

Mỗi record chứa:

- `caseId`, class, target/rule family/rule ID;
- `sanitizedReportClaim` — reporter claim là dữ liệu không tin cậy;
- `sanitizedTargetSnapshot`;
- Evidence Bundle reference;
- missing requirement, counter-evidence và exception context;
- slice profile: language, length, context, sufficiency, attack, policy mode;
- oracle: expected invariant, allowed/prohibited outcome, final decision/action,
  blocked reason và executable oracle ref;
- reviewer source/status và disagreement status;
- exact version pins và notes.

`sanitizedReportClaim`, target text, media reference và prior-AI content đều có
thể mang prompt injection. Chúng không phải system instruction và không phải
evidence authority chỉ vì được mô hình nhắc lại trong explanation.

## 4. Phân lớp oracle

| Class | Số lượng | Ý nghĩa |
|---|---:|---|
| `HARD_SAFETY` | 20 | Invariant có source/test oracle, phải pass 100% |
| `SEMANTIC_CANDIDATE` | 4 | Candidate để reviewer gắn nhãn sau; chưa phải ground truth |
| `DISAGREEMENT` | 2 | Bất đồng đang mở, cần business review |

Rule bảo vệ:

- semantic candidate phải có `PROVISIONAL_NOT_GROUND_TRUTH`;
- disagreement phải có `NEEDS_BUSINESS_REVIEW + OPEN`;
- cả hai class bị loại khỏi provider quality denominator;
- unknown rule chỉ hợp lệ trong negative case `UNKNOWN_RULE`;
- policy proposed/inactive phải clamp
  `NEEDS_MANUAL_REVIEW + NO_ACTION`;
- manual-review oracle phải nêu ít nhất một blocked reason;
- allowed và prohibited outcome không được giao nhau;
- expected action không được nằm trong prohibited action.

## 5. Slice coverage V1

Dataset có `26` record và `9` Evidence Bundle.

| Slice | Phân bố |
|---|---|
| Target | BLOG `18`; COMMENT `8` |
| Class | hard `20`; provisional `4`; disagreement `2` |
| Language | VI `5`; EN `11`; MIXED `5`; NONE `5` |
| Context | direct `18`; context-dependent `8` |
| Sufficiency | sufficient `13`; insufficient `5`; conflicted `3`; unassessable `5` |
| Length | short, normal, long và exact boundary `4000` đều có |
| Policy mode | proposed, inactive và test-only active simulation đều có |

Attack vector bắt buộc đều có ít nhất một case:

- unknown rule/version/evidence;
- stale snapshot;
- injection từ target text, report claim, media reference và prior AI;
- schema boundary;
- clean control.

V1 ưu tiên độ phủ invariant, không đại diện cho phân bố report production. Tỷ lệ
BLOG/COMMENT hay rule family không được dùng làm prevalence estimate.

## 6. Harness flow

```mermaid
flowchart TD
    A["Load schema, dataset, rubric, manifest"] --> B["Ajv strict validation"]
    B --> C["Verify SHA-256 and version parity"]
    C --> D["Validate IDs, bundle binding and slices"]
    D --> E["Validate oracle and ground-truth guards"]
    E --> F["Run 6 negative self-tests"]
    F --> G["Run S2 schema and n8n source parity"]
    G --> H["Run adversarial and M07 suites"]
    H --> I["Run Backend focused tests"]
    I --> J["Emit deterministic JSON summary"]
```

Harness không thực thi command từ dataset/rubric. Danh sách command hard-coded
trong source harness để dataset không thể chèn command. Trên Windows, Maven được
gọi qua `ComSpec` với argument cố định; trên nền tảng khác dùng `mvn`.

Hai mode:

- `--metadata-only`: schema/fingerprint/invariant/slice/negative guards;
- mặc định: toàn bộ metadata gate cộng external hard gates.

## 7. Scoring và cách diễn giải

Hard safety:

```text
pass khi và chỉ khi:
dataset self gate
AND schema gate
AND adversarial gate
AND M07 fixture/cross-review
AND Backend focused gate
đều pass
```

Ngưỡng hard safety là `100%`; một hard gate fail làm toàn package fail.

Provider quality:

```text
status      = NOT_EVALUATED_NO_PROVIDER_CALL
denominator = 0
provider    = not called
```

Không được báo precision, recall, F1, calibration hoặc global accuracy từ S2-03.
Bốn provisional semantic case và hai disagreement case chỉ giúp chuẩn bị quy
trình review; chúng không phải nhãn chất lượng đã chốt.

## 8. Verify thực tế

| Tiêu chí | Lệnh/phương pháp | Kết quả |
|---|---|---|
| Metadata/invariant | `node docker/tests/validate-admin-report-ai-evaluation-dataset.mjs --metadata-only` | PASS; negative `6/6` |
| Full harness | `node docker/tests/validate-admin-report-ai-evaluation-dataset.mjs` | PASS |
| Dataset | schema/count/version/fingerprint | `26` record, `9` bundle |
| S2 schema | canonical compile/bounds/parity | `7/7 PASS` |
| Adversarial | existing deterministic suite | `12/12 PASS` |
| M07 fixtures | strict schema + semantics | `18/18 PASS` |
| M07 cross-review | dossier/source consistency | `16/16 PASS` |
| Backend focused | resolution + semantic validator | `50/50 PASS` |
| Backend regression | `mvn test` | `638`, fail/error `0`, skipped `1` |
| Provider | no call | `NOT_EVALUATED`, denominator `0` |
| Production side effect | scope/diff review | không đổi production source/FE/DB/runtime |

S2-03 chỉ thêm test/data/harness và tài liệu. Changed-production coverage gate
không áp dụng; production class count thay đổi bởi S2-03 là `0`.

## 9. Issue và repair loop

| ID | Loại | Phát hiện | Sửa | Retest |
|---|---|---|---|---|
| `S2-03-ISSUE-001` | CONFIG_ENV | Maven baseline gọi tại monorepo root | chạy đúng Backend cwd | `50/50 PASS` |
| `S2-03-ISSUE-002` | DATA/TEST | harness ép blocked reason lên case không bị block | chỉ bắt khi expected decision là manual | metadata + negative `PASS` |
| `S2-03-ISSUE-003` | CONFIG_ENV/HARNESS | Node không spawn trực tiếp `mvn.cmd` | Windows adapter qua `ComSpec` | full harness `PASS` |
| `S2-03-ISSUE-004` | CONFIG_ENV/VERIFY | sensitive scan dùng `||` không tương thích | dùng `$LASTEXITCODE` | scan `PASS` |
| `S2-03-ISSUE-005` | CONFIG_ENV/SEARCH | dùng Bash brace expansion trong PowerShell | dùng `$paths` array | search exit `0` |

Không issue nào được che bằng thay đổi production source.

## 10. Hạn chế và bước kế tiếp

Đã chứng minh:

- artifact có cấu trúc strict, versioned và fingerprinted;
- minimum slice hiện tại tồn tại;
- hard-safety regression chạy lặp lại được;
- provisional/disagreement không bị tính nhầm thành ground truth;
- không gọi provider và không tạo side effect runtime.

Chưa chứng minh:

- model hiểu đúng semantic content;
- prompt family nào tốt hơn;
- provider latency/cost/availability;
- quality theo reviewer-approved ground truth;
- production distribution, threshold hoặc automation readiness;
- UI/full-path behavior mới của Sprint 2.

S2-03 đã được khóa bằng `APPROVE_S2_03`. Bước kế tiếp là
`IMPLEMENT_S2_04` để chọn rule-family text-only pilot dựa trên dataset đã khóa.
S2-04 phải giữ hard gate S2-03 làm regression và không được biến provisional
label thành business truth.
