# Issue log — G0-12M-08 cross-review

## M08-CONTRACT-01

- Classification: `CODE_BUG`
- Evidence: M01/M02/M03 còn `envelopeVersion=1.0.0` và các alias
  `CONTENT_SNAPSHOT`, `MEDIA_OBSERVATION`, `PARENT_CONTEXT_SNAPSHOT`; M04 dùng envelope shape
  trước canonical M05/M07.
- Affected behavior: người implement có thể chọn nhầm kind/version/field shape dù M07 đã có schema.
- Likely owner: metadata contract documentation.
- Proposed action: cập nhật example sang `1.0.0-rc.1` và canonical M05/M07 shape; không đổi business
  decision đã approve.
- Auto-fix allowed: `yes`.
- Status: `FIXED`.

## M08-CONTRACT-02

- Classification: `CODE_BUG`
- Evidence: M07 schema để `publicProfileContext`, `ownershipContext`, `publicPageContext` là object mở;
  semantic validator chưa reject forbidden PII/secret/authority-score keys trong nested payload.
- Affected behavior: một structurally valid packet có thể chứa email/phone/session/token hoặc
  `riskScore` ở nested object.
- Likely owner: executable metadata contract.
- Proposed action: close target-specific context objects; thêm recursive forbidden-key guard và
  negative fixture.
- Auto-fix allowed: `yes`.
- Status: `FIXED`.

## M08-CONTRACT-03

- Classification: `CODE_BUG`
- Evidence: policy lifecycle chuẩn là
  `DRAFT → PROPOSED → APPROVED → ACTIVE → DEPRECATED → RETIRED`, nhưng M07 schema thiếu `DRAFT` và
  `RETIRED`.
- Affected behavior: known inactive lifecycle có thể bị báo schema unknown thay vì policy stop.
- Likely owner: executable metadata contract.
- Proposed action: thêm hai lifecycle values; giữ semantic rule chỉ `ACTIVE` mới có authority.
- Auto-fix allowed: `yes`.
- Status: `FIXED`.

## M08-CONTRACT-04

- Classification: `CODE_BUG`
- Evidence: M07 design gọi `assessmentConfidence` là optional, nhưng JSON Schema required field cho
  phép number/null.
- Affected behavior: hai nguồn hướng dẫn mâu thuẫn.
- Likely owner: metadata contract documentation.
- Proposed action: chốt `required but nullable`, không cho score cấp authority.
- Auto-fix allowed: `yes`.
- Status: `FIXED`.
