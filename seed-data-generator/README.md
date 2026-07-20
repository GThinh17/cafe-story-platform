# CafeStory — Seed Data Generator

Sinh SQL migration seed dữ liệu demo cho DB (users + regions + user_roles). Không phải service runtime, chỉ chạy 1 lần khi cần refresh dữ liệu demo.

## Cấu trúc

### User seeds
- `generate_seed_users.py` — script chính cho seed **user**; đọc name list + ward list, sinh SQL. Nhận `--city cantho|danang`.
- `username_generator.py` — port thuật toán `AuthServiceImpl.generateUserNameCandidates` từ Java sang Python, để username sinh ra khớp validator BE (`^[a-z0-9](?!.*[._]{2})[a-z0-9._]{3,8}[a-z0-9]$`, 5–10 ký tự, không thuộc `RESERVED_USER_NAMES`).
- `vietnamese_names.py` — họ + tên đệm + tên riêng phổ biến VN (male/female).
- `cantho_wards.py` — 31 Phường Cần Thơ, trích từ migration `2026-06-13_normalized_vietnam_regions.sql`.
- `danang_wards.py` — 23 Phường Đà Nẵng, trích cùng migration.

### Cafe page seeds
- `generate_seed_cafe_pages.py` — sinh SQL seed 20 cafe page thật ở Cần Thơ. Mỗi cafe = 1 region + 1 cafe_pages (status ACTIVE, page_active=true) + 1 page_members (OWNER) + 1 payments (PAID) + 1 payment_details + upsert user_roles (CAFE_PAGE). Data từ `cantho_cafes.py`.
- `cantho_cafes.py` — 20 quán cafe thật, tên duy nhất, kèm street + ward + package_key (`6mo`/`3mo`).
- `upload_cafe_images.py` — upload 20 avatar + 20 cover từ `cafe_images/{avatars,covers}/` lên Cloudinary (folder `cafestory/cafes`, unsigned preset đọc từ FE `.env`), ghi mapping vào `cafe_image_urls.json`.
- `cafe_images/` — folder ảnh gốc, gitignored. Xem `cafe_images/README.md` để biết cấu trúc + convention naming (2 chữ số đầu = index cafe 1-20).

### Output
- `output/` — file SQL sinh ra, apply qua docker.

Muốn thêm thành phố mới: tạo `{city}_wards.py` theo cùng shape (`PROVINCE_CODE`, `PROVINCE_NAME`, `CITY_CODE`, `CITY_NAME`, `{CITY}_WARDS`), thêm nhánh vào `_load_city_config` trong `generate_seed_users.py` với `seed` khác các thành phố hiện có.

## Chạy

Cần Python ≥ 3.9 (chỉ dùng stdlib, không cần pip install).

```bash
cd seed-data-generator

# Batch đầu tiên
python generate_seed_users.py --city cantho

# Batch tiếp theo phải exclude batch trước để tránh trùng username/email
python generate_seed_users.py --city danang \
    --exclude-file output/2026-07-16_seed_100_demo_users_cantho.sql

# Batch thứ ba (nếu có) exclude cả 2 file trước
# python generate_seed_users.py --city hanoi \
#     --exclude-file output/2026-07-16_seed_100_demo_users_cantho.sql \
#     --exclude-file output/2026-07-16_seed_100_demo_users_danang.sql
```

Output:
- `output/2026-07-16_seed_100_demo_users_cantho.sql`
- `output/2026-07-16_seed_100_demo_users_danang.sql`

Vì sao cần `--exclude-file`: dù mỗi city có `SEED` riêng, 2 batch dùng chung pool tên VN nên có tổ hợp `họ + tên` trùng nhau → cùng candidate username → email collision khi apply lên DB. `--exclude-file` cho script biết username nào đã dùng ở batch trước để nó tự chọn candidate khác (`trinh.dung` → `trinh_dung` → ...).

### Seed cafe pages (chỉ Cần Thơ)

Prereq: đã seed users Cần Thơ + 4 extra_fee packages.

```bash
python generate_seed_cafe_pages.py
```

Output: `output/2026-07-16_seed_20_cafe_pages_cantho.sql` — DO block runtime pick 20 owner từ Cần Thơ users (`ORDER BY md5(user_id::text)` cho stable pseudo-random), sinh 20 cafe với 10 gói 6mo + 10 gói 3mo, tất cả STRIPE_CARD + payment_status=PAID + status=ACTIVE.

## Apply lên DB

Dùng cùng docker command như các migration khác:

```powershell
docker run --rm `
  -e "PGPASSWORD=$env:PGPASSWORD" `
  -v "D:\cafe-story-platform\seed-data-generator\output\2026-07-16_seed_100_demo_users_cantho.sql:/migration.sql" `
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
