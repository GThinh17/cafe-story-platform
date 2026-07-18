"""
Download 200 ảnh cho blog reviewer (40 reviewer × avg 2.5 blog × 2 img).

Layout: 20 rev với 2 blog + 20 rev với 3 blog = 100 blog × 2 img = 200 img.
File name: cafe_images/reviewer_blog_photos/rev{XX}/blog{N}_img{M}.jpg
  XX = reviewer_index 01-40
  N = blog_index 1-3 (max 3)
  M = img_index 1-2

Themes rotate theo (blog_index, img_index) — riêng cho reviewer angle:
  blog1 img1 → tasting            blog1 img2 → cafe-drink
  blog2 img1 → coffee-review      blog2 img2 → coffee-artisan
  blog3 img1 → cafe-hopping       blog3 img2 → cafe-vibes

Chạy: python download_reviewer_blog_images.py
"""

from __future__ import annotations

import sys
import time
from pathlib import Path
from urllib import request
from urllib.error import HTTPError, URLError

if sys.stdout.encoding and sys.stdout.encoding.lower() != "utf-8":
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except (AttributeError, OSError):
        pass

BASE_DIR = Path(__file__).parent / "cafe_images" / "reviewer_blog_photos"
UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/126.0.0.0 Safari/537.36"

# Pool 25 theme đa dạng — coffee, trà sữa, matcha, không gian, dessert...
# LoremFlickr single-tag only (multi-tag đang 500). Với 200 ảnh và 25 tag,
# mỗi tag ~8 ảnh khác nhau nhờ lock seed → không cluster monotone.
REVIEWER_THEMES = [
    # Coffee drinks (7)
    "coffee", "espresso", "latte", "cappuccino", "americano", "coldbrew", "mocha",
    # Non-coffee drinks (6)
    "matcha", "milktea", "bubbletea", "tea", "smoothie", "juice",
    # Cafe space / atmosphere (6)
    "cafe", "coffeeshop", "interior", "decor", "cozy", "restaurant",
    # Dessert / food (4)
    "cake", "dessert", "croissant", "brunch",
    # Chill / vibes (2)
    "chill", "bookshop",
]


def _theme_for(rev_index: int, blog_index: int, img_index: int) -> str:
    """Pick 1 tag từ pool, deterministic per (rev, blog, img)."""
    # Kết hợp 3 dim để spread rộng trong pool
    key = rev_index * 37 + blog_index * 7 + img_index * 3
    return REVIEWER_THEMES[key % len(REVIEWER_THEMES)]

TOTAL_REVIEWERS = 40
COUNT_2_BLOG = 20  # 20 reviewer đầu có 2 blog
COUNT_3_BLOG = 20  # 20 reviewer sau có 3 blog


def _try_download(url: str) -> bytes | None:
    req = request.Request(url, headers={"User-Agent": UA})
    try:
        with request.urlopen(req, timeout=25) as resp:
            data = resp.read()
        if len(data) < 5000:
            return None
        return data
    except (HTTPError, URLError, TimeoutError):
        return None


def _download(rev_index: int, blog_index: int, img_index: int, target: Path) -> bool:
    if target.exists() and target.stat().st_size > 5000:
        return True
    theme = _theme_for(rev_index, blog_index, img_index)
    seed = 20000 + rev_index * 100 + blog_index * 10 + img_index

    urls = [
        f"https://loremflickr.com/800/600/{theme}?lock={seed}",
        f"https://picsum.photos/seed/{seed}/800/600",
    ]
    for url in urls:
        data = _try_download(url)
        if data:
            target.write_bytes(data)
            return True
    print(f"    ✗ all providers failed")
    return False


def _blogs_for(rev_index: int) -> int:
    """Reviewer index 1-20 → 2 blog; 21-40 → 3 blog."""
    return 2 if rev_index <= COUNT_2_BLOG else 3


def main() -> None:
    BASE_DIR.mkdir(parents=True, exist_ok=True)

    total = 0
    ok_count = 0
    for rev_index in range(1, TOTAL_REVIEWERS + 1):
        n_blogs = _blogs_for(rev_index)
        rev_dir = BASE_DIR / f"rev{rev_index:02d}"
        rev_dir.mkdir(exist_ok=True)
        print(f"\nrev#{rev_index:02d} ({n_blogs} blog)")

        for blog_index in range(1, n_blogs + 1):
            for img_index in range(1, 3):
                target = rev_dir / f"blog{blog_index}_img{img_index}.jpg"
                theme = _theme_for(rev_index, blog_index, img_index)
                if _download(rev_index, blog_index, img_index, target):
                    print(f"  ✓ blog{blog_index}_img{img_index} [{theme}]")
                    ok_count += 1
                else:
                    print(f"  ✗ blog{blog_index}_img{img_index}")
                total += 1
                time.sleep(0.15)

    print("\n" + "=" * 60)
    print(f"Downloaded: {ok_count}/{total} images")
    print(f"Expected: 20 × 2 × 2 + 20 × 3 × 2 = {20 * 2 * 2 + 20 * 3 * 2}")


if __name__ == "__main__":
    main()
