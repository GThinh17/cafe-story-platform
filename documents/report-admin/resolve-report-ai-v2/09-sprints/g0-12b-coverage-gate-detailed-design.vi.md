# G0-12B Detailed Design — Changed-file Coverage Gate

## 1. Document control

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-12B` |
| Trạng thái | `COMPLETED_COVERAGE_APPROVED` |
| Approval đã nhận | `APPROVE_G0-12B` |
| Hard gate | `100%` line và `>=85%` branch trên từng changed production class |
| Evidence run | `09-sprints/evidence/2026-07-26T12-58-33-831+07-00/` |
| Runtime authority | `false` |

## 2. Bound

### 2.1. Mục tiêu

Chứng minh các class chứa logic recommendation-only, evidence semantics và Backend ↔ n8n security boundary
đã được kiểm thử đủ theo gate đã chốt, thay vì suy ra chất lượng từ việc Maven build pass.

### 2.2. Class trong scope

1. `AdminReportAiAutoApplyJobServiceImpl`
2. `AdminReportAiResolutionServiceImpl`
3. `AdminReportAiPolicyCatalog`
4. `AdminReportAiSemanticValidator`
5. `AdminReportAiWebhookSigner`

DTO/entity/repository chỉ chứa data mapping hoặc interface không được đưa vào hard branch gate của G0-12B.
Các thành phần đó tiếp tục được kiểm tra gián tiếp qua service/controller/full regression.

### 2.3. Ngoài phạm vi

- apply Flyway/PostgreSQL runtime;
- import/activate/publish n8n;
- gọi provider thật;
- Admin browser/E2E;
- production deploy.

## 3. Execute

### 3.1. Test coverage bổ sung

- A0 auto-apply: null/disabled request, detached legacy jobs, null batches và nullable history mapping.
- Semantic validator: contract/correlation, version pin, categorical values, evidence sufficiency,
  Rule ID, Evidence ID và decision/action matrix.
- Resolution service: target types, COMMENT parent context, media references, moderation-derived evidence,
  idempotency, legacy compatibility, invalid target, invalid audit metadata và fail-closed webhook.
- Security signer: reuse positive/negative test suite từ G0-12A.

### 3.2. Production delta

`AdminReportAiSemanticValidator.isDecisionActionAllowed()` từng có case
`NEEDS_MANUAL_REVIEW` không thể chạy vì cùng giá trị đã return trước switch. G0-12B thay switch cuối bằng:

```text
manual decision guard
→ manual-only target guard
→ REJECT allowlist
→ RESOLVE allowlist
```

Không thay đổi ma trận action hợp lệ. Focused matrix test và full regression chứng minh behavior giữ nguyên.

## 4. Verify

| Class | Line | Branch | Kết quả |
|---|---:|---:|---|
| `AdminReportAiAutoApplyJobServiceImpl` | `71/71 = 100%` | `30/30 = 100%` | PASS |
| `AdminReportAiResolutionServiceImpl` | `418/418 = 100%` | `159/181 = 87.85%` | PASS |
| `AdminReportAiPolicyCatalog` | `35/35 = 100%` | `2/2 = 100%` | PASS |
| `AdminReportAiSemanticValidator` | `71/71 = 100%` | `79/80 = 98.75%` | PASS |
| `AdminReportAiWebhookSigner` | `88/88 = 100%` | `48/56 = 85.71%` | PASS |

Focused:

```powershell
mvn '-Dtest=AdminReportAiResolutionServiceImplTest,AdminReportAiAutoApplyJobServiceImplTest,AdminReportAiSemanticValidatorTest,AdminReportAiWebhookSignerTest' test
```

Kết quả: `56/56 PASS`.

Regression:

```powershell
mvn test
```

Kết quả: `617` tests, `0` failure, `0` error, `1` PostgreSQL integration test skipped,
`BUILD SUCCESS`.

## 5. Done boundary

G0-12B đã đủ evidence và được người dùng phê duyệt:

```text
G0-12B STATUS: COMPLETED_COVERAGE_APPROVED
NEXT TOKEN: IMPLEMENT_G0_12C
```

Approval G0-12B không xác nhận migration/runtime, n8n/provider hoặc Admin UI/E2E.
