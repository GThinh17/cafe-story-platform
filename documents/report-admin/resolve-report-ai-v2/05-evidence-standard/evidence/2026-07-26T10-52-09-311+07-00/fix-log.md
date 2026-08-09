# Fix log — G0-12M-07

| Issue | Hành động | Re-test | Trạng thái |
|---|---|---|---|
| `M07-ENV-01` | Không thay đổi môi trường; route sang Ajv có sẵn | Ajv availability `8.20.0` | `ROUTED` |
| `M07-VAL-01` | Thêm explicit object type; re-test lần 1 phát hiện và bổ sung string type cho `snapshotVersion` | Ajv strict compile lần 2 pass; full suite 16/16 pass | `FIXED` |
