"""
Download 200 ảnh cà phê từ LoremFlickr cho 100 blog (20 cafe × 5 blog × 2 img).

Themes rotate để đa dạng: espresso, latte-art, cafe-interior, coffee-beans, ...
File name: cafe_images/blog_photos/{cafe_index:02d}_{slug}/blog{N}_img{M}.jpg

Chạy: python download_blog_images.py
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

BASE_DIR = Path(__file__).parent / "cafe_images" / "blog_photos"
UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/126.0.0.0 Safari/537.36"

# Rotate themes for variety across 10 photos per cafe
BLOG_IMAGE_THEMES = [
    "coffee,espresso",           # blog 1 img 1
    "cafe,interior",             # blog 1 img 2
    "latte-art,coffee-cup",      # blog 2 img 1
    "coffee-beans,roasting",     # blog 2 img 2
    "cappuccino,foam",           # blog 3 img 1
    "cafe,chill,relax",          # blog 3 img 2
    "coffee,book,study",         # blog 4 img 1
    "barista,coffee-machine",    # blog 4 img 2
    "coffee-shop,vintage",       # blog 5 img 1
    "coffee,dessert,cake",       # blog 5 img 2
]

# 20 cafe slugs khớp với cantho_cafes.py (order matters — index 1-20)
CAFE_SLUGS = [
    "phuc_long", "highlands_vincom", "trung_nguyen_legend", "katinat", "the_80s_icafe",
    "dau_oi", "tram_coffee", "nha_pham_cf", "nha_pham_rung", "nha_pham_cau",
    "la_cafe", "raw_coffee", "co_ngot", "time_cafe", "sky_bar_iris",
    "aurora", "hihi_onigiri", "mat_ngot", "chin_cafe", "nha_co_khach",
]


def _download(url: str, target: Path) -> bool:
    if target.exists() and target.stat().st_size > 5000:
        return True  # already have
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


def _theme_for(blog_index: int, img_index: int) -> str:
    """Trả về theme cho (blog N=1..5, img M=1..2)."""
    slot = (blog_index - 1) * 2 + (img_index - 1)
    return BLOG_IMAGE_THEMES[slot]


def _url_for(cafe_index: int, blog_index: int, img_index: int) -> str:
    theme = _theme_for(blog_index, img_index)
    # LoremFlickr size 800x600 (landscape, blog-friendly)
    # lock=<seed> để deterministic per (cafe, blog, img)
    seed = cafe_index * 100 + blog_index * 10 + img_index
    return f"https://loremflickr.com/800/600/{theme}?lock={seed}"


def main() -> None:
    BASE_DIR.mkdir(parents=True, exist_ok=True)

    total = 0
    ok_count = 0
    for cafe_index in range(1, 21):
        slug = CAFE_SLUGS[cafe_index - 1]
        cafe_dir = BASE_DIR / f"{cafe_index:02d}_{slug}"
        cafe_dir.mkdir(exist_ok=True)
        print(f"\n#{cafe_index:02d} {slug}")

        for blog_index in range(1, 6):
            for img_index in range(1, 3):
                url = _url_for(cafe_index, blog_index, img_index)
                target = cafe_dir / f"blog{blog_index}_img{img_index}.jpg"
                theme = _theme_for(blog_index, img_index)
                if _download(url, target):
                    print(f"  ✓ blog{blog_index}_img{img_index} [{theme}]")
                    ok_count += 1
                else:
                    print(f"  ✗ blog{blog_index}_img{img_index}")
                total += 1
                time.sleep(0.15)

    print("\n" + "=" * 60)
    print(f"Downloaded: {ok_count}/{total} images")


if __name__ == "__main__":
    main()
