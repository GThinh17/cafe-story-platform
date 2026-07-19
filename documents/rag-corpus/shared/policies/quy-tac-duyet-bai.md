---
title: Quy tắc duyệt bài viết
slug: quy-tac-duyet-bai
platform: both
category: blog
tags: [kiem-duyet, blog, ai-moderation, threshold]
version: 2
updated_at: 2026-07-10
owner: team-product
---

## Giới thiệu

Mỗi bài viết đăng lên CafeStory đi qua pipeline kiểm duyệt tự động của service AI Python trước khi hiển thị công khai. Pipeline chấm điểm cả nội dung chữ (caption) và hình ảnh, sau đó chọn một trong ba kết luận: duyệt tự động, chuyển admin xem xét thủ công, hoặc từ chối. Trạng thái blog trong database đổi tương ứng thành `PUBLISHED`, `HIDDEN` (chờ admin), hoặc `REMOVED`.

Tài liệu này liệt kê các ngưỡng, danh sách lý do, và giới hạn số lượng đang enforce trong code, để đội hỗ trợ và người dùng cuối tra cứu nhất quán khi có thắc mắc "vì sao bài của tôi bị chặn".

## Quy định

**1. Ba kết luận khả dĩ của pipeline AI.**

- `SAFE` → blog chuyển sang `PUBLISHED` và hiển thị trong feed.
- `NEEDS_REVIEW` → blog chuyển sang `HIDDEN`, chờ admin duyệt thủ công.
- `VIOLATION` → blog chuyển sang `REMOVED`, không còn xuất hiện trong feed (cả personalized lẫn organic đều loại bài này).

**2. Ngưỡng caption và ảnh.**

- Điểm toxicity của caption ≥ **70/100** → kết luận `VIOLATION` (reject).
- Điểm confidence của phân tích ảnh < **50/100** → kết luận `NEEDS_REVIEW` (chuyển admin).
- Caption không liên quan đến cafe (theo mô hình phân loại) → reject, mã lý do `NOT_COFFEE`.

**3. Năm mã lý do reject caption.**

Khi bài bị từ chối do caption, mã lý do được ghi cụ thể để user biết cần sửa gì:

- `COFFEE_RELATED` — caption có nội dung liên quan cafe nhưng vi phạm khía cạnh khác.
- `NOT_COFFEE` — caption không liên quan đến cafe hay trải nghiệm quán.
- `PROFANITY` — chứa ngôn từ xúc phạm, tục tĩu, thù ghét.
- `IRRELEVANT` — không liên quan chủ đề CafeStory.
- `SPAM` — quảng cáo trá hình, nội dung lặp, spam.

**4. Giới hạn số lượng ảnh.**

- Mỗi bài viết tối đa **10 ảnh**. Validator `BlogCreateDTO` chặn cứng — API trả 400 nếu vượt.
- AI moderation chỉ xét **10 ảnh đầu**. Nếu bài có nhiều ảnh hơn (không xảy ra do validator), phần thừa bị bỏ qua.
- Chỉ **3 ảnh đầu** được dùng để gán tag phân loại cafe (mục 6).

**5. Xử lý các tình huống không chắc chắn.**

- Bài **không có ảnh** → tự động chuyển admin xem xét, không auto-approve.
- AI xử lý ảnh gặp lỗi (ảnh corrupt, timeout) → chuyển admin.
- Service AI moderation **unavailable** (down, timeout tổng) → blog chuyển admin kèm lý do "AI moderation service unavailable" thay vì reject nhầm.

**6. Danh sách 14 tag cafe cố định.**

AI chọn 1–3 tag từ danh sách 14 tag sau, chỉ chấp nhận tag có confidence ≥ **60**:

`photo cafe`, `pet cafe`, `study cafe`, `vintage cafe`, `garden cafe`, `anime cafe`, `model cafe`, `acoustic cafe`, `brunch cafe`, `takeaway`, `vietnam traditional cafe`, `cafe space`, `cafe's drinks`, `cafe's menu`.

Danh sách này cố định trong file cấu hình `moderation_rules.json`, không nhận tag do user tự đặt vào bước AI (user có thể thêm hashtag thường ở tầng UI, nhưng tag phân loại cafe là do AI gán).

**7. Retry và stale-processing cho luồng report.**

Khi user report một bài viết, hệ thống tạo moderation job riêng để admin xử lý:

- Retry tối đa **3 lần** nếu job thất bại tạm thời.
- Backoff giữa các lần retry: **30 giây**, **2 phút**, **10 phút**.
- Job ở trạng thái processing quá **10 phút** bị đánh dấu stale để job scheduler quét lại.

**8. Ba hành động admin khi review.**

Admin xem lại bài (do NEEDS_REVIEW hoặc do bị report) có 3 quyết định:

