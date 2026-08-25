# Issue log — G0-12M-07

## M07-ENV-01

- Classification: `CONFIG_ENV`
- Evidence: `python -c "import jsonschema"` trả về `ModuleNotFoundError: No module named 'jsonschema'`.
- Affected behavior: không thể dùng Python jsonschema validator có sẵn.
- Likely owner: local environment.
- Proposed action: không cài dependency; dùng Ajv `8.20.0` đã tồn tại trong workspace.
- Auto-fix allowed: `no`.
- Status: `ROUTED`.

## M07-VAL-01

- Classification: `CODE_BUG`
- Evidence: Ajv strict compile lần 1 báo thiếu `type: object` tại schema có
  `unevaluatedProperties`; re-test lần 1 báo `snapshotVersion.pattern` thiếu explicit
  `type: string` trong discriminator.
- Affected behavior: schema `2.0.0-rc.1` chưa compile được.
- Likely owner: M07 contract artifact.
- Proposed action: thêm explicit object/string type cho bốn target discriminator schema.
- Auto-fix allowed: `yes`.
- Status: `FIXED`.
