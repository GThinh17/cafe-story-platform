# G0-12C — Issue log

## G012C-TEST-001

| Thuộc tính | Giá trị |
|---|---|
| Classification | `TEST_BUG` |
| Owner | DB verification probe |
| Affected behavior | Negative probe cho unique index không đi tới unique constraint |
| Evidence | Lần chạy đầu trả `admin_report_ai_resolutions_report_decision_check` |
| Nguyên nhân | Probe dùng `report_decision=NO_VIOLATION`, không thuộc enum hiện tại `RESOLVE/REJECT/NEEDS_MANUAL_REVIEW` |
| Tác động dữ liệu | Không có; transaction bị hủy và row count sau lỗi bằng `0` |
| Auto-fix allowed | `yes` |
| Hành động | Đổi fixture của probe sang `REJECT/NO_ACTION`, giữ nguyên source và migration |

## G012C-ENV-002

| Thuộc tính | Giá trị |
|---|---|
| Classification | `CONFIG_ENV` |
| Owner | Local runtime profile |
| Affected behavior | Scheduled workers vẫn query DB và custom cache vẫn thử kết nối Redis trong runtime test |
| Evidence | Backend vẫn start; log có query worker định kỳ và warning `Unable to connect to Redis` |
| Nguyên nhân | Repo chưa có một runtime profile chính thức để tắt toàn bộ background jobs/custom Redis cache |
| Tác động G0-12C | Không chặn migration/JPA/API smoke; DB disposable rỗng và row count cuối bằng `0` |
| Auto-fix allowed | `no` |
| Hành động | Đề xuất bổ sung profile integration-runtime với job/cache kill switches ở phase cải tiến |

## G012C-DATA-003

| Thuộc tính | Giá trị |
|---|---|
| Classification | `TEST_DATA` |
| Owner | Migration test fixture |
| Affected behavior | Chưa chứng minh migration trên snapshot có legacy resolution rows |
| Evidence | Synthetic pre-V2 snapshot có 62 bảng nhưng `admin_report_ai_resolutions=0` |
| Tác động G0-12C | Không chặn DDL/runtime gate trên DB disposable; vẫn là residual risk trước production |
| Auto-fix allowed | `no` |
| Hành động | Thêm sanitized pre-V2 fixture hoặc Testcontainers migration integration test ở phase sau |
