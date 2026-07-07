import unittest

from app.services.rag.chunker import chunk_blog, chunk_cafe_page, estimate_tokens
from app.services.rag.markdown_parser import parse_doc_file
from app.services.rag.sanitizer import sanitize_text


class SanitizerTest(unittest.TestCase):

    def test_masks_email_and_phone(self):
        text = "Lien he chu quan: chuquan@gmail.com hoac 0912345678 nhe"
        sanitized = sanitize_text(text)
        self.assertNotIn("chuquan@gmail.com", sanitized)
        self.assertNotIn("0912345678", sanitized)

    def test_masks_mentions_when_enabled(self):
        sanitized = sanitize_text("cam on @minh.tran da gioi thieu", mask_mentions=True)
        self.assertNotIn("@minh.tran", sanitized)

    def test_keeps_normal_text(self):
        text = "Quan cafe view dep, gia 45k mot ly."
        self.assertEqual(sanitize_text(text), text)


class CafePageChunkerTest(unittest.TestCase):

    DATA = {
        "name": "The Hidden Garden",
        "address": "12 Nguyen Hue",
        "city": "Ho Chi Minh",
        "province": "TP.HCM",
        "description": "Quan yen tinh trong hem.",
        "regionId": "r-1",
        "followerCount": 10,
        "likeCount": 5,
        "avatarUrl": "http://img/avatar.jpg",
    }

    def test_one_cafe_is_one_chunk(self):
        chunks = chunk_cafe_page("cafe-1", self.DATA)
        self.assertEqual(len(chunks), 1)
        self.assertIn("The Hidden Garden", chunks[0].body)
        self.assertIn("TP.HCM", chunks[0].body)
        self.assertEqual(chunks[0].metadata["province"], "TP.HCM")


class BlogChunkerTest(unittest.TestCase):

    def test_paragraph_based_stable_boundaries(self):
        data = {
            "content": "Doan mot noi ve khong gian.\n\nDoan hai noi ve do uong.",
            "pageName": "Quan A",
            "tags": ["garden cafe"],
        }
        chunks = chunk_blog("blog-1", data)
        self.assertEqual(len(chunks), 2)
        # Hash tren body — khong chua header/pageName
        self.assertEqual(chunks[0].body, "Doan mot noi ve khong gian.")
        self.assertIn("Quan A", chunks[0].embed_text)
        self.assertIn("garden cafe", chunks[0].embed_text)

    def test_page_name_change_does_not_change_hash(self):
        content = {"content": "Noi dung khong doi."}
        hash_a = chunk_blog("b", {**content, "pageName": "Quan A"})[0].body_hash
        hash_b = chunk_blog("b", {**content, "pageName": "Quan B"})[0].body_hash
        self.assertEqual(hash_a, hash_b)

    def test_oversized_paragraph_split_at_sentence_boundary(self):
        long_paragraph = " ".join(f"Cau so {i} noi ve trai nghiem quan." for i in range(200))
        chunks = chunk_blog("blog-2", {"content": long_paragraph})
        self.assertGreater(len(chunks), 1)
        for chunk in chunks:
            self.assertTrue(chunk.body.endswith("."), f"cat giua cau: ...{chunk.body[-40:]}")


class MarkdownParserTest(unittest.TestCase):

    DOC = """---
title: Chinh sua avatar
slug: chinh-sua-avatar
platform: web
category: profile
---

# Chinh sua avatar tren web

## Dieu kien tien quyet

Ban phai dang nhap.

## Cac buoc thuc hien

### Buoc 1: Mo cai dat

Click vao anh dai dien.

### Buoc 2: Tai anh

Chon anh tu may tinh.

## Loi thuong gap

Kiem tra dinh dang file.
"""

    def test_sections_have_breadcrumb_and_stable_hash(self):
        meta, chunks = parse_doc_file("web/profile/chinh-sua-avatar.md", self.DOC)
        self.assertEqual(meta["platform"], "web")
        self.assertGreaterEqual(len(chunks), 3)
        for chunk in chunks:
            self.assertTrue(chunk.embed_text.startswith("["), "thieu breadcrumb")
            self.assertNotIn("[", chunk.body[:1], "hash body khong duoc chua breadcrumb")
            self.assertEqual(chunk.source_id, "web/profile/chinh-sua-avatar.md")

    def test_heading_change_keeps_body_hash(self):
        _, chunks_a = parse_doc_file("f.md", self.DOC)
        _, chunks_b = parse_doc_file("f.md", self.DOC.replace("# Chinh sua avatar tren web", "# Tieu de moi"))
        hashes_a = {c.body_hash for c in chunks_a}
        hashes_b = {c.body_hash for c in chunks_b}
        self.assertEqual(hashes_a, hashes_b)

    def test_code_block_not_split(self):
        doc = """---
title: Test
---

# Doc

## Section

```bash
## day la comment trong code, khong phai heading
echo hello
```

Ket thuc.
"""
        _, chunks = parse_doc_file("f.md", doc)
        self.assertEqual(len(chunks), 1)
        self.assertIn("day la comment trong code", chunks[0].body)


class TokenEstimateTest(unittest.TestCase):

    def test_estimate_positive(self):
        self.assertGreaterEqual(estimate_tokens("xin chao"), 1)


if __name__ == "__main__":
    unittest.main()
