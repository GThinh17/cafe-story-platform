# Spam and Platform Abuse Rules

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-08` |
| Trạng thái | `PROPOSED` |
| Rule | `CSR.SPAM.001` |
| Family | `PF-SPAM` |
| Catalog | `RC-2.0.0-proposed.1` |

## `CSR.SPAM.001` — Spam or platform manipulation

### Criteria

Repetitive, unsolicited hoặc coordinated behavior làm sai lệch trải nghiệm/nền tảng.
Một nội dung quảng bá đơn lẻ hoặc nội dung người dùng không thích không tự là spam.

### Evidence model

- **Required:** content snapshot; evidence về repetition, unsolicited delivery, automation hoặc manipulation
  tương ứng với subtype được kết luận.
- **Counter-evidence/exceptions:** promotion được cho phép, transactional message, duplicate do retry kỹ thuật,
  cross-post hợp lệ.
- **Critical missing:** rule cần pattern nhưng chỉ có một item; thiếu thời gian/actor/channel; không phân biệt
  duplicate kỹ thuật với intentional spam.
- **Candidate action:** content-level candidate; actor/page enforcement cần pattern và human review.

Rule version `1.0.0-proposed.1`; automation `A0`; effective date `TBD_ACTIVATION`.
Spam subtype/rate threshold chưa được chốt và không được model tự phát minh.
