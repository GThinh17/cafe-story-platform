# Workflow improvement

- Thêm assertion hình học ngay khi test UI có yêu cầu vị trí tương đối; screenshot đơn thuần không đủ chống regression.
- Chụp riêng trạng thái layout thay vì chỉ chụp Policy Sheet hoặc action section.
- Có thể tách report detail thành các component `ReportMainColumn`, `RecommendationHistory`, `EvidenceColumn` nếu màn hình tiếp tục mở rộng; hiện chưa cần refactor vì thay đổi nhỏ và rõ ràng.
