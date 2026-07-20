"""
Download 100 ảnh blog reviewer Đà Nẵng từ Pexels — focus barista / quán nước /
thức uống tại quán, KHÁC themes với batch reviewer Cần Thơ và user blog CT.

25 theme × 4 ảnh/theme = 100 ảnh. Dedup theo Pexels photo_id trong batch
(một số theme trả về photo trùng của theme khác → skip, dùng buffer).

Slot mapping:
  slot_idx  = enumeration order (rev, blog, img)
  theme_idx = slot_idx % 25
  photo_idx = slot_idx // 25  (0..3)

File name: cafe_images/danang_reviewer_blog_photos/rev{XX}/blog{N}_img{M}.jpg

Chạy: python download_pexels_danang_reviewer_images.py
"""

from __future__ import annotations

import json
import re
import sys
import time
from pathlib import Path
from typing import Dict, List, Set, Tuple
from urllib import request
from urllib.error import HTTPError, URLError

if sys.stdout.encoding and sys.stdout.encoding.lower() != "utf-8":
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except (AttributeError, OSError):
        pass

BASE_DIR = Path(__file__).parent / "cafe_images" / "danang_reviewer_blog_photos"
ENV_PATH = Path(__file__).parent / ".env"
UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/126.0.0.0 Safari/537.36"

# 25 theme mới — barista / thức uống tại quán / quán nước (khác batch trước)
THEMES = [
    # Barista skills (10)
    "barista pouring",
    "barista skills",
    "barista training",
    "barista latte",
    "espresso machine",
    "coffee grinder",
    "coffee brewing",
    "pour over coffee",
    "hand drip coffee",
    "coffee siphon",
    # Thức uống signature tại quán (10)
    "specialty coffee",
    "artisan coffee",
    "flat white coffee",
    "mocha coffee",
    "affogato",
    "chai latte",
    "coconut coffee",
    "hot chocolate cafe",
    "coffee foam",
    "iced americano",
    # Không gian quán nước (5)
    "coffee shop menu",
    "cafe workspace",
    "cafe atmosphere",
    "cafe display",
    "coffee art",
]
PHOTOS_PER_THEME = 4  # 25 × 4 = 100
BUFFER_PER_THEME = 4  # fetch 8/theme để dedup có options

TOTAL_REVIEWERS = 20
COUNT_2_BLOG = 10  # rev 1-10 có 2 blog
COUNT_3_BLOG = 10  # rev 11-20 có 3 blog

PEXELS_PHOTO_ID_RE = re.compile(r"/photos/(\d+)/")


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


def _blogs_for(rev_index: int) -> int:
    return 2 if rev_index <= COUNT_2_BLOG else 3


def _build_slots() -> List[Tuple[int, int, int]]:
    slots = []
    for rev in range(1, TOTAL_REVIEWERS + 1):
        n_blogs = _blogs_for(rev)
        for blog in range(1, n_blogs + 1):
            for img in (1, 2):
                slots.append((rev, blog, img))
    return slots


def _extract_photo_id(url: str) -> str:
    m = PEXELS_PHOTO_ID_RE.search(url)
    return m.group(1) if m else url


def main() -> None:
    api_key = _load_api_key()
    BASE_DIR.mkdir(parents=True, exist_ok=True)

    slots = _build_slots()
    total_slots = len(slots)
    print(f"Total slots to fill: {total_slots}")
    assert total_slots == 100, f"Expect 100 slots, got {total_slots}"

    # Fetch với buffer, dedup theo photo_id trên toàn batch
    print(f"\nFetching Pexels — {len(THEMES)} themes × {PHOTOS_PER_THEME}+{BUFFER_PER_THEME} buffer:")
    used_photo_ids: Set[str] = set()
    theme_photos: Dict[str, List[str]] = {}
    for i, theme in enumerate(THEMES, start=1):
        fetch_count = PHOTOS_PER_THEME + BUFFER_PER_THEME
        photos = _search_pexels(api_key, theme, fetch_count)
        distinct_urls: List[str] = []
        for p in photos:
            url = p["src"]["large"]
            pid = _extract_photo_id(url)
            if pid in used_photo_ids:
                continue
            used_photo_ids.add(pid)
            distinct_urls.append(url)
            if len(distinct_urls) >= PHOTOS_PER_THEME:
                break
        if len(distinct_urls) < PHOTOS_PER_THEME:
            print(f"  ⚠ [{i}/{len(THEMES)}] '{theme}' chỉ {len(distinct_urls)}/{PHOTOS_PER_THEME} URL sau dedup")
        else:
            print(f"  ✓ [{i}/{len(THEMES)}] '{theme}' → {len(distinct_urls)} URL (distinct)")
        theme_photos[theme] = distinct_urls
        time.sleep(0.3)

    print(f"\nTotal distinct photo_ids in batch: {len(used_photo_ids)}")

    # Download slot-by-slot
    print(f"\nDownloading {total_slots} images:")
    ok_count = 0
    for slot_idx, (rev, blog, img) in enumerate(slots):
        theme = THEMES[slot_idx % len(THEMES)]
        photo_idx = slot_idx // len(THEMES)
        if photo_idx >= len(theme_photos[theme]):
            print(f"  ✗ rev{rev:02d} blog{blog} img{img}: no more photos for '{theme}'")
            continue

        url = theme_photos[theme][photo_idx]
        rev_dir = BASE_DIR / f"rev{rev:02d}"
        rev_dir.mkdir(exist_ok=True)
        target = rev_dir / f"blog{blog}_img{img}.jpg"

        if _download(url, target):
            ok_count += 1
            if slot_idx % 25 == 0:
                print(f"  ✓ [{slot_idx + 1}/{total_slots}] rev{rev:02d} blog{blog} img{img} [{theme}]")
        time.sleep(0.1)

    print("\n" + "=" * 60)
    print(f"Downloaded: {ok_count}/{total_slots} images")


if __name__ == "__main__":
    main()
