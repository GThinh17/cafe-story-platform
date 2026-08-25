"""
Upload 20 avatar + 20 cover ảnh cafe lên Cloudinary, xuất mapping index → URL.

**Yêu cầu cấu trúc thư mục** (bạn chuẩn bị trước khi chạy):

    seed-data-generator/
    └── cafe_images/
        ├── avatars/
        │   ├── 01_phuc_long.jpg     (chỉ số đầu 2 chữ số = index cafe 1-20)
        │   ├── 02_highlands.jpg
        │   ├── ... đủ 20 file
        │   └── 20_tiem_nha_co_khach.jpg
        └── covers/
            ├── 01_phuc_long.jpg
            ├── 02_highlands.jpg
            └── ...

- Extension nhận: .jpg / .jpeg / .png / .webp
- Chỉ cần 2 ký tự đầu là index (01-20), phần còn lại tự do
- Ảnh 01 = cafe #01 = "Phúc Long Coffee & Tea" (theo cantho_cafes.py)

Chạy:
    python upload_cafe_images.py

Output:
    - Console: log từng URL upload
    - File `cafe_image_urls.json` — mapping {"1": {"avatar": "...", "cover": "..."}, ...}

Cloudinary config đọc từ 2-cafe-story-nextjs-web/.env (NEXT_PUBLIC_CLOUDINARY_*).
"""

from __future__ import annotations

import json
import mimetypes
import re
import sys
import uuid
from pathlib import Path
from typing import Dict, List, Tuple
from urllib import request
from urllib.error import HTTPError, URLError

if sys.stdout.encoding and sys.stdout.encoding.lower() != "utf-8":
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except (AttributeError, OSError):
        pass

CLOUDINARY_FOLDER = "cafestory/cafes"
IMAGES_DIR = Path(__file__).parent / "cafe_images"
OUTPUT_JSON = Path(__file__).parent / "cafe_image_urls.json"
FE_ENV_PATH = Path(__file__).parent.parent / "2-cafe-story-nextjs-web" / ".env"
ALLOWED_EXT = {".jpg", ".jpeg", ".png", ".webp"}
INDEX_RE = re.compile(r"^(\d{1,2})")


def _load_env(path: Path) -> Dict[str, str]:
    """Parse simple KEY=VALUE lines từ .env, bỏ qua # comment + blank."""
    result: Dict[str, str] = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        if "=" not in line:
            continue
        key, _, value = line.partition("=")
        result[key.strip()] = value.strip().strip('"').strip("'")
    return result


def _encode_multipart(
    fields: Dict[str, str],
    file_name: str,
    file_content: bytes,
) -> Tuple[bytes, str]:
    """Encode multipart/form-data body — 1 file + n text fields."""
    boundary = uuid.uuid4().hex
    lines: List[bytes] = []
    for name, value in fields.items():
        lines.append(f"--{boundary}".encode())
        lines.append(
            f'Content-Disposition: form-data; name="{name}"'.encode("utf-8")
        )
        lines.append(b"")
        lines.append(str(value).encode("utf-8"))

    content_type = mimetypes.guess_type(file_name)[0] or "application/octet-stream"
    lines.append(f"--{boundary}".encode())
    lines.append(
        f'Content-Disposition: form-data; name="file"; filename="{file_name}"'.encode(
            "utf-8"
        )
    )
    lines.append(f"Content-Type: {content_type}".encode())
    lines.append(b"")
    lines.append(file_content)

    lines.append(f"--{boundary}--".encode())
    lines.append(b"")
    body = b"\r\n".join(lines)
    return body, boundary


def _upload(
    cloud_name: str,
    upload_preset: str,
    file_path: Path,
) -> str:
    """POST multipart lên Cloudinary, trả về secure_url."""
    file_content = file_path.read_bytes()
    body, boundary = _encode_multipart(
        fields={
            "upload_preset": upload_preset,
            "folder": CLOUDINARY_FOLDER,
        },
        file_name=file_path.name,
        file_content=file_content,
    )
    url = f"https://api.cloudinary.com/v1_1/{cloud_name}/image/upload"
    req = request.Request(
        url,
        data=body,
        headers={
            "Content-Type": f"multipart/form-data; boundary={boundary}",
        },
        method="POST",
    )
    try:
        with request.urlopen(req, timeout=60) as resp:
            payload = json.loads(resp.read().decode("utf-8"))
    except HTTPError as exc:
        detail = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError(
            f"Cloudinary trả HTTP {exc.code} khi upload {file_path.name}: {detail}"
        ) from exc
    except URLError as exc:
        raise RuntimeError(f"Không nối được Cloudinary: {exc}") from exc

    secure_url = payload.get("secure_url")
    if not isinstance(secure_url, str) or not secure_url:
        raise RuntimeError(
            f"Response thiếu secure_url cho {file_path.name}: {payload!r}"
        )
    return secure_url


