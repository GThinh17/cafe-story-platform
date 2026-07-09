import re


# Defense-in-depth (plan §3 Rule 5): Java đã strip PII ở DTO layer,
# lớp này chặn email/phone lỡ lọt trong UGC content trước khi embed.

_EMAIL_PATTERN = re.compile(r"[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}")
# SĐT Việt Nam: 0/84/+84 + đầu số di động, cho phép chấm/gạch/space giữa các cụm.
_PHONE_PATTERN = re.compile(
    r"(?<!\d)(?:\+?84|0)(?:[\s.\-]?\d){8,10}(?!\d)"
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
