# S2-DONE-FIX-02-DB-HASH-LENGTH — Fix log

| Issue | Thay đổi | Retest |
|---|---|---|
| `001` | Thêm migration `20260730.01`, đổi entity length `64 → 71`, thêm schema-contract test | Focused `54/54`; full `642`; PostgreSQL/Flyway/JPA/API pass; hai E2E pass |
| `002` | Tách composite PowerShell command thành port/container/health probes riêng | PostgreSQL và n8n ready |
| `003` | Không sửa source; giữ đúng classification local optional Redis | Không ảnh hưởng functional gate |
| `004` | Không mở rộng sang Report Moderation; ghi handoff riêng | Admin AI E2E pass; cleanup xóa moderation jobs của fixture |
