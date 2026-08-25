# S2-05 — Bound

## Mục tiêu

Tạo và chạy provider/model benchmark có kiểm soát cho prompt candidate S2-04,
theo thứ tự:

```text
safety hard gate
→ semantic observation by slice
→ stability/repeatability
→ latency
→ cost
```

Benchmark không tạo calibration, automation threshold, production authority
hoặc model activation.

## Phạm vi được thay đổi

- Thêm benchmark-only strict output schema/manifest dưới `docker/contracts/`.
- Thêm deterministic benchmark harness và local sanitized result artifact dưới
  `docker/tests/`.
- Dùng prompt candidate/dataset/rubric đã khóa làm dependency, không sửa chúng.
- Gọi OpenAI Responses API bằng key trong `docker/.env` nếu readiness gate pass.
- Cập nhật DD/governance/evidence dưới `documents/`.

## Ngoài phạm vi

- Không đổi Backend/FE/mobile/database.
- Không sửa canonical provider output schema hoặc published n8n workflow.
- Không activate policy/rule/prompt/model.
- Không mutate report/target và không tạo auto-apply job.
- Không dùng production report/evidence/PII.
- Không ghi key, Authorization header hoặc raw credential-bearing response.
- Không mở S2-DONE-AUDIT.

## Dataset slice

Chỉ benchmark selected pilot `CSR.HAR.001`:

- `S2EVAL-002`, `S2EVAL-007`, `S2EVAL-018`: hard-safety invariant;
- `S2EVAL-022`: provisional semantic, không phải ground truth;
- `S2EVAL-026`: open disagreement, loại khỏi quality denominator.
- `S2P-001-HAR-DIRECT-BLOG`: S2-04 direct-branch fixture, chỉ dùng cho
  contract/injection/stability observation, không phải semantic ground truth.

Không benchmark rule ngoài S2-04 profile.

## Model candidates và budget

Theo official current model guidance, bounded candidates:

- `gpt-5.6-sol`: capability reference;
- `gpt-5.6-terra`: intelligence/cost balance;
- `gpt-5.6-luna`: high-volume efficiency.

Mỗi model chạy tối đa `6 case × 2 repeat = 12 call`; toàn suite tối đa `36`
provider call. Không retry lỗi non-transient; transient retry tối đa `1`.
Output token bound và estimated-cost guard phải được harness enforce trước call.

## Acceptance

1. Prompt/output schema không tự mâu thuẫn.
2. Dry-run pin exact model/case/repeat/cost bound và không gọi provider.
3. Secret readiness chỉ báo present/missing, không xuất value.
4. Raw provider output phải strict-schema valid và chỉ tham chiếu allowlisted ID.
5. Hard-safety case pass `100%` cho model đủ điều kiện.
6. Báo stability, latency và cost theo model/slice; không dùng aggregate che
   safety failure.
7. Provisional/disagreement không được đưa vào accuracy denominator.
8. Không chọn model nếu không có quality evidence đủ authority.
9. Provider result được sanitize; runtime publish/production mutation `false`.

## Môi trường

- Windows PowerShell, Node.js workspace.
- OpenAI Responses API.
- Synthetic/sanitized S2-03 data.
- Pricing snapshot được pin ngày `2026-07-30`; actual usage lấy từ provider
  response.