- `APPROVE` → blog về `PUBLISHED`, hiển thị bình thường.
- `HIDE` → blog về `HIDDEN`, ẩn tạm khỏi feed nhưng chưa xoá vĩnh viễn.
- `REMOVE` → blog về `REMOVED`, coi như vi phạm, loại khỏi mọi feed.

## Ngoại lệ

- Bài viết đăng dưới danh nghĩa Cafe Page (do OWNER/CO_OWNER đăng) vẫn đi qua cùng pipeline moderation, không được bỏ qua bước AI.
- Người dùng có role admin không được auto-bypass moderation — mọi bài viết đều qua pipeline như user thường.
- Trong lúc AI unavailable, blog chuyển admin chứ KHÔNG bị treo hoặc reject nhầm. Đây là fail-safe để tránh mất bài của user hợp lệ khi hạ tầng AI có sự cố.
- Report của user không tự động khiến bài bị ẩn ngay; bài chỉ bị ẩn hoặc xoá sau khi admin xử lý report.

## Câu hỏi thường gặp

**Vì sao bài của tôi bị chuyển admin dù không có gì sai?**
Có 3 khả năng: (1) ảnh có confidence phân loại < 50 nên AI không dám auto-approve, (2) bài không có ảnh, hoặc (3) service AI moderation tạm thời gặp sự cố. Bài sẽ được admin duyệt lại trong thời gian ngắn.

**Bài bị reject nhưng tôi nghĩ liên quan cafe — sao lại NOT_COFFEE?**
Mô hình phân loại có ngưỡng riêng. Bạn có thể thêm nội dung nhắc rõ đến quán, đồ uống, hoặc trải nghiệm cafe, sau đó đăng lại. Nếu vẫn tin bài đúng chủ đề, dùng luồng khiếu nại để admin xem lại thủ công.

**Vì sao tôi upload 12 ảnh mà chỉ chọn được 10?**
Đây là giới hạn cứng ở validator backend. Cắt xuống 10 ảnh trước khi đăng.

**Bài của tôi có bao nhiêu tag cafe?**
AI chọn 1–3 tag từ 14 tag cố định, với điều kiện confidence ≥ 60. Nếu không tag nào đủ ngưỡng, bài vẫn được duyệt nhưng không có tag cafe cụ thể.

**Tôi report bài xong bao lâu bài đó bị ẩn?**
Report tạo job cho admin xử lý. Không có ẩn tự động chỉ dựa vào report. Admin phản hồi trong SLA nội bộ, tối đa 3 lần retry với backoff 30s / 2 phút / 10 phút nếu có lỗi tạm thời.

**Blog `REMOVED` có được khôi phục không?**
`REMOVED` là trạng thái vi phạm, không xuất hiện trong feed. Việc khôi phục cần admin can thiệp trực tiếp qua backend, không có luồng UI cho user tự phục hồi.

## Nguồn code

- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/AiBlogModerationServiceImpl.java:205-219` — 3 kết luận `SAFE`/`NEEDS_REVIEW`/`VIOLATION` mapping sang `PUBLISHED`/`HIDDEN`/`REMOVED`.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/AiBlogModerationServiceImpl.java:119-120` — chỉ gửi 10 ảnh đầu cho AI xử lý.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/AiBlogModerationServiceImpl.java:37,177-187` — fail-safe khi AI unavailable, chuyển admin.
- `4-cafe-story-ai-python/app/config/moderation_rules.json:49-52` — ngưỡng toxicity ≥ 70, confidence ảnh < 50.
- `4-cafe-story-ai-python/app/config/moderation_rules.json:20-21` — chỉ tag từ 3 ảnh đầu.
- `4-cafe-story-ai-python/app/config/moderation_rules.json:22-47` — danh sách 14 tag cafe cố định, confidence ≥ 60.
- `4-cafe-story-ai-python/app/services/text_moderator.py:12-18` — 5 mã lý do reject caption (`COFFEE_RELATED`, `NOT_COFFEE`, `PROFANITY`, `IRRELEVANT`, `SPAM`).
- `4-cafe-story-ai-python/app/services/blog_evaluator.py:27-45` — blog 0 ảnh hoặc lỗi ảnh → chuyển admin.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/dto/requestDTO/BlogCreateDTO.java:22` — tối đa 10 ảnh/bài (validator).
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/ReportModerationServiceImpl.java:49-52` — retry 3 lần, backoff 30s/2m/10m.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/service/serviceImplement/ReportModerationServiceImpl.java:451-458` — timeout stale-processing 10 phút.
- `1-cafe-story-backend-javaspring/src/main/java/com/cafestory/entity/enums/ModerationResolveAction.java` — 3 hành động admin `APPROVE`, `HIDE`, `REMOVE`.
