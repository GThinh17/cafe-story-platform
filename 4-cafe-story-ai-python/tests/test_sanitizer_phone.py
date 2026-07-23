import unittest

from app.services.rag.sanitizer import EMAIL_MASK, PHONE_MASK, sanitize_text


class SanitizerPhoneTest(unittest.TestCase):
    """Plan C10: regex mới chỉ mask SĐT di động VN hợp lệ, không nuốt nhầm
    giá tiền / mã đơn (chuỗi số dài không phải SĐT)."""

    def test_masks_domestic_mobile(self):
        out = sanitize_text("Liên hệ 0912345678 nhé")
        self.assertIn(PHONE_MASK, out)
        self.assertNotIn("0912345678", out)

    def test_masks_mobile_with_separators(self):
        out = sanitize_text("Gọi +84 912.345.678 gặp mình")
        self.assertIn(PHONE_MASK, out)
        self.assertNotIn("912.345.678", out)

    def test_does_not_mask_price_like_number(self):
        # 10 chữ số nhưng không có prefix 0/84 và không phải đầu số di động.
        out = sanitize_text("Giá 1234567890 đồng")
        self.assertNotIn(PHONE_MASK, out)
        self.assertIn("1234567890", out)

    def test_does_not_mask_invalid_prefix_after_zero(self):
        # '0' + '12...' → '1' không phải đầu số di động hợp lệ (bug cũ sẽ mask).
        out = sanitize_text("Mã đơn 0123456789 xử lý")
        self.assertNotIn(PHONE_MASK, out)
        self.assertIn("0123456789", out)

    def test_still_masks_email(self):
        out = sanitize_text("Mail abc@example.com")
        self.assertIn(EMAIL_MASK, out)
        self.assertNotIn("abc@example.com", out)


if __name__ == "__main__":
    unittest.main()