def _collect_files(subdir: Path) -> Dict[int, Path]:
    """Return {cafe_index: file_path} từ 1 folder. Bail nếu duplicate index."""
    if not subdir.is_dir():
        raise SystemExit(f"Không tìm thấy folder: {subdir}")
    result: Dict[int, Path] = {}
    for file_path in sorted(subdir.iterdir()):
        if not file_path.is_file():
            continue
        if file_path.suffix.lower() not in ALLOWED_EXT:
            print(f"  ⚠ bỏ qua {file_path.name} (extension không hỗ trợ)")
            continue
        match = INDEX_RE.match(file_path.stem)
        if not match:
            print(f"  ⚠ bỏ qua {file_path.name} (không có 1-2 chữ số đầu tên)")
            continue
        index = int(match.group(1))
        if index < 1 or index > 20:
            print(f"  ⚠ bỏ qua {file_path.name} (index {index} ngoài range 1-20)")
            continue
        if index in result:
            raise SystemExit(
                f"Trùng index {index}: {result[index].name} vs {file_path.name}"
            )
        result[index] = file_path
    return result


def main() -> None:
    if not FE_ENV_PATH.is_file():
        raise SystemExit(f"Không tìm thấy .env FE: {FE_ENV_PATH}")
    env = _load_env(FE_ENV_PATH)
    cloud_name = env.get("NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME")
    upload_preset = env.get("NEXT_PUBLIC_CLOUDINARY_UPLOAD_PRESET")
    if not cloud_name or not upload_preset:
        raise SystemExit(
            "Thiếu NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME / _UPLOAD_PRESET trong FE .env"
        )
    print(f"Cloudinary: cloud={cloud_name}, preset={upload_preset}, folder={CLOUDINARY_FOLDER}")

    avatars_dir = IMAGES_DIR / "avatars"
    covers_dir = IMAGES_DIR / "covers"
    print(f"\nScan avatars ở {avatars_dir}:")
    avatar_files = _collect_files(avatars_dir)
    print(f"  → tìm được {len(avatar_files)} file")

    print(f"\nScan covers ở {covers_dir}:")
    cover_files = _collect_files(covers_dir)
    print(f"  → tìm được {len(cover_files)} file")

    missing_avatars = set(range(1, 21)) - set(avatar_files)
    missing_covers = set(range(1, 21)) - set(cover_files)
    if missing_avatars or missing_covers:
        print("\n⚠ Thiếu ảnh:")
        if missing_avatars:
            print(f"  avatars: {sorted(missing_avatars)}")
        if missing_covers:
            print(f"  covers: {sorted(missing_covers)}")
        answer = input("Vẫn upload phần đã có? [y/N] ").strip().lower()
        if answer != "y":
            raise SystemExit("Abort.")

    # Load existing mapping (nếu upload lại từng phần)
    urls: Dict[str, Dict[str, str]] = {}
    if OUTPUT_JSON.is_file():
        urls = json.loads(OUTPUT_JSON.read_text(encoding="utf-8"))

    print("\nUpload avatars:")
    for index in sorted(avatar_files):
        file_path = avatar_files[index]
        try:
            secure_url = _upload(cloud_name, upload_preset, file_path)
        except RuntimeError as exc:
            print(f"  #{index:02d} FAIL: {exc}")
            continue
        urls.setdefault(str(index), {})["avatar"] = secure_url
        print(f"  #{index:02d} {file_path.name} → {secure_url}")

    print("\nUpload covers:")
    for index in sorted(cover_files):
        file_path = cover_files[index]
        try:
            secure_url = _upload(cloud_name, upload_preset, file_path)
        except RuntimeError as exc:
            print(f"  #{index:02d} FAIL: {exc}")
            continue
        urls.setdefault(str(index), {})["cover"] = secure_url
        print(f"  #{index:02d} {file_path.name} → {secure_url}")

    OUTPUT_JSON.write_text(
        json.dumps(urls, indent=2, ensure_ascii=False, sort_keys=True),
        encoding="utf-8",
    )
    complete = sum(
        1 for entry in urls.values() if "avatar" in entry and "cover" in entry
    )
    print(f"\nWrote {OUTPUT_JSON}")
    print(f"  {complete}/20 cafe có đủ cả avatar + cover URL.")


if __name__ == "__main__":
    main()
