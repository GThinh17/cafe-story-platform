"""
Upload 100 ảnh reviewer blog Đà Nẵng từ cafe_images/danang_reviewer_blog_photos/rev{XX}/
lên Cloudinary folder cafestory/reviewer_blogs (dùng chung folder với CT, tag khác nhau
qua public_id timestamp).

Output: danang_reviewer_blog_image_urls.json
{
  "1": {"1": ["url_a", "url_b"], "2": [...]},        # rev 1-10: 2 blog
  "11": {"1": [...], "2": [...], "3": [...]},        # rev 11-20: 3 blog
  ...
}

Chạy: python upload_danang_reviewer_blog_images.py
"""

from __future__ import annotations

import json
import mimetypes
import re
import sys
import uuid
from pathlib import Path
from typing import Dict, List
from urllib import request
from urllib.error import HTTPError, URLError

if sys.stdout.encoding and sys.stdout.encoding.lower() != "utf-8":
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except (AttributeError, OSError):
        pass

CLOUDINARY_FOLDER = "cafestory/reviewer_blogs"
PHOTOS_DIR = Path(__file__).parent / "cafe_images" / "danang_reviewer_blog_photos"
OUTPUT_JSON = Path(__file__).parent / "danang_reviewer_blog_image_urls.json"
FE_ENV_PATH = Path(__file__).parent.parent / "2-cafe-story-nextjs-web" / ".env"
FOLDER_RE = re.compile(r"^rev(\d{2})$")
FILE_RE = re.compile(r"^blog(\d+)_img(\d+)\.(jpg|jpeg|png|webp)$", re.IGNORECASE)


def _load_env(path: Path) -> Dict[str, str]:
    result: Dict[str, str] = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, _, value = line.partition("=")
        result[key.strip()] = value.strip().strip('"').strip("'")
    return result


def _encode_multipart(fields, file_name, file_content):
    boundary = uuid.uuid4().hex
    lines: List[bytes] = []
    for name, value in fields.items():
        lines.append(f"--{boundary}".encode())
        lines.append(f'Content-Disposition: form-data; name="{name}"'.encode("utf-8"))
        lines.append(b"")
        lines.append(str(value).encode("utf-8"))
    content_type = mimetypes.guess_type(file_name)[0] or "application/octet-stream"
    lines.append(f"--{boundary}".encode())
    lines.append(f'Content-Disposition: form-data; name="file"; filename="{file_name}"'.encode("utf-8"))
    lines.append(f"Content-Type: {content_type}".encode())
    lines.append(b"")
    lines.append(file_content)
    lines.append(f"--{boundary}--".encode())
    lines.append(b"")
    return b"\r\n".join(lines), boundary


def _upload(cloud_name: str, upload_preset: str, file_path: Path) -> str:
    body, boundary = _encode_multipart(
        {"upload_preset": upload_preset, "folder": CLOUDINARY_FOLDER},
        file_path.name,
        file_path.read_bytes(),
    )
    req = request.Request(
        f"https://api.cloudinary.com/v1_1/{cloud_name}/image/upload",
        data=body,
        headers={"Content-Type": f"multipart/form-data; boundary={boundary}"},
        method="POST",
    )
    try:
        with request.urlopen(req, timeout=60) as resp:
            payload = json.loads(resp.read().decode("utf-8"))
    except HTTPError as exc:
        detail = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"HTTP {exc.code} {file_path.name}: {detail}") from exc
    except URLError as exc:
        raise RuntimeError(f"Network {file_path.name}: {exc}") from exc
    secure_url = payload.get("secure_url")
    if not isinstance(secure_url, str) or not secure_url:
        raise RuntimeError(f"No secure_url for {file_path.name}: {payload!r}")
    return secure_url


def main() -> None:
    if not FE_ENV_PATH.is_file():
        raise SystemExit(f"Missing FE .env: {FE_ENV_PATH}")
    env = _load_env(FE_ENV_PATH)
    cloud_name = env.get("NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME")
    upload_preset = env.get("NEXT_PUBLIC_CLOUDINARY_UPLOAD_PRESET")
    if not cloud_name or not upload_preset:
        raise SystemExit("Missing Cloudinary config in FE .env")
    print(f"Cloudinary: cloud={cloud_name}, preset={upload_preset}, folder={CLOUDINARY_FOLDER}")

    if not PHOTOS_DIR.is_dir():
        raise SystemExit(f"Missing dir: {PHOTOS_DIR}")

    urls: Dict[str, Dict[str, List[str]]] = {}
    if OUTPUT_JSON.is_file():
        urls = json.loads(OUTPUT_JSON.read_text(encoding="utf-8"))

    total = 0
    ok_count = 0
    fail_count = 0

    for rev_folder in sorted(PHOTOS_DIR.iterdir()):
        if not rev_folder.is_dir():
            continue
        match = FOLDER_RE.match(rev_folder.name)
        if not match:
            continue
        rev_index = str(int(match.group(1)))
        rev_map = urls.setdefault(rev_index, {})
        print(f"\nrev#{rev_index:>2} {rev_folder.name}")

        files = []
        for fp in rev_folder.iterdir():
            m = FILE_RE.match(fp.name)
            if not m:
                continue
            files.append((int(m.group(1)), int(m.group(2)), fp))
        files.sort(key=lambda x: (x[0], x[1]))

        for blog_idx, img_idx, fp in files:
            total += 1
            blog_key = str(blog_idx)
            existing = rev_map.get(blog_key, [])
            while len(existing) < img_idx:
                existing.append("")
            if existing[img_idx - 1]:
                print(f"  ⤵ blog{blog_idx}_img{img_idx} — skip")
                ok_count += 1
                continue
            try:
                secure_url = _upload(cloud_name, upload_preset, fp)
                existing[img_idx - 1] = secure_url
                rev_map[blog_key] = existing
                ok_count += 1
                print(f"  ✓ blog{blog_idx}_img{img_idx} → {secure_url[-40:]}")
                OUTPUT_JSON.write_text(
                    json.dumps(urls, indent=2, ensure_ascii=False, sort_keys=True),
                    encoding="utf-8",
                )
            except RuntimeError as exc:
                fail_count += 1
                print(f"  ✗ blog{blog_idx}_img{img_idx} FAIL: {exc}")

    print("\n" + "=" * 60)
    print(f"Uploaded: {ok_count}/{total} images ({fail_count} failed)")


if __name__ == "__main__":
    main()
