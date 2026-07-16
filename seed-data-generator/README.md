# CafeStory — Seed Data Generator

Sinh SQL migration seed dữ liệu demo cho DB (users + regions + user_roles). Không phải service runtime, chỉ chạy 1 lần khi cần refresh dữ liệu demo.

## Cấu trúc

- `generate_seed_users.py` — script chính; đọc name list + ward list, sinh SQL.
- `username_generator.py` — port thuật toán `AuthServiceImpl.generateUserNameCandidates` từ Java sang Python, để username sinh ra khớp validator BE (`^[a-z0-9](?!.*[._]{2})[a-z0-9._]{3,8}[a-z0-9]$`, 5–10 ký tự, không thuộc `RESERVED_USER_NAMES`).
- `vietnamese_names.py` — họ + tên đệm + tên riêng phổ biến VN (male/female).
- `cantho_wards.py` — danh sách Phường Cần Thơ, trích từ migration `2026-06-13_normalized_vietnam_regions.sql`.
- `output/` — file SQL được sinh ra.

## Chạy

Cần Python ≥ 3.9 (chỉ dùng stdlib, không cần pip install).

```bash
cd seed-data-generator
python generate_seed_users.py
```

Output: `output/2026-07-16_seed_100_demo_users.sql`.

## Apply lên DB

Dùng cùng docker command như các migration khác:

```powershell
docker run --rm `
  -e "PGPASSWORD=$env:PGPASSWORD" `
  -v "D:\cafe-story-platform\seed-data-generator\output\2026-07-16_seed_100_demo_users.sql:/migration.sql" `
  postgres:17-alpine `
  psql -h aws-1-ap-northeast-2.pooler.supabase.com -p 5432 -U "postgres.oafzgtyyttdhocqkyxox" -d postgres -f /migration.sql
```

Migration sẽ:
- Đảm bảo role `USER` tồn tại.
- Abort nếu 1 trong 100 email seed đã có sẵn (bảo vệ trước duplicate).
- Sinh 100 region row + 100 user row + 100 user_role row.

## Đặc điểm dữ liệu sinh ra

- Deterministic: SEED cố định (`20260716`) → mỗi lần chạy sinh cùng dataset.
- 50 nam + 50 nữ, tên đa dạng từ ~30 họ × ~25 tên đệm × ~50 tên riêng.
- Địa chỉ: Phường ngẫu nhiên trong 31 Phường Cần Thơ, street/area = NULL.
- Password: `123456` hash BCrypt strength 10 (Postgres `pgcrypto.crypt` + `gen_salt('bf', 10)`).
- Avatar: DiceBear `avataaars` SVG, seed bằng username → mỗi user 1 URL cố định, không cần host ảnh.
