"""
Download 200 ảnh reviewer blog từ Pexels API — ảnh chất lượng cao, chủ đề chuẩn
(coffee, matcha, milk tea, cafe interior, ...) không lẫn ảnh rác như LoremFlickr.

25 theme × 8 ảnh/theme = 200 ảnh, mỗi ảnh unique (distinct Pexels photo_id).
API key đọc từ seed-data-generator/.env (biến PEXELS_API_KEY).

Slot mapping (round-robin):
  slot_idx  = enumeration order of (rev, blog, img) tuples
  theme_idx = slot_idx % 25
  photo_idx = slot_idx // 25  (0..7, mỗi theme 8 ảnh)

File name: cafe_images/reviewer_blog_photos/rev{XX}/blog{N}_img{M}.jpg

Chạy: python download_pexels_reviewer_images.py
"""

from __future__ import annotations

import json
import sys
import time
from pathlib import Path
from typing import Dict, List, Tuple
from urllib import request
from urllib.error import HTTPError, URLError

if sys.stdout.encoding and sys.stdout.encoding.lower() != "utf-8":
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except (AttributeError, OSError):
        pass

BASE_DIR = Path(__file__).parent / "cafe_images" / "reviewer_blog_photos"
ENV_PATH = Path(__file__).parent / ".env"
UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/126.0.0.0 Safari/537.36"

# 25 theme đa dạng — coffee drinks + non-coffee + không gian + dessert
THEMES = [
    # Coffee drinks (8)
    "coffee shop",
    "espresso",
    "latte art",
    "cappuccino",
    "cold brew coffee",
    "coffee cup",
    "iced coffee",
    "vietnamese coffee",
    # Non-coffee drinks (6)
    "matcha latte",
    "matcha tea",
    "milk tea",
    "bubble tea",
    "smoothie",
    "iced tea",
    # Cafe space (5)
    "cafe interior",
    "coffee bar",
    "barista",
    "cozy cafe",
    "cafe decor",
    # Dessert / food (4)
    "coffee cake",
    "dessert cafe",
    "croissant coffee",
    "brunch cafe",
    # Vibes (2)
    "coffee beans",
    "cafe table",
]
PHOTOS_PER_THEME = 8  # 25 × 8 = 200

TOTAL_REVIEWERS = 40
COUNT_2_BLOG = 20  # rev 1-20 có 2 blog
COUNT_3_BLOG = 20  # rev 21-40 có 3 blog


def _load_api_key() -> str:
    for line in ENV_PATH.read_text(encoding="utf-8").splitlines():
        if line.startswith("PEXELS_API_KEY="):
            return line.split("=", 1)[1].strip().strip('"').strip("'")
    raise SystemExit(f"Thiếu PEXELS_API_KEY trong {ENV_PATH}")


def _search_pexels(api_key: str, query: str, count: int) -> List[Dict]:
    """Return list of photo dict từ Pexels."""
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


def _blogs_for(rev_index: int) -> int:
    return 2 if rev_index <= COUNT_2_BLOG else 3


def _build_slots() -> List[Tuple[int, int, int]]:
    """Return list (rev_index, blog_index, img_index) theo thứ tự interleaved."""
    slots = []
    for rev in range(1, TOTAL_REVIEWERS + 1):
        n_blogs = _blogs_for(rev)
        for blog in range(1, n_blogs + 1):
            for img in (1, 2):
                slots.append((rev, blog, img))
    return slots


def main() -> None:
    api_key = _load_api_key()
    BASE_DIR.mkdir(parents=True, exist_ok=True)

    slots = _build_slots()
    total_slots = len(slots)
    print(f"Total slots to fill: {total_slots}")
    assert total_slots == 200, f"Expect 200 slots, got {total_slots}"

    # Step 1: fetch photos cho từng theme
    print(f"\nFetching Pexels — {len(THEMES)} themes × {PHOTOS_PER_THEME} ảnh:")
    theme_photos: Dict[str, List[str]] = {}
    for i, theme in enumerate(THEMES, start=1):
        photos = _search_pexels(api_key, theme, PHOTOS_PER_THEME + 2)  # +2 buffer
        urls = [p["src"]["large"] for p in photos[:PHOTOS_PER_THEME]]
        if len(urls) < PHOTOS_PER_THEME:
            print(f"  ⚠ [{i}/{len(THEMES)}] '{theme}' chỉ {len(urls)} ảnh")
        else:
            print(f"  ✓ [{i}/{len(THEMES)}] '{theme}' → {len(urls)} URL")
        theme_photos[theme] = urls
        time.sleep(0.3)  # polite rate limit

    # Step 2: download từng ảnh theo round-robin theme
    print(f"\nDownloading 200 images:")
    ok_count = 0
    for slot_idx, (rev, blog, img) in enumerate(slots):
        theme = THEMES[slot_idx % len(THEMES)]
        photo_idx = slot_idx // len(THEMES)
        if photo_idx >= len(theme_photos[theme]):
            print(f"  ✗ rev{rev:02d} blog{blog} img{img}: no more photos for theme '{theme}'")
            continue

        url = theme_photos[theme][photo_idx]
        rev_dir = BASE_DIR / f"rev{rev:02d}"
        rev_dir.mkdir(exist_ok=True)
        target = rev_dir / f"blog{blog}_img{img}.jpg"

        if _download(url, target):
            ok_count += 1
            if slot_idx % 25 == 0:
                print(f"  ✓ [{slot_idx + 1}/200] rev{rev:02d} blog{blog} img{img} [{theme}]")
        time.sleep(0.1)

    print("\n" + "=" * 60)
    print(f"Downloaded: {ok_count}/{total_slots} images")


if __name__ == "__main__":
    main()
