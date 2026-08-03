# Policy Versioning

## 1. Kiểm soát tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-07` |
| Trạng thái | `PROPOSED` |
| Framework draft version | `PF-2.0.0-proposed.1` |

## 2. Versioned artifact set

Mỗi recommendation phải pin:

| Field | Artifact |
|---|---|
| `policyFrameworkVersion` | Nguyên tắc/framework |
| `ruleCatalogVersion` | Rule set |
| `intakeCatalogVersion` | Report reason mapping |
| `decisionSchemaVersion` | Request/response contract |
| `promptVersion` | System/developer prompt bundle |
| `workflowVersion` | n8n published workflow |
| `modelProvider` | Provider |
| `modelName` | Model requested |
| `modelResolvedVersion` | Model/version thực tế nếu provider trả |
| `evaluationProfileVersion` | Calibration/acceptance profile |
| `actionPolicyVersion` | Automation allowlist/authority |

## 3. Lifecycle states

```text
DRAFT → PROPOSED → APPROVED → ACTIVE → DEPRECATED → RETIRED
```

| State | Được dùng cho new production recommendation? |
|---|---|
| `DRAFT` | Không |
| `PROPOSED` | Không |
| `APPROVED` | Chưa, cho tới activation |
| `ACTIVE` | Có |
| `DEPRECATED` | Không cho new request; đọc history |
| `RETIRED` | Chỉ archive/audit |

`APPROVE_G0-07` chỉ cho phép hoàn thiện bản dự thảo `PROPOSED`, không chuyển tài liệu sang `APPROVED/ACTIVE`.

## 4. Version scheme

Đề xuất semantic version:

```text
MAJOR.MINOR.PATCH
```

- `MAJOR`: thay đổi semantics/authority/action hoặc compatibility.
- `MINOR`: thêm rule/family/field backward-compatible.
- `PATCH`: sửa wording/metadata không đổi decision semantics.
- Pre-release dùng `-proposed.N`, `-rc.N`.

Exact scheme cần business adoption tại G0-10.

## 5. Artifact manifest

Mỗi active release MUST có immutable manifest:

```text
releaseId
policyFrameworkVersion
ruleCatalogVersion
intakeCatalogVersion
decisionSchemaVersion
promptVersion
workflowVersion
actionPolicyVersion
evaluationProfileVersion
checksums
effectiveFrom
approvedBy
activatedBy
status
rollbackToReleaseId
```

## 6. Normative rules

| ID | Policy |
|---|---|
| `PV-001` | New recommendation MUST dùng một active release manifest. |
| `PV-002` | Version/checksum MUST được persist với recommendation. |
| `PV-003` | Published artifact MUST immutable; thay đổi tạo version mới. |
| `PV-004` | History MUST giữ version cũ và MUST NOT bị reinterpret silent. |
| `PV-005` | Pending job MUST revalidate version/transition policy trước execution. |
| `PV-006` | Missing/mismatched checksum MUST block automation. |
| `PV-007` | Prompt alias hoặc model alias MUST lưu resolved identity khi available. |
| `PV-008` | Rollback activation MUST tạo audit event; không xóa release lỗi. |
| `PV-009` | Policy change MUST có impact/evaluation/migration/rollback plan. |
| `PV-010` | Legacy rows MAY thiếu version nhưng MUST được đánh dấu `LEGACY_UNVERSIONED`, không backfill giả. |

## 7. Compatibility

| Change | Required treatment |
|---|---|
| Rename legacy `APPROVE` → `KEEP_VISIBLE` | Adapter + canonical field; history không rewrite |
| Deprecate `riskScore` | Lưu `legacyRiskScore`; không infer canonical scores |
| Add evidence objects | New schema version; old records `LEGACY_UNGROUNDED` |
| Reason catalog reconcile | Versioned mapping + data migration plan |
| Workflow/prompt change | Version/checksum mới |
| Model change | Evaluation profile review trước activation |

## 8. Activation gates

Trước `ACTIVE`:

- document review;
- business approval;
- rule/contract traceability;
- schema compatibility;
- test/evaluation pass;
- security/privacy review;
- migration plan;
- rollback/kill switch;
- runtime artifact checksum verification.

## 9. Audit and retention

- Version metadata là audit-critical.
- Raw payload không thay thế version manifest.
- Audit record nên append-only hoặc tamper-evident.
- Retention period chưa chốt; raw payload có thể ngắn hơn structured record.

## 10. Chưa chốt

- policy owner/approver;
- activation authority;
- retention period;
- exact semver/change classification;
- rollout/rollback window;
- checksum/signing mechanism.

Chuyển G0-10/G0-11.
