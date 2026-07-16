"""
Sinh file SQL seed 100 user demo cho CafeStory.

Chi tiết:
- Tên: tổ hợp họ + tên đệm + tên riêng VN (vietnamese_names.py), 50 nam + 50 nữ,
  đảm bảo full_name không trùng.
- Username: dùng port của Java AuthServiceImpl (username_generator.py) để chọn
  candidate hợp lệ đầu tiên, tránh trùng trong batch.
- Email: {username}@gmail.com — cũng unique nhờ username unique.
- Địa chỉ: random 1 Phường ở Cần Thơ (cantho_wards.py), street/area để NULL.
- Avatar: DiceBear https://api.dicebear.com/9.x/avataaars/svg?seed={username}.
- Password: '123456' — được hash bằng pgcrypto.crypt(...) trong chính SQL sinh ra.
- Role: chỉ 'USER'.

Chạy: python generate_seed_users.py
Output: ./output/2026-07-16_seed_100_demo_users.sql
"""

from __future__ import annotations

import random
import sys
from pathlib import Path
from typing import List, Tuple

# Windows console (cp1252) can't render Vietnamese diacritics; force UTF-8.
if sys.stdout.encoding and sys.stdout.encoding.lower() != "utf-8":
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except (AttributeError, OSError):
        pass

from cantho_wards import (
    CANTHO_WARDS,
    CITY_CODE,
    CITY_NAME,
    PROVINCE_CODE,
    PROVINCE_NAME,
)
from username_generator import pick_username
from vietnamese_names import (
    FAMILY_NAMES,
    FEMALE_FIRST_NAMES,
    FEMALE_MIDDLE_NAMES,
    MALE_FIRST_NAMES,
    MALE_MIDDLE_NAMES,
)

SEED = 20260716  # deterministic re-runs
TOTAL_USERS = 100
MALE_RATIO = 0.5
DICEBEAR_STYLE = "avataaars"  # trung tính, hoạt động với mọi seed
OUTPUT_PATH = Path(__file__).parent / "output" / "2026-07-16_seed_100_demo_users.sql"


def _sql_escape(text: str) -> str:
    """Escape single quotes for Postgres string literals."""
    return text.replace("'", "''")


def _generate_full_names(rng: random.Random, count: int, is_male: bool) -> List[str]:
    middles = MALE_MIDDLE_NAMES if is_male else FEMALE_MIDDLE_NAMES
    firsts = MALE_FIRST_NAMES if is_male else FEMALE_FIRST_NAMES

    seen: set[str] = set()
    result: List[str] = []
    max_attempts = count * 50
    attempts = 0
    while len(result) < count and attempts < max_attempts:
        attempts += 1
        name = f"{rng.choice(FAMILY_NAMES)} {rng.choice(middles)} {rng.choice(firsts)}"
        if name in seen:
            continue
        seen.add(name)
        result.append(name)

    if len(result) < count:
        raise RuntimeError(
            f"Không tạo đủ {count} tên duy nhất (chỉ có {len(result)}) — mở rộng list tên."
        )
    return result


def _build_users(rng: random.Random) -> List[dict]:
    male_count = int(TOTAL_USERS * MALE_RATIO)
    female_count = TOTAL_USERS - male_count

    male_names = _generate_full_names(rng, male_count, is_male=True)
    female_names = _generate_full_names(rng, female_count, is_male=False)

    all_entries: List[Tuple[str, str]] = (
        [(name, "M") for name in male_names]
        + [(name, "F") for name in female_names]
    )
    rng.shuffle(all_entries)

    taken_usernames: set[str] = set()
    users: List[dict] = []
    for full_name, gender in all_entries:
        username = pick_username(full_name, taken_usernames)
        if not username:
            raise RuntimeError(
                f"Không sinh được username hợp lệ cho '{full_name}'."
            )
        taken_usernames.add(username)

        ward_code, ward_name = rng.choice(CANTHO_WARDS)
        email = f"{username}@gmail.com"
        avatar = f"https://api.dicebear.com/9.x/{DICEBEAR_STYLE}/svg?seed={username}"

        users.append({
            "full_name": full_name,
            "gender": gender,
            "username": username,
            "email": email,
            "avatar_url": avatar,
            "ward_code": ward_code,
            "ward_name": ward_name,
        })
    return users


