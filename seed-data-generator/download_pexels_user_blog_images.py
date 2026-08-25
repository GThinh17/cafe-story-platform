"""
Download 40 ảnh blog user thường từ Pexels API — chủ đề quán nước, thức uống,
cà phê, quầy bar.

20 theme × 2 ảnh/theme = 40 ảnh, mỗi ảnh unique.
API key đọc từ seed-data-generator/.env (biến PEXELS_API_KEY).

Slot mapping (round-robin):
  slot_idx  = user_index - 1  (0..39)
  theme_idx = slot_idx % 20
  photo_idx = slot_idx // 20  (0..1)

File name: cafe_images/user_blog_photos/user{XXX}.jpg

Chạy: python download_pexels_user_blog_images.py
"""

from __future__ import annotations

import json
import sys
import time
from pathlib import Path
from typing import Dict, List
from urllib import request
from urllib.error import HTTPError, URLError

if sys.stdout.encoding and sys.stdout.encoding.lower() != "utf-8":
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except (AttributeError, OSError):
        pass

BASE_DIR = Path(__file__).parent / "cafe_images" / "user_blog_photos"
ENV_PATH = Path(__file__).parent / ".env"
UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/126.0.0.0 Safari/537.36"

# 20 theme: quán nước, thức uống, cà phê, quầy bar
THEMES = [
    # Cà phê (7)
    "vietnamese coffee",
    "coffee cup",
    "iced coffee",
    "espresso",
    "latte art",
    "cappuccino",
    "cold brew coffee",
    # Thức uống non-coffee (7)
    "milk tea",
    "bubble tea",
    "matcha latte",
    "smoothie drink",
    "fruit tea",
    "lemonade",
    "iced tea",
    # Quầy bar (3)
    "cocktail bar",
    "bar counter",
    "bartender",
    # Quán nước / vibes (3)
    "coffee bar",
    "juice bar",
    "cafe drinks",
]
PHOTOS_PER_THEME = 2  # 20 × 2 = 40
TOTAL_USERS = 40


def _load_api_key() -> str:
    for line in ENV_PATH.read_text(encoding="utf-8").splitlines():
        if line.startswith("PEXELS_API_KEY="):
            return line.split("=", 1)[1].strip().strip('"').strip("'")
    raise SystemExit(f"Thiếu PEXELS_API_KEY trong {ENV_PATH}")


def _search_pexels(api_key: str, query: str, count: int) -> List[Dict]:
    url = f"https://api.pexels.com/v1/search?query={query.replace(' ', '+')}&per_page={count}"
    req = request.Request(url, headers={"Authorization": api_key, "User-Agent": UA})
    try:
        with request.urlopen(req, timeout=25) as resp:
            payload = json.loads(resp.read().decode("utf-8"))
    except (HTTPError, URLError, TimeoutError) as exc:
        raise RuntimeError(f"Pexels search '{query}' fail: {exc}") from exc
    return payload.get("photos", [])


def _download(url: str, target: Path) -> bool:
    if target.exists() and target.stat().st_size > 5000:
        return True
    req = request.Request(url, headers={"User-Agent": UA})
    try:
        with request.urlopen(req, timeout=25) as resp:
            data = resp.read()
    except (HTTPError, URLError, TimeoutError) as exc:
        print(f"    ✗ {exc}")
        return False
    if len(data) < 5000:
        print(f"    ✗ too small ({len(data)} bytes)")
        return False
    target.write_bytes(data)
    return True


def main() -> None:
    api_key = _load_api_key()
    BASE_DIR.mkdir(parents=True, exist_ok=True)

    assert len(THEMES) * PHOTOS_PER_THEME == TOTAL_USERS, (
        f"Themes × per_theme phải = {TOTAL_USERS}, "
        f"got {len(THEMES)} × {PHOTOS_PER_THEME}"
    )

    print(f"Fetching Pexels — {len(THEMES)} themes × {PHOTOS_PER_THEME} ảnh:")
    theme_photos: Dict[str, List[str]] = {}
    for i, theme in enumerate(THEMES, start=1):
        photos = _search_pexels(api_key, theme, PHOTOS_PER_THEME + 2)  # +2 buffer
        urls = [p["src"]["large"] for p in photos[:PHOTOS_PER_THEME]]
        if len(urls) < PHOTOS_PER_THEME:
            print(f"  ⚠ [{i}/{len(THEMES)}] '{theme}' chỉ {len(urls)} ảnh")
        else:
            print(f"  ✓ [{i}/{len(THEMES)}] '{theme}' → {len(urls)} URL")
        theme_photos[theme] = urls
        time.sleep(0.3)

    print(f"\nDownloading {TOTAL_USERS} images:")
    ok_count = 0
    for user_idx in range(1, TOTAL_USERS + 1):
        slot_idx = user_idx - 1
        theme = THEMES[slot_idx % len(THEMES)]
        photo_idx = slot_idx // len(THEMES)
        if photo_idx >= len(theme_photos[theme]):
            print(f"  ✗ user{user_idx:03d}: no more photos for '{theme}'")
            continue

        url = theme_photos[theme][photo_idx]
        target = BASE_DIR / f"user{user_idx:03d}.jpg"

        if _download(url, target):
            ok_count += 1
            if user_idx % 20 == 0 or user_idx == 1:
                print(f"  ✓ [{user_idx:>3}/{TOTAL_USERS}] user{user_idx:03d} [{theme}]")
        time.sleep(0.1)

    print("\n" + "=" * 60)
    print(f"Downloaded: {ok_count}/{TOTAL_USERS} images")


if __name__ == "__main__":
    main()
