import re


# Defense-in-depth (plan §3 Rule 5): Java đã strip PII ở DTO layer,
# lớp này chặn email/phone lỡ lọt trong UGC content trước khi embed.

_EMAIL_PATTERN = re.compile(r"[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}")
# SĐT di động Việt Nam: prefix 0 / 84 / +84 + đầu số di động hợp lệ (03/05/07/08/09)
# + đúng 7 chữ số còn lại (tổng 10 số quốc nội). Cho phép chấm/gạch/space giữa các cụm.
# Siết chặt so với bản cũ (khớp 8-10 số bất kỳ) để không mask nhầm giá tiền / ID dài.
_PHONE_PATTERN = re.compile(
    r"(?<!\d)(?:\+?84|0)[\s.\-]?(?:3[2-9]|5[25689]|7[06-9]|8[1-9]|9\d)(?:[\s.\-]?\d){7}(?!\d)"
)
_MENTION_PATTERN = re.compile(r"@[\w.]{2,}")

EMAIL_MASK = "[email đã ẩn]"
PHONE_MASK = "[số điện thoại đã ẩn]"
MENTION_MASK = "[người dùng]"


def sanitize_text(text: str | None, mask_mentions: bool = False) -> str:
    if not text:
        return ""
    sanitized = _EMAIL_PATTERN.sub(EMAIL_MASK, text)
    sanitized = _PHONE_PATTERN.sub(PHONE_MASK, sanitized)
    if mask_mentions:
        sanitized = _MENTION_PATTERN.sub(MENTION_MASK, sanitized)
    return sanitized
