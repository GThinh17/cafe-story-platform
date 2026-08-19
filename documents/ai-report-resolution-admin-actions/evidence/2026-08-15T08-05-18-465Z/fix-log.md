# Fix log

1. Bổ sung policy DTO/service/controller read-only và test mapping/404/delegation.
2. Bổ sung target content actions cho BLOG/COMMENT với confirm, stale-AI marker và không tự đóng report.
3. Bổ sung Policy Sheet, technical metadata và EN/VI copy cho toàn bộ 20 Rule ID hiện tại.
4. Làm chặt system prompt để rationale dẫn chỉ supplied Evidence ID, nêu dấu hiệu text cụ thể, tách observation/inference và thừa nhận thiếu evidence.
5. Nâng runtime validator lên exact Contract V2 và ba case ngôn ngữ thật.
6. Sửa hai lỗi test locator/envelope theo phạm vi hẹp.
7. Sửa layering của Policy Sheet mà không thay đổi z-index mặc định của Sheet khác.
8. Bổ sung keyboard focus/Enter và mobile scroll evidence; chạy lại E2E thành công.
9. Dừng backend/admin runtime do task tạo; giữ n8n published workflow chạy.

Không stash/reset/revert/amend/commit. Commit `4874057` vẫn là HEAD và không bị thay đổi.
