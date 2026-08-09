# Workflow Improvement — DOD-FIX-05

- Dùng stale FE selection + fresh Backend status để fault-inject một item mà không sửa n8n/provider.
- Chạy Playwright grep literal riêng trên Windows, không dùng regex chứa shell pipe qua `npm.cmd`.
- Visual QA phải kiểm tra failure-detail thật sự nằm trong viewport, không chỉ kiểm DOM.
- Nên tách helper tạo/cleanup synthetic BLOG dùng chung cho FIX-03–06 ở lần refactor test sau.
- Tương lai có thể bổ sung per-item structured error code/stage cho bulk operations analytics.
