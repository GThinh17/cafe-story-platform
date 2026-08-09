# Workflow improvement — DOD-FIX-03

## Điều đã hoạt động tốt

1. Fixture tạo đúng semantic condition bằng parent BLOG whitespace, không giả lập output AI.
2. Baseline đỏ tách được `TEST_BUG` khỏi `CODE_BUG`; lỗi cleanup không bị dùng để kết luận sai về
   production.
3. Backend vẫn là trust boundary cuối: provider trả gì cũng không vượt được critical-evidence
   guard.
4. UI assertion, API payload, target before/after, auto-job query và cleanup cùng nằm trong một
   executable scenario.

## Cải tiến khuyến nghị cho các gate sau

1. Tạo reusable disposable-fixture library cho report/blog/comment để DOD-FIX-04–06 không lặp
   SQL dependency cleanup.
2. Tách semantic assertions khỏi exact wording của explanation; explanation là diễn giải, không
   phải evidence.
3. Ghi attempt number và failure phase tự động vào raw evidence để audit không phải suy ra từ
   fix-log.
4. Với provider failure và bulk ở FIX-04/05, capture riêng transport result, business result và
   side-effect result.
5. Sau FIX-06, chạy lại audit Definition of Done thay vì cộng trừ thủ công các con số audit cũ.

Các đề xuất trên không được tự động triển khai trong DOD-FIX-03.