def _emit_sql(users: List[dict]) -> str:
    header = f"""-- Seed {TOTAL_USERS} demo users (50 male + 50 female) for CafeStory.
-- Full names generated from Vietnamese family + middle + first name lists.
-- Addresses: random Phường in Thành phố Cần Thơ (province_code {PROVINCE_CODE},
-- city_code {CITY_CODE}). No street/area — user requested ward-level only.
-- Avatars: DiceBear {DICEBEAR_STYLE} seeded by username → each user gets a stable
-- unique avatar URL.
-- Password '123456' hashed via pgcrypto.crypt(...gen_salt('bf', 10)) at INSERT
-- time so Spring's BCryptPasswordEncoder can verify ($2a$10$... format).
-- Role assignment: USER only.

BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
DECLARE
    v_role_user integer;
    v_region_id uuid;
    v_user_id   uuid;
BEGIN
    -- Ensure USER role exists (idempotent, matches AuthServiceImpl.assignDefaultUserRole)
    INSERT INTO roles (name) VALUES ('USER') ON CONFLICT (name) DO NOTHING;
    SELECT id INTO v_role_user FROM roles WHERE name = 'USER';

    IF v_role_user IS NULL THEN
        RAISE EXCEPTION 'USER role could not be resolved.';
    END IF;

    -- Abort if any seed email already present — keeps this migration safely re-runnable
    IF EXISTS (
        SELECT 1 FROM users
        WHERE user_email IN (
"""

    email_literals = ",\n".join(
        f"            '{_sql_escape(u['email'])}'" for u in users
    )

    header += email_literals + """
        )
    ) THEN
        RAISE EXCEPTION 'One of the seed emails already exists — abort to avoid duplicates.';
    END IF;

"""

    body_parts: List[str] = []
    for index, user in enumerate(users, start=1):
        block = f"""    -- User {index:03d}: {user['full_name']} ({user['gender']}) — {user['ward_name']}
    INSERT INTO regions (region_id, province_code, city_code, ward_code,
                         province, city, ward, street, area,
                         created_at, updated_at)
    VALUES (gen_random_uuid(), '{PROVINCE_CODE}', '{CITY_CODE}', '{user['ward_code']}',
            '{_sql_escape(PROVINCE_NAME)}', '{_sql_escape(CITY_NAME)}', '{_sql_escape(user['ward_name'])}',
            NULL, NULL, now(), now())
    RETURNING region_id INTO v_region_id;

    INSERT INTO users (user_id, user_name, user_full_name, user_password, user_email,
                       user_avatar, user_like, user_follower, account_status,
                       hide_cafe_page_on_profile, region_id)
    VALUES (gen_random_uuid(), '{_sql_escape(user['username'])}', '{_sql_escape(user['full_name'])}',
            crypt('123456', gen_salt('bf', 10)),
            '{_sql_escape(user['email'])}',
            '{_sql_escape(user['avatar_url'])}',
            0, 0, true, false, v_region_id)
    RETURNING user_id INTO v_user_id;

    INSERT INTO user_roles (user_id, role_id, created_at)
    VALUES (v_user_id, v_role_user, now());
"""
        body_parts.append(block)

    footer = """END $$;

COMMIT;
"""
    return header + "\n".join(body_parts) + "\n" + footer


def main() -> None:
    rng = random.Random(SEED)
    users = _build_users(rng)
    sql = _emit_sql(users)

    OUTPUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT_PATH.write_text(sql, encoding="utf-8")

    male_count = sum(1 for u in users if u["gender"] == "M")
    female_count = sum(1 for u in users if u["gender"] == "F")
    print(f"Wrote {OUTPUT_PATH}")
    print(f"  Users: {len(users)} ({male_count} male, {female_count} female)")
    print(f"  Unique usernames: {len({u['username'] for u in users})}")
    print(f"  Wards used: {len({u['ward_code'] for u in users})} / {len(CANTHO_WARDS)}")
    print(f"  Sample: {users[0]['full_name']} → {users[0]['username']}"
          f" @ {users[0]['ward_name']}")


if __name__ == "__main__":
    main()
