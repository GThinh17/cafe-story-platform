# S2-03 — Issue log

## S2-03-ISSUE-001 — Maven baseline được gọi sai working directory

- Classification: `CONFIG_ENV`.
- Mức độ: `LOW / TEST_INVOCATION`.
- Evidence:
  `mvn '-Dtest=AdminReportAiResolutionServiceImplTest,AdminReportAiSemanticValidatorTest' test`
  được gọi tại monorepo root và trả `MissingProjectException` vì không có `pom.xml`.
- Affected behavior: không có; schema, n8n, adversarial và M07 baseline trước đó đều pass.
- Likely owner: command working-directory configuration.
- Proposed action: chạy lại cùng selector tại `1-cafe-story-backend-javaspring`.
- Auto-fix allowed: `no` đối với source; chỉ sửa command context.
- Verify: cùng selector tại Backend module trả `50/50 PASS`.
- Trạng thái: `RESOLVED_COMMAND_CONTEXT`.

## S2-03-ISSUE-002 — Harness ép blocked reason lên cả case không bị block

- Classification: `DATA/TEST`.
- Mức độ: `MEDIUM / FALSE_NEGATIVE`.
- Evidence: lần chạy
  `node docker/tests/validate-admin-report-ai-evaluation-dataset.mjs --metadata-only`
  trả exit code `1` tại `S2EVAL-017: empty blocked reason`.
- Root cause: invariant của harness yêu cầu `requiredBlockedReasons` không rỗng cho
  mọi record. Quy tắc này sai với case schema boundary có `ANY_SAFE` và case
  complete reject; blocked reason chỉ bắt buộc khi oracle kỳ vọng
  `NEEDS_MANUAL_REVIEW`.
- Affected behavior: chỉ làm dataset gate báo fail giả; không ảnh hưởng runtime.
- Proposed narrow fix: chuyển assertion sang điều kiện
  `expectedFinalDecision === NEEDS_MANUAL_REVIEW`.
- Auto-fix allowed: `yes`, chỉ trong harness S2-03.
- Verify: chạy lại metadata-only và sáu negative guard.
- Retest thực tế: metadata-only `PASS`, `26` records, `9` bundles và
  `S2_DATASET_NEGATIVE_GUARDS=6/6`.
- Trạng thái: `RESOLVED`.

## S2-03-ISSUE-004 — Sensitive-value scan dùng toán tử không tương thích PowerShell

- Classification: `CONFIG_ENV/VERIFY_COMMAND`.
- Mức độ: `LOW / TEST_INVOCATION`.
- Evidence: lệnh scan trả `ParserError` tại toán tử `||`; scan chưa được thực thi.
- Root cause: PowerShell hiện tại không hỗ trợ `||` như statement separator.
- Affected behavior: không ảnh hưởng artifact/runtime; chỉ làm bước kiểm chứng
  sensitive value chưa có kết quả.
- Proposed narrow fix: dùng statement tuần tự và xử lý `$LASTEXITCODE` bằng
  `if/elseif`.
- Auto-fix allowed: `yes`, chỉ sửa cách gọi lệnh.
- Verify: chạy lại cùng regex trên dataset/rubric/manifest và evidence folder.
- Retest thực tế: `SENSITIVE_VALUE_SCAN=PASS`.
- Trạng thái: `RESOLVED`.

## S2-03-ISSUE-005 — Search command dùng Bash brace expansion trong PowerShell

- Classification: `CONFIG_ENV/SEARCH_COMMAND`.
- Mức độ: `LOW / READ_ONLY_INVOCATION`.
- Evidence: PowerShell trả `Missing argument in parameter list` trước khi `rg`
  chạy.
- Root cause: dùng `{a,b}` thay vì PowerShell array.
- Affected behavior: không tác động file, test hay runtime; lần search đó không
  tạo kết quả.
- Proposed narrow fix: truyền `$paths=@(...)` vào `rg`.
- Auto-fix allowed: `yes`, chỉ sửa cách gọi lệnh read-only.
- Retest thực tế: search chạy exit code `0` và trả đúng các vị trí cần cập nhật.
- Trạng thái: `RESOLVED`.

## S2-03-ISSUE-003 — Node không spawn trực tiếp được Maven batch trên Windows

- Classification: `CONFIG_ENV/HARNESS`.
- Mức độ: `MEDIUM / GATE_EXECUTION`.
- Evidence: full harness trả
  `AssertionError: BACKEND_FOCUSED exited null`; `Get-Command mvn` xác nhận Maven
  là `mvn.cmd`, không phải executable native.
- Root cause: `spawnSync("mvn.cmd", ...)` không tự đi qua Windows command
  processor trong môi trường hiện tại.
- Affected behavior: full harness dừng trước Backend focused; không phải Maven
  test failure.
- Proposed narrow fix: trên Windows gọi đối số Maven cố định qua
  `%ComSpec% /d /s /c mvn.cmd`; nền tảng khác tiếp tục gọi `mvn`.
- Auto-fix allowed: `yes`, chỉ trong harness S2-03.
- Verify: chạy lại full harness từ đầu và yêu cầu Backend `50/50 PASS`.
- Retest thực tế: full harness `PASS`; Backend focused `50/50 PASS`; toàn bộ
  năm external hard gate và dataset self gate đều pass.
- Trạng thái: `RESOLVED`.
