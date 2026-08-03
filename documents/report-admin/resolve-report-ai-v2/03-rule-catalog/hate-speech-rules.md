# Hateful Conduct Rules

| Thuộc tính | Giá trị |
|---|---|
| Gate | `G0-08` |
| Trạng thái | `PROPOSED` |
| Family | `PF-HATE` |
| Catalog | `RC-2.0.0-proposed.1` |

## `CSR.HATE.001` — Hateful attack on protected class

- **Criteria:** nội dung tấn công, hạ thấp nhân phẩm, kêu gọi loại trừ hoặc gây hại một người/nhóm
  dựa trên protected characteristic đã được policy phê duyệt.
- **Required evidence:** đoạn content cụ thể, target của phát ngôn, protected characteristic liên quan
  và context đủ để xác định phát ngôn là attack chứ không phải mention.
- **Counter-evidence/exceptions:** phản bác hate speech, tường thuật, giáo dục, tự nhận diện,
  trích dẫn để phê bình hoặc thảo luận trung lập.
- **Critical missing:** danh mục protected characteristics chưa được business phê duyệt;
  context ngôn ngữ làm thay đổi chủ thể/ngữ nghĩa; media không đọc được.
- **Candidate action:** content-level `HIDE/REMOVE` qua human; actor/page action cần pattern hoặc
  evidence riêng và không được suy từ một item.

Rule version là `1.0.0-proposed.1`; automation `A0`; effective date `TBD_ACTIVATION`.
Danh mục protected characteristics và jurisdictional exception là quyết định bắt buộc tại G0-10.
